package io.github.abasheger.guardrail4j;

import io.github.abasheger.guardrail4j.annotation.LLMGuarded;
import io.github.abasheger.guardrail4j.config.Guardrail4jProperties;
import io.github.abasheger.guardrail4j.cost.CostEstimator;
import io.github.abasheger.guardrail4j.decision.GuardrailDecisionEngine;
import io.github.abasheger.guardrail4j.model.GuardrailAction;
import io.github.abasheger.guardrail4j.model.GuardrailDecision;
import io.github.abasheger.guardrail4j.model.UsageRecord;
import io.github.abasheger.guardrail4j.store.InMemoryUsageStore;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Guardrail4jTests {

    @Test
    void costEstimationUsesConfiguredPriceTable() throws Exception {
        Guardrail4jProperties properties = new Guardrail4jProperties();
        CostEstimator estimator = new CostEstimator(properties);
        LLMGuarded guarded = TestClass.class.getMethod("annotated").getAnnotation(LLMGuarded.class);

        BigDecimal estimate = estimator.estimate(guarded);

        assertEquals(new BigDecimal("0.00030000"), estimate);
    }

    @Test
    void costEstimationReturnsZeroForUnknownModel() throws Exception {
        Guardrail4jProperties properties = new Guardrail4jProperties();
        CostEstimator estimator = new CostEstimator(properties);
        LLMGuarded guarded = TestClass.class.getMethod("unknownModelAnnotated").getAnnotation(LLMGuarded.class);

        assertEquals(BigDecimal.ZERO, estimator.estimate(guarded));
    }

    @Test
    void decisionBlocksWhenDefaultActionIsBlockAndBudgetExceeded() throws Exception {
        Guardrail4jProperties properties = new Guardrail4jProperties();
        properties.setDailyBudgetUsd(new BigDecimal("0.00001"));
        properties.setDefaultAction(GuardrailAction.BLOCK);

        GuardrailDecisionEngine engine = new GuardrailDecisionEngine(new InMemoryUsageStore(), properties);
        LLMGuarded guarded = TestClass.class.getMethod("annotated").getAnnotation(LLMGuarded.class);

        GuardrailDecision decision = engine.decide(guarded, new BigDecimal("1.00"));

        assertEquals(GuardrailDecision.BLOCK, decision);
    }

    @Test
    void decisionReturnsFallbackWhenMethodRequestsFallbackOnViolation() throws Exception {
        Guardrail4jProperties properties = new Guardrail4jProperties();
        properties.setDailyBudgetUsd(new BigDecimal("0.00001"));

        InMemoryUsageStore store = new InMemoryUsageStore();
        store.save(new UsageRecord("openai", "gpt-4o-mini", "u1", "t1", "f", 100, 100,
                new BigDecimal("1.00"), Instant.now()));

        GuardrailDecisionEngine engine = new GuardrailDecisionEngine(store, properties);
        Method method = TestClass.class.getMethod("fallbackAnnotated");
        LLMGuarded guarded = method.getAnnotation(LLMGuarded.class);

        assertEquals(GuardrailDecision.FALLBACK, engine.decide(guarded, new BigDecimal("0.1")));
    }

    @Test
    void usageStoreAggregationsIncludeUserAndTenantBudgets() {
        InMemoryUsageStore store = new InMemoryUsageStore();
        Instant now = Instant.now();
        LocalDate utcDay = LocalDate.ofInstant(now, ZoneOffset.UTC);
        YearMonth utcMonth = YearMonth.from(now.atZone(ZoneOffset.UTC));

        store.save(new UsageRecord("openai", "gpt-4o-mini", "u1", "t1", "summary", 10, 10,
                new BigDecimal("0.25"), now));
        store.save(new UsageRecord("openai", "gpt-4o-mini", "u1", "t1", "summary", 10, 10,
                new BigDecimal("0.75"), now));

        assertTrue(store.sumByDay(utcDay).compareTo(new BigDecimal("1.00")) == 0);
        assertTrue(store.sumByUserByDay("u1", utcDay).compareTo(new BigDecimal("1.00")) == 0);
        assertTrue(store.sumByTenantByMonth("t1", utcMonth).compareTo(new BigDecimal("1.00")) == 0);
    }

    @Test
    void annotationParsingWorks() throws Exception {
        LLMGuarded guarded = TestClass.class.getMethod("annotated").getAnnotation(LLMGuarded.class);
        assertEquals("openai", guarded.provider());
        assertEquals("gpt-4o-mini", guarded.model());
    }

    static class TestClass {
        @LLMGuarded
        public void annotated() {}

        @LLMGuarded(provider = "openai", model = "not-in-table")
        public void unknownModelAnnotated() {}

        @LLMGuarded(onViolation = GuardrailAction.FALLBACK)
        public void fallbackAnnotated() {}
    }
}
