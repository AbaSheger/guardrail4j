package io.github.abasheger.guardrail4j;

import io.github.abasheger.guardrail4j.config.Guardrail4jAutoConfiguration;
import io.github.abasheger.guardrail4j.model.UsageRecord;
import io.github.abasheger.guardrail4j.store.InMemoryUsageStore;
import io.github.abasheger.guardrail4j.store.UsageStore;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class Guardrail4jAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(Guardrail4jAutoConfiguration.class));

    @Test
    void customUsageStoreBeanOverridesInMemoryDefault() {
        contextRunner
                .withUserConfiguration(CustomUsageStoreConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(UsageStore.class);
                    assertThat(context).doesNotHaveBean(InMemoryUsageStore.class);
                    assertThat(context.getBean(UsageStore.class)).isInstanceOf(CustomUsageStore.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomUsageStoreConfig {

        @Bean
        UsageStore usageStore() {
            return new CustomUsageStore();
        }
    }

    static class CustomUsageStore implements UsageStore {

        @Override
        public void save(UsageRecord record) {
        }

        @Override
        public List<UsageRecord> findAll() {
            return List.of();
        }

        @Override
        public BigDecimal sumByDay(LocalDate day) {
            return BigDecimal.ZERO;
        }

        @Override
        public BigDecimal sumByMonth(YearMonth month) {
            return BigDecimal.ZERO;
        }

        @Override
        public BigDecimal sumByUserByDay(String userId, LocalDate day) {
            return BigDecimal.ZERO;
        }

        @Override
        public BigDecimal sumByTenantByMonth(String tenantId, YearMonth month) {
            return BigDecimal.ZERO;
        }
    }
}
