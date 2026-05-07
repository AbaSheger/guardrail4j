package io.github.abasheger.guardrail4j.store;

import io.github.abasheger.guardrail4j.model.UsageRecord;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Storage abstraction for usage records and budget aggregation.
 *
 * <p>Applications can provide their own Spring bean implementing this interface
 * to replace the default in-memory store. Production deployments that run more
 * than one application instance should use a shared persistent implementation
 * so every instance evaluates budgets against the same usage data.</p>
 */
public interface UsageStore {
    void save(UsageRecord record);
    List<UsageRecord> findAll();
    BigDecimal sumByDay(LocalDate day);
    BigDecimal sumByMonth(YearMonth month);
    BigDecimal sumByUserByDay(String userId, LocalDate day);
    BigDecimal sumByTenantByMonth(String tenantId, YearMonth month);
}
