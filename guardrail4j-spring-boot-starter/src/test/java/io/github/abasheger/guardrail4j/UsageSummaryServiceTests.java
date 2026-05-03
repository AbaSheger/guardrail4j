package io.github.abasheger.guardrail4j;

import io.github.abasheger.guardrail4j.model.UsageRecord;
import io.github.abasheger.guardrail4j.model.UsageSummaryResponse;
import io.github.abasheger.guardrail4j.store.InMemoryUsageStore;
import io.github.abasheger.guardrail4j.usage.UsageSummaryService;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UsageSummaryServiceTests {

    @Test
    void summaryAggregatesTotalAndCostsByDimension() {
        InMemoryUsageStore store = new InMemoryUsageStore();
        UsageSummaryService service = new UsageSummaryService(store);
        Instant now = Instant.now();

        store.save(new UsageRecord("openai", "gpt-4o-mini", "alice", "acme", "summary",
                100, 50, new BigDecimal("0.10"), now));
        store.save(new UsageRecord("openai", "gpt-4o-mini", "alice", "acme", "summary",
                200, 100, new BigDecimal("0.20"), now));
        store.save(new UsageRecord("anthropic", "claude-3-5-haiku", "bob", "beta", "chat",
                300, 150, new BigDecimal("0.30"), now));

        UsageSummaryResponse summary = service.summarize();

        assertThat(summary.totalCalls()).isEqualTo(3);
        assertThat(summary.totalEstimatedCostUsd()).isEqualByComparingTo("0.60");
        assertThat(summary.costByProvider())
                .containsEntry("openai", new BigDecimal("0.30"))
                .containsEntry("anthropic", new BigDecimal("0.30"));
        assertThat(summary.costByModel())
                .containsEntry("gpt-4o-mini", new BigDecimal("0.30"))
                .containsEntry("claude-3-5-haiku", new BigDecimal("0.30"));
        assertThat(summary.costByUser())
                .containsEntry("alice", new BigDecimal("0.30"))
                .containsEntry("bob", new BigDecimal("0.30"));
        assertThat(summary.costByTenant())
                .containsEntry("acme", new BigDecimal("0.30"))
                .containsEntry("beta", new BigDecimal("0.30"));
        assertThat(summary.costByFeature())
                .containsEntry("summary", new BigDecimal("0.30"))
                .containsEntry("chat", new BigDecimal("0.30"));
    }

    @Test
    void summaryReturnsZerosAndEmptyGroupsWhenNoUsageExists() {
        UsageSummaryService service = new UsageSummaryService(new InMemoryUsageStore());

        UsageSummaryResponse summary = service.summarize();

        assertThat(summary.totalCalls()).isZero();
        assertThat(summary.totalEstimatedCostUsd()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.costByProvider()).isEmpty();
        assertThat(summary.costByModel()).isEmpty();
        assertThat(summary.costByUser()).isEmpty();
        assertThat(summary.costByTenant()).isEmpty();
        assertThat(summary.costByFeature()).isEmpty();
    }
}
