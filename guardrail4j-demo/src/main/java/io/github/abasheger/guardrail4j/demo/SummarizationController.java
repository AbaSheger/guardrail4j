package io.github.abasheger.guardrail4j.demo;

import io.github.abasheger.guardrail4j.annotation.LLMGuarded;
import io.github.abasheger.guardrail4j.model.GuardrailAction;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SummarizationController {

    @PostMapping("/summarize")
    @LLMGuarded(provider = "openai", model = "gpt-4o-mini", userId = "demo-user", tenantId = "demo-tenant", feature = "doc-summary", onViolation = GuardrailAction.FALLBACK)
    public Map<String, String> summarize(@RequestBody Map<String, String> request) {
        String text = request.getOrDefault("text", "");
        String summary = text.length() > 120 ? text.substring(0, 120) + "..." : text;
        return Map.of("summary", "[fake-llm-summary] " + summary);
    }
}
