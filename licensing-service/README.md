# Licensing Service

A Spring Boot 3 application that issues and validates license **access tokens (JWT/EdDSA)** for client applications. It
integrates with **Keycloak** to read per‑user license metadata, provides **detached signature** verification for request
integrity, and uses **Caffeine** caches for performance and token/session handling. This document describes only the *
*licensing-service** subproject.

---

## Table of Contents

* [Key Capabilities](#key-capabilities)
* [High-Level Flow](#high-level-flow)
* [API](#api)

    * [Issue Access](#issue-access)
    * [Validate Access](#validate-access)
* [Validation & Errors](#validation--errors)
* [Security](#security)
* [Configuration](#configuration)

    * [YAML reference](#yaml-reference)
    * [Environment variables](#environment-variables)
* [Caching](#caching)
* [JWT & Crypto](#jwt--crypto)
* [OpenAPI / Swagger](#openapi--swagger)
* [Build & Run](#build--run)
* [Project Structure](#project-structure)
* [Troubleshooting](#troubleshooting)

---

## Key Capabilities

* **License access issue/validate** via REST
* **JWT (EdDSA/Ed25519)** signing & verification
* **Detached signature** validation on requests
* **Keycloak** as user/license source
* **Usage accounting** (tracks unique instanceIds)
* **Token refresh orchestration** (blacklist & re-issue)
* **Caffeine** caches (online/offline user info, sessions, blacklist)
* **Internationalized** error messages (messages.properties)
* **OpenAPI** with schema wrappers (generic ApiResponse<T>)

---

## High-Level Flow

1. **Client** calls `POST /v1/licenses/access` with a license key + detached signature.
2. Service **decrypts** the license key → extracts **userId**.
3. Service **evaluates license** (Keycloak + policy checks).
4. Service **issues JWT** and **caches** client session context.
5. Client later calls `POST /v1/licenses/access/validate` with the JWT + detached signature.
6. Service verifies signature, JWT validity/blacklist, and **matches request to cached context**.

* If expired *and* cache matches → returns **TOKEN\_REFRESHED** and a new JWT.

---

## API

Base path (configurable):

```
/licensing-service
```

Controller: `LicenseController`

### Issue Access

`POST /v1/licenses/access`

**Request Body** — `IssueAccessRequest`

```json
{
  "serviceId": "crm",
  "serviceVersion": "1.5.0",
  "instanceId": "crm~host123~00:AA:BB:CC:DD:EE",
  "signature": "<Base64 detached signature>",
  "checksum": "<optional checksum>",
  "licenseKey": "<BSAYLI~RANDOM_BASE64URL~ENCRYPTED_USER_ID>",
  "forceTokenRefresh": false
}
```

**Response** — `ApiResponse<LicenseAccessResponse>`

```json
{
  "status": 200,
  "message": "License is valid",
  "data": {
    "status": "TOKEN_CREATED | TOKEN_ACTIVE | TOKEN_REFRESHED",
    "licenseToken": "<JWT>"
  },
  "errors": []
}
```

**cURL**

```bash
curl -u licensingUser:licensingPass \
  -H 'Content-Type: application/json' \
  -d '{
        "serviceId":"crm",
        "serviceVersion":"1.5.0",
        "instanceId":"crm~host123~00:AA:BB:CC:DD:EE",
        "signature":"<BASE64>",
        "checksum":"<OPTIONAL>",
        "licenseKey":"<LICENSE_KEY>",
        "forceTokenRefresh":false
      }' \
  http://localhost:8081/licensing-service/v1/licenses/access
```

### Validate Access

`POST /v1/licenses/access/validate`

**Headers**

```
License-Token: <JWT>
```

**Request Body** — `ValidateAccessRequest`

```json
{
  "serviceId": "crm",
  "serviceVersion": "1.5.0",
  "instanceId": "crm~host123~00:AA:BB:CC:DD:EE",
  "signature": "<Base64 detached signature>",
  "checksum": "<optional checksum>"
}
```

**Response** — `ApiResponse<LicenseAccessResponse>`

```json
{
  "status": 200,
  "message": "License is valid",
  "data": {
    "status": "TOKEN_ACTIVE | TOKEN_REFRESHED",
    "licenseToken": "<NEW_JWT_IF_REFRESHED>"
  },
  "errors": []
}
```

**cURL**

```bash
curl -u licensingUser:licensingPass \
  -H 'Content-Type: application/json' \
  -H 'License-Token: <JWT>' \
  -d '{
        "serviceId":"crm",
        "serviceVersion":"1.5.0",
        "instanceId":"crm~host123~00:AA:BB:CC:DD:EE",
        "signature":"<BASE64>",
        "checksum":"<OPTIONAL>"
      }' \
  http://localhost:8081/licensing-service/v1/licenses/access/validate
```

---

## Validation & Errors

* **Bean Validation** on DTOs (`@NotBlank`, `@Size`, etc.) with message keys from `messages.properties`.
* **Header validation** via `@ValidLicenseToken` composite constraint.
* **Global advice**: `LicenseControllerAdvice` returns `ApiResponse<Void>` with localized error messages.
* Domain errors map to `ServiceErrorCode` → HTTP status.

Common error keys (subset):

* `license.not.found`, `license.invalid`, `license.expired`, `license.inactive`
* `license.usage.limit.exceeded`, `license.service.id.not.supported`, `license.invalid.checksum`
* `license.service.version.not.supported`, `license.signature.invalid`
* `license.token.invalid`, `license.token.expired`, `license.token.refreshed`, `license.token.too.old`

---

## Security

* **HTTP Basic** auth (single in‑memory user via properties)
* **Stateless** sessions, CSRF disabled (API only)
* **Custom 401** JSON via `RestAuthenticationEntryPoint`

Whitelisted endpoints:

```
/actuator/health, /v3/api-docs/**, /swagger-ui/**, /swagger-ui.html, /v3/api-docs.yaml
```

---

## Configuration

### YAML reference

`src/main/resources/application.yml` (snippet)

```yaml
server:
  port: 8081
  servlet:
    context-path: /licensing-service

licensing.api.basic:
  username: licensingUser
  password: licensingPass
  realm: LicensingService

license:
  secret.key: 'Base64AESKey'
  jwt:
    private.key: 'Base64PKCS8PrivateKey'
    public.key: 'Base64X509PublicKey'
  service:
    ids: [ crm, billing, reporting ]
    checksum-required: [ crm, billing, reporting ]

userid.secret.key: 'Base64AESKey'
signature.public.key: 'Base64-encoded DSA/EdDSA public key'

jwt.token:
  expiration: 60m
  max.jitter: 2m

caching.spring:
  clientLicenseInfoTTL: 1h
  clientLicenseInfoOffLineSupportTTL: 24h
```

### Environment variables

All keys can be overridden by environment variables using Spring’s relaxed binding, e.g.:

* `SERVER_PORT`, `SERVER_SERVLET_CONTEXT_PATH`
* `LICENSING_API_BASIC_USERNAME`, `LICENSING_API_BASIC_PASSWORD`, `LICENSING_API_BASIC_REALM`
* `LICENSE_SECRET_KEY`, `USERID_SECRET_KEY`
* `LICENSE_JWT_PRIVATE_KEY`, `LICENSE_JWT_PUBLIC_KEY`
* `SIGNATURE_PUBLIC_KEY`
* `JWT_TOKEN_EXPIRATION`, `JWT_TOKEN_MAX_JITTER`

---

## Caching

Defined in `CacheConfig` (Caffeine):

* `userInfoCache` — online license data (TTL = `caching.spring.clientLicenseInfoTTL`)
* `userOfflineInfoCache` — offline fallback (TTL = `caching.spring.clientLicenseInfoOffLineSupportTTL`)
* `activeClients` — session snapshots (`ClientSessionCache`) with bounded TTL (≤ 3h)
* `blacklistedTokens` — JWTs revoked on force refresh

---

## JWT & Crypto

* **Algorithm**: EdDSA (Ed25519) via BouncyCastle provider
* `JwtServiceImpl` signs on issue and verifies on validate
* **Format pre-check**: base64url parts & `alg` header enforcement
* **Detached signature** (`SignatureValidator`): validates Base64 signature over canonical JSON payload (
  `SignatureData`)
* **AES/GCM** utilities:

    * `UserIdEncryptorImpl` — encrypt/decrypt user UUIDs

> For key generation and signing helpers, see the **license-generator** subproject.

---

## OpenAPI / Swagger

* OpenAPI is customized to wrap responses in a generic `ApiResponse<T>` envelope using `SwaggerResponseCustomizer` and
  `SwaggerLicensingResponseCustomizer`.
* UI: `http://localhost:8081/licensing-service/swagger-ui.html`
* JSON: `/v3/api-docs` · YAML: `/v3/api-docs.yaml`

---

## Build & Run

### Prerequisites

* JDK 21
* Maven 3.x

### Build

```bash
mvn clean package
```

### Run (local)

```bash
mvn spring-boot:run
# or
java -jar target/licensing-service-1.0.1.jar
```

### With Docker Compose (from repo root)

```bash
cd docker-compose/server
docker-compose up -d
# wait ~45s on first run
```

---

## Project Structure

```
licensing-service/
├─ src/main/java/io/github/bsayli/licensing/
│  ├─ api/
│  │  ├─ controller/LicenseController.java
│  │  ├─ dto/(IssueAccessRequest, ValidateAccessRequest, LicenseAccessResponse)
│  │  └─ validation/annotations/ValidLicenseToken.java
│  ├─ common/(api, exception, i18n, openapi)
│  ├─ config/(CacheConfig, CryptoProviderConfig, SecretConfig, security/*)
│  ├─ domain/(model, result)
│  ├─ repository/user/UserRepositoryImpl.java
│  ├─ security/(impls: UserIdEncryptorImpl, SignatureValidatorImpl)
│  ├─ service/
│  │  ├─ impl/(LicenseOrchestrationServiceImpl, LicenseValidationServiceImpl, ...)
│  │  ├─ jwt/(impl/JwtServiceImpl, JwtBlacklistServiceImpl)
│  │  ├─ token/LicenseTokenManager.java
│  │  ├─ user/(cache, core, orchestration)
│  │  └─ validation/(impl/*)
│  └─ ...
├─ src/main/resources/
│  ├─ application.yml
│  └─ messages.properties
└─ pom.xml
```

---

## Troubleshooting

* **401 Unauthorized**: wrong Basic Auth credentials or missing header.
* **400 Bad Request**: bean validation errors → check `errors[]` messages.
* **401 (Token issues)**: `TOKEN_INVALID`, `TOKEN_EXPIRED`, or checksum/version mismatch.
* **License not found**: ensure Keycloak contains required attributes; realm/client ids match config.
* **Signature invalid**: ensure detached signature matches canonical JSON payload expected by `SignatureValidatorImpl`.
* **Clock/Jitter**: token TTL + jitter define expiry; keep client and server clocks in sync.

---

## See Also

* **[license-generator](../license-generator/README.md)**: key & signature tooling, CLI examples
* **[licensing-service-sdk](../licensing-service-sdk/README.md)** / *
  *[sdk-cli](../licensing-service-sdk/sdk-cli/README.md)**: client integrations and runnable demo
