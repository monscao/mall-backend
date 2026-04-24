package com.malllite.agent.service;

import com.malllite.agent.dto.AgentCartItemRequest;
import com.malllite.agent.dto.AgentChatRequest;
import com.malllite.agent.dto.AgentChatResponse;
import com.malllite.agent.dto.AgentMessageRequest;
import com.malllite.auth.dto.AuthUser;
import com.malllite.auth.service.JwtTokenService;
import com.malllite.cart.model.CartItemSnapshot;
import com.malllite.cart.repository.CartRepository;
import com.malllite.catalog.model.Category;
import com.malllite.catalog.model.Product;
import com.malllite.catalog.repository.CatalogRepository;
import com.malllite.common.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.function.Consumer;

@Service
public class AgentService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final OpenAiResponsesClient openAiResponsesClient;
    private final CatalogRepository catalogRepository;
    private final CartRepository cartRepository;
    private final JwtTokenService jwtTokenService;

    public AgentService(
            OpenAiResponsesClient openAiResponsesClient,
            CatalogRepository catalogRepository,
            CartRepository cartRepository,
            JwtTokenService jwtTokenService
    ) {
        this.openAiResponsesClient = openAiResponsesClient;
        this.catalogRepository = catalogRepository;
        this.cartRepository = cartRepository;
        this.jwtTokenService = jwtTokenService;
    }

    public AgentChatResponse chat(AgentChatRequest request, String authorizationHeader) {
        String message = requireMessage(request);
        List<AgentMessageRequest> history = sanitizeHistory(request);

        if (!openAiResponsesClient.isConfigured()) {
            return new AgentChatResponse(
                    "Moca 的后端聊天链路已经接好了，但还没有配置真实模型。你可以设置 `OPENAI_API_KEY` 走 OpenAI，或者把 `OPENAI_ENDPOINT` 指向 LM Studio 的 `http://127.0.0.1:1234/v1/responses` 后重启后端。",
                    false,
                    null
            );
        }

        String instructions = buildInstructions(request, authorizationHeader);

        try {
            String reply = openAiResponsesClient.createReply(instructions, history, message);
            return new AgentChatResponse(reply, true, openAiResponsesClient.model());
        } catch (RuntimeException exception) {
            return new AgentChatResponse(
                    "我刚刚没连上模型服务。你可以稍后重试，或者检查后端里的 `OPENAI_API_KEY`、`OPENAI_ENDPOINT`、LM Studio 服务状态和模型配置。",
                    false,
                    openAiResponsesClient.model()
            );
        }
    }

    public AgentChatResponse streamChat(AgentChatRequest request, String authorizationHeader, Consumer<String> onDelta) {
        String message = requireMessage(request);
        List<AgentMessageRequest> history = sanitizeHistory(request);

        if (!openAiResponsesClient.isConfigured()) {
            String fallback = "Moca 的后端聊天链路已经接好了，但还没有配置真实模型。你可以设置 `OPENAI_API_KEY` 走 OpenAI，或者把 `OPENAI_ENDPOINT` 指向 LM Studio 的 `http://127.0.0.1:1234/v1/responses` 后重启后端。";
            onDelta.accept(fallback);
            return new AgentChatResponse(fallback, false, null);
        }

        String instructions = buildInstructions(request, authorizationHeader);

        try {
            openAiResponsesClient.streamReply(instructions, history, message, onDelta);
            return new AgentChatResponse("", true, openAiResponsesClient.model());
        } catch (RuntimeException exception) {
            String fallback = "我刚刚没连上模型服务。你可以稍后重试，或者检查后端里的 `OPENAI_API_KEY`、`OPENAI_ENDPOINT`、LM Studio 服务状态和模型配置。";
            onDelta.accept(fallback);
            return new AgentChatResponse(fallback, false, openAiResponsesClient.model());
        }
    }

    private String buildInstructions(AgentChatRequest request, String authorizationHeader) {
        String language = normalizeLanguage(request.language());
        List<Category> categories = catalogRepository.findFeaturedCategories().stream()
                .sorted(Comparator.comparing(Category::sortOrder, Comparator.nullsLast(Integer::compareTo)))
                .limit(6)
                .toList();
        Product currentProduct = resolveCurrentProduct(request.currentPath()).orElse(null);
        List<Product> focusProducts = currentProduct != null
                ? catalogRepository.findRelatedProducts(currentProduct.categoryCode(), currentProduct.slug(), 6)
                : List.of();
        List<Product> products = currentProduct != null && !focusProducts.isEmpty()
                ? focusProducts
                : catalogRepository.findProducts(null, null, "featured", null, 8, 0);
        List<String> cartLines = resolveCartContext(request.guestCartItems(), authorizationHeader);

        return """
                You are Moca, an AI shopping assistant for the MONSCAO electronics storefront.
                Your job is to help users choose products with concise, practical, sales-aware guidance.
                Keep answers friendly, direct, and useful for shopping decisions.
                Prefer recommending products that match the user's budget, use case, and urgency.
                When enough context exists, mention 2-4 specific relevant products or categories from the store context.
                If there is a current product, prioritize that product and similar products in the same category before using global recommendations.
                If the user has not provided budget, usage, or gift recipient details, ask at most one short follow-up question.
                Do not invent products, categories, prices, or cart items beyond the context below.
                Respond in %s.

                Current route: %s

                Current product:
                %s

                Featured categories:
                %s

                Priority products:
                %s

                Cart context:
                %s
                """.formatted(
                language,
                blankToDefault(request.currentPath(), "/"),
                formatCurrentProduct(currentProduct),
                formatCategories(categories),
                formatProducts(products),
                cartLines.isEmpty() ? "No cart items yet." : String.join("\n", cartLines)
        );
    }

    private Optional<Product> resolveCurrentProduct(String currentPath) {
        if (currentPath == null || currentPath.isBlank()) {
            return Optional.empty();
        }

        String pathOnly = currentPath.split("\\?")[0];
        if (!pathOnly.startsWith("/product/")) {
            return Optional.empty();
        }

        String slug = pathOnly.substring("/product/".length()).trim();
        if (slug.isBlank()) {
            return Optional.empty();
        }

        return catalogRepository.findProductBySlug(slug);
    }

    private List<String> resolveCartContext(List<AgentCartItemRequest> guestCartItems, String authorizationHeader) {
        Optional<AuthUser> authUser = resolveAuthUser(authorizationHeader);
        if (authUser.isPresent()) {
            Long cartId = cartRepository.findOrCreateCartId(authUser.get().userId());
            return cartRepository.findItemsByCartId(cartId).stream()
                    .map(this::formatCartSnapshot)
                    .toList();
        }

        return Optional.ofNullable(guestCartItems).orElse(List.of()).stream()
                .filter(item -> item != null && item.productName() != null && !item.productName().isBlank())
                .map(item -> "- %s / %s / qty %s / price %s".formatted(
                        item.productName(),
                        blankToDefault(item.skuName(), "default"),
                        blankToDefault(item.quantity(), "1"),
                        blankToDefault(item.salePrice(), "unknown")
                ))
                .toList();
    }

    private Optional<AuthUser> resolveAuthUser(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());
        if (token.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(jwtTokenService.parseToken(token));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private String formatCategories(List<Category> categories) {
        if (categories.isEmpty()) {
            return "- No categories available.";
        }

        return categories.stream()
                .map(category -> "- %s (%s): %s".formatted(
                        category.name(),
                        category.code(),
                        blankToDefault(category.description(), "No description")
                ))
                .collect(Collectors.joining("\n"));
    }

    private String formatProducts(List<Product> products) {
        if (products.isEmpty()) {
            return "- No products available.";
        }

        return products.stream()
                .map(product -> "- %s | category=%s | brand=%s | price=%s-%s | tags=%s | subtitle=%s".formatted(
                        product.name(),
                        blankToDefault(product.categoryName(), product.categoryCode()),
                        blankToDefault(product.brand(), "MONSCAO"),
                        blankToDefault(product.priceFrom(), "?"),
                        blankToDefault(product.priceTo(), "?"),
                        blankToDefault(product.tags(), "none"),
                        blankToDefault(product.subtitle(), "none")
                ))
                .collect(Collectors.joining("\n"));
    }

    private String formatCurrentProduct(Product product) {
        if (product == null) {
            return "No current product context.";
        }

        return "- %s | slug=%s | category=%s | brand=%s | price=%s-%s | tags=%s | subtitle=%s".formatted(
                product.name(),
                product.slug(),
                blankToDefault(product.categoryName(), product.categoryCode()),
                blankToDefault(product.brand(), "MONSCAO"),
                blankToDefault(product.priceFrom(), "?"),
                blankToDefault(product.priceTo(), "?"),
                blankToDefault(product.tags(), "none"),
                blankToDefault(product.subtitle(), "none")
        );
    }

    private String formatCartSnapshot(CartItemSnapshot item) {
        return "- %s / %s / qty %s / price %s".formatted(
                item.productName(),
                blankToDefault(item.skuName(), item.skuCode()),
                item.quantity(),
                blankToDefault(item.salePrice(), "unknown")
        );
    }

    private String normalizeLanguage(String language) {
        if (language == null) {
            return "Simplified Chinese unless the user is clearly using English";
        }

        return switch (language.trim().toLowerCase()) {
            case "en", "en-us" -> "English";
            case "zh", "zh-cn" -> "Simplified Chinese";
            default -> "the same language as the user";
        };
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String requireMessage(AgentChatRequest request) {
        String message = request.message() == null ? "" : request.message().trim();
        if (message.isBlank()) {
            throw new BadRequestException("Agent message is required");
        }
        return message;
    }

    private List<AgentMessageRequest> sanitizeHistory(AgentChatRequest request) {
        return Optional.ofNullable(request.history()).orElse(List.of()).stream()
                .filter(item -> item != null && item.content() != null && !item.content().isBlank())
                .limit(8)
                .toList();
    }
}
