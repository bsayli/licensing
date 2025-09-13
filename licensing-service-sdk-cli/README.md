# Licensing Service SDK CLI

[![Build](https://github.com/bsayli/licensing/actions/workflows/build.yml/badge.svg)](https://github.com/bsayli/licensing/actions/workflows/build.yml)
[![JDK](https://img.shields.io/badge/JDK-21%2B-blue)](https://openjdk.org/projects/jdk/21/)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

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
target/licensing-service-sdk-cli-<version>.jar
```

---

## Usage

Run with Java 21 or higher:

```bash
java -jar target/licensing-service-sdk-cli-<version>.jar \
  -k <LICENSE_KEY> \
  -s <SERVICE_ID> \
  -v <SERVICE_VERSION> \
  -i <INSTANCE_ID>
```

### Options

| Flag | Long Option         | Description                                                                  | Required |
| ---- | ------------------- | ---------------------------------------------------------------------------- | -------- |
| `-k` | `--key`             | License key string (`PREFIX~RANDOM~ENCRYPTED_USER_ID`)                       | Yes      |
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
java -jar target/licensing-service-sdk-cli-1.0.1.jar \
  -k 'BSAYLI~X66e_qYlfPxWiIaN2ahPb9tQFyqjMuTih06LCytzjZ0~0aT6lLTZGkO1zHHPHFDzwF7zPiZLRLWSl06HSVQO5z+NqtzzcFCUkkVFuqHTYKcAcI9037sQQQSfBQakQDUoCA==' \
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
licensing.sdk.server.url=http://localhost:8082/licensing-service-sdk
licensing.sdk.server.app.user=licensingSdkUser
licensing.sdk.server.app.pass=licensingSdkPass
```

Override the base URL via environment variable:

```
LICENSE_SERVICE_SDK_URL=http://my-server/licensing-service-sdk
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
| ---- | ----------------------------------------------- |
| `0`  | License validated successfully                  |
| `1`  | License validation failed (client/server error) |
| `2`  | CLI usage error                                 |

---

## Related Projects

* **license-generator** → generate keys and signatures
* **licensing-service** → REST API server for license validation
* **licensing-service-sdk** → SDK used by this CLI

---

## License

This project is licensed under the MIT License.
