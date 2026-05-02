# Licensing Agent CLI (2.0.0 – Contract-First)

[![Build](https://github.com/bsayli/licensing/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/bsayli/licensing/actions/workflows/build.yml)
[![Java](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Picocli](https://img.shields.io/badge/Picocli-4.7.x-purple)](https://picocli.info/)
[![HttpClient5](https://img.shields.io/badge/Apache%20HttpClient-5.x-black?logo=apache)](https://hc.apache.org/httpcomponents-client-5.0.x/)
[![License](https://img.shields.io/badge/license-MIT-green)](../LICENSE)

Command-line client for calling **licensing-agent**.

This CLI is a small external consumer of the Agent API. It does **not** talk directly to `licensing-service`.

```text
CLI → licensing-agent → licensing-service
```

The CLI is useful for:

* local validation tests
* operator checks
* CI/CD smoke validation
* demonstrating the external Agent contract

---

## Architectural Role

`licensing-agent-cli` is intentionally thin.

It is responsible for:

* collecting runtime inputs
* calling the Agent endpoint
* parsing success/error contracts
* returning process exit codes

It is **not** responsible for:

* license policy evaluation
* detached signature generation
* token refresh decisions
* direct communication with licensing-service

Those responsibilities belong to `licensing-agent`.

---

## Contract Model

### Success Response

The CLI expects:

```text
ServiceResponse<LicenseToken>
```

Example:

```json
{
  "data": {
    "licenseToken": "<JWT>"
  },
  "meta": {
    "serverTime": "2026-05-02T16:00:00Z",
    "sort": []
  }
}
```

### Error Response

The CLI expects:

```text
LicenseAgentErrorResponse
```

Example:

```json
{
  "errorCode": "BAD_REQUEST",
  "message": "Invalid request payload.",
  "errors": null
}
```

The CLI does not parse `ProblemDetail`. `ProblemDetail` is internal to the service/agent implementation and is not part of the public CLI-facing contract.

---

## Build

```bash
mvn -q -DskipTests package
```

The executable JAR is produced under:

```text
target/licensing-agent-cli-<version>.jar
```

---

## Usage

```bash
java -jar target/licensing-agent-cli-<version>.jar \
  -k <LICENSE_KEY> \
  -s <SERVICE_ID> \
  -v <SERVICE_VERSION> \
  -i <INSTANCE_ID>
```

---

## Options

| Flag | Long Option         | Description                                     | Required |
| ---- | ------------------- | ----------------------------------------------- | -------- |
| `-k` | `--key`             | License key (`BSAYLI.<opaquePayloadBase64Url>`) | Yes      |
| `-s` | `--service-id`      | Service identifier, for example `crm`           | Yes      |
| `-v` | `--service-version` | Service version, for example `1.5.0`            | Yes      |
| `-i` | `--instance-id`     | Unique runtime instance identifier              | Yes      |
| `-h` | `--help`            | Show help message                               | No       |

Default values can also be injected from environment variables:

```text
LICENSE_KEY
SERVICE_ID
SERVICE_VERSION
INSTANCE_ID
```

---

## Example

```bash
java -jar target/licensing-agent-cli-2.0.0.jar \
  -k 'BSAYLI.<opaquePayloadBase64Url>' \
  -s crm \
  -v 1.5.0 \
  -i 'crm~host123~00:AA:BB:CC:DD:EE'
```

Sample output:

```text
INFO  LicenseAgentClientServiceImpl - License validated successfully.
INFO  LicenseAgentClientServiceImpl - Token: <JWT_TOKEN>
```

---

## Configuration

The CLI loads defaults from `application.properties` on the classpath.

```properties
licensing.agent.server.url=http://localhost:8082/licensing-agent
licensing.agent.server.app.user=licensingAgentUser
licensing.agent.server.app.pass=licensingAgentPass
licensing.agent.api.path=/v1/licenses/access
licensing.agent.http.connect-timeout-seconds=40
licensing.agent.http.response-timeout-seconds=40
licensing.agent.http.retries=3
licensing.agent.http.retry-interval-seconds=3
```

---

## Environment Overrides

```text
LICENSE_AGENT_URL
LICENSE_AGENT_CONNECT_TIMEOUT
LICENSE_AGENT_RESPONSE_TIMEOUT
LICENSE_AGENT_RETRIES
LICENSE_AGENT_RETRY_INTERVAL
LICENSE_AGENT_API_PATH
```

These override the corresponding values from `application.properties`.

---

## Runtime Flow

1. CLI reads command-line arguments.
2. CLI loads Agent client properties.
3. CLI builds `LicenseAccessRequest`.
4. CLI sends `POST /v1/licenses/access` to licensing-agent.
5. CLI parses:

    * `ServiceResponse<LicenseToken>` on success
    * `LicenseAgentErrorResponse` on HTTP error
6. CLI exits with a process code.

---

## Exit Codes

| Code | Meaning                                |
| ---- | -------------------------------------- |
| `0`  | License token obtained successfully    |
| `1`  | Validation failed or Agent call failed |
| `2`  | CLI usage error                        |

---

## Internals

* Picocli for command-line parsing
* Apache HttpClient5 Fluent API for HTTP calls
* Jackson for JSON parsing
* `JavaTimeModule` for `ServiceResponse.meta.serverTime`
* Basic Auth for Agent access
* Retry strategy configured via properties

---

## Important Distinction

This CLI is **not an SDK**.

It is a command-line consumer of the Agent boundary.

The Agent owns:

* request signing
* token caching
* refresh/re-issue decisions
* remote licensing-service communication

The CLI only validates the external contract from the client side.

---

## Related Modules

* `licensing-service` — vendor-side license authority
* `licensing-service-client` — generated client used by Agent
* `licensing-agent` — boundary/orchestration layer
* `license-generator` — key and signature tooling

---

## License

This project is licensed under the MIT License.