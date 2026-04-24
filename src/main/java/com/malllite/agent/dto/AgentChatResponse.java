package com.malllite.agent.dto;

public record AgentChatResponse(
        String reply,
        boolean liveModel,
        String model
) {
}
