package io.github.abasheger.guardrail4j.cost;

import io.github.abasheger.guardrail4j.annotation.LLMGuarded;
import io.github.abasheger.guardrail4j.config.Guardrail4jProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CostEstimator {

    private static final BigDecimal TOKENS_PER_MILLION = BigDecimal.valueOf(1_000_000);
    private static final String MODEL_KEY_SEPARATOR = ":";

    private final Guardrail4jProperties properties;

    public CostEstimator(Guardrail4jProperties properties) {
        this.properties = properties;
    }

    public BigDecimal estimate(LLMGuarded guarded) {
        String key = guarded.provider() + MODEL_KEY_SEPARATOR + guarded.model();
        Guardrail4jProperties.ModelPrice modelPrice = properties.getPricing().get(key);
        if (modelPrice == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal input = modelPrice.inputPer1MUsd()
                .multiply(BigDecimal.valueOf(guarded.estimatedInputTokens()))
                .divide(TOKENS_PER_MILLION, 8, RoundingMode.HALF_UP);
        BigDecimal output = modelPrice.outputPer1MUsd()
                .multiply(BigDecimal.valueOf(guarded.estimatedOutputTokens()))
                .divide(TOKENS_PER_MILLION, 8, RoundingMode.HALF_UP);
        return input.add(output).setScale(8, RoundingMode.HALF_UP);
    }
}
