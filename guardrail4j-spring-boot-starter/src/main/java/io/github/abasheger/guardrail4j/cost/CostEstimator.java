package io.github.abasheger.guardrail4j.cost;

import io.github.abasheger.guardrail4j.annotation.LLMGuarded;
import io.github.abasheger.guardrail4j.config.Guardrail4jProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CostEstimator {
    private final Guardrail4jProperties properties;

    public CostEstimator(Guardrail4jProperties properties) { this.properties = properties; }

    public BigDecimal estimate(LLMGuarded guarded) {
        String key = guarded.provider() + ":" + guarded.model();
        Guardrail4jProperties.ModelPrice modelPrice = properties.getPricing().get(key);
        if (modelPrice == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal input = modelPrice.inputPer1MUsd().multiply(BigDecimal.valueOf(guarded.estimatedInputTokens()))
                .divide(BigDecimal.valueOf(1_000_000), 8, RoundingMode.HALF_UP);
        BigDecimal output = modelPrice.outputPer1MUsd().multiply(BigDecimal.valueOf(guarded.estimatedOutputTokens()))
                .divide(BigDecimal.valueOf(1_000_000), 8, RoundingMode.HALF_UP);
        return input.add(output).setScale(8, RoundingMode.HALF_UP);
    }
}
