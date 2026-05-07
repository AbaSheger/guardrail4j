package io.github.abasheger.guardrail4j.store;

import io.github.abasheger.guardrail4j.model.UsageRecord;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Local, process-bound {@link UsageStore} implementation.
 *
 * <p>This store is intended for demos, tests, and local development only. It
 * keeps usage data in the current JVM, loses all records on restart, and does
 * not share state across horizontally scaled application instances. Production
 * deployments should provide a shared persistent {@link UsageStore}
 * implementation, such as a future PostgreSQL or Redis-backed store.</p>
 */
public class InMemoryUsageStore implements UsageStore {

    private final CopyOnWriteArrayList<UsageRecord> records = new CopyOnWriteArrayList<>();

    @Override
    public void save(UsageRecord record) {
        records.add(record);
    }

    @Override
    public List<UsageRecord> findAll() {
        return List.copyOf(records);
    }

    @Override
    public BigDecimal sumByDay(LocalDate day) {
        return records.stream()
                .filter(r -> LocalDate.ofInstant(r.timestamp(), ZoneOffset.UTC).equals(day))
                .map(UsageRecord::estimatedCostUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal sumByMonth(YearMonth month) {
        return records.stream()
                .filter(r -> YearMonth.from(r.timestamp().atZone(ZoneOffset.UTC)).equals(month))
                .map(UsageRecord::estimatedCostUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal sumByUserByDay(String userId, LocalDate day) {
        return records.stream()
                .filter(r -> r.userId().equals(userId))
                .filter(r -> LocalDate.ofInstant(r.timestamp(), ZoneOffset.UTC).equals(day))
                .map(UsageRecord::estimatedCostUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal sumByTenantByMonth(String tenantId, YearMonth month) {
        return records.stream()
                .filter(r -> r.tenantId().equals(tenantId))
                .filter(r -> YearMonth.from(r.timestamp().atZone(ZoneOffset.UTC)).equals(month))
                .map(UsageRecord::estimatedCostUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
