package io.github.bsayli.license.signature.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: SignatureData")
class SignatureDataTest {

  @Test
  @DisplayName("Only encryptedLicenseKeyHash set -> build OK")
  void build_onlyEncryptedKeyHash_ok() {
    SignatureData d =
        new SignatureData.Builder()
            .serviceId("svc")
            .serviceVersion("1.0.0")
            .instanceId("inst-1")
            .encryptedLicenseKeyHash("b64hash-enc")
            .build();

    assertEquals("svc", d.getServiceId());
    assertEquals("1.0.0", d.getServiceVersion());
    assertEquals("inst-1", d.getInstanceId());
    assertEquals("b64hash-enc", d.getEncryptedLicenseKeyHash());
    assertNull(d.getLicenseTokenHash());
  }

  @Test
  @DisplayName("Only licenseTokenHash set -> build OK")
  void build_onlyTokenHash_ok() {
    SignatureData d =
        new SignatureData.Builder()
            .serviceId("svc")
            .serviceVersion("1.0.0")
            .instanceId("inst-1")
            .licenseTokenHash("b64hash-token")
            .build();

    assertEquals("svc", d.getServiceId());
    assertEquals("1.0.0", d.getServiceVersion());
    assertEquals("inst-1", d.getInstanceId());
    assertEquals("b64hash-token", d.getLicenseTokenHash());
    assertNull(d.getEncryptedLicenseKeyHash());
  }

  @Test
  @DisplayName("Both hashes set -> should fail")
  void build_bothHashes_shouldFail() {
    assertThrows(IllegalStateException.class, this::buildWithBothHashes);
  }

  @Test
  @DisplayName("No hash set -> should fail")
  void build_noHash_shouldFail() {
    assertThrows(IllegalStateException.class, this::buildWithNoHash);
  }

  @Test
  @DisplayName("Missing required fields -> should fail")
  void build_missingRequired_shouldFail() {
    assertThrows(IllegalStateException.class, this::buildMissingServiceId);
    assertThrows(IllegalStateException.class, this::buildMissingServiceVersion);
    assertThrows(IllegalStateException.class, this::buildMissingInstanceId);
  }

  // ---- helpers (instance, no-arg, single-invocation) ----

  private SignatureData buildWithBothHashes() {
    return new SignatureData.Builder()
        .serviceId("svc")
        .serviceVersion("1.0.0")
        .instanceId("inst-1")
        .encryptedLicenseKeyHash("x")
        .licenseTokenHash("y")
        .build();
  }

  private SignatureData buildWithNoHash() {
    return new SignatureData.Builder()
        .serviceId("svc")
        .serviceVersion("1.0.0")
        .instanceId("inst-1")
        .build();
  }

  private SignatureData buildMissingServiceId() {
    return new SignatureData.Builder()
        .serviceVersion("1.0.0")
        .instanceId("inst-1")
        .encryptedLicenseKeyHash("x")
        .build();
  }

  private SignatureData buildMissingServiceVersion() {
    return new SignatureData.Builder()
        .serviceId("svc")
        .instanceId("inst-1")
        .encryptedLicenseKeyHash("x")
        .build();
  }

  private SignatureData buildMissingInstanceId() {
    return new SignatureData.Builder()
        .serviceId("svc")
        .serviceVersion("1.0.0")
        .encryptedLicenseKeyHash("x")
        .build();
  }
}
