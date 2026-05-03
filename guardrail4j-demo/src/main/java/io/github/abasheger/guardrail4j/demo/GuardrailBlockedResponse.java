package io.github.abasheger.guardrail4j.demo;

public record GuardrailBlockedResponse(
        String error,
        String message,
        String decision,
        String provider,
        String model,
        String userId,
        String tenantId,
        String feature
) {}
