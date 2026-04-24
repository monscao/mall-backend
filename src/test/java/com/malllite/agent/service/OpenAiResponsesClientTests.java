package com.malllite.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.malllite.agent.dto.AgentMessageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenAiResponsesClientTests {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void extractsTopLevelOutputText() throws Exception {
        server = startServer("""
                {"output_text":"你好，我可以帮你挑选手机。"}
                """);

        OpenAiResponsesClient client = new OpenAiResponsesClient(
                new ObjectMapper(),
                "test-key",
                "gpt-5.2",
                "http://localhost:%s".formatted(server.getAddress().getPort())
        );

        String reply = client.createReply("You are Moca", List.of(), "推荐手机");
        assertEquals("你好，我可以帮你挑选手机。", reply);
    }

    @Test
    void extractsMessageContentWhenOutputTextIsMissing() throws Exception {
        server = startServer("""
                {
                  "output": [
                    {
                      "type": "message",
                      "content": [
                        { "type": "output_text", "text": "可以先看轻薄本。" }
                      ]
                    }
                  ]
                }
                """);

        OpenAiResponsesClient client = new OpenAiResponsesClient(
                new ObjectMapper(),
                "test-key",
                "gpt-5.2",
                "http://localhost:%s".formatted(server.getAddress().getPort())
        );

        String reply = client.createReply("You are Moca", List.of(), "推荐笔记本");
        assertEquals("可以先看轻薄本。", reply);
    }

    @Test
    void throwsWhenModelReturnsNoText() throws Exception {
        server = startServer("""
                {"output":[{"type":"tool_call","content":[]}]}
                """);

        OpenAiResponsesClient client = new OpenAiResponsesClient(
                new ObjectMapper(),
                "test-key",
                "gpt-5.2",
                "http://localhost:%s".formatted(server.getAddress().getPort())
        );

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> client.createReply("You are Moca", List.of(), "推荐相机"));
        assertTrue(exception.getMessage().contains("no text"));
    }

    @Test
    void localEndpointDoesNotRequireApiKey() throws Exception {
        AtomicReference<String> authorizationHeader = new AtomicReference<>();
        server = startServer("""
                {"output_text":"LM Studio 已连接。"}
                """, exchange -> authorizationHeader.set(exchange.getRequestHeaders().getFirst("Authorization")));

        OpenAiResponsesClient client = new OpenAiResponsesClient(
                new ObjectMapper(),
                "",
                "local-model",
                "http://127.0.0.1:%s/v1/responses".formatted(server.getAddress().getPort())
        );

        String reply = client.createReply("You are Moca", List.of(), "测试一下");

        assertTrue(client.isConfigured());
        assertEquals("LM Studio 已连接。", reply);
        assertEquals(null, authorizationHeader.get());
    }

    @Test
    void assistantHistoryUsesOutputTextContentType() throws Exception {
        AtomicReference<String> requestBody = new AtomicReference<>();
        server = startServer("""
                {"output_text":"history ok"}
                """, exchange -> requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)));

        OpenAiResponsesClient client = new OpenAiResponsesClient(
                new ObjectMapper(),
                "test-key",
                "gpt-5.2",
                "http://localhost:%s".formatted(server.getAddress().getPort())
        );

        client.createReply(
                "You are Moca",
                List.of(
                        new AgentMessageRequest("user", "先推荐一台笔记本"),
                        new AgentMessageRequest("assistant", "可以看看 AeroBook 14。")
                ),
                "我还想看相机"
        );

        String body = requestBody.get();
        assertTrue(body.contains("\"role\":\"assistant\""));
        assertTrue(body.contains("\"type\":\"output_text\""));
    }

    private HttpServer startServer(String responseBody) throws IOException {
        return startServer(responseBody, exchange -> {
        });
    }

    private HttpServer startServer(String responseBody, ExchangeObserver observer) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/", exchange -> respond(exchange, responseBody, observer));
        httpServer.start();
        return httpServer;
    }

    private void respond(HttpExchange exchange, String responseBody, ExchangeObserver observer) throws IOException {
        observer.observe(exchange);
        byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(body);
        }
    }

    @FunctionalInterface
    private interface ExchangeObserver {
        void observe(HttpExchange exchange) throws IOException;
    }
}
