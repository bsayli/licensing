# License Management Project — **license-generator**

> Tools and libraries for generating keys, license strings, detached signatures, and validating JWT-based license
> tokens. This README is aimed at **devs/ops** who provision keys and run CLI flows.

---

## Who should read this?

* **Integrators / Client app devs**: skim this for CLI usage, then see the service-side guide in **licensing-service**.
* **Ops / Security**: use this to **generate**, **rotate**, and **distribute** keys (Ed25519 & AES) and to run offline
  token checks.

---

## Golden Path (Quickstart)

A minimal end‑to‑end flow to produce a license key, sign the request payload, and validate a returned JWT.

### 0) Build

```bash
mvn -q -DskipTests package
```

### 1) Generate keys

**Ed25519 keypair (for signatures & JWT verification, SPKI pub / PKCS#8 priv):**

```bash
mvn -q org.codehaus.mojo:exec-maven-plugin:3.5.1:java \
  -Dexec.mainClass=io.github.bsayli.license.cli.Ed25519KeygenCli
```

This prints Base64 keys to stdout and/or writes to files if you pass `--outPrivate` / `--outPublic`.

**AES key (32 bytes) for encrypting Keycloak UUID → ENCRYPTED\_USER\_ID:**

```bash
mvn -q org.codehaus.mojo:exec-maven-plugin:3.5.1:java \
  -Dexec.mainClass=io.github.bsayli.license.cli.KeygenCli \
  -Dexec.args="--type aes"
```

### 2) Produce a license key (PREFIX~~RANDOM~~ENCRYPTED\_USER\_ID)

```bash
mvn -q org.codehaus.mojo:exec-maven-plugin:3.5.1:java \
  -Dexec.mainClass=io.github.bsayli.license.cli.LicenseKeyGeneratorCli \
  -Dexec.args="--userId <KEYCLOAK_USER_UUID>"
```

Output looks like:

```
BSAYLI~<RANDOM_URLSAFE_BASE64>~<ENCRYPTED_USER_ID>
```

### 3) Create a **detached signature** for the request

The licensing-service expects a detached **Ed25519** signature over the canonical JSON **SignatureData** (
see [Canonical Signing Contract](#canonical-signing-contract)).

```bash
mvn -q org.codehaus.mojo:exec-maven-plugin:3.5.1:java \
  -Dexec.mainClass=io.github.bsayli.license.cli.SignatureCli \
  -Dexec.args='--mode sign \
               --privateKey <BASE64_PKCS8_PRIV> \
               --dataJson {"serviceId":"crm","serviceVersion":"1.5.0","instanceId":"crm~host~mac","encryptedLicenseKeyHash":"<Base64(SHA-256 of 3rd licenseKey segment)>"}'
```

Copy the resulting Base64 signature as the `signature` field in the **/issue** request.

### 4) Ask the licensing-service for a token (server side)

Use the REST API from the **licensing-service** project (`POST /v1/licenses/tokens`). See that README for cURL.

### 5) Validate the returned JWT offline (optional)

```bash
mvn -q org.codehaus.mojo:exec-maven-plugin:3.5.1:java \
  -Dexec.mainClass=io.github.bsayli.license.cli.LicenseTokenCli \
  -Dexec.args="--publicKey <BASE64_SPKI_ED25519> --token <JWT>"
```

The CLI verifies EdDSA signature/expiry and prints claims (`licenseStatus`, `licenseTier`, optional `message`).

---

## What this module contains

### Packages

* **`io.github.bsayli.license.common`**

    * `CryptoConstants`, `CryptoUtils` – algorithms, sizes, Base64 helpers, AES‑GCM utils
    * `LicenseConstants` – license prefix/delimiter + JWT claim keys
* **`io.github.bsayli.license.licensekey`**

    * `UserIdEncrypter` – AES/GCM encrypt/decrypt for Keycloak UUID
    * `LicenseKeyGenerator` – builds `PREFIX~RANDOM~ENCRYPTED_USER_ID`
    * `model.LicenseKeyData` – immutable value object
* **`io.github.bsayli.license.signature`**

    * `SignatureGenerator` / `SignatureValidator` – Ed25519 detached signature
    * `model.SignatureData` – canonical JSON payload for signing
* **`io.github.bsayli.license.securekey`**

    * `SecureKeyGenerator` – AES (symmetric)
    * `SecureEdDSAKeyPairGenerator` – Ed25519 keypair (SPKI/PKCS#8)
* **`io.github.bsayli.license.token`**

    * `JwtTokenExtractor` – validates JWT (EdDSA) with public key and returns `LicenseValidationResult`
    * `model.LicenseValidationResult` – tier, status, expiration summary
* **`io.github.bsayli.license.cli`** – runnable tools (see below)

---

## CLI Reference

> All commands assume you already ran `mvn -q -DskipTests package` in this module.

### EncryptUserIdCli

AES/GCM encrypts/decrypts a Keycloak UUID for use in the license key.

```bash
mvn -q org.codehaus.mojo:exec-maven-plugin:3.5.1:java \
  -Dexec.mainClass=io.github.bsayli.license.cli.EncryptUserIdCli \
  -Dexec.args="--userId <uuid>"
```

### LicenseKeyGeneratorCli

Builds the final license key: `PREFIX~RANDOM~ENCRYPTED_USER_ID`.

```bash
mvn -q org.codehaus.mojo:exec-maven-plugin:3.5.1:java \
  -Dexec.mainClass=io.github.bsayli.license.cli.LicenseKeyGeneratorCli \
  -Dexec.args="--userId <uuid>"
```

### SignatureCli

Create or verify **detached** Ed25519 signatures over a JSON payload.

```bash
# sign
mvn -q org.codehaus.mojo:exec-maven-plugin:3.5.1:java \
  -Dexec.mainClass=io.github.bsayli.license.cli.SignatureCli \
  -Dexec.args='--mode sign --dataJson <json> --privateKey <BASE64_PKCS8_PRIV>'

# verify
mvn -q org.codehaus.mojo:exec-maven-plugin:3.5.1:java \
  -Dexec.mainClass=io.github.bsayli.license.cli.SignatureCli \
  -Dexec.args='--mode verify --dataJson <json> --signatureB64 <BASE64_SIG> --publicKey <BASE64_SPKI_PUB>'
```

### LicenseTokenCli

Offline JWT validation (EdDSA) and claim extraction.

```bash
mvn -q org.codehaus.mojo:exec-maven-plugin:3.5.1:java \
  -Dexec.mainClass=io.github.bsayli.license.cli.Licen
```
