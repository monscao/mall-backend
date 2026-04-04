package com.malllite.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void categoriesShouldReturnFeaturedCatalogData() throws Exception {
        mockMvc.perform(get("/api/catalog/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("$[0].bannerImage").exists());
    }

    @Test
    void productsShouldSupportCategoryFiltering() throws Exception {
        mockMvc.perform(get("/api/catalog/products").param("categoryCode", "phones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$.items[0].categoryCode").value("phones"))
                .andExpect(jsonPath("$.items[0].coverImage").exists())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(24));
    }

    @Test
    void productsShouldSupportSortAndLimit() throws Exception {
        mockMvc.perform(get("/api/catalog/products")
                        .param("sort", "sales")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].salesCount").exists())
                .andExpect(jsonPath("$.total").value(greaterThan(0)));
    }

    @Test
    void productsShouldSupportKeywordSearchAndPagination() throws Exception {
        mockMvc.perform(get("/api/catalog/products")
                        .param("q", "nova")
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.keyword").value("nova"))
                .andExpect(jsonPath("$.hasNext").exists());
    }

    @Test
    void productDetailShouldReturnSkuList() throws Exception {
        mockMvc.perform(get("/api/catalog/products/nova-x-pro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("nova-x-pro"))
                .andExpect(jsonPath("$.skus.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$.skus[0].skuCode").exists());
    }
}
