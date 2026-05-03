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

    public GuardrailDecision decide(
            LLMGuarded guarded,
            BigDecimal estimatedCost,
            String userId,
            String tenantId
    ) {
        if (!properties.isEnabled()) {
            return GuardrailDecision.ALLOW;
        }
        LocalDate today = LocalDate.now();
        YearMonth month = YearMonth.now();
        if (!isBudgetExceeded(estimatedCost, userId, tenantId, today, month)) {
            return GuardrailDecision.ALLOW;
        }
        GuardrailAction annotationAction = guarded.onViolation();
        GuardrailAction action = annotationAction != GuardrailAction.WARN
                ? annotationAction
                : properties.getDefaultAction();
        return switch (action) {
            case WARN -> GuardrailDecision.WARN;
            case BLOCK -> GuardrailDecision.BLOCK;
            case FALLBACK -> GuardrailDecision.FALLBACK;
        };
    }

    private boolean isBudgetExceeded(
            BigDecimal cost,
            String userId,
            String tenantId,
            LocalDate today,
            YearMonth month
    ) {
        boolean dailyExceeded = usageStore.sumByDay(today).add(cost)
                .compareTo(properties.getDailyBudgetUsd()) > 0;
        boolean monthlyExceeded = usageStore.sumByMonth(month).add(cost)
                .compareTo(properties.getMonthlyBudgetUsd()) > 0;
        boolean userDailyExceeded = usageStore.sumByUserByDay(userId, today).add(cost)
                .compareTo(properties.getPerUserDailyBudgetUsd()) > 0;
        boolean tenantMonthlyExceeded = usageStore.sumByTenantByMonth(tenantId, month).add(cost)
                .compareTo(properties.getPerTenantMonthlyBudgetUsd()) > 0;
        return dailyExceeded || monthlyExceeded || userDailyExceeded || tenantMonthlyExceeded;
    }
}
