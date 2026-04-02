package com.malllite.catalog.controller;

import com.malllite.catalog.dto.AdminProductRowResponse;
import com.malllite.auth.annotation.RequireRole;
import com.malllite.catalog.dto.CategoryResponse;
import com.malllite.catalog.dto.CreateProductRequest;
import com.malllite.catalog.dto.ProductCardResponse;
import com.malllite.catalog.dto.ProductDetailResponse;
import com.malllite.catalog.dto.UpdateAdminProductRequest;
import com.malllite.catalog.dto.UploadResponse;
import com.malllite.catalog.service.CatalogService;
import com.malllite.common.storage.LocalFileStorageService;
import com.malllite.common.storage.StoredFile;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;
    private final LocalFileStorageService fileStorageService;

    public CatalogController(CatalogService catalogService, LocalFileStorageService fileStorageService) {
        this.catalogService = catalogService;
        this.fileStorageService = fileStorageService;
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

    @PostMapping("/admin/products")
    @RequireRole("ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDetailResponse createProduct(@Valid @RequestBody CreateProductRequest request) {
        return catalogService.createProduct(request);
    }

    @GetMapping("/admin/products")
    @RequireRole("ADMIN")
    public List<AdminProductRowResponse> listAdminProducts() {
        return catalogService.listAdminProducts();
    }

    @PutMapping("/admin/products/{productId}")
    @RequireRole("ADMIN")
    public AdminProductRowResponse updateProduct(@PathVariable Long productId, @RequestBody UpdateAdminProductRequest request) {
        return catalogService.updateProduct(productId, request);
    }

    @DeleteMapping("/admin/products/{productId}")
    @RequireRole("ADMIN")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long productId) {
        catalogService.deleteProduct(productId);
    }

    @PostMapping("/admin/uploads")
    @RequireRole("ADMIN")
    @ResponseStatus(HttpStatus.CREATED)
    public UploadResponse uploadImage(@RequestPart("file") MultipartFile file) throws IOException {
        StoredFile storedFile = fileStorageService.store(file);
        return new UploadResponse(storedFile.path(), storedFile.key(), storedFile.originalName());
    }
}
