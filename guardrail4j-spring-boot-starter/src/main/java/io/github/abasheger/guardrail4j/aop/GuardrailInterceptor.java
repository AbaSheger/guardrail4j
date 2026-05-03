package io.github.abasheger.guardrail4j.aop;

import io.github.abasheger.guardrail4j.annotation.LLMGuarded;
import io.github.abasheger.guardrail4j.config.Guardrail4jProperties;
import io.github.abasheger.guardrail4j.cost.CostEstimator;
import io.github.abasheger.guardrail4j.decision.GuardrailDecisionEngine;
import io.github.abasheger.guardrail4j.model.GuardrailDecision;
import io.github.abasheger.guardrail4j.model.UsageRecord;
import io.github.abasheger.guardrail4j.spel.SpelExpressionResolver;
import io.github.abasheger.guardrail4j.store.UsageStore;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class GuardrailInterceptor {

    private static final Logger log = LoggerFactory.getLogger(GuardrailInterceptor.class);
    private static final String BLOCK_MESSAGE =
            "Guardrail4J blocked this LLM call due to budget limits";

    private final UsageStore usageStore;
    private final CostEstimator costEstimator;
    private final GuardrailDecisionEngine decisionEngine;
    private final Guardrail4jProperties properties;
    private final SpelExpressionResolver spelResolver;

    public GuardrailInterceptor(
            UsageStore usageStore,
            CostEstimator costEstimator,
            GuardrailDecisionEngine decisionEngine,
            Guardrail4jProperties properties,
            SpelExpressionResolver spelResolver
    ) {
        this.usageStore = usageStore;
        this.costEstimator = costEstimator;
        this.decisionEngine = decisionEngine;
        this.properties = properties;
        this.spelResolver = spelResolver;
    }

    @Around("@annotation(guarded)")
    public Object around(ProceedingJoinPoint joinPoint, LLMGuarded guarded) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object[] args = joinPoint.getArgs();

        String userId = spelResolver.resolve(guarded.userId(), method, args);
        String tenantId = spelResolver.resolve(guarded.tenantId(), method, args);

        BigDecimal estimatedCost = costEstimator.estimate(guarded);
        GuardrailDecision decision = decisionEngine.decide(guarded, estimatedCost, userId, tenantId);

        if (decision == GuardrailDecision.BLOCK) {
            throw new IllegalStateException(BLOCK_MESSAGE);
        }
        if (decision == GuardrailDecision.WARN) {
            log.warn("Guardrail4J budget threshold exceeded for method {}",
                    joinPoint.getSignature().toShortString());
        }
        if (decision == GuardrailDecision.FALLBACK) {
            handleFallback(joinPoint, guarded);
        }

        Object result = joinPoint.proceed();
        usageStore.save(new UsageRecord(
                guarded.provider(),
                guarded.model(),
                userId,
                tenantId,
                guarded.feature(),
                guarded.estimatedInputTokens(),
                guarded.estimatedOutputTokens(),
                estimatedCost,
                Instant.now()
        ));
        return result;
    }

    private void handleFallback(ProceedingJoinPoint joinPoint, LLMGuarded guarded) {
        String fallbackModel = guarded.fallbackModel().isBlank()
                ? properties.getFallbackModel()
                : guarded.fallbackModel();
        log.warn(
                "Guardrail4J fallback for method {}. Suggested model: {}."
                        + " Model switching is not implemented in the MVP yet.",
                joinPoint.getSignature().toShortString(),
                fallbackModel
        );
    }
}
