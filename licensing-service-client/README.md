# Licensing Service Client (2.0.0 – Contract-First, Generated)

[![Build](https://github.com/bsayli/licensing/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/bsayli/licensing/actions/workflows/build.yml)
[![Java](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![OpenAPI Generator](https://img.shields.io/badge/OpenAPI%20Generator-7.x-blue?logo=openapiinitiative)](https://openapi-generator.tech/)
[![Maven](https://img.shields.io/badge/Maven-3.9-blue?logo=apachemaven)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/license-MIT-green)](../LICENSE)

Java **client library** generated from the `licensing-service` OpenAPI using **generics-aware templates** (openapi-generics).

This module is a **transport + contract binding layer** used by `licensing-agent`.

---

## Architectural Role

```text
licensing-agent → licensing-service-client → licensing-service
```

Responsibilities:

* Bind OpenAPI → typed Java API
* Enforce **contract-first** alignment (`ServiceResponse<T>`)
* Provide HTTP client configuration (timeouts, pooling)
* Normalize error handling into `ApiProblemException`

Non-responsibilities:

* ❌ Business logic
* ❌ Token orchestration
* ❌ Caching

---

## Contract Model

### Success

All endpoints return:

```text
ServiceResponse<T>
```

Example:

```json
{
  "data": {
    "status": "TOKEN_ACTIVE",
    "licenseToken": "<JWT>"
  },
  "meta": {
    "serverTime": "2026-05-02T16:00:00Z",
    "sort": []
  }
}
```

### Error

HTTP errors are mapped to:

```text
ApiProblemException
```

* wraps `ProblemDetail`
* extracts `errorCode`
* extracts `ErrorItem[]`

---

## Generated API

Primary entry point:

```java
LicenseControllerApi
```

Adapter abstraction (recommended):

```java
LicensingServiceClientAdapter
```

---

## Example Usage

### Issue Access

```java
IssueAccessRequest req = new IssueAccessRequest()
    .serviceId("crm")
    .serviceVersion("1.5.0")
    .instanceId("crm~host~mac")
    .licenseKey("BSAYLI.<opaque>")
    .signature("BASE64SIG");

ServiceResponse<LicenseAccessResponse> resp = adapter.issueAccess(req);
```

### Validate Access

```java
ValidateAccessRequest req = new ValidateAccessRequest()
    .serviceId("crm")
    .serviceVersion("1.5.0")
    .instanceId("crm~host~mac")
    .signature("BASE64SIG");

ServiceResponse<LicenseAccessResponse> resp =
    adapter.validateAccess("<JWT>", req);
```

---

## Configuration

Defined in `LicensingServiceApiClientConfig`.

### application.yml

```yaml
licensing-service-api:
  base-url: http://localhost:8081/licensing-service
  basic:
    username: licensingUser
    password: licensingPass
  connect-timeout-seconds: 10
  connection-request-timeout-seconds: 10
  read-timeout-seconds: 15
  max-connections-total: 64
  max-connections-per-route: 16
```

### Beans

* `CloseableHttpClient` → pooled HTTP client
* `RestClient` → Spring HTTP abstraction
* `ApiClient` → OpenAPI generated invoker
* `LicenseControllerApi` → typed endpoint client

---

## Error Handling

All HTTP error responses are intercepted:

```java
throw new ApiProblemException(problemDetail, status)
```

Features:

* Extracts `errorCode`
* Maps nested `errors[]`
* Provides structured access for upper layers

---

## HTTP Layer

* Apache HttpClient5 (pooling + timeouts)
* Retry disabled by default (policy belongs to caller)
* Basic Auth supported

---

## Code Generation

Configured via Maven plugin:

* generator: `java-generics-contract`
* input: `licensing-service-api-docs.yaml`
* output: `generated/api`, `generated/dto`, `generated/invoker`

Generated code is **ephemeral** and should not be manually modified.

---

## Testing

Uses **MockWebServer** for integration tests:

* verifies request paths
* verifies headers
* verifies response mapping

```bash
mvn test
```

---

## Project Structure

```
licensing-service-client/
├─ adapter/
│  ├─ config/
│  └─ impl/
├─ common/problem/
├─ generated/
│  ├─ api/
│  ├─ dto/
│  └─ invoker/
└─ pom.xml
```

---

## Migration Notes (2.0.0)

* `ApiResponse` → ❌ removed
* `ApiClientResponse` → ❌ removed
* `ServiceResponse<T>` → ✅ canonical contract
* `ProblemDetail` → internal only
* `ApiProblemException` → unified error abstraction
* aligned with openapi-generics pipeline

---

## Design Principles

* Contract-first (Java → OpenAPI → client)
* No model drift
* No framework leakage to consumers
* Separation of mechanism (client) vs policy (agent)

---

## Related Modules

* licensing-service
* licensing-agent
* licensing-agent-cli
* license-generator

---

## License

MIT License