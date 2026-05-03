package io.github.abasheger.guardrail4j.demo;

import io.github.abasheger.guardrail4j.exception.GuardrailViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DemoExceptionHandler {

    private static final String ERROR_GUARDRAIL_BLOCKED = "GUARDRAIL_BLOCKED";

    @ExceptionHandler(GuardrailViolationException.class)
    public ResponseEntity<GuardrailBlockedResponse> handleGuardrailViolation(
            GuardrailViolationException exception
    ) {
        GuardrailBlockedResponse response = new GuardrailBlockedResponse(
                ERROR_GUARDRAIL_BLOCKED,
                exception.getMessage(),
                exception.getDecision() != null ? exception.getDecision().name() : null,
                exception.getProvider(),
                exception.getModel(),
                exception.getUserId(),
                exception.getTenantId(),
                exception.getFeature()
        );
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }
}
