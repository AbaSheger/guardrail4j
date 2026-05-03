package io.github.abasheger.guardrail4j.usage;

import io.github.abasheger.guardrail4j.model.UsageRecord;
import io.github.abasheger.guardrail4j.model.UsageSummaryResponse;
import io.github.abasheger.guardrail4j.store.UsageStore;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UsageSummaryService {

    private final UsageStore usageStore;

    public UsageSummaryService(UsageStore usageStore) {
        this.usageStore = usageStore;
    }

    public UsageSummaryResponse summarize() {
        List<UsageRecord> records = usageStore.findAll();

        return new UsageSummaryResponse(
                records.size(),
                totalCost(records),
                costBy(records, UsageRecord::provider),
                costBy(records, UsageRecord::model),
                costBy(records, UsageRecord::userId),
                costBy(records, UsageRecord::tenantId),
                costBy(records, UsageRecord::feature)
        );
    }

    private BigDecimal totalCost(List<UsageRecord> records) {
        return records.stream()
                .map(UsageRecord::estimatedCostUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<String, BigDecimal> costBy(
            List<UsageRecord> records,
            Function<UsageRecord, String> classifier
    ) {
        return records.stream()
                .collect(Collectors.groupingBy(
                        classifier,
                        Collectors.mapping(
                                UsageRecord::estimatedCostUsd,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));
    }
}
