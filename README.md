# Guardrail4J

Guardrail4J is an open-source Spring Boot starter that adds lightweight guardrails around existing LLM service calls.

## Why it exists
Teams shipping AI features need a simple way to enforce spend limits, fallback behavior, and abuse protection without rewriting app code.

## Features (v0)
- `@LLMGuarded` annotation for LLM-calling methods
- Budget-aware decisions: `ALLOW`, `WARN`, `BLOCK`, `FALLBACK`
- In-memory usage tracking for MVP
- Cost estimation for OpenAI + Anthropic models (configurable)
- REST endpoints:
  - `GET /guardrail4j/usage`
  - `GET /guardrail4j/health`

## Installation
```xml
<dependency>
  <groupId>io.github.abasheger</groupId>
  <artifactId>guardrail4j-spring-boot-starter</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Usage
Annotate methods that perform LLM work:
```java
@LLMGuarded(provider = "openai", model = "gpt-4o-mini", feature = "doc-summary")
public String summarize(String text) { ... }
```

## YAML config
```yaml
guardrail4j:
  enabled: true
  defaultAction: WARN # WARN | BLOCK | FALLBACK
  monthlyBudgetUsd: 50
  dailyBudgetUsd: 5
  perUserDailyBudgetUsd: 1
  perTenantMonthlyBudgetUsd: 15
  fallbackModel: claude-3-5-haiku
```

## Roadmap
- Durable storage (PostgreSQL/Redis)
- Provider adapters + richer model catalogs
- Better per-request identities via resolver SPI
- Metrics integration (Micrometer)
