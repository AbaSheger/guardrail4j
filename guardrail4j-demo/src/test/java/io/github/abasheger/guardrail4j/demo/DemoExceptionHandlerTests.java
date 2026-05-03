package io.github.abasheger.guardrail4j.demo;

import io.github.abasheger.guardrail4j.exception.GuardrailViolationException;
import io.github.abasheger.guardrail4j.model.GuardrailDecision;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class DemoExceptionHandlerTests {

    @Test
    void guardrailViolationReturnsTooManyRequestsWithContext() {
        DemoExceptionHandler handler = new DemoExceptionHandler();
        GuardrailViolationException exception = new GuardrailViolationException(
                "Guardrail4J blocked this LLM call due to budget limits",
                GuardrailDecision.BLOCK,
                "openai",
                "gpt-4o-mini",
                "alice",
                "acme",
                "document-summary"
        );

        ResponseEntity<GuardrailBlockedResponse> response =
                handler.handleGuardrailViolation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody()).isEqualTo(new GuardrailBlockedResponse(
                "GUARDRAIL_BLOCKED",
                "Guardrail4J blocked this LLM call due to budget limits",
                "BLOCK",
                "openai",
                "gpt-4o-mini",
                "alice",
                "acme",
                "document-summary"
        ));
    }
}
