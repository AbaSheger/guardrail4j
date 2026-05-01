package io.github.abasheger.guardrail4j.annotation;

import io.github.abasheger.guardrail4j.model.GuardrailAction;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LLMGuarded {
    String provider() default "openai";
    String model() default "gpt-4o-mini";
    String userId() default "anonymous";
    String tenantId() default "default";
    String feature() default "general";
    int estimatedInputTokens() default 1000;
    int estimatedOutputTokens() default 250;
    GuardrailAction onViolation() default GuardrailAction.WARN;
    String fallbackModel() default "";
}
