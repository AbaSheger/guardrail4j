package io.github.abasheger.guardrail4j;

import io.github.abasheger.guardrail4j.annotation.LLMGuarded;
import io.github.abasheger.guardrail4j.config.Guardrail4jAutoConfiguration;
import io.github.abasheger.guardrail4j.model.UsageRecord;
import io.github.abasheger.guardrail4j.store.UsageStore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

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
    }
}
