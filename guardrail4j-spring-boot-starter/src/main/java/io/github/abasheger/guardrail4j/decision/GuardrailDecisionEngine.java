package io.github.abasheger.guardrail4j.decision;

import io.github.abasheger.guardrail4j.annotation.LLMGuarded;
import io.github.abasheger.guardrail4j.config.Guardrail4jProperties;
import io.github.abasheger.guardrail4j.model.GuardrailAction;
import io.github.abasheger.guardrail4j.model.GuardrailDecision;
import io.github.abasheger.guardrail4j.store.UsageStore;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

public class GuardrailDecisionEngine {
    private final UsageStore usageStore;
    private final Guardrail4jProperties properties;

    public GuardrailDecisionEngine(UsageStore usageStore, Guardrail4jProperties properties) {
        this.usageStore = usageStore;
        this.properties = properties;
    }

    public GuardrailDecision decide(LLMGuarded guarded, BigDecimal estimatedCost) {
        return decide(guarded, estimatedCost, guarded.userId(), guarded.tenantId());
    }

    public GuardrailDecision decide(LLMGuarded guarded, BigDecimal estimatedCost, String userId, String tenantId) {
        if (!properties.isEnabled()) return GuardrailDecision.ALLOW;
        LocalDate today = LocalDate.now();
        YearMonth month = YearMonth.now();
        boolean exceeded = usageStore.sumByDay(today).add(estimatedCost).compareTo(properties.getDailyBudgetUsd()) > 0
                || usageStore.sumByMonth(month).add(estimatedCost).compareTo(properties.getMonthlyBudgetUsd()) > 0
                || usageStore.sumByUserByDay(userId, today).add(estimatedCost).compareTo(properties.getPerUserDailyBudgetUsd()) > 0
                || usageStore.sumByTenantByMonth(tenantId, month).add(estimatedCost).compareTo(properties.getPerTenantMonthlyBudgetUsd()) > 0;
        if (!exceeded) return GuardrailDecision.ALLOW;
        GuardrailAction action = guarded.onViolation() != GuardrailAction.WARN ? guarded.onViolation() : properties.getDefaultAction();
        return switch (action) {
            case WARN -> GuardrailDecision.WARN;
            case BLOCK -> GuardrailDecision.BLOCK;
            case FALLBACK -> GuardrailDecision.FALLBACK;
        };
    }
}
