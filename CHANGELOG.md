# Changelog

All notable changes to Guardrail4J are documented here.

Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [Unreleased]

### Added
- SpEL-based dynamic `userId` and `tenantId` extraction from method arguments (`#userId`, `#p0`, etc.) — see PR #4
- `SpelExpressionResolver` helper with safe fallback on resolution failure
- `<parameters>true</parameters>` compiler flag so Spring 6.1+ can discover parameter names

---

## [0.1.0-SNAPSHOT] — MVP

### Added (PR #1 — initial scaffold)
- `@LLMGuarded` annotation with `provider`, `model`, `userId`, `tenantId`, `feature`, `estimatedInputTokens`, `estimatedOutputTokens`, `onViolation`, `fallbackModel`
- `GuardrailInterceptor` AOP aspect — intercepts annotated methods, estimates cost, enforces budgets
- `GuardrailDecisionEngine` — evaluates four budget tiers (daily, monthly, per-user-daily, per-tenant-monthly) and returns `ALLOW / WARN / BLOCK / FALLBACK`
- `CostEstimator` — calculates estimated USD cost from annotation token fields and a configurable price table
- `InMemoryUsageStore` — thread-safe in-memory `UsageStore` with aggregation queries
- `Guardrail4jAutoConfiguration` — Spring Boot auto-configuration wiring all components
- `Guardrail4jProperties` — bound to `guardrail4j.*`; pre-configured with OpenAI and Anthropic pricing
- `GuardrailController` — REST endpoints: `GET /guardrail4j/usage`, `GET /guardrail4j/health`
- Demo application (`guardrail4j-demo`) with a fake summarization endpoint
- GitHub Actions CI workflow

### Changed (PR #2 — cleanup and hardening)
- Simplified auto-configuration logging
- Clarified MVP limitations in README
- Removed dead code from initial scaffold

### Changed (PR #3 — Maven and CI hardening)
- Pinned Maven plugin versions (`compiler`, `surefire`, `failsafe`, `enforcer`)
- Added `maven-enforcer-plugin` to require Java 21+ and Maven 3.9+
- Documented `mvn clean verify` as the canonical build command
