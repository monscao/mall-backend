package com.malllite.agent.dto;

public record AgentCartItemRequest(
        String productName,
        String skuName,
        String quantity,
        String salePrice
) {
}
