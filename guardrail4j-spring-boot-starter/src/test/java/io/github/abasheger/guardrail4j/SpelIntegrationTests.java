package io.github.abasheger.guardrail4j;

import io.github.abasheger.guardrail4j.annotation.LLMGuarded;
import io.github.abasheger.guardrail4j.config.Guardrail4jAutoConfiguration;
import io.github.abasheger.guardrail4j.exception.GuardrailViolationException;
import io.github.abasheger.guardrail4j.model.GuardrailAction;
import io.github.abasheger.guardrail4j.model.GuardrailDecision;
import io.github.abasheger.guardrail4j.model.UsageRecord;
import io.github.abasheger.guardrail4j.store.UsageStore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpelIntegrationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AopAutoConfiguration.class, Guardrail4jAutoConfiguration.class))
            .withBean(TestLlmService.class);

    @Test
    void spelResolvesUserIdAndTenantIdFromMethodArguments() {
        contextRunner.run(ctx -> {
            TestLlmService service = ctx.getBean(TestLlmService.class);
            service.processDynamic("some text", "user-123", "tenant-456");

            UsageRecord record = ctx.getBean(UsageStore.class).findAll().get(0);
            assertThat(record.userId()).isEqualTo("user-123");
            assertThat(record.tenantId()).isEqualTo("tenant-456");
        });
    }

    @Test
    void staticUserIdAndTenantIdStillWork() {
        contextRunner.run(ctx -> {
            TestLlmService service = ctx.getBean(TestLlmService.class);
            service.processStatic("some text");

            UsageRecord record = ctx.getBean(UsageStore.class).findAll().get(0);
            assertThat(record.userId()).isEqualTo("static-user");
            assertThat(record.tenantId()).isEqualTo("static-tenant");
        });
    }

    @Test
    void usageRecordReflectsResolvedValues() {
        contextRunner.run(ctx -> {
            TestLlmService service = ctx.getBean(TestLlmService.class);
            service.processDynamic("text", "resolved-user", "resolved-tenant");

            UsageStore store = ctx.getBean(UsageStore.class);
            assertThat(store.findAll()).hasSize(1);
            UsageRecord record = store.findAll().get(0);
            assertThat(record.userId()).isEqualTo("resolved-user");
            assertThat(record.tenantId()).isEqualTo("resolved-tenant");
            assertThat(record.feature()).isEqualTo("integration-test");
        });
    }

    @Test
    void blockThrowsGuardrailViolationExceptionWithContext() {
        contextRunner
                .withPropertyValues("guardrail4j.daily-budget-usd=0.00")
                .run(ctx -> {
                    TestLlmService service = ctx.getBean(TestLlmService.class);

                    assertThatThrownBy(() -> service.processBlock("text", "alice", "acme"))
                            .isInstanceOfSatisfying(GuardrailViolationException.class, exception -> {
                                assertThat(exception.getMessage())
                                        .isEqualTo("Guardrail4J blocked this LLM call due to budget limits");
                                assertThat(exception.getDecision()).isEqualTo(GuardrailDecision.BLOCK);
                                assertThat(exception.getProvider()).isEqualTo("openai");
                                assertThat(exception.getModel()).isEqualTo("gpt-4o-mini");
                                assertThat(exception.getUserId()).isEqualTo("alice");
                                assertThat(exception.getTenantId()).isEqualTo("acme");
                                assertThat(exception.getFeature()).isEqualTo("blocked-summary");
                            });
                    assertThat(ctx.getBean(UsageStore.class).findAll()).isEmpty();
                });
    }

    @Test
    void warnStillProceedsAndRecordsUsage() {
        contextRunner
                .withPropertyValues(
                        "guardrail4j.daily-budget-usd=0.00",
                        "guardrail4j.default-action=WARN"
                )
                .run(ctx -> {
                    TestLlmService service = ctx.getBean(TestLlmService.class);

                    assertThat(service.processWarn("text", "alice", "acme")).isEqualTo("warn-result");

                    UsageRecord record = ctx.getBean(UsageStore.class).findAll().get(0);
                    assertThat(record.userId()).isEqualTo("alice");
                    assertThat(record.tenantId()).isEqualTo("acme");
                    assertThat(record.feature()).isEqualTo("warn-summary");
                });
    }

    @Test
    void fallbackStillProceedsAndRecordsUsage() {
        contextRunner
                .withPropertyValues("guardrail4j.daily-budget-usd=0.00")
                .run(ctx -> {
                    TestLlmService service = ctx.getBean(TestLlmService.class);

                    assertThat(service.processFallback("text", "alice", "acme"))
                            .isEqualTo("fallback-result");

                    UsageRecord record = ctx.getBean(UsageStore.class).findAll().get(0);
                    assertThat(record.userId()).isEqualTo("alice");
                    assertThat(record.tenantId()).isEqualTo("acme");
                    assertThat(record.feature()).isEqualTo("fallback-summary");
                });
    }

    @Test
    void allowStillProceedsAndRecordsUsage() {
        contextRunner.run(ctx -> {
            TestLlmService service = ctx.getBean(TestLlmService.class);

            assertThat(service.processDynamic("text", "alice", "acme")).isEqualTo("result");

            UsageRecord record = ctx.getBean(UsageStore.class).findAll().get(0);
            assertThat(record.userId()).isEqualTo("alice");
            assertThat(record.tenantId()).isEqualTo("acme");
            assertThat(record.feature()).isEqualTo("integration-test");
        });
    }

    static class TestLlmService {

        @LLMGuarded(
                provider = "openai",
                model = "gpt-4o-mini",
                userId = "#userId",
                tenantId = "#tenantId",
                feature = "integration-test"
        )
        public String processDynamic(String text, String userId, String tenantId) {
            return "result";
        }

        @LLMGuarded(
                provider = "openai",
                model = "gpt-4o-mini",
                userId = "static-user",
                tenantId = "static-tenant",
                feature = "integration-test"
        )
        public String processStatic(String text) {
            return "result";
        }

        @LLMGuarded(
                provider = "openai",
                model = "gpt-4o-mini",
                userId = "#userId",
                tenantId = "#tenantId",
                feature = "blocked-summary",
                onViolation = GuardrailAction.BLOCK
        )
        public String processBlock(String text, String userId, String tenantId) {
            return "block-result";
        }

        @LLMGuarded(
                provider = "openai",
                model = "gpt-4o-mini",
                userId = "#userId",
                tenantId = "#tenantId",
                feature = "warn-summary"
        )
        public String processWarn(String text, String userId, String tenantId) {
            return "warn-result";
        }

        @LLMGuarded(
                provider = "openai",
                model = "gpt-4o-mini",
                userId = "#userId",
                tenantId = "#tenantId",
                feature = "fallback-summary",
                onViolation = GuardrailAction.FALLBACK
        )
        public String processFallback(String text, String userId, String tenantId) {
            return "fallback-result";
        }
    }
}
