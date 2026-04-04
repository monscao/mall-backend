package com.malllite.home.service;

import com.malllite.catalog.dto.CategoryResponse;
import com.malllite.catalog.dto.ProductCardResponse;
import com.malllite.catalog.service.CatalogService;
import com.malllite.home.dto.HomeHeroResponse;
import com.malllite.home.dto.HomeResponse;
import com.malllite.home.dto.HomeSectionResponse;
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
        List<ProductCardResponse> featuredProducts = catalogService.listProducts(null, true, "featured", 4, null, 1, 4).items();
        List<ProductCardResponse> hotProducts = catalogService.listProducts(null, null, "sales", 4, null, 1, 4).items();
        List<ProductCardResponse> latestProducts = catalogService.listProducts(null, null, "latest", 4, null, 1, 4).items();

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
