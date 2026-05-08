# Changelog

All notable changes to Guardrail4J are documented here.

Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [Unreleased]

### Added
- SpEL-based dynamic `userId` and `tenantId` extraction from method arguments (`#userId`, `#p0`, etc.) - see PR #4
- `SpelExpressionResolver` helper with safe fallback on resolution failure
- `<parameters>true</parameters>` compiler flag so Spring 6.1+ can discover parameter names
- Usage summary endpoint for grouped provider, model, user, tenant, and feature cost totals
- Social demo asset generated from static HTML and Playwright

---

## [0.1.0-SNAPSHOT] - MVP

### Added (PR #1 - initial scaffold)
- `@LLMGuarded` annotation with `provider`, `model`, `userId`, `tenantId`, `feature`, `estimatedInputTokens`, `estimatedOutputTokens`, `onViolation`, and `fallbackModel`
- `GuardrailInterceptor` AOP aspect that intercepts annotated methods, estimates cost, and enforces budgets
- `GuardrailDecisionEngine` for daily, monthly, per-user daily, and per-tenant monthly budget decisions
- `CostEstimator` for estimated USD cost from annotation token fields and a configurable price table
- `InMemoryUsageStore` with thread-safe in-memory storage and aggregation queries
- `Guardrail4jAutoConfiguration` for Spring Boot auto-configuration
- `Guardrail4jProperties` bound to `guardrail4j.*` with default OpenAI and Anthropic pricing
- `GuardrailController` REST endpoints: `GET /guardrail4j/usage`, `GET /guardrail4j/usage/summary`, and `GET /guardrail4j/health`
- Demo application (`guardrail4j-demo`) with a fake summarization endpoint
- GitHub Actions CI workflow

### Changed (PR #2 - cleanup and hardening)
- Simplified auto-configuration logging
- Clarified MVP limitations in README
- Removed dead code from initial scaffold

### Changed (PR #3 - Maven and CI hardening)
- Pinned Maven plugin versions (`compiler`, `surefire`, `failsafe`, `enforcer`)
- Added `maven-enforcer-plugin` to require Java 21+ and Maven 3.9+
- Documented `mvn clean verify` as the canonical build command
