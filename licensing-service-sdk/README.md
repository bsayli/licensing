# Licensing Service SDK

[![Build](https://github.com/bsayli/licensing/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/bsayli/licensing/actions/workflows/build.yml)
[![Java](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![OpenAPI](https://img.shields.io/badge/OpenAPI-3.x-blue?logo=openapiinitiative)](https://www.openapis.org/)
[![JWT](https://img.shields.io/badge/JWT-EdDSA-lightgrey?logo=jsonwebtokens)](https://jwt.io/)
[![Maven](https://img.shields.io/badge/Maven-3.9-blue?logo=apachemaven)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/license-MIT-green)](../LICENSE)

A Spring Boot 3 client SDK for integrating with the **Licensing Service**. The SDK handles issuing and validating
license **access tokens (JWT/EdDSA)**, manages client‑side caching, signs requests with detached signatures, and
provides a simple orchestration API for applications. This document describes only the **licensing-service-sdk**
subproject.

---

## Table of Contents

* [Key Capabilities](#key-capabilities)
* [High-Level Flow](#high-level-flow)
* [API](#api)

    * [Obtain License Token](#obtain-license-token)
* [Validation & Errors](#validation--errors)
* [Security](#security)
* [Configuration](#configuration)

    * [YAML reference](#yaml-reference)
    * [Environment variables](#environment-variables)
* [Caching](#caching)
* [JWT & Signatures](#jwt--signatures)
* [OpenAPI / Swagger](#openapi--swagger)
* [Build & Run](#build--run)
* [Project Structure](#project-structure)
* [Troubleshooting](#troubleshooting)

---

## Key Capabilities

* **Simplified client SDK** for licensing-service
* **License token orchestration** (issue, validate, refresh)
* **Client-side caching** of tokens (Caffeine)
* **Detached signature** generation for request integrity
* **Error handling & i18n** (localized messages)
* **Swagger/OpenAPI** documentation included

---

## High-Level Flow

1. **Application** calls SDK’s `/v1/licenses/access` endpoint with a license key.
2. SDK computes **clientId** (hash of instanceId + service info).
3. SDK checks **local cache** for a token.

* If **cache miss** → calls **Licensing Service /access** to issue a new token.
* If **cache hit** → calls **Licensing Service /access/validate** with cached token.

4. **Licensing Service** responds:

* Valid token → `TOKEN_ACTIVE`
* Refreshed token → `TOKEN_REFRESHED` (SDK updates cache)

5. SDK returns **LicenseToken** to the calling application.

---

## API

Base path (configurable):

```
/licensing-service-sdk
```

Controller: `LicenseController`

### Obtain License Token

`POST /v1/licenses/access`

**Request Body** — `LicenseAccessRequest`

```json
{
  "licenseKey": "<LICENSE_KEY>",
  "instanceId": "crm~host123~00:AA:BB:CC:DD:EE",
  "checksum": "<optional>",
  "serviceId": "crm",
  "serviceVersion": "1.5.0"
}
```

**Response** — `ApiResponse<LicenseToken>`

```json
{
  "status": 200,
  "message": "License is valid",
  "data": {
    "licenseToken": "<JWT>"
  },
  "errors": []
}
```

**cURL**

```bash
curl -u licensingSdkUser:licensingSdkPass \
  -H 'Content-Type: application/json' \
  -d '{
        "licenseKey":"<LICENSE_KEY>",
        "instanceId":"crm~host123~00:AA:BB:CC:DD:EE",
        "checksum":"<OPTIONAL>",
        "serviceId":"crm",
        "serviceVersion":"1.5.0"
      }' \
  http://localhost:8082/licensing-service-sdk/v1/licenses/access
```

---

## Validation & Errors

* **Bean Validation** on DTOs (`@NotBlank`, `@Size`, etc.)
* **Global advice** (`LicenseControllerAdvice`) standardizes error output
* Domain & transport errors are mapped to error codes:

    * `INVALID_PARAMETER`
    * `TRANSPORT_ERROR`
    * `REMOTE_ERROR`
    * `EMPTY_TOKEN`

Response format: `ApiResponse<Void>` with list of `ApiError` entries.

---

## Security

* **HTTP Basic** auth (configurable credentials)
* **Stateless** sessions
* SDK signs requests with **EdDSA private key** (detached signature)

---

## Configuration

### YAML reference

`src/main/resources/application.yml` (snippet)

```yaml
server:
  port: 8082
  servlet:
    context-path: /licensing-service-sdk

licensing-service-api:
  base-url: "http://localhost:8081/licensing-service"
  basic:
    username: licensingUser
    password: licensingPass
  connect-timeout-seconds: 10
  read-timeout-seconds: 15

caching:
  spring:
    licenseTokenTTL: 90m

signature:
  private:
    key: "<Base64-PKCS8-PrivateKey>"
```

### Environment variables

* `SERVER_PORT`, `SERVER_SERVLET_CONTEXT_PATH`
* `LICENSING_SERVICE_API_BASE_URL`, `LICENSING_SERVICE_API_BASIC_USERNAME`, `LICENSING_SERVICE_API_BASIC_PASSWORD`
* `CACHING_SPRING_LICENSETOKENTTL`
* `SIGNATURE_PRIVATE_KEY`

---

## Caching

* `licenseTokens` — local token cache (Caffeine)
* Configurable TTL (default `65m`) to align with Licensing Service JWT expiration (90m − jitter)

---

## JWT & Signatures

* **Algorithm**: EdDSA (Ed25519)
* **Detached signature** generation for issue/validate requests
* **ClientId** computed via SHA-256 hash (instanceId + serviceId + serviceVersion + checksum)

---

## OpenAPI / Swagger

* Swagger UI: `http://localhost:8082/licensing-service-sdk/swagger-ui.html`
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
java -jar target/licensing-service-sdk-1.0.0.jar
```

---

## Project Structure

```
licensing-service-sdk/
├─ src/main/java/io/github/bsayli/licensing/sdk/
│  ├─ api/(controller, dto, exception)
│  ├─ cache/(CacheNames.java, CacheConfig)
│  ├─ common/(api, exception, i18n)
│  ├─ config/(SecurityConfig)
│  ├─ generator/(ClientIdGeneratorImpl, SignatureGeneratorImpl)
│  ├─ service/(impl, client, handler)
│  └─ ...
├─ src/main/resources/
│  ├─ application.yml
│  └─ messages.properties
└─ pom.xml
```

---

## Troubleshooting

* **401 Unauthorized**: wrong Basic Auth credentials or signature mismatch
* **400 Bad Request**: invalid input, check validation error messages
* **502 Bad Gateway**: transport error when contacting Licensing Service
* **Token empty**: ensure Licensing Service is correctly configured

---

## See Also

* **[licensing-service](../licensing-service/README.md)** — server component
* **[license-generator](../license-generator/README.md)** — key & signature tooling
* **[licensing-service-sdk-cli](../licensing-service-sdk-cli/README.md)** — command-line client demo
