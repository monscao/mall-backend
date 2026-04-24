package com.malllite.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.malllite.agent.service.OpenAiResponsesClient;
import com.malllite.auth.dto.RegisterRequest;
import com.malllite.cart.dto.CartItemInput;
import com.malllite.cart.dto.SyncCartRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AgentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void resetStub() {
        StubOpenAiResponsesClient.configured = true;
        StubOpenAiResponsesClient.reply = "这是来自测试桩的推荐回复。";
        StubOpenAiResponsesClient.lastInstructions = "";
    }

    @Test
    void guestChatShouldSendStoreAndGuestCartContextToModel() throws Exception {
        String payload = """
                {
                  "message": "帮我推荐一款礼物",
                  "history": [{"role":"user","content":"预算 1000"}],
                  "currentPath": "/product/echobeat-pro",
                  "language": "zh",
                  "guestCartItems": [
                    {"productName":"EchoBeat Pro","skuName":"Standard","quantity":"1","salePrice":"899"}
                  ]
                }
                """;

        mockMvc.perform(post("/api/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liveModel").value(true))
                .andExpect(jsonPath("$.reply").value("这是来自测试桩的推荐回复。"));

        assertTrue(StubOpenAiResponsesClient.lastInstructions.contains("Featured categories"));
        assertTrue(StubOpenAiResponsesClient.lastInstructions.contains("EchoBeat Pro"));
        assertTrue(StubOpenAiResponsesClient.lastInstructions.contains("Current product"));
        assertTrue(StubOpenAiResponsesClient.lastInstructions.contains("slug=echobeat-pro"));
    }

    @Test
    void authenticatedChatShouldPreferServerCartContext() throws Exception {
        String token = registerAndGetToken();

        SyncCartRequest syncRequest = new SyncCartRequest(List.of(
                new CartItemInput("EBP-STD-BLK", 1)
        ));

        mockMvc.perform(put("/api/cart")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/agent/chat")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"我购物车里这个适合送人吗？","history":[],"currentPath":"/cart","language":"zh","guestCartItems":[]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liveModel").value(true));

        assertTrue(StubOpenAiResponsesClient.lastInstructions.contains("Cart context"));
        assertTrue(StubOpenAiResponsesClient.lastInstructions.contains("EchoBeat Pro"));
    }

    @Test
    void returnsSetupMessageWhenModelIsNotConfigured() throws Exception {
        StubOpenAiResponsesClient.configured = false;

                mockMvc.perform(post("/api/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"你好","history":[],"currentPath":"/","language":"zh","guestCartItems":[]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liveModel").value(false))
                .andExpect(jsonPath("$.reply").value(org.hamcrest.Matchers.containsString("LM Studio")));
    }

    private String registerAndGetToken() throws Exception {
        String username = "agent_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        RegisterRequest request = new RegisterRequest(
                username,
                "buyer123456",
                "Agent User",
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

    @TestConfiguration
    static class AgentTestConfig {

        @Bean
        @Primary
        OpenAiResponsesClient openAiResponsesClient() {
            return new StubOpenAiResponsesClient();
        }
    }

    static final class StubOpenAiResponsesClient extends OpenAiResponsesClient {

        static boolean configured = true;
        static String reply = "stub";
        static String lastInstructions = "";

        StubOpenAiResponsesClient() {
            super(new ObjectMapper(), "", "gpt-5.2", "http://localhost");
        }

        @Override
        public boolean isConfigured() {
            return configured;
        }

        @Override
        public String createReply(String instructions, List<com.malllite.agent.dto.AgentMessageRequest> history, String userMessage) {
            lastInstructions = instructions;
            return reply;
        }
    }
}
