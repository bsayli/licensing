# License Management Project

## Overview

This project provides a **modular licensing framework** designed to issue, encrypt, validate, and verify software
licenses. It integrates with identity providers like **Keycloak** and uses modern cryptography standards to ensure
license authenticity and user binding.

The system is organized into multiple packages, each with a clear responsibility:

* **`licensekey`**: generation and encryption of license keys.
* **`signature`**: detached digital signatures over license payloads.
* **`securekey`**: generation of secure symmetric/asymmetric keys.
* **`token`**: parsing and validating license tokens (JWT).
* **`common`**: shared constants, utilities, and domain-level definitions.
* **`cli`**: command-line tooling for encryption, key generation, token validation, and signatures.

---

## Core Concepts

### 1. License Key Format

A license key has three segments:

```
PREFIX ~ RANDOM_URLSAFE_BASE64 ~ ENCRYPTED_USER_ID
```

* **PREFIX** → Project/brand identifier (e.g., `BSAYLI`).
* **Random** → 32-byte URL-safe Base64 string.
* **Encrypted User ID** → AES/GCM-encrypted Keycloak UUID.

All segments are wrapped in the immutable DTO: `LicenseKeyData`.

### 2. Cryptography

* **AES/GCM** → for encrypting user identifiers & license keys.
* **SHA-256** → for hashing license-related data.
* **DSA / EdDSA (Ed25519)** → for digital signatures.
* **JWT (EdDSA)** → license tokens validated with Ed25519 public keys.

Cryptographic constants and utilities are centralized in:

* `CryptoConstants`
* `CryptoUtils`

### 3. JWT Claims

JWT tokens embed license-related claims:

* `licenseStatus` → Active, Expired, etc.
* `licenseTier` → Free, Professional, Enterprise.
* `message` → Additional metadata or user-facing info.
* Standard JWT claims (`exp`, `iat`, `sub`) are also supported.

---

## Package Structure

### `io.github.bsayli.license.common`

* `CryptoConstants` → shared algorithm names, key sizes, Base64 codecs.
* `CryptoUtils` → helpers for AES-GCM encryption/decryption, array concatenation, Base64 conversions.
* `LicenseConstants` → license-specific constants (prefix, delimiter, JWT claims).

### `io.github.bsayli.license.licensekey`

* `UserIdEncrypter` → AES/GCM-based encryption of Keycloak UUIDs.
* `LicenseKeyGenerator` → builds full license keys using prefix, random segment, and encrypted user id.
* `model.LicenseKeyData` → immutable record representing license key segments.

### `io.github.bsayli.license.signature`

* `SignatureGenerator` → creates detached signatures for license payloads.
* `SignatureValidator` → verifies detached signatures with DSA public keys.
* `model.SignatureData` → JSON payload containing service/license metadata.

### `io.github.bsayli.license.securekey`

* `SecureKeyGenerator` → generates AES symmetric keys.
* `SecureEdDSAKeyPairGenerator` → generates Ed25519 key pairs for signatures.

### `io.github.bsayli.license.token`

* `JwtTokenExtractor` → validates JWT license tokens and extracts `LicenseValidationResult`.
* `model.LicenseValidationResult` → DTO holding validation outcome.

### `io.github.bsayli.license.cli`

Command-line tools for common licensing flows:

* `EncryptUserIdCli` → Encrypt a Keycloak user UUID.
* `LicenseKeyGeneratorCli` → Generate a license key (PREFIX \~ RANDOM \~ ENCRYPTED\_USER\_ID).
* `LicenseTokenCli` → Validate JWT license tokens.
* `SignatureCli` → Create or verify detached digital signatures.
* `KeygenCli` → Generate AES symmetric keys or Ed25519 key pairs.

---

## Example Flows

### A. License Key Issuance

1. User registers in Keycloak → UUID generated.
2. `UserIdEncrypter.encrypt(UUID)` → secure identifier.
3. `LicenseKeyGenerator.generateLicenseKey(encryptedId)` → produces license key string.
4. Optional: sign license data with `SignatureGenerator`.

### B. License Validation via JWT

1. Application receives JWT from licensing server.
2. `JwtTokenExtractor(publicKeyBase64).validateAndGetToken(token)` → verifies signature & expiration.
3. Returns `LicenseValidationResult` with tier, status, expiration.

---

## CLI Usage

After packaging the project with Maven:

```bash
mvn -q -DskipTests package
```

Run any CLI with `exec-maven-plugin`:

```bash
mvn -q org.codehaus.mojo:exec-maven-plugin:3.5.0:java \
  -Dexec.mainClass=io.github.bsayli.license.cli.EncryptUserIdCli \
  -Dexec.args="--userId ba035b3e-d8b6-4a09-89c7-ab0459f2585b"
```

### Available CLIs

* **EncryptUserIdCli**

```bash
java -cp license-generator.jar io.github.bsayli.license.cli.EncryptUserIdCli --userId <uuid>
```

* **LicenseKeyGeneratorCli**

```bash
java -cp license-generator.jar io.github.bsayli.license.cli.LicenseKeyGeneratorCli --userId <uuid>
```

* **LicenseTokenCli**

```bash
java -cp license-generator.jar io.github.bsayli.license.cli.LicenseTokenCli --token <jwt> --publicKey <base64>
```

* **SignatureCli**

```bash
java -cp license-generator.jar io.github.bsayli.license.cli.SignatureCli --mode sign|verify --data <json> --key <key>
```

* **KeygenCli**

```bash
java -cp license-generator.jar io.github.bsayli.license.cli.KeygenCli --type aes|ed25519
```
