package com.malllite.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.malllite.auth.dto.LoginRequest;
import com.malllite.auth.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
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
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void customerShouldCreateAndCancelOrder() throws Exception {
        String token = registerAndGetToken();

        String response = mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contactName":"Buyer",
                                  "contactPhone":"13500000000",
                                  "shippingAddress":"Shanghai",
                                  "note":"Ring the bell",
                                  "paymentMethod":"cod",
                                  "shippingFee":"18",
                                  "items":[
                                    {
                                      "productSlug":"nova-x-pro",
                                      "skuCode":"NXP-12-256-BLK",
                                      "productName":"Nova X Pro",
                                      "skuName":"Nova X Pro 12GB+256GB",
                                      "coverImage":"https://example.com/cover.jpg",
                                      "salePrice":"4999.00",
                                      "quantity":1
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.customerActionable").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long orderId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalQuantity").value(greaterThan(0)));

        mockMvc.perform(delete("/api/orders/" + orderId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.customerActionable").value(false));
    }

    @Test
    void adminShouldAdvanceOrderStatus() throws Exception {
        String customerToken = registerAndGetToken();
        String adminToken = loginAndGetToken("admin", "admin123456");

        String response = mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearer(customerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contactName":"Buyer",
                                  "contactPhone":"13500000000",
                                  "shippingAddress":"Beijing",
                                  "paymentMethod":"card",
                                  "shippingFee":"0",
                                  "items":[
                                    {
                                      "productSlug":"pixel-wave-lite",
                                      "skuCode":"PWL-8-128-BLU",
                                      "productName":"Pixel Wave Lite",
                                      "skuName":"Pixel Wave Lite 8GB+128GB",
                                      "coverImage":"https://example.com/cover.jpg",
                                      "salePrice":"2299.00",
                                      "quantity":1
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long orderId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(put("/api/orders/admin/" + orderId + "/status")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "PROCESSING"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"));

        mockMvc.perform(put("/api/orders/admin/" + orderId + "/status")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "SHIPPED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));

        mockMvc.perform(get("/api/orders/admin")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").exists());
    }

    @Test
    void customerShouldNotAccessAdminOrderEndpoints() throws Exception {
        String token = registerAndGetToken();

        mockMvc.perform(get("/api/orders/admin")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    private String registerAndGetToken() throws Exception {
        String username = "order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        RegisterRequest request = new RegisterRequest(
                username,
                "buyer123456",
                "Order User",
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

        return objectMapper.readTree(response).get("token").asText();
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
