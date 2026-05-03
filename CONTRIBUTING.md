# Contributing to Guardrail4J

Thanks for your interest. Guardrail4J is an early-stage open-source project and contributions are welcome — especially bug reports, test coverage, and feedback on the API shape.

## Prerequisites

- Java 21+
- Maven 3.9+
- Git

## Getting started

```bash
git clone https://github.com/AbaSheger/guardrail4j.git
cd guardrail4j
mvn clean verify
```

All tests should pass before you make any changes.

## Running tests

```bash
# Run all tests across all modules
mvn clean test

# Run a single test class
mvn -Dtest=Guardrail4jTests test -pl guardrail4j-spring-boot-starter

# Run a single test method
mvn -Dtest=Guardrail4jTests#costEstimationUsesConfiguredPriceTable test -pl guardrail4j-spring-boot-starter
```

## Branch naming

| Type | Pattern | Example |
|------|---------|---------|
| Feature | `feature/<short-description>` | `feature/redis-usage-store` |
| Bug fix | `fix/<short-description>` | `fix/decision-engine-overflow` |
| Documentation | `docs/<short-description>` | `docs/improve-readme` |

Work from `main`. Open a PR against `main`.

## Before opening a PR

- [ ] `mvn clean verify` passes locally
- [ ] New behaviour is covered by tests
- [ ] No unrelated files are included in the diff
- [ ] The PR description explains what changed and why

## PR expectations

Keep PRs small and focused. A PR that does one thing is much easier to review than one that does three. If your change is large, consider breaking it into sequential PRs.

The project uses a PR template — fill it out completely. Reviewers may ask you to add tests or split a PR before merging.

## Code style

The project follows standard Java conventions. There is no checkstyle enforcer yet, but please match the style of the surrounding code: 4-space indentation, no wildcard imports, descriptive names.

## Reporting bugs

Open a [GitHub issue](https://github.com/AbaSheger/guardrail4j/issues) using the bug report template. For security issues, see [SECURITY.md](SECURITY.md).
