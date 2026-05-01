package io.github.abasheger.guardrail4j.config;

import io.github.abasheger.guardrail4j.model.GuardrailAction;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "guardrail4j")
public class Guardrail4jProperties {
    private boolean enabled = true;
    private GuardrailAction defaultAction = GuardrailAction.WARN;
    private BigDecimal monthlyBudgetUsd = new BigDecimal("100.00");
    private BigDecimal dailyBudgetUsd = new BigDecimal("10.00");
    private BigDecimal perUserDailyBudgetUsd = new BigDecimal("2.00");
    private BigDecimal perTenantMonthlyBudgetUsd = new BigDecimal("20.00");
    private String fallbackModel = "gpt-4o-mini";
    private Map<String, ModelPrice> pricing = new HashMap<>();

    public Guardrail4jProperties() {
        pricing.put("openai:gpt-4o-mini", new ModelPrice(new BigDecimal("0.15"), new BigDecimal("0.60")));
        pricing.put("anthropic:claude-3-5-haiku", new ModelPrice(new BigDecimal("0.25"), new BigDecimal("1.25")));
    }

    public record ModelPrice(BigDecimal inputPer1MUsd, BigDecimal outputPer1MUsd) {}
    // getters/setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public GuardrailAction getDefaultAction() { return defaultAction; }
    public void setDefaultAction(GuardrailAction defaultAction) { this.defaultAction = defaultAction; }
    public BigDecimal getMonthlyBudgetUsd() { return monthlyBudgetUsd; }
    public void setMonthlyBudgetUsd(BigDecimal monthlyBudgetUsd) { this.monthlyBudgetUsd = monthlyBudgetUsd; }
    public BigDecimal getDailyBudgetUsd() { return dailyBudgetUsd; }
    public void setDailyBudgetUsd(BigDecimal dailyBudgetUsd) { this.dailyBudgetUsd = dailyBudgetUsd; }
    public BigDecimal getPerUserDailyBudgetUsd() { return perUserDailyBudgetUsd; }
    public void setPerUserDailyBudgetUsd(BigDecimal perUserDailyBudgetUsd) { this.perUserDailyBudgetUsd = perUserDailyBudgetUsd; }
    public BigDecimal getPerTenantMonthlyBudgetUsd() { return perTenantMonthlyBudgetUsd; }
    public void setPerTenantMonthlyBudgetUsd(BigDecimal perTenantMonthlyBudgetUsd) { this.perTenantMonthlyBudgetUsd = perTenantMonthlyBudgetUsd; }
    public String getFallbackModel() { return fallbackModel; }
    public void setFallbackModel(String fallbackModel) { this.fallbackModel = fallbackModel; }
    public Map<String, ModelPrice> getPricing() { return pricing; }
    public void setPricing(Map<String, ModelPrice> pricing) { this.pricing = pricing; }
}
