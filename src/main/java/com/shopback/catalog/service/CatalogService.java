package com.shopback.catalog.service;

import com.shopback.catalog.dto.CategoryResponse;
import com.shopback.catalog.dto.ProductCardResponse;
import com.shopback.catalog.dto.ProductDetailResponse;
import com.shopback.catalog.dto.ProductSkuResponse;
import com.shopback.catalog.model.Category;
import com.shopback.catalog.model.Product;
import com.shopback.catalog.model.ProductSku;
import com.shopback.catalog.repository.CatalogRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

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

    public ProductDetailResponse getProductDetail(String slug) {
        Product product = catalogRepository.findProductBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        List<ProductSkuResponse> skus = catalogRepository.findSkusByProductId(product.id())
                .stream()
                .map(this::toProductSkuResponse)
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
                skus
        );
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
}
