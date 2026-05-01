package io.github.abasheger.guardrail4j.aop;

import io.github.abasheger.guardrail4j.annotation.LLMGuarded;
import io.github.abasheger.guardrail4j.config.Guardrail4jProperties;
import io.github.abasheger.guardrail4j.cost.CostEstimator;
import io.github.abasheger.guardrail4j.decision.GuardrailDecisionEngine;
import io.github.abasheger.guardrail4j.model.GuardrailDecision;
import io.github.abasheger.guardrail4j.model.UsageRecord;
import io.github.abasheger.guardrail4j.store.UsageStore;
import java.math.BigDecimal;
import java.time.Instant;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class GuardrailInterceptor {
    private final UsageStore usageStore;
    private final CostEstimator costEstimator;
    private final GuardrailDecisionEngine decisionEngine;
    private final Guardrail4jProperties properties;

    public GuardrailInterceptor(UsageStore usageStore, CostEstimator costEstimator,
                                GuardrailDecisionEngine decisionEngine, Guardrail4jProperties properties) {
        this.usageStore = usageStore;
        this.costEstimator = costEstimator;
        this.decisionEngine = decisionEngine;
        this.properties = properties;
    }

    @Around("@annotation(guarded)")
    public Object around(ProceedingJoinPoint joinPoint, LLMGuarded guarded) throws Throwable {
        BigDecimal estimatedCost = costEstimator.estimate(guarded);
        GuardrailDecision decision = decisionEngine.decide(guarded, estimatedCost);

        if (decision == GuardrailDecision.BLOCK) {
            throw new IllegalStateException("Guardrail4J blocked this LLM call due to budget limits");
        }
        if (decision == GuardrailDecision.WARN) {
            System.out.println("[Guardrail4J WARN] budget threshold exceeded for method " + joinPoint.getSignature().toShortString());
        }
        if (decision == GuardrailDecision.FALLBACK) {
            System.out.println("[Guardrail4J FALLBACK] Switch to fallback model: " + properties.getFallbackModel());
        }

        Object result = joinPoint.proceed();
        usageStore.save(new UsageRecord(guarded.provider(), guarded.model(), guarded.userId(), guarded.tenantId(), guarded.feature(),
                guarded.estimatedInputTokens(), guarded.estimatedOutputTokens(), estimatedCost, Instant.now()));
        return result;
    }
}
