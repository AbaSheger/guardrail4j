package io.github.abasheger.guardrail4j.controller;

import io.github.abasheger.guardrail4j.config.Guardrail4jProperties;
import io.github.abasheger.guardrail4j.model.UsageRecord;
import io.github.abasheger.guardrail4j.store.UsageStore;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/guardrail4j")
public class GuardrailController {
    private final UsageStore usageStore;
    private final Guardrail4jProperties properties;

    public GuardrailController(UsageStore usageStore, Guardrail4jProperties properties) {
        this.usageStore = usageStore;
        this.properties = properties;
    }

    @GetMapping("/usage")
    public List<UsageRecord> usage() { return usageStore.findAll(); }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "enabled", properties.isEnabled(), "records", usageStore.findAll().size());
    }
}
