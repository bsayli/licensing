# Licensing API Contract

Shared API contract types used by the licensing modules.

This module contains the response envelope used by `licensing-service`, `licensing-service-client`, `licensing-service-sdk` and `licensing-service-sdk-cli` to ensure a consistent API contract across all layers.

```java
ApiResponse<T>
ApiError
```

## Purpose

`licensing-api-contract` keeps the API response shape in one place so server and client modules use the same contract instead of redefining response wrappers independently.

It is also used as a BYOE (Bring Your Own Envelope) example for `openapi-generics`: generated client response wrappers extend the existing `ApiResponse<T>` type instead of generating a new envelope model.

Example generated client wrapper:

```java
public class ApiResponseLicenseAccessResponse
    extends ApiResponse<LicenseAccessResponse> {
}
```

## Contract Shape

`ApiResponse<T>` represents the standard licensing API envelope:

```json
{
  "status": 200,
  "message": "License is valid",
  "data": {},
  "errors": []
}
```

Fields:

| Field     | Type             | Description                                     |
| --------- | ---------------- | ----------------------------------------------- |
| `status`  | `int`            | HTTP-style status code returned in the envelope |
| `message` | `String`         | Human-readable response message                 |
| `data`    | `T`              | Typed response payload                          |
| `errors`  | `List<ApiError>` | Structured error details                        |

## BYOE Requirements

Because generated wrappers extend `ApiResponse<T>`, the envelope is intentionally subclass-friendly:

* public no-argument constructor
* public getters and setters
* generic payload accessor through `getData()` / `setData(T data)`

This keeps the envelope compatible with generated client wrappers while preserving the original API contract.

## Module Coordinates

```xml
<dependency>
  <groupId>io.github.bsayli</groupId>
  <artifactId>licensing-api-contract</artifactId>
  <version>1.0.6</version>
</dependency>
```

## See Also

* [`licensing-service`](../licensing-service/README.md) — REST API that returns `ApiResponse<T>`
* [`licensing-service-client`](../licensing-service-client/README.md) — generated client using the shared envelope
* [`licensing-service-sdk`](../licensing-service-sdk/README.md) — SDK layer built on top of the generated client
* [`licensing-service-sdk-cli`](../licensing-service-sdk-cli/README.md) — command-line client demo
