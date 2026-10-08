# Marlabs GenAI Spring Boot Candidate Assessment

## Employee policy and reimbursement triage

Two-service local implementation:

- **Spring Boot 3 / Java 21**: public API, caller lookup, request validation, multipart manifest validation, batch orchestration, response contract and traceability.
- **Python FastAPI**: TXT/PDF extraction, policy retrieval, deterministic offline answer generation, and reimbursement analysis.
- **No database / queue / cloud / frontend**. Policies are repository data as required by the assessment.
- **Offline model double** is the default. It is deterministic and requires no API key.

The implementation follows the assessment's evidence boundaries: only Approved policies matching caller tenant/role and the requested date are eligible; submitted requests are never policy sources; annual limits do not imply remaining balance or payable amount; every submitted item requires human review.

## Prerequisites

- Java 21
- Maven 3.9+
- Python 3.11+
- pip

## Start Python service

```bash
cd python-service
python -m venv .venv
# Linux/macOS: source .venv/bin/activate
# Windows: .venv\Scripts\activate
pip install -r requirements.txt
uvicorn app.main:app --host 127.0.0.1 --port 8001
```

## Start Spring Boot

```bash
cd spring-api
mvn spring-boot:run
```

Spring API: `http://127.0.0.1:8080`
Python API: `http://127.0.0.1:8001`

## Offline configuration

```text
PYTHON_SERVICE_URL=http://127.0.0.1:8001
CLIENT_TIMEOUT_MS=3000
```

The Python service has no model network dependency. `MODEL_MODE=offline` is the default.

## Public endpoints

### POST /answer

Headers:

```text
Content-Type: application/json
X-Caller-Id: atlas-employee-01
```

Body:

```json
{"question":"What is my annual certification reimbursement limit?","as_of":"2026-09-21"}
```

### POST /batches

`multipart/form-data` with:

- `metadata`: JSON manifest
- repeated `files`: uploaded UTF-8 TXT or text-based PDF files

Example metadata:

```json
{"batch_id":"demo-01","as_of":"2026-09-21","documents":[{"document_id":"request-01","filename":"request-01.txt"}]}
```

## Chosen nested response shapes

Each batch result uses:

```json
{
  "document_id":"request-01",
  "processing_status":"COMPLETED",
  "extracted": {
    "benefit":"certification",
    "amount":18000,
    "currency":"INR",
    "reference":"CERT-101"
  },
  "field_evidence": {
    "benefit":["..."],
    "amount":["..."],
    "currency":["..."],
    "reference":["..."]
  },
  "policy_outcome": {
    "status":"ANSWERED",
    "answer":"...",
    "citations":[{"chunk_id":"atlas-cert-current","quote":"..."}]
  },
  "review_required":true,
  "review_issues":["..."],
  "duplicate_of":null,
  "error":null
}
```

`/answer` returns `{status, answer, citations}` exactly. A conflict returns `CONFLICT` with `answer: null`; insufficient evidence returns `INSUFFICIENT_EVIDENCE` with empty citations.

## Stable error codes

- `INVALID_CALLER`
- `INVALID_DATE`
- `INVALID_QUESTION`
- `INVALID_METADATA`
- `DUPLICATE_MANIFEST_ID`
- `MISSING_FILE_PART`
- `EXTRA_FILE_PART`
- `EMPTY_FILE`
- `UNREADABLE_FILE`
- `DEPENDENCY_TIMEOUT`
- `DEPENDENCY_UNAVAILABLE`
- `MALFORMED_PROVIDER_RESPONSE`

## Supplied demonstration data

`data/policies/policies.json` contains the 12 policy records unchanged. `data/requests/` contains request-01 through request-08, including request-06 as an exact byte duplicate and request-08 as zero-byte.

Run the mixed batch:

```bash
curl -X POST http://127.0.0.1:8080/batches \
  -H 'X-Caller-Id: atlas-employee-01' \
  -F 'metadata={"batch_id":"demo-01","as_of":"2026-09-21","documents":[{"document_id":"request-01","filename":"request-01.txt"},{"document_id":"request-02","filename":"request-02.pdf"},{"document_id":"request-03","filename":"request-03.txt"},{"document_id":"request-04","filename":"request-04.txt"},{"document_id":"request-05","filename":"request-05.txt"},{"document_id":"request-06","filename":"request-06.txt"},{"document_id":"request-07","filename":"request-07.txt"},{"document_id":"request-08","filename":"request-08.txt"}]};type=application/json' \
  -F 'files=@../data/requests/request-01.txt' \
  -F 'files=@../data/requests/request-02.pdf' \
  -F 'files=@../data/requests/request-03.txt' \
  -F 'files=@../data/requests/request-04.txt' \
  -F 'files=@../data/requests/request-05.txt' \
  -F 'files=@../data/requests/request-06.txt' \
  -F 'files=@../data/requests/request-07.txt' \
  -F 'files=@../data/requests/request-08.txt'
```

## Tests

```bash
cd spring-api
mvn test

cd ../python-service
pytest -q
```

The tests cover policy outcomes, caller access, effective dates, quotations, ambiguous extraction, exact duplicates, mixed completed/failed batch behavior, timeout/unavailable/malformed provider doubles, and prompt-injection isolation.

## Design decision note

**Decision:** policy eligibility is computed deterministically from metadata before generation. The model double receives only eligible policy records and structured extracted fields; it cannot choose tenant/role or bypass policy filtering. This was chosen over giving the model the complete corpus because the assessment explicitly treats policy passages and model output as untrusted data.

**Rejected alternative:** embedding/vector retrieval. The supplied corpus is intentionally tiny, and deterministic metadata filtering plus benefit matching makes evidence selection explainable and testable without an embedding dependency.

**Main limitation:** benefit identification is intentionally lightweight and rule-based; production would need a controlled taxonomy and stronger document parsing/model validation. No real external model is included.

**Time / unfinished work / AI assistance:** complete the remaining local setup and run the commands above. If submitted, replace this line with actual time spent and a truthful statement of AI assistance.

## Production design note (<=400 words)

For 10,000 requests/day containing personal information, I would first introduce a managed database for policy and processing metadata, encrypted object storage for documents, and a queue between API intake and document processing. I would keep the Spring Boot API stateless behind an API gateway/load balancer and run the Python workers separately with autoscaling. Kubernetes is a reasonable deployment target if already supported by the organization; otherwise managed container services reduce operational overhead.

The most important security risks are unauthorized cross-tenant access, sensitive document leakage, prompt injection, secrets exposure, excessive logging, and insecure model/provider boundaries. I would enforce caller identity at the gateway/service boundary, tenant/role authorization server-side, encryption in transit and at rest, short-lived credentials, least-privilege service identities, malware/content scanning, structured redaction, retention/deletion policies, and immutable audit events. Model input would contain only explicitly eligible policy evidence and sanitized extracted fields. Model output would be schema-validated and never allowed to approve or pay a claim.

Operationally, I would add correlation IDs, metrics for extraction/provider latency, failure rates and queue age, distributed tracing, bounded timeouts, circuit breaking and dead-letter handling. I would also make policy versions immutable and auditable so a historical decision can be reproduced.

Before committing to a delivery date, I would clarify the authoritative policy source and conflict-precedence process, identity provider and authorization model, document retention/data residency requirements, expected file sizes/types, provider SLA and rate limits, PII classification, recovery objectives, expected peak concurrency, human-review workflow/API, and acceptance criteria for extraction accuracy. I would also agree how policy changes are approved and rolled back, because that directly affects evidence correctness.
