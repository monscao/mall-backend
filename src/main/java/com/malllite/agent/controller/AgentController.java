package com.malllite.agent.controller;

import com.malllite.agent.dto.AgentChatRequest;
import com.malllite.agent.dto.AgentChatResponse;
import com.malllite.agent.service.AgentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentService agentService;
    private final ObjectMapper objectMapper;

    public AgentController(AgentService agentService, ObjectMapper objectMapper) {
        this.agentService = agentService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/chat")
    public AgentChatResponse chat(
            @Valid @RequestBody AgentChatRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return agentService.chat(request, authorizationHeader);
    }

    @PostMapping(path = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> streamChat(
            @Valid @RequestBody AgentChatRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        StreamingResponseBody body = outputStream -> {
            AgentChatResponse result = agentService.streamChat(request, authorizationHeader, delta ->
                    writeEvent(outputStream, null, Map.of("delta", delta))
            );
            writeEvent(outputStream, "done", Map.of(
                    "liveModel", result.liveModel(),
                    "model", result.model()
            ));
        };

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(body);
    }

    private void writeEvent(java.io.OutputStream outputStream, String eventName, Map<String, Object> payload) {
        try {
            if (eventName != null && !eventName.isBlank()) {
                outputStream.write(("event: " + eventName + "\n").getBytes(StandardCharsets.UTF_8));
            }
            outputStream.write(("data: " + objectMapper.writeValueAsString(payload) + "\n\n").getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to stream agent response", exception);
        }
    }
}
