# Licensing Service Client

[![Build](https://github.com/bsayli/licensing/actions/workflows/build.yml/badge.svg)](https://github.com/bsayli/licensing/actions/workflows/build.yml)
[![JDK](https://img.shields.io/badge/JDK-21%2B-blue)](https://openjdk.org/projects/jdk/21/)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

A Java **client library** generated from the `licensing-service` OpenAPI definition using custom **generics-aware
templates**. This module provides a typed SDK for issuing and validating license tokens against the `licensing-service`
REST API.

---

## Key Capabilities

* Typed client API (`LicenseControllerApi`) generated from OpenAPI
* Integrated with **Spring Boot 3** and `RestClient`
* Custom connection pool and timeout configuration via `CloseableHttpClient`
* Unified response envelope using `ApiClientResponse<T>`
* Supports **issue** and **validate** license access tokens
* Includes integration tests with **MockWebServer**

---

## Usage

### Maven Dependency

Add to your service's `pom.xml`:

```xml

<dependency>
    <groupId>io.github.bsayli</groupId>
    <artifactId>licensing-service-client</artifactId>
    <version>0.1.0</version>
</dependency>
```

---

### Configuration

`LicensingServiceApiClientConfig` registers all beans:

* `CloseableHttpClient` — pooled HTTP client
* `HttpComponentsClientHttpRequestFactory` — applies timeouts
* `RestClient` — base REST client with request factory
* `ApiClient` — OpenAPI generated client
* `LicenseControllerApi` — typed API interface

Example properties (application.yml):

```yaml
licensing-service-api:
  base-url: http://localhost:8081/licensing-service
  max-connections-total: 64
  max-connections-per-route: 16
  connect-timeout-seconds: 10
  connection-request-timeout-seconds: 10
  read-timeout-seconds: 15
```

---

### Example: Issue Access

```java
IssueAccessRequest req = new IssueAccessRequest()
        .serviceId("crm")
        .serviceVersion("1.5.0")
        .instanceId("crm~host~mac")
        .licenseKey("BSAYLI~RND~ENC")
        .signature("BASE64SIG");

ApiClientResponse<LicenseAccessResponse> resp = adapter.issueAccess(req);
```

### Example: Validate Access

```java
ValidateAccessRequest req = new ValidateAccessRequest()
        .serviceId("crm")
        .serviceVersion("1.5.0")
        .instanceId("crm~host~mac")
        .signature("BASE64SIG");

String jwt = "jwt-123";
ApiClientResponse<LicenseAccessResponse> resp = adapter.validateAccess(jwt, req);
```

---

## Testing

Integration tests (`LicensingServiceClientAdapterIT`) run against **MockWebServer** to verify:

* Request paths and headers (`/v1/licenses/access`, `/v1/licenses/access/validate`)
* Mapping of JSON responses into typed `LicenseAccessResponse`

Run:

```bash
mvn test
```

---

## Project Structure

```
licensing-service-client/
├─ src/main/java/io/github/bsayli/licensing/client/
│  ├─ adapter/config/LicensingServiceApiClientConfig.java
│  ├─ adapter/impl/LicensingServiceClientAdapterImpl.java
│  ├─ common/(core, contract)
│  └─ generated/(api, dto, invoker)
├─ src/test/java/io/github/bsayli/licensing/client/adapter/
│  └─ LicensingServiceClientAdapterIT.java
└─ pom.xml
```

---

## Build

```bash
mvn clean package
```

Produces:

```
target/licensing-service-client-0.1.0.jar
```

---

## See Also

* [licensing-service](../licensing-service/README.md) — REST service implementation
* [license-generator](../license-generator/README.md) — CLI utilities for keys and signatures