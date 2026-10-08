# Architecture

```text
Caller
  |
  | X-Caller-Id + request
  v
Spring Boot Public API :8080
  |-- Caller lookup (tenant/role)
  |-- validation
  |-- batch manifest + file matching
  |-- duplicate SHA-256 detection
  |-- public response contract
  |
  | HTTP internal calls (bounded client timeout)
  v
Python FastAPI :8001
  |-- TXT/PDF extraction
  |-- field evidence extraction
  |-- policy eligibility filter
  |     tenant + role + Approved + effective date
  |-- deterministic offline model double
  |-- answer / conflict / insufficient evidence
  v
Repository data/policies/policies.json
```

Important boundary: submitted document text is never merged into the policy corpus and cannot alter caller identity, tenant, role, approval state, dates, or application behavior.
