# Guardrail4J

Guardrail4J is an open-source Spring Boot starter that adds lightweight guardrails around existing LLM service calls.

## Why it exists

Teams shipping AI features need a simple way to enforce spend limits, fallback decisions, and abuse protection without rewriting app code.

## Current status

Guardrail4J is an early MVP scaffold. It is useful for experimenting with annotation-based guardrails, cost estimation, and in-memory usage tracking, but it is not production-ready yet.

Important limitations:

- Usage is stored in memory and is lost when the app restarts.
- `userId` and `tenantId` in `@LLMGuarded` are static annotation metadata for now.
- Dynamic user or tenant extraction from method arguments, request headers, or Spring Security is planned.
- `FALLBACK` currently produces a decision/log signal only. It does not yet switch provider or model automatically.
- No real OpenAI, Anthropic, or other provider calls are made by the starter.

## Features (v0)

- `@LLMGuarded` annotation for LLM-calling methods
- Budget-aware decisions: `ALLOW`, `WARN`, `BLOCK`, `FALLBACK`
- In-memory usage tracking for MVP
- Cost estimation for OpenAI and Anthropic models using a configurable price table
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
@LLMGuarded(
    provider = "openai",
    model = "gpt-4o-mini",
    userId = "demo-user",
    tenantId = "demo-tenant",
    feature = "doc-summary"
)
public String summarize(String text) {
    // your existing LLM call
}
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

To disable Guardrail4J auto-configuration:

```yaml
guardrail4j:
  enabled: false
```

## Running locally

Build and run all tests from the repository root:

```bash
mvn clean verify
```

For a faster test-only run:

```bash
mvn clean test
```

Run the demo app:

```bash
mvn -pl guardrail4j-demo spring-boot:run
```

Then test the fake summarization endpoint:

```bash
curl -X POST http://localhost:8080/api/summarize \
  -H "Content-Type: application/json" \
  -d '{"text":"This is a long document that should be summarized by the fake demo service."}'
```

## Roadmap

- Dynamic identity resolution using SpEL, request headers, or Spring Security
- Durable storage with PostgreSQL or Redis
- Provider adapters and richer model catalogs
- Real fallback execution strategy
- Micrometer metrics integration
- More granular budget policies per feature, tenant, and environment
