package com.malllite.agent.dto;

import jakarta.validation.constraints.NotBlank;

public record AgentMessageRequest(
        @NotBlank(message = "Agent message role is required")
        String role,
        @NotBlank(message = "Agent message content is required")
        String content
) {
}
