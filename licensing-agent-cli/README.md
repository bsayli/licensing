# Licensing Service Agent CLI

[![Build](https://github.com/bsayli/licensing/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/bsayli/licensing/actions/workflows/build.yml)
[![Java](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Picocli](https://img.shields.io/badge/Picocli-4.7.x-purple)](https://picocli.info/)
[![HttpClient5](https://img.shields.io/badge/Apache%20HttpClient-5.x-black?logo=apache)](https://hc.apache.org/httpcomponents-client-5.0.x/)
[![License](https://img.shields.io/badge/license-MIT-green)](../LICENSE)

Command-line client for interacting with the **Licensing Service** using the official SDK.

This tool is intended for **developers** and **operators** who want to:

* Validate licenses against a running `licensing-service`
* Retrieve JWT license tokens for their service instances
* Integrate license validation into CI/CD or automation flows

---

## Build

```bash
mvn -q -DskipTests package
```

The shaded JAR will be available at:

```
target/licensing-agent-cli-<version>.jar
```

---

## Usage

Run with Java 21 or higher:

```bash
java -jar target/licensing-agent-cli-<version>.jar \
  -k <LICENSE_KEY> \
  -s <SERVICE_ID> \
  -v <SERVICE_VERSION> \
  -i <INSTANCE_ID>
```

### Options

| Flag | Long Option         | Description                                                                  | Required |
|------|---------------------|------------------------------------------------------------------------------|----------|
| `-k` | `--key`             | License key string (`BSAYLI.<opaquePayloadBase64Url>`)                       | Yes      |
| `-s` | `--service-id`      | Service identifier (e.g. `crm`)                                              | Yes      |
| `-v` | `--service-version` | Service version (e.g. `1.5.0`)                                               | Yes      |
| `-i` | `--instance-id`     | Unique instance identifier (e.g. `licensing-service~demo~00:11:22:33:44:55`) | Yes      |
| `-h` | `--help`            | Show help message                                                            | No       |

Default values can also be injected from environment variables:

* `LICENSE_KEY`
* `SERVICE_ID`
* `SERVICE_VERSION`
* `INSTANCE_ID`

---

## Example

```bash
java -jar target/licensing-agent-cli-1.0.1.jar \
  -k 'BSAYLI.AQA3-gCQ66DQsfC0LwnAO8DTjKgad7DhPaOtCf7WG2bFUoK9pmScIhhCf2S-D0j8g4jC7nlFrLDpuM0ezEoDQc79SizxxEIUGN9YUhTNBW7iRQ' \
  -s crm \
  -v 1.5.0 \
  -i 'licensing-service~demo~00:11:22:33:44:55'
```

Sample output:

```
INFO  LicenseSdkClientServiceImpl - License validated successfully.
INFO  LicenseSdkClientServiceImpl - Token: <JWT_TOKEN>
INFO  LicenseSdkClientServiceImpl - Message: License is valid
```

---

## Configuration

The CLI loads properties from `application.properties` (on classpath):

```properties
licensing.agent.server.url=http://localhost:8082/licensing-agent
licensing.agent.server.app.user=licensingSdkUser
licensing.agent.server.app.pass=licensingSdkPass
licensing.agent.api.path=/v1/licenses/access
licensing.agent.http.connect-timeout-seconds=40
licensing.agent.http.response-timeout-seconds=40
licensing.agent.http.retries=3
licensing.agent.http.retry-interval-seconds=3
```

Override via environment variables:

```
LICENSE_SERVICE_AGENT_URL=http://my-server/licensing-agent
LICENSE_SERVICE_AGENT_CONNECT_TIMEOUT=20
LICENSE_SERVICE_AGENT_RESPONSE_TIMEOUT=20
LICENSE_SERVICE_AGENT_RETRIES=5
LICENSE_SERVICE_AGENT_RETRY_INTERVAL=2
LICENSE_SERVICE_AGENT_API_PATH=/v1/licenses/access
```

---

## Internals

* Sends `POST /v1/licenses/access` with JSON body (`LicenseAccessRequest`).
* Expects `ApiResponse<LicenseToken>` JSON back from the server.
* Uses Apache HttpClient5 Fluent API with retry strategy.
* Handles error responses by parsing error `ApiResponse` if JSON is returned.
* Logs license token and validation messages.

---

## Exit Codes

| Code | Meaning                                         |
|------|-------------------------------------------------|
| `0`  | License validated successfully                  |
| `1`  | License validation failed (client/server error) |
| `2`  | CLI usage error                                 |

---

## Related Projects

* **license-generator** → generate keys and signatures
* **licensing-service** → REST API server for license validation
* **licensing-agent** → Agent used by this CLI

---

## License

This project is licensed under the MIT License.
