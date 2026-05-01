package io.github.abasheger.guardrail4j.model;

import java.math.BigDecimal;
import java.time.Instant;

public record UsageRecord(
        String provider,
        String model,
        String userId,
        String tenantId,
        String feature,
        int estimatedInputTokens,
        int estimatedOutputTokens,
        BigDecimal estimatedCostUsd,
        Instant timestamp
) {}
