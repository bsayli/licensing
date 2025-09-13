# Welcome to the Licensing Project!

[![Build](https://github.com/bsayli/licensing/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/bsayli/licensing/actions/workflows/build.yml)
[![codecov](https://codecov.io/gh/bsayli/licensing/branch/main/graph/badge.svg)](https://codecov.io/gh/bsayli/licensing)
[![Java](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Keycloak](https://img.shields.io/badge/Keycloak-26.x-purple?logo=keycloak)](https://www.keycloak.org/)
[![Redis](https://img.shields.io/badge/Redis-8.x-red?logo=redis)](https://redis.io/)
[![Maven](https://img.shields.io/badge/Maven-3.9-blue?logo=apachemaven)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)](https://www.docker.com/)
[![Caffeine](https://img.shields.io/badge/Caffeine-Cache-orange)](https://github.com/ben-manes/caffeine)
[![JWT](https://img.shields.io/badge/JWT-EdDSA-lightgrey?logo=jsonwebtokens)](https://jwt.io/)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

<p align="center">
  <img src="docs/images/social-preview.png" alt="Licensing Project preview" width="720"/>
  <br/>
  <em>Spring Boot • Keycloak • Redis • EdDSA — Complete licensing framework</em>
</p>

## Project Purpose

This project provides a **complete licensing framework** for applications, combining secure key generation, detached
digital signatures, and token validation (JWT/EdDSA). It is designed to ensure license authenticity, prevent misuse, and
integrate seamlessly with **Keycloak** for user identity and license metadata.

---

## Subprojects

* **license-generator**: Java project for license key generation, encryption, and cryptographic tooling.
* **licensing-service**: Spring Boot application that issues and validates license tokens.
* **licensing-service-sdk**: SDK for integrating licensing capabilities into external applications.
* **licensing-service-sdk-cli**: Command-line tool for testing and interacting with the licensing service.

---

## Repository Structure

* **db**: Keycloak database backup (`licensing-keycloak.zip`)
* **docker-compose**: Docker Compose files to run servers and client
* **scripts**: Script to run the client (`run_license_sdk_cli.sh`)

---

## Prerequisites

* Git client installed
* Docker installed and running
* Docker Compose installed and running
* Java (>= 21.x)
* Maven (>= 3.x)

---

## Setting Up the Environment

1. **Clone the Repository**

```bash
git clone https://github.com/bsayli/licensing.git
```

2. **Extract Keycloak DB**

* Get `licensing-keycloak.zip` from `/licensing/db`
* Copy and extract into your home directory (\$HOME)

---

## Running the Licensing Service

1. Navigate to server docker-compose:

```bash
cd licensing/docker-compose/server
```

2. Start the server components:

```bash
docker-compose up -d
```

This starts Keycloak, Licensing Service, and Licensing Service SDK in the background.

3. Wait \~45 seconds for the services to initialize on the first run.

---

## Running the License Validation Tool via Docker

1. Navigate to client docker-compose:

```bash
cd licensing/docker-compose/client
```

2. Start the client service:

```bash
docker-compose up
```

3. Check logs for successful license validation. Example:

```text
licensing-service-sdk-cli | INFO License validated successfully.
licensing-service-sdk-cli | INFO Token: <JWT_TOKEN>
licensing-service-sdk-cli | INFO Message: License is valid
```

4. Stopping containers:

```bash
# Client
docker-compose down

# Server
cd ../server
docker-compose down
```

---

## Running the License Validation Tool Directly (Optional 1)

1. Build the JAR:

```bash
cd licensing/licensing-service-sdk-cli
mvn clean package
```

2. The JAR will be in `target/`:

```bash
cd target
```

3. Run it:

```bash
java -jar licensing-service-sdk-cli-1.0.1.jar -s crm -v 1.5.0 -i "crm~macbook~00:2A:8D:BE:F1:56" -k "<LICENSE_KEY>"
```

---

## Running the License Validation Tool with Script (Optional 2)

1. Navigate to script location:

```bash
cd licensing/scripts
```

2. Make it executable:

```bash
chmod +x run_license_sdk_cli.sh
```

3. Run with options:

```bash
./run_license_sdk_cli.sh -s billing -v 2.0.0 -i "billing~macbook~00:2A:8D:BE:F1:56" -k "<LICENSE_KEY>"
```

---

## Notes

* CLI examples must always be provided **on a single line**.
* If parameters contain spaces or special characters, they should be enclosed in quotes (`"..."`).

---

## Feedback & Questions

If you notice any issues in this documentation or have suggestions for improvements, feel free to open an issue or a
pull request.

For any questions related to the project, please leave a comment in the repository’s discussion or issue tracker.

## 🗺️ Roadmap

Planned improvements and upcoming features:

- [ ] Move sensitive configs to **HashiCorp Vault** for secure secrets management
- [ ] Extend **Keycloak integration** to manage licenses (create, update, revoke) via dedicated endpoints

## ⭐ Support

If you found this project useful, please consider giving it a star ⭐ on GitHub — it helps others discover it too!
