package com.malllite.agent.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record AgentChatRequest(
        @NotBlank(message = "Agent message is required")
        String message,
        @Valid List<AgentMessageRequest> history,
        String currentPath,
        String language,
        @Valid List<AgentCartItemRequest> guestCartItems
) {
}
