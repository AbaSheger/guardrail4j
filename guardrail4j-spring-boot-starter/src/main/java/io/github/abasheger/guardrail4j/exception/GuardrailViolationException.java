package io.github.abasheger.guardrail4j.exception;

import io.github.abasheger.guardrail4j.model.GuardrailDecision;

public class GuardrailViolationException extends RuntimeException {

    private final GuardrailDecision decision;
    private final String provider;
    private final String model;
    private final String userId;
    private final String tenantId;
    private final String feature;

    public GuardrailViolationException(String message) {
        this(message, null, null, null, null, null, null);
    }

    public GuardrailViolationException(
            String message,
            GuardrailDecision decision,
            String provider,
            String model,
            String userId,
            String tenantId,
            String feature
    ) {
        super(message);
        this.decision = decision;
        this.provider = provider;
        this.model = model;
        this.userId = userId;
        this.tenantId = tenantId;
        this.feature = feature;
    }

    public GuardrailDecision getDecision() {
        return decision;
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    public String getUserId() {
        return userId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getFeature() {
        return feature;
    }
}
