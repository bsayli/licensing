# Licensing Service Agent (2.0.0 – Contract-First)

[![Build](https://github.com/bsayli/licensing/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/bsayli/licensing/actions/workflows/build.yml)
[![Java](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![OpenAPI](https://img.shields.io/badge/OpenAPI-3.x-blue?logo=openapiinitiative)](https://www.openapis.org/)
[![JWT](https://img.shields.io/badge/JWT-EdDSA-lightgrey?logo=jsonwebtokens)](https://jwt.io/)
[![Maven](https://img.shields.io/badge/Maven-3.9-blue?logo=apachemaven)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/license-MIT-green)](../LICENSE)

A Spring Boot 3 **Agent** that integrates client applications with the **Licensing Service** using a **contract-first architecture**.

This version adopts:

* **`ServiceResponse<T>`** as the canonical success contract
* **RFC 9457 Problem Details** internally
* **`LicenseAgentErrorResponse`** as the public error envelope (no ProblemDetail leakage)

The Agent is responsible for **token orchestration**, not business validation.

---

## Key Capabilities

* Contract-first integration with licensing-service
* Token orchestration (issue / validate / refresh / re-issue)
* Client-side token caching (Caffeine)
* Detached signature generation (EdDSA / Ed25519)
* Clear boundary: transport → orchestration → contract mapping
* Framework-agnostic public error model

---

## Architectural Position

```
Client App → Agent → Licensing Service
```

* **Licensing Service** = source of truth (license validation, policy)
* **Agent** = orchestration + caching + signature + contract boundary
* **Client App** = simple integration (no protocol knowledge)

---

## High-Level Flow

1. Application calls Agent `/v1/licenses/access`
2. Agent computes `clientId`
3. Agent checks cache

### Cache MISS

→ call licensing-service `/access`
→ receive token
→ cache + return

### Cache HIT

→ call licensing-service `/access/validate`

Response cases:

* TOKEN_ACTIVE → return cached token
* TOKEN_REFRESHED → update cache
* TOKEN_TOO_OLD → re-issue token

---

## API

Base path:

```
/licensing-agent
```

Controller: `LicenseAgentController`

### Obtain License Token

`POST /v1/licenses/access`

#### Request

```json
{
  "licenseKey": "<BSAYLI.<opaque>>",
  "instanceId": "crm~host123~00:AA:BB:CC:DD:EE",
  "checksum": "<optional>",
  "serviceId": "crm",
  "serviceVersion": "1.5.0"
}
```

#### Response (SUCCESS)

```json
{
  "data": {
    "licenseToken": "<JWT>"
  }
}
```

> Wrapped by `ServiceResponse<LicenseToken>`

---

## Error Model

Agent **does NOT expose ProblemDetail**.

Instead:

```json
{
  "errorCode": "BAD_REQUEST",
  "message": "Unrecognized field: 'foo'",
  "errors": [
    {
      "code": "VALIDATION_ERROR",
      "message": "...",
      "field": "..."
    }
  ]
}
```

### Key Principle

* Internal → ProblemDetail (rich, extensible)
* External → `LicenseAgentErrorResponse` (stable, client-safe)

---

## Contract Model

### Success

* `ServiceResponse<T>`

### Error

* `LicenseAgentErrorResponse`

### Why?

* Prevent framework leakage
* Keep public contract stable
* Allow internal evolution (ProblemDetail, extensions)

---

## Core Orchestration Logic

The Agent:

1. Generates signature
2. Calls remote service via generated client
3. Interprets `ServiceResponse`
4. Maps `ApiProblemException` → domain error
5. Decides:

  * return cached
  * refresh
  * re-issue

---

## Configuration

### application.yml (simplified)

```yaml
server:
  port: 8082
  servlet:
    context-path: /licensing-agent

spring:
  jackson:
    deserialization:
      fail-on-unknown-properties: true

licensing-service-api:
  base-url: http://localhost:8081/licensing-service
  basic:
    username: licensingUser
    password: licensingPass

caching:
  spring:
    licenseTokenTTL: 90m

signature:
  private:
    key: <Base64 PKCS8 Ed25519 key>
```

---

## Caching Strategy

* Key: `clientId`
* Store: `licenseToken`
* TTL < server expiration (pre-refresh window)

---

## Security

* HTTP Basic (Agent API)
* Detached signature (Agent → Service)
* No session state

---

## OpenAPI

* Swagger UI available
* Contract generated from controller + generics

---

## Build & Run

```bash
mvn clean package
mvn spring-boot:run
```

---

## Project Structure

```
licensing-agent/
├─ api/
├─ service/
│  ├─ impl/
│  ├─ client/
│  ├─ handler/
├─ generator/
├─ cache/
├─ config/
└─ ...
```

---

## Migration Note (2.0.0)

This version:

* Removes `ApiResponse`
* Removes direct ProblemDetail exposure
* Introduces contract-first approach
* Aligns with openapi-generics ecosystem

---

## See Also

* licensing-service
* licensing-agent-cli
* openapi-generics

---

## Summary

This Agent is **not just a client**.

It is a:

* protocol boundary
* contract adapter
* orchestration layer

between application and licensing domain.
