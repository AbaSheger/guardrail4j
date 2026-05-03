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

    private static final int MAX_SUMMARY_LENGTH = 120;
    private static final String TRUNCATION_SUFFIX = "...";
    private static final String FAKE_LLM_PREFIX = "[fake-llm-summary] ";
    private static final String RESPONSE_KEY_SUMMARY = "summary";

    @PostMapping("/summarize")
    @LLMGuarded(
            provider = "openai",
            model = "gpt-4o-mini",
            userId = "#request.userId",
            tenantId = "#request.tenantId",
            feature = "document-summary",
            estimatedInputTokens = 2000,
            estimatedOutputTokens = 500,
            onViolation = GuardrailAction.BLOCK
    )
    public Map<String, String> summarize(@RequestBody SummarizeRequest request) {
        String text = request.text() != null ? request.text() : "";
        String summary = text.length() > MAX_SUMMARY_LENGTH
                ? text.substring(0, MAX_SUMMARY_LENGTH) + TRUNCATION_SUFFIX
                : text;
        return Map.of(RESPONSE_KEY_SUMMARY, FAKE_LLM_PREFIX + summary);
    }
}
