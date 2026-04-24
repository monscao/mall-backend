package com.malllite.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.malllite.agent.dto.AgentMessageRequest;
import com.malllite.common.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

@Component
public class OpenAiResponsesClient {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String apiKey;
    private final String model;
    private final String endpoint;

    public OpenAiResponsesClient(
            ObjectMapper objectMapper,
            @Value("${agent.openai.api-key:}") String apiKey,
            @Value("${agent.openai.model:gpt-5.2}") String model,
            @Value("${agent.openai.endpoint:https://api.openai.com/v1/responses}") String endpoint
    ) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model == null ? "" : model.trim();
        this.endpoint = endpoint == null ? "" : endpoint.trim();
    }

    public boolean isConfigured() {
        return !endpoint.isBlank() && (!apiKey.isBlank() || isLocalEndpoint());
    }

    public String model() {
        return model;
    }

    public String createReply(String instructions, List<AgentMessageRequest> history, String userMessage) {
        if (!isConfigured()) {
            throw new BadRequestException("Agent model is not configured");
        }

        try {
            HttpRequest request = buildRequest(buildPayload(instructions, history, userMessage, false));

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Agent model request failed with status " + response.statusCode() + ": " + response.body());
            }

            return extractOutputText(objectMapper.readTree(response.body()));
        } catch (IOException | InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Agent model request failed", exception);
        }
    }

    public void streamReply(String instructions, List<AgentMessageRequest> history, String userMessage, Consumer<String> onDelta) {
        if (!isConfigured()) {
            throw new BadRequestException("Agent model is not configured");
        }

        try {
            HttpRequest request = buildRequest(buildPayload(instructions, history, userMessage, true));
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String body = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                throw new IllegalStateException("Agent model stream failed with status " + response.statusCode() + ": " + body);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) {
                        continue;
                    }

                    String data = line.substring(5).trim();
                    if (data.isBlank() || "[DONE]".equals(data)) {
                        continue;
                    }

                    JsonNode event = objectMapper.readTree(data);
                    if ("response.output_text.delta".equals(event.path("type").asText())) {
                        String delta = event.path("delta").asText("");
                        if (!delta.isBlank()) {
                            onDelta.accept(delta);
                        }
                    }
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Agent model stream failed", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Agent model stream interrupted", exception);
        }
    }

    private HttpRequest buildRequest(String payload) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(45))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));

        if (!apiKey.isBlank()) {
            builder.header("Authorization", "Bearer " + apiKey);
        }

        return builder.build();
    }

    private String buildPayload(String instructions, List<AgentMessageRequest> history, String userMessage, boolean stream) throws IOException {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", model);
        root.put("instructions", instructions);
        root.put("stream", stream);

        ObjectNode text = root.putObject("text");
        text.putObject("format").put("type", "text");

        ArrayNode input = root.putArray("input");
        for (AgentMessageRequest message : history) {
            String normalizedRole = normalizeRole(message.role());
            input.addObject()
                    .put("role", normalizedRole)
                    .putArray("content")
                    .addObject()
                    .put("type", contentTypeForRole(normalizedRole))
                    .put("text", message.content());
        }

        input.addObject()
                .put("role", "user")
                .putArray("content")
                .addObject()
                .put("type", "input_text")
                .put("text", userMessage);

        return objectMapper.writeValueAsString(root);
    }

    private String extractOutputText(JsonNode root) {
        String outputText = root.path("output_text").asText("");
        if (!outputText.isBlank()) {
            return outputText.trim();
        }

        StringBuilder builder = new StringBuilder();
        for (JsonNode item : root.path("output")) {
            if (!"message".equals(item.path("type").asText())) {
                continue;
            }

            for (JsonNode content : item.path("content")) {
                String text = content.path("text").asText("");
                if (text.isBlank()) {
                    continue;
                }

                if (!builder.isEmpty()) {
                    builder.append("\n\n");
                }
                builder.append(text.trim());
            }
        }

        if (!builder.isEmpty()) {
            return builder.toString();
        }

        throw new IllegalStateException("Agent model returned no text");
    }

    private String normalizeRole(String role) {
        if ("assistant".equalsIgnoreCase(role)) {
            return "assistant";
        }
        return "user";
    }

    private String contentTypeForRole(String normalizedRole) {
        if ("assistant".equals(normalizedRole)) {
            return "output_text";
        }
        return "input_text";
    }

    private boolean isLocalEndpoint() {
        try {
            URI uri = URI.create(endpoint);
            String host = uri.getHost();
            if (host == null) {
                return false;
            }

            String normalizedHost = host.toLowerCase(Locale.ROOT);
            return "localhost".equals(normalizedHost)
                    || "127.0.0.1".equals(normalizedHost)
                    || "::1".equals(normalizedHost);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
