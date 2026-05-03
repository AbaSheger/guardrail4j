package io.github.abasheger.guardrail4j.model;

import java.math.BigDecimal;
import java.util.Map;

public record UsageSummaryResponse(
        long totalCalls,
        BigDecimal totalEstimatedCostUsd,
        Map<String, BigDecimal> costByProvider,
        Map<String, BigDecimal> costByModel,
        Map<String, BigDecimal> costByUser,
        Map<String, BigDecimal> costByTenant,
        Map<String, BigDecimal> costByFeature
) {}
