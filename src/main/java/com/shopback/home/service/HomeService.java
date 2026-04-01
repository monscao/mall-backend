package com.shopback.home.service;

import com.shopback.catalog.dto.CategoryResponse;
import com.shopback.catalog.dto.ProductCardResponse;
import com.shopback.catalog.service.CatalogService;
import com.shopback.home.dto.HomeHeroResponse;
import com.shopback.home.dto.HomeResponse;
import com.shopback.home.dto.HomeSectionResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeService {

    private final CatalogService catalogService;

    public HomeService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public HomeResponse getHome() {
        List<CategoryResponse> featuredCategories = catalogService.listFeaturedCategories();
        List<ProductCardResponse> featuredProducts = catalogService.listProducts(null, true, "featured", 4);
        List<ProductCardResponse> hotProducts = catalogService.listProducts(null, null, "sales", 4);
        List<ProductCardResponse> latestProducts = catalogService.listProducts(null, null, "latest", 4);

        ProductCardResponse heroProduct = featuredProducts.isEmpty() ? null : featuredProducts.getFirst();

        return new HomeResponse(
                "apple-lite",
                new HomeHeroResponse(
                        "Crafted For Everyday Wonder",
                        "Powerful products, presented with calm confidence.",
                        "A premium storefront that tells a story first, then gets out of the way when customers are ready to buy.",
                        "dark-cinematic",
                        heroProduct
                ),
                featuredCategories,
                List.of(
                        new HomeSectionResponse(
                                "featured",
                                "Flagship Highlights",
                                "Products with the strongest premium feel for the first scroll-driven sections.",
                                "sticky-showcase",
                                featuredProducts
                        ),
                        new HomeSectionResponse(
                                "hot",
                                "Most Wanted",
                                "Best-selling products that can transition the homepage from storytelling into shopping.",
                                "grid-compact",
                                hotProducts
                        ),
                        new HomeSectionResponse(
                                "latest",
                                "Fresh Picks",
                                "Newer arrivals for a lighter, more editorial ending to the homepage.",
                                "split-editorial",
                                latestProducts
                        )
                )
        );
    }
}
