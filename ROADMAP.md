# Roadmap

This file tracks the planned evolution of Guardrail4J. Items are realistic and sequenced by dependency — later milestones depend on earlier ones being stable.

---

## v0.1 — MVP (current)

**Goal:** prove the annotation-based interception model works end-to-end.

- [x] `@LLMGuarded` annotation
- [x] AOP-based `GuardrailInterceptor`
- [x] In-memory `UsageStore` with daily, monthly, per-user, and per-tenant aggregation
- [x] `CostEstimator` with configurable price table
- [x] `GuardrailDecisionEngine` with `ALLOW / WARN / BLOCK / FALLBACK` outcomes
- [x] Spring Boot auto-configuration
- [x] REST monitoring endpoints (`/guardrail4j/usage`, `/guardrail4j/health`)
- [x] Demo application

---

## v0.2 — Dynamic Identity and Better Observability

**Goal:** make the starter useful for real multi-tenant SaaS apps.

- [ ] SpEL-based `userId` and `tenantId` extraction from method arguments (`#userId`, `#p0`)
- [ ] Identity extraction from Spring Security context (optional integration)
- [ ] Structured usage summaries per feature, user, and tenant
- [ ] Improved logging format with structured MDC fields

---

## v0.3 — Persistent Storage

**Goal:** usage data survives restarts and works across multiple instances.

- [ ] `UsageStore` backed by PostgreSQL (Spring Data JPA)
- [ ] `UsageStore` backed by Redis (optional, for high-throughput scenarios)
- [ ] Migration script for schema setup
- [ ] Documentation for choosing a storage backend

---

## v0.4 — Micrometer Metrics

**Goal:** integrate with standard Spring Boot observability tooling.

- [ ] Counters for `ALLOW`, `WARN`, `BLOCK`, `FALLBACK` decisions
- [ ] Gauges for daily and monthly spend
- [ ] Histograms for estimated cost per call
- [ ] Prometheus scrape endpoint compatibility via existing Actuator setup

---

## Future

These are exploratory and depend on community interest.

- **Real fallback execution** — automatically re-invoke with the fallback model when `FALLBACK` is triggered
- **Provider adapters** — thin wrappers for OpenAI and Anthropic SDKs so token counts come from actual API responses
- **Richer pricing catalog** — auto-updated model pricing pulled from a public registry
- **Hosted dashboard** — a lightweight UI for visualising spend across users and tenants
- **Policy DSL** — a more expressive budget policy language beyond flat per-user / per-tenant thresholds
