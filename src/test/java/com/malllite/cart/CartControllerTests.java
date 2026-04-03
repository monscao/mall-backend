package com.malllite.cart;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.malllite.auth.dto.LoginRequest;
import com.malllite.auth.dto.RegisterRequest;
import com.malllite.cart.dto.CartItemInput;
import com.malllite.cart.dto.SyncCartRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CartControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void authenticatedUserShouldSyncReadAndClearCart() throws Exception {
        String token = registerAndGetToken();

        SyncCartRequest syncRequest = new SyncCartRequest(List.of(
                new CartItemInput("NXP-12-256-BLK", 2),
                new CartItemInput("PWL-8-128-BLU", 1),
                new CartItemInput("NXP-12-256-BLK", 1)
        ));

        mockMvc.perform(put("/api/cart")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(4))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].lineTotal").exists());

        mockMvc.perform(get("/api/cart")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$.subtotal").isString());

        mockMvc.perform(delete("/api/cart")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    void missingTokenShouldRejectCartRequests() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidSkuShouldBeRejectedDuringCartSync() throws Exception {
        String token = registerAndGetToken();

        SyncCartRequest syncRequest = new SyncCartRequest(List.of(new CartItemInput("NOT-EXIST", 1)));

        mockMvc.perform(put("/api/cart")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cart item SKU is invalid"));
    }

    private String registerAndGetToken() throws Exception {
        String username = "cart_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        RegisterRequest request = new RegisterRequest(
                username,
                "buyer123456",
                "Cart User",
                username + "@shop.local",
                "13600000000"
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

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
