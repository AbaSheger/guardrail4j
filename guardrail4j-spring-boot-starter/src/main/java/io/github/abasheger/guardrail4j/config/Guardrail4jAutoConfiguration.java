package io.github.abasheger.guardrail4j.config;

import io.github.abasheger.guardrail4j.aop.GuardrailInterceptor;
import io.github.abasheger.guardrail4j.controller.GuardrailController;
import io.github.abasheger.guardrail4j.cost.CostEstimator;
import io.github.abasheger.guardrail4j.decision.GuardrailDecisionEngine;
import io.github.abasheger.guardrail4j.spel.SpelExpressionResolver;
import io.github.abasheger.guardrail4j.store.InMemoryUsageStore;
import io.github.abasheger.guardrail4j.store.UsageStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(Guardrail4jProperties.class)
@ConditionalOnProperty(prefix = "guardrail4j", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Guardrail4jAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public UsageStore usageStore() {
        return new InMemoryUsageStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public CostEstimator costEstimator(Guardrail4jProperties properties) {
        return new CostEstimator(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public GuardrailDecisionEngine decisionEngine(UsageStore usageStore, Guardrail4jProperties properties) {
        return new GuardrailDecisionEngine(usageStore, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SpelExpressionResolver spelExpressionResolver() {
        return new SpelExpressionResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public GuardrailInterceptor interceptor(
            UsageStore usageStore,
            CostEstimator costEstimator,
            GuardrailDecisionEngine decisionEngine,
            Guardrail4jProperties properties,
            SpelExpressionResolver spelExpressionResolver
    ) {
        return new GuardrailInterceptor(usageStore, costEstimator, decisionEngine, properties, spelExpressionResolver);
    }

    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnMissingBean
    public GuardrailController guardrailController(UsageStore usageStore, Guardrail4jProperties properties) {
        return new GuardrailController(usageStore, properties);
    }
}
