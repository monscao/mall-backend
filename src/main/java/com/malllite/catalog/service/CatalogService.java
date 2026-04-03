package com.malllite.catalog.service;

import com.malllite.catalog.dto.AdminProductRowResponse;
import com.malllite.catalog.dto.CategoryResponse;
import com.malllite.catalog.dto.CreateProductRequest;
import com.malllite.catalog.dto.CreateProductSkuRequest;
import com.malllite.catalog.dto.ProductCardResponse;
import com.malllite.catalog.dto.ProductDetailResponse;
import com.malllite.catalog.dto.ProductAssetResponse;
import com.malllite.catalog.dto.ProductSkuResponse;
import com.malllite.catalog.dto.UpdateAdminProductRequest;
import com.malllite.catalog.dto.UploadedImageRequest;
import com.malllite.catalog.model.ProductAsset;
import com.malllite.catalog.model.Category;
import com.malllite.catalog.model.Product;
import com.malllite.catalog.model.ProductSku;
import com.malllite.catalog.repository.CatalogRepository;
import com.malllite.common.exception.BadRequestException;
import com.malllite.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class CatalogService {

    private final CatalogRepository catalogRepository;

    public CatalogService(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    public List<CategoryResponse> listFeaturedCategories() {
        return catalogRepository.findFeaturedCategories()
                .stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    public List<ProductCardResponse> listProducts(String categoryCode, Boolean featuredOnly, String sort, Integer limit) {
        return catalogRepository.findProducts(categoryCode, featuredOnly, normalizeSort(sort), normalizeLimit(limit))
                .stream()
                .map(this::toProductCardResponse)
                .toList();
    }

    public List<AdminProductRowResponse> listAdminProducts() {
        return catalogRepository.findAdminProducts()
                .stream()
                .map(this::toAdminProductRowResponse)
                .toList();
    }

    public ProductDetailResponse getProductDetail(String slug) {
        Product product = catalogRepository.findProductBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        List<ProductSkuResponse> skus = catalogRepository.findSkusByProductId(product.id())
                .stream()
                .map(this::toProductSkuResponse)
                .toList();

        List<ProductAssetResponse> assets = catalogRepository.findAssetsByProductId(product.id())
                .stream()
                .map(this::toProductAssetResponse)
                .toList();

        return new ProductDetailResponse(
                product.id(),
                product.slug(),
                product.name(),
                product.subtitle(),
                product.brand(),
                product.categoryCode(),
                product.categoryName(),
                product.coverImage(),
                product.priceFrom(),
                product.priceTo(),
                product.marketPrice(),
                product.rating(),
                product.salesCount(),
                product.stockStatus(),
                product.featured(),
                product.description(),
                splitTags(product.tags()),
                skus,
                assets
        );
    }

    public ProductDetailResponse createProduct(CreateProductRequest request) {
        Long categoryId = catalogRepository.findCategoryIdByCode(request.categoryCode())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        String tags = request.tags() == null ? "" : String.join(",", request.tags());
        Long productId = catalogRepository.insertProduct(
                categoryId,
                request.name(),
                request.subtitle(),
                normalizeSlug(request.slug(), request.name()),
                request.brand(),
                request.coverImage(),
                request.coverImageKey(),
                request.coverImageName(),
                request.priceFrom(),
                request.priceTo(),
                request.marketPrice(),
                request.stockStatus(),
                tags,
                request.description(),
                request.featured() != null && request.featured(),
                request.onShelf() == null || request.onShelf()
        );

        for (CreateProductSkuRequest sku : request.skus()) {
            catalogRepository.insertSku(
                    productId,
                    sku.skuCode(),
                    sku.name(),
                    sku.specSummary(),
                    sku.salePrice(),
                    sku.marketPrice(),
                    sku.stock(),
                    sku.coverImage(),
                    sku.isDefault() != null && sku.isDefault()
            );
        }

        List<UploadedImageRequest> galleryImages = request.galleryImages() == null ? List.of() : request.galleryImages();
        int order = 1;
        for (UploadedImageRequest image : galleryImages) {
            if (image != null && image.url() != null && !image.url().isBlank()) {
                catalogRepository.insertAsset(
                        productId,
                        image.url(),
                        image.key(),
                        image.name(),
                        image.name() != null && !image.name().isBlank() ? image.name() : request.name(),
                        order++
                );
            }
        }

        return getProductDetail(normalizeSlug(request.slug(), request.name()));
    }

    public AdminProductRowResponse updateProduct(Long productId, UpdateAdminProductRequest request) {
        if (request == null) {
            throw new BadRequestException("Request is required");
        }

        Product existing = catalogRepository.findProductById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        String nextCategoryCode = firstNonBlank(request.categoryCode(), existing.categoryCode());
        Long categoryId = catalogRepository.findCategoryIdByCode(nextCategoryCode)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        catalogRepository.updateProduct(
                productId,
                categoryId,
                firstNonBlank(request.name(), existing.name()),
                request.subtitle() != null ? request.subtitle() : existing.subtitle(),
                firstNonBlank(request.slug(), existing.slug()),
                request.brand() != null ? request.brand() : existing.brand(),
                firstNonBlank(request.priceFrom(), existing.priceFrom()),
                firstNonBlank(request.priceTo(), existing.priceTo()),
                firstNonBlank(request.marketPrice(), existing.marketPrice()),
                firstNonBlank(request.stockStatus(), existing.stockStatus()),
                request.featured() != null ? request.featured() : existing.featured(),
                existing.onShelf()
        );

        Product updated = catalogRepository.findProductById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return toAdminProductRowResponse(updated);
    }

    public AdminProductRowResponse updateProductShelf(Long productId, boolean onShelf) {
        Product existing = catalogRepository.findProductById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        catalogRepository.updateProduct(
                productId,
                existing.categoryId(),
                existing.name(),
                existing.subtitle(),
                existing.slug(),
                existing.brand(),
                existing.priceFrom(),
                existing.priceTo(),
                existing.marketPrice(),
                existing.stockStatus(),
                existing.featured(),
                onShelf
        );

        return catalogRepository.findProductById(productId)
                .map(this::toAdminProductRowResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    public void deleteProduct(Long productId) {
        catalogRepository.findProductById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        catalogRepository.deleteProduct(productId);
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(
                category.id(),
                category.code(),
                category.name(),
                category.description(),
                category.icon(),
                category.bannerImage(),
                category.featured()
        );
    }

    private ProductCardResponse toProductCardResponse(Product product) {
        return new ProductCardResponse(
                product.id(),
                product.slug(),
                product.name(),
                product.subtitle(),
                product.brand(),
                product.categoryCode(),
                product.categoryName(),
                product.coverImage(),
                product.priceFrom(),
                product.priceTo(),
                product.marketPrice(),
                product.rating(),
                product.salesCount(),
                product.stockStatus(),
                product.featured(),
                splitTags(product.tags())
        );
    }

    private AdminProductRowResponse toAdminProductRowResponse(Product product) {
        return new AdminProductRowResponse(
                product.id(),
                product.slug(),
                product.name(),
                product.subtitle(),
                product.brand(),
                product.categoryCode(),
                product.categoryName(),
                product.coverImage(),
                product.priceFrom(),
                product.priceTo(),
                product.marketPrice(),
                product.stockStatus(),
                product.featured(),
                product.onShelf()
        );
    }

    private ProductSkuResponse toProductSkuResponse(ProductSku sku) {
        return new ProductSkuResponse(
                sku.skuCode(),
                sku.name(),
                sku.specSummary(),
                sku.salePrice(),
                sku.marketPrice(),
                sku.stock(),
                sku.coverImage(),
                sku.isDefault()
        );
    }

    private ProductAssetResponse toProductAssetResponse(ProductAsset asset) {
        return new ProductAssetResponse(
                asset.imageUrl(),
                asset.altText(),
                asset.sortOrder()
        );
    }

    private List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .toList();
    }

    private String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return "featured";
        }
        return switch (sort) {
            case "featured", "sales", "latest", "priceAsc", "priceDesc" -> sort;
            default -> "featured";
        };
    }

    private Integer normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return null;
        }
        return Math.min(limit, 24);
    }

    private String normalizeSlug(String slug, String name) {
        if (slug != null && !slug.isBlank()) {
            return slug;
        }
        return name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    private String firstNonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
