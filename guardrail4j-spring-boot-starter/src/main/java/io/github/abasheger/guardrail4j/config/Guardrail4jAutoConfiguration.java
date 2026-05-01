package io.github.abasheger.guardrail4j.config;

import io.github.abasheger.guardrail4j.aop.GuardrailInterceptor;
import io.github.abasheger.guardrail4j.controller.GuardrailController;
import io.github.abasheger.guardrail4j.cost.CostEstimator;
import io.github.abasheger.guardrail4j.decision.GuardrailDecisionEngine;
import io.github.abasheger.guardrail4j.store.InMemoryUsageStore;
import io.github.abasheger.guardrail4j.store.UsageStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(Guardrail4jProperties.class)
public class Guardrail4jAutoConfiguration {
    @Bean @ConditionalOnMissingBean public UsageStore usageStore() { return new InMemoryUsageStore(); }
    @Bean @ConditionalOnMissingBean public CostEstimator costEstimator(Guardrail4jProperties p) { return new CostEstimator(p); }
    @Bean @ConditionalOnMissingBean public GuardrailDecisionEngine decisionEngine(UsageStore s, Guardrail4jProperties p) { return new GuardrailDecisionEngine(s, p); }
    @Bean @ConditionalOnMissingBean public GuardrailInterceptor interceptor(UsageStore s, CostEstimator c, GuardrailDecisionEngine d, Guardrail4jProperties p) { return new GuardrailInterceptor(s, c, d, p); }
    @Bean @ConditionalOnWebApplication public GuardrailController guardrailController(UsageStore s, Guardrail4jProperties p) { return new GuardrailController(s, p); }
}
