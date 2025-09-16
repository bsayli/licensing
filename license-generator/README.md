# License Generator

[![Build](https://github.com/bsayli/licensing/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/bsayli/licensing/actions/workflows/build.yml)
[![Java](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![JJWT](https://img.shields.io/badge/JJWT-0.12.x-orange)](https://github.com/jwtk/jjwt)
[![BouncyCastle](https://img.shields.io/badge/BouncyCastle-1.81-blue)](https://www.bouncycastle.org/)
[![Jackson](https://img.shields.io/badge/Jackson-2.19.x-lightgrey)](https://github.com/FasterXML/jackson)
[![License](https://img.shields.io/badge/license-MIT-green)](../LICENSE)

Tools and libraries for generating cryptographic keys, constructing license strings, creating **detached Ed25519 signatures**, and validating **JWT-based license tokens**.

> Audience: **devs/ops** who provision keys and run CLI flows.

---

## Quickstart

### 0) Build once

```bash
mvn -q -DskipTests package
```

### 1) Generate keys (files only)

**Ed25519 keypair** (SPKI public, PKCS#8 private):

```bash
# Writes to {DIR}/signature.public.key and {DIR}/signature.private.key
mvn -q org.codehaus.mojo:exec-maven-plugin:3.5.1:java \
  -Dexec.mainClass=io.github.bsayli.license.cli.KeygenCli \
  -Dexec.args="--mode ed25519 --dir /secure/keys"
```

**AES key** (default 256-bit) for encrypting Keycloak UUID (forms the opaque part of the license key):

```bash
# Writes to {DIR}/aes.key (Base64)
mvn -q org.codehaus.mojo:exec-maven-plugin:3.5.1:java \
  -Dexec.mainClass=io.github.bsayli.license.cli.KeygenCli \
  -Dexec.args="--mode aes --size 256 --dir /secure/keys"
```

> The CLI **never prints secrets**. Files are created and (POSIX) chmod **600** is applied when supported.

### 2) Generate the license key

License key format is:

```
<prefix>.<opaquePayloadBase64Url>
```

Run:

```bash
mvn -q org.codehaus.mojo:exec-maven-plugin:3.5.1:java \
  -Dexec.mainClass=io.github.bsayli.license.cli.LicenseKeyGeneratorCli \
  -Dexec.args="--userId <KEYCLOAK_USER_UUID> \
               --secretKeyFile /secure/keys/aes.key \
               --printSegments"
```

Example output:

```
License Key: BSAYLI.AQBjuG4HyAIXdvEdSJxUukHSnABx1UX_wiJJuiTeI0lLrhsmrVu9q2UMQniygjE30I5q8cinzAoTzK2K_Ax4-fy55zDrXVRpOT9PbhwVyuztyQ
  prefix                   : BSAYLI
  opaquePayload(Base64URL) : AQBjuG4HyAIXdvEdSJxUukHSnABx1UX_wiJJuiTeI0lLrhsmrVu9q2UMQniygjE30I5q8cinzAoTzK2K_Ax4-fy55zDrXVRpOT9PbhwVyuztyQ
```

### 3) Create a **detached signature** for requests (service→licensing-service)

The licensing-service expects a detached **Ed25519** signature over canonical JSON `SignatureData`.

* When issuing a license (request contains a full `licenseKey`): set `encryptedLicenseKeyHash = Base64(SHA-256(licenseKey))`
* When validating a JWT token: set `licenseTokenHash = Base64(SHA-256(token))`

Use the **SignatureCli** (sign mode) to let the tool compute the proper hash and signature:

```bash
# Sign for license issue (hashes the full licenseKey for you)
mvn -q org.codehaus.mojo:exec-maven-plugin:3.5.1:java \
  -Dexec.mainClass=io.github.bsayli.license.cli.SignatureCli \
  -Dexec.args="--mode sign \
               --serviceId crm \
               --serviceVersion 1.5.0 \
               --instanceId licensing-service~demo~00:11:22:33:44:55 \
               --licenseKey BSAYLI.<opaque> \
               --privateKey <BASE64_PKCS8_PRIV>"
```

```bash
# Sign for token validation (hashes the JWT token for you)
mvn -q org.codehaus.mojo:exec-maven-plugin:3.5.1:java \
  -Dexec.mainClass=io.github.bsayli.license.cli.SignatureCli \
  -Dexec.args="--mode sign \
               --serviceId crm \
               --serviceVersion 1.5.0 \
               --instanceId licensing-service~demo~00:11:22:33:44:55 \
               --token <JWT_FROM_ISSUE_RESPONSE> \
               --privateKey <BASE64_PKCS8_PRIV>"
```

The CLI prints the canonical JSON and the **Base64 signature**; include the signature in your service’s request body.

### 4) (Server side) Request a JWT from licensing-service

Use `POST /v1/licenses/tokens` from the **licensing-service** project (see that README).

### 5) Offline JWT validation (optional)

```bash
mvn -q org.codehaus.mojo:exec-maven-plugin:3.5.1:java \
  -Dexec.mainClass=io.github.bsayli.license.cli.LicenseTokenCli \
  -Dexec.args="--publicKey <BASE64_SPKI_ED25519> --token <JWT>"
```

This verifies the EdDSA signature, enforces expiry, and prints claims (`licenseStatus`, `licenseTier`, optional `message`).

---

## What this module contains

### Packages

* **`io.github.bsayli.license.common`**

  * `CryptoConstants`, `CryptoUtils` – algorithms, sizes, Base64 helpers, AES‑GCM utils
  * `LicenseConstants` – license prefix/delimiter + JWT claim keys

* **`io.github.bsayli.license.licensekey`**

  * `UserIdEncrypter` – AES/GCM encrypt/decrypt for Keycloak UUID
  * `LicenseKeyGenerator` – builds `<prefix>.<opaque>`
  * `model.LicenseKeyData` – immutable value object

* **`io.github.bsayli.license.signature`**

  * `SignatureGenerator` / `SignatureValidator` – Ed25519 detached signatures
  * `model.SignatureData` – canonical JSON payload for signing

* **`io.github.bsayli.license.securekey`**

  * `SecureKeyGenerator` – AES (symmetric)
  * `SecureEdDSAKeyPairGenerator` – Ed25519 keypair (SPKI/PKCS#8)

* **`io.github.bsayli.license.token`**

  * `JwtTokenExtractor` – validates JWT (EdDSA) with public key and returns `LicenseValidationResult`
  * `model.LicenseValidationResult` – tier, status, expiration summary

* **`io.github.bsayli.license.cli`** – runnable tools (see above)

---
