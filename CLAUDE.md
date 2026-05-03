# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**guardrail4j** is a Spring Boot starter that adds cost and budget guardrails to LLM API calls via a single `@LLMGuarded` annotation. It intercepts annotated methods, estimates their cost, checks configured budgets, and either allows, warns, blocks, or suggests a fallback model. The project is at MVP stage with in-memory-only state (no persistence).

## Build & Test Commands

Requires Java 21+ and Maven 3.9+.

```bash
# Full build + tests (matches CI)
mvn -B -ntp clean verify

# Fast test-only run
mvn clean test

# Run a specific test class
mvn -Dtest=Guardrail4jTests test

# Run a specific test method
mvn -Dtest=Guardrail4jTests#costEstimationUsesConfiguredPriceTable test

# Run the demo app
mvn -pl guardrail4j-demo spring-boot:run
```

The demo app starts on `http://localhost:8080`. Key endpoints:
- `POST /api/summarize` — annotated with `@LLMGuarded`, triggers the guardrail flow
- `GET /guardrail4j/usage` — all recorded UsageRecords
- `GET /guardrail4j/health` — status + record count

## Module Layout

| Module | Purpose |
|--------|---------|
| `guardrail4j-spring-boot-starter` | The library itself — all core logic lives here |
| `guardrail4j-demo` | Sample Spring Boot app that consumes the starter |

The root `pom.xml` is the parent aggregator (`guardrail4j-parent`).

## Architecture & Data Flow

```
@LLMGuarded method call
        ↓
GuardrailInterceptor  (AOP @Around aspect)
        ↓
CostEstimator         estimates USD cost from annotation token fields + pricing map
        ↓
GuardrailDecisionEngine  evaluates 4 budget tiers against UsageStore aggregates
        ↓
ALLOW → proceed          (budget OK)
WARN  → log + proceed    (budget soft breach)
BLOCK → throw IllegalStateException
FALLBACK → log suggestion (not yet implemented)
        ↓
UsageStore.save(UsageRecord)  records the call (skipped on BLOCK)
```

### Key Classes

- **`@LLMGuarded`** (`annotation/`) — method annotation carrying `provider`, `model`, `userId`, `tenantId`, `feature`, `estimatedInputTokens`, `estimatedOutputTokens`, `onViolation`, `fallbackModel`
- **`GuardrailInterceptor`** (`aop/`) — Spring AOP aspect; the entry point for every guarded call
- **`GuardrailDecisionEngine`** (`decision/`) — checks daily, monthly, per-user-daily, and per-tenant-monthly budgets and returns `GuardrailDecision` (ALLOW/WARN/BLOCK/FALLBACK)
- **`CostEstimator`** (`cost/`) — looks up the `provider:model` key in the `pricing` map from `Guardrail4jProperties` and applies the token formula
- **`UsageStore`** (`store/`) — interface with aggregation queries; only implementation is `InMemoryUsageStore` (thread-safe via `CopyOnWriteArrayList`, not persistent)
- **`Guardrail4jAutoConfiguration`** (`config/`) — registers all beans; guards behind `@ConditionalOnProperty(prefix="guardrail4j", name="enabled", matchIfMissing=true)`
- **`Guardrail4jProperties`** (`config/`) — bound to `guardrail4j.*`; holds budget thresholds, default action, fallback model, and `pricing` map

### Configuration Shape

```yaml
guardrail4j:
  enabled: true
  defaultAction: WARN          # WARN | BLOCK | FALLBACK
  dailyBudgetUsd: 10.00
  monthlyBudgetUsd: 100.00
  perUserDailyBudgetUsd: 2.00
  perTenantMonthlyBudgetUsd: 20.00
  fallbackModel: gpt-4o-mini
  pricing:
    openai:gpt-4o-mini:
      inputPer1MUsd: 0.15
      outputPer1MUsd: 0.60
    anthropic:claude-3-5-haiku:
      inputPer1MUsd: 0.25
      outputPer1MUsd: 1.25
```

## Important MVP Limitations

- `InMemoryUsageStore` is the only `UsageStore` implementation — all usage data is lost on restart.
- The FALLBACK action logs a suggestion but does not actually swap the model; the method executes normally.
- Cost tracking is based on *estimated* tokens from the annotation, not actual API response token counts.
- No persistence, no distributed state — not suitable for multi-instance deployments as-is.

## Code Style

- **Line length:** 100 characters max. Break method chains, long argument lists, and string concatenations across lines — never cram them onto one.
- **Method length:** If a method needs a mental scroll to read, extract a helper with a name that makes the comment redundant.
- **Naming:** Names should read like prose. Abbreviations are fine only when universally understood (`id`, `url`, `dto`); otherwise spell it out.
- **Single responsibility:** One class, one reason to change. One method, one level of abstraction.
- **No magic literals:** Named constants or enum values only — no bare `"WARN"`, `0.15`, `86400`, etc. scattered through logic.
- **Fail fast, flat:** Prefer guard clauses over nested if-else pyramids. Happy path should read top-to-bottom without indentation creep.
- **Imports:** No wildcard imports. Unused imports must be removed.
