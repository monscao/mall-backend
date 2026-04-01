package com.shopback.catalog.controller;

import com.shopback.catalog.dto.CategoryResponse;
import com.shopback.catalog.dto.ProductCardResponse;
import com.shopback.catalog.dto.ProductDetailResponse;
import com.shopback.catalog.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/categories")
    public List<CategoryResponse> listFeaturedCategories() {
        return catalogService.listFeaturedCategories();
    }

    @GetMapping("/products")
    public List<ProductCardResponse> listProducts(
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false, defaultValue = "featured") String sort,
            @RequestParam(required = false) Integer limit
    ) {
        return catalogService.listProducts(categoryCode, featured, sort, limit);
    }

    @GetMapping("/products/{slug}")
    public ProductDetailResponse getProductDetail(@PathVariable String slug) {
        return catalogService.getProductDetail(slug);
    }
}
