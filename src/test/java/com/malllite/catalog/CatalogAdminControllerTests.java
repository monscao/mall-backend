package com.malllite.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.malllite.auth.dto.LoginRequest;
import com.malllite.auth.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogAdminControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminShouldUploadImageAndToggleShelfStatus() throws Exception {
        String adminToken = loginAndGetToken("admin", "admin123456");
        String uniqueSlug = "studio-display-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hero.png",
                "image/png",
                "image-content".getBytes(StandardCharsets.UTF_8)
        );

        String uploadResponse = mockMvc.perform(multipart("/api/catalog/admin/uploads")
                        .file(file)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String imageUrl = objectMapper.readTree(uploadResponse).get("url").asText();

        String createdResponse = mockMvc.perform(post("/api/catalog/admin/products")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryCode":"laptops",
                                  "name":"Studio Display Mini",
                                  "subtitle":"Creative desktop screen",
                                  "slug":"%s",
                                  "brand":"MallLite",
                                  "coverImage":"%s",
                                  "priceFrom":"1999.00",
                                  "priceTo":"1999.00",
                                  "marketPrice":"2299.00",
                                  "stockStatus":"IN_STOCK",
                                  "featured":false,
                                  "onShelf":true,
                                  "galleryImages":[],
                                  "skus":[
                                    {
                                      "skuCode":"SDM-%s",
                                      "name":"Studio Display Mini",
                                      "specSummary":"27 inch / 4K",
                                      "salePrice":"1999.00",
                                      "marketPrice":"2299.00",
                                      "stock":8,
                                      "coverImage":"%s",
                                      "isDefault":true
                                    }
                                  ]
                                }
                                """.formatted(uniqueSlug, imageUrl, uniqueSlug.substring(uniqueSlug.length() - 8).toUpperCase(), imageUrl)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value(uniqueSlug))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long productId = objectMapper.readTree(createdResponse).get("id").asLong();

        mockMvc.perform(put("/api/catalog/admin/products/" + productId + "/shelf")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("onShelf", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onShelf").value(false));
    }

    @Test
    void customerShouldNotAccessUploadEndpoint() throws Exception {
        String customerToken = registerAndGetToken();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hero.png",
                "image/png",
                "image-content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/catalog/admin/uploads")
                        .file(file)
                        .header("Authorization", bearer(customerToken)))
                .andExpect(status().isForbidden());
    }

    private String registerAndGetToken() throws Exception {
        String username = "catalog_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        RegisterRequest request = new RegisterRequest(
                username,
                "buyer123456",
                "Catalog User",
                username + "@shop.local",
                "13500000000"
        );

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode payload = objectMapper.readTree(response);
        return payload.get("token").asText();
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(username, password))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode payload = objectMapper.readTree(response);
        return payload.get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
