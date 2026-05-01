package io.github.abasheger.guardrail4j.store;

import io.github.abasheger.guardrail4j.model.UsageRecord;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface UsageStore {
    void save(UsageRecord record);
    List<UsageRecord> findAll();
    BigDecimal sumByDay(LocalDate day);
    BigDecimal sumByMonth(YearMonth month);
    BigDecimal sumByUserByDay(String userId, LocalDate day);
    BigDecimal sumByTenantByMonth(String tenantId, YearMonth month);
}
