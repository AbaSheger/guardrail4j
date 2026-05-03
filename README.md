# Guardrail4J

**A Spring Boot starter that enforces LLM cost budgets, fallback decisions, and abuse protection — one annotation.**

[![CI](https://github.com/AbaSheger/guardrail4j/actions/workflows/ci.yml/badge.svg)](https://github.com/AbaSheger/guardrail4j/actions/workflows/ci.yml)
[![Java 21](https://img.shields.io/badge/Java-21-blue?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.3+](https://img.shields.io/badge/Spring%20Boot-3.3+-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/build-Maven-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

---

## Why Guardrail4J?

Teams shipping AI features need a simple way to enforce spend limits and protect against runaway costs — without rewriting their application code. Guardrail4J wraps any existing LLM method call with a single annotation. No vendor lock-in, no SDK replacement.

- Drop it into an existing Spring Boot app in minutes
- Budget enforcement at the daily, monthly, per-user, and per-tenant levels
- Decisions are declarative: `WARN`, `BLOCK`, or `FALLBACK`
- REST endpoints for live usage inspection

---

> **Status:** Early MVP. Suitable for experimentation and local development. Not production-ready. See [limitations](#current-limitations) and [roadmap](ROADMAP.md).

---

## Features

- `@LLMGuarded` annotation for any LLM-calling method
- Budget-aware decisions: `ALLOW` · `WARN` · `BLOCK` · `FALLBACK`
- In-memory usage tracking with per-user and per-tenant aggregation
- Cost estimation for OpenAI and Anthropic models via a configurable price table
- Dynamic `userId` / `tenantId` extraction from method arguments via SpEL
- REST monitoring endpoints: `/guardrail4j/usage` and `/guardrail4j/health`
- Spring Boot auto-configuration — zero boilerplate setup

---

## Quick Start

### 1. Add the dependency

```xml
<dependency>
  <groupId>io.github.abasheger</groupId>
  <artifactId>guardrail4j-spring-boot-starter</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### 2. Annotate your LLM method

```java
import io.github.abasheger.guardrail4j.annotation.LLMGuarded;
import io.github.abasheger.guardrail4j.model.GuardrailAction;

@LLMGuarded(
    provider = "openai",
    model = "gpt-4o-mini",
    userId = "user-123",
    tenantId = "acme-corp",
    feature = "doc-summary",
    estimatedInputTokens = 2000,
    estimatedOutputTokens = 500,
    onViolation = GuardrailAction.BLOCK
)
public String summarize(String document) {
    // your existing LLM call — unchanged
    return openAiClient.complete(document);
}
```

### Dynamic identity with SpEL

Set `userId` or `tenantId` to a Spring Expression starting with `#` to resolve values from the method's arguments at runtime:

```java
@LLMGuarded(
    provider = "openai",
    model = "gpt-4o-mini",
    userId = "#userId",
    tenantId = "#tenantId",
    feature = "document-summary",
    onViolation = GuardrailAction.BLOCK
)
public String summarizeDocument(String text, String userId, String tenantId) {
    return openAiClient.complete(text);
}
```

Positional references (`#p0`, `#p1`, …) also work when parameter names are unavailable. If the expression cannot be resolved, the raw string is used as a fallback.

### 3. Configure budgets in `application.yml`

```yaml
guardrail4j:
  enabled: true
  defaultAction: WARN          # WARN | BLOCK | FALLBACK
  dailyBudgetUsd: 5.00
  monthlyBudgetUsd: 50.00
  perUserDailyBudgetUsd: 1.00
  perTenantMonthlyBudgetUsd: 15.00
  fallbackModel: gpt-4o-mini
  pricing:
    openai:gpt-4o-mini:
      inputPer1MUsd: 0.15
      outputPer1MUsd: 0.60
    anthropic:claude-3-5-haiku:
      inputPer1MUsd: 0.25
      outputPer1MUsd: 1.25
```

To disable Guardrail4J without removing it:

```yaml
guardrail4j:
  enabled: false
```

---

## Annotation Reference

| Field | Default | Description |
|-------|---------|-------------|
| `provider` | `"openai"` | Provider key for price lookup |
| `model` | `"gpt-4o-mini"` | Model key for price lookup |
| `userId` | `"anonymous"` | User identifier; supports SpEL (`#userId`) |
| `tenantId` | `"default"` | Tenant identifier; supports SpEL (`#tenantId`) |
| `feature` | `"general"` | Feature tag for usage records |
| `estimatedInputTokens` | `1000` | Estimated prompt tokens |
| `estimatedOutputTokens` | `250` | Estimated completion tokens |
| `onViolation` | `WARN` | Action on budget breach: `WARN`, `BLOCK`, `FALLBACK` |
| `fallbackModel` | `""` | Suggested fallback model (logged only, not yet switched) |

---

## Monitoring Endpoints

These are available automatically when the app is a web application.

| Endpoint | Description |
|----------|-------------|
| `GET /guardrail4j/health` | Returns enabled status and total usage record count |
| `GET /guardrail4j/usage` | Returns all recorded `UsageRecord` entries |

---

## Running the Demo

```bash
# Build and run all tests
mvn clean verify

# Start the demo app (port 8080)
mvn -pl guardrail4j-demo spring-boot:run

# Hit the guarded endpoint
curl -X POST http://localhost:8080/api/summarize \
  -H "Content-Type: application/json" \
  -d '{"text":"This document needs to be summarized.","userId":"alice","tenantId":"acme"}'

# Inspect usage
curl http://localhost:8080/guardrail4j/usage
```

---

## Demo: dynamic per-user and per-tenant tracking

The demo endpoint accepts `userId` and `tenantId` in the request body. The `@LLMGuarded` annotation resolves them at runtime via SpEL (`#request.userId`, `#request.tenantId`), so each call is tracked against the correct user and tenant budgets.

**1. Make a guarded call as user `alice` in tenant `acme`:**

```bash
curl -X POST http://localhost:8080/api/summarize \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Spring Boot is a framework that simplifies building production-ready Java applications.",
    "userId": "alice",
    "tenantId": "acme"
  }'
```

```json
{ "summary": "[fake-llm-summary] Spring Boot is a framework that simplifies building production-ready Java applications." }
```

**2. Make a second call as a different user in the same tenant:**

```bash
curl -X POST http://localhost:8080/api/summarize \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Guardrail4J protects your LLM spend with annotation-based budget enforcement.",
    "userId": "bob",
    "tenantId": "acme"
  }'
```

**3. Inspect the usage log — each record shows the resolved identity:**

```bash
curl http://localhost:8080/guardrail4j/usage
```

```json
[
  {
    "provider": "openai",
    "model": "gpt-4o-mini",
    "userId": "alice",
    "tenantId": "acme",
    "feature": "document-summary",
    "estimatedCostUsd": 0.00063
  },
  {
    "provider": "openai",
    "model": "gpt-4o-mini",
    "userId": "bob",
    "tenantId": "acme",
    "feature": "document-summary",
    "estimatedCostUsd": 0.00063
  }
]
```

Once a user's daily budget is exhausted, the next call returns HTTP 500 with `Guardrail4J blocked this LLM call due to budget limits`.

---

## Current Limitations

Guardrail4J is an early MVP. Be aware of these constraints before using it in production:

- **In-memory only** — all usage data is lost on restart; no persistence yet
- **Estimated tokens only** — cost is based on annotation fields, not actual API response counts
- **No real LLM calls** — the starter enforces budgets but does not make or intercept actual provider API calls
- **FALLBACK is advisory** — logs a suggested model but does not switch the provider or model automatically
- **Single-instance** — in-memory state is not shared across multiple app instances

---

## Roadmap

See [ROADMAP.md](ROADMAP.md) for the full plan.

| Version | Theme |
|---------|-------|
| v0.1 | MVP: annotation, in-memory budgets, cost estimation |
| v0.2 | Dynamic identity via SpEL, improved usage summaries |
| v0.3 | Persistent storage (PostgreSQL / Redis) |
| v0.4 | Micrometer metrics integration |
| Future | Hosted dashboard, provider adapters, real fallback execution |

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). All contributions welcome — especially tests and feedback on the API shape.

---

## License

[MIT](LICENSE)
