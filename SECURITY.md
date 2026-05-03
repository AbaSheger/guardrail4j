# Security Policy

## Reporting a vulnerability

**Please do not open a public GitHub issue for security vulnerabilities.**

If you discover a security issue, email the maintainer directly:

**antetibo@gmail.com**

Include:
- A description of the vulnerability
- Steps to reproduce it
- The version or commit hash where you observed the issue
- Any suggested fix, if you have one

You will receive a response within 7 days. If the issue is confirmed, a fix will be prioritised and a patched version released as soon as practical.

## Current project status

Guardrail4J is an **early MVP** and is **not production-ready**. Known limitations relevant to security:

- Usage data is stored in memory with no access control. Do not expose `/guardrail4j/usage` or `/guardrail4j/health` to untrusted clients without adding your own authentication layer.
- No input sanitisation is applied to `userId`, `tenantId`, or `feature` strings before they are stored in the usage record.
- The project has not been through a security audit.

Use appropriate network-level controls if you deploy this in any environment beyond local development.
