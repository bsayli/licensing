# Licensing Service (v2.0.0 – Contract‑First)

[![Build](https://github.com/bsayli/licensing/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/bsayli/licensing/actions/workflows/build.yml)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Keycloak](https://img.shields.io/badge/Keycloak-26.x-8A2BE2?logo=keycloak)](https://www.keycloak.org/)
[![Redis](https://img.shields.io/badge/Redis-8.x-red?logo=redis)](https://redis.io/)
[![OpenAPI](https://img.shields.io/badge/OpenAPI-3.x-blue?logo=openapiinitiative)](https://www.openapis.org/)
[![License](https://img.shields.io/badge/license-MIT-green)](../LICENSE)

---

## ⚠️ Important: v2.0.0 Architectural Shift

This version introduces a **contract‑first architecture** based on `openapi-generics`.

Key changes:

* ❌ Removed legacy `ApiResponse<T>` envelope
* ✅ Adopted canonical `ServiceResponse<T>` for success responses
* ✅ Standardized error handling via **RFC 9457 ProblemDetail (internal only)**
* ✅ Clear separation of boundaries: **Service ↔ Agent ↔ Client**

> This service is **NOT intended to be consumed directly by end clients**.
> It is a **vendor‑side licensing authority**.

---

## 🎯 What This Service Is

`licensing-service` is the **central license authority** operated by the vendor.

It is responsible for:

* Validating license ownership and constraints
* Issuing and validating access tokens (JWT)
* Enforcing usage limits (instanceId tracking)
* Managing license lifecycle rules

---

## 🧱 System Architecture (v2.0.0)

```text
Customer Environment
└─ Product Runtime
   └─ licensing-agent (boundary layer)
        ↓
Vendor Cloud
└─ licensing-service (this service)
```

### Why Agent Exists

This service is **not directly exposed to product code**.

Instead:

* `licensing-agent` acts as a **controlled boundary**
* Handles:

  * caching
  * token lifecycle
  * signature orchestration
  * retry / resilience

> This avoids leaking vendor contract complexity into customer runtime.

---

## 🔑 Key Capabilities

* License validation (Keycloak-backed)
* JWT issuance (EdDSA / Ed25519)
* Token refresh orchestration
* Detached signature verification
* Usage tracking per instance
* Redis-backed session + cache layer
* Internationalized error handling

---

## 🔄 High-Level Flow

### Issue Flow

1. Agent calls `/v1/licenses/access`
2. License key is decrypted → userId extracted
3. License is validated (Keycloak + rules)
4. JWT is issued
5. Session context cached

### Validate Flow

1. Agent calls `/v1/licenses/access/validate`
2. JWT + signature verified
3. Context matched with cache
4. If needed → token refreshed

---

## 📡 API

Base path:

```
/licensing-service
```

---

### POST `/v1/licenses/access`

**Request**

```json
{
  "serviceId": "crm",
  "serviceVersion": "1.5.0",
  "instanceId": "crm~host~mac",
  "signature": "<Base64>",
  "checksum": "<optional>",
  "licenseKey": "BSAYLI.<opaque>"
}
```

**Response**

```json
{
  "data": {
    "status": "TOKEN_CREATED | TOKEN_ACTIVE | TOKEN_REFRESHED",
    "licenseToken": "<JWT>"
  },
  "meta": {
    "serverTime": "..."
  }
}
```

---

### POST `/v1/licenses/access/validate`

**Headers**

```
License-Token: <JWT>
```

**Request**

```json
{
  "serviceId": "crm",
  "serviceVersion": "1.5.0",
  "instanceId": "crm~host~mac",
  "signature": "<Base64>",
  "checksum": "<optional>"
}
```

**Response**

```json
{
  "data": {
    "status": "TOKEN_ACTIVE | TOKEN_REFRESHED",
    "licenseToken": "<NEW_IF_REFRESHED>"
  },
  "meta": {
    "serverTime": "..."
  }
}
```

---

## ❗ Error Model

### Internal

* Uses **RFC 9457 ProblemDetail**
* Includes structured extensions (`ErrorItem[]`)

### External Boundary

* ProblemDetail is **NOT exposed beyond agent boundary**
* Agent converts errors into **public error contract**

---

## 🔐 Security

* HTTP Basic authentication
* Stateless API
* Detached signature verification
* JWT validation (EdDSA)

---

## ⚙️ Configuration

```yaml
server:
  port: 8081
  servlet:
    context-path: /licensing-service
```

(See application.yml for full configuration)

---

## 🧠 Design Principles

### 1. Contract-First

Java → OpenAPI → Client

Single source of truth.

### 2. Boundary Isolation

* Service = authority
* Agent = integration boundary
* Client = consumer

### 3. No Contract Leakage

* Internal error model ≠ external contract

### 4. Deterministic Token Lifecycle

* Issue
* Validate
* Refresh

---

## 🚀 Build & Run

```bash
mvn clean package
mvn spring-boot:run
```

---

## 📁 Project Structure (Simplified)

```
licensing-service
├─ api (controller, dto)
├─ service (orchestration, validation)
├─ domain (models)
├─ repository (Keycloak)
├─ security (crypto, signature)
├─ config
```

---

## 🔗 See Also

* licensing-agent → boundary layer
* licensing-agent-cli → example client
* license-generator → key/signature tooling

---

## 🧭 Migration Note

v1.x users:

* `ApiResponse<T>` → removed
* must migrate to:

  * `ServiceResponse<T>`
  * ProblemDetail error model
  * Agent-based integration

---

## 📌 Summary

This service is **not a client SDK endpoint**.

It is:

> A centralized, vendor-controlled licensing authority designed to be consumed via an agent boundary in a contract-first ecosystem.

---
