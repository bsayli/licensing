package io.github.bsayli.licensing.security.impl;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.domain.model.SignatureData;
import io.github.bsayli.licensing.security.SignatureValidator;
import io.github.bsayli.licensing.service.exception.security.SignatureInvalidException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: SignatureValidatorImpl")
class SignatureValidatorImplTest {

  private static KeyPair genEd25519KeyPair() throws Exception {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
    return kpg.generateKeyPair();
  }

  private static byte[] sha256(byte[] in) throws Exception {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    return md.digest(in);
  }

  private static String sha256B64(String s) throws Exception {
    return Base64.getEncoder().encodeToString(sha256(s.getBytes(StandardCharsets.UTF_8)));
  }

  private static String x509PublicKeyB64(PublicKey pub) {
    return Base64.getEncoder().encodeToString(pub.getEncoded());
  }

  private static String signPayloadJson(PrivateKey priv, String json) throws Exception {
    Signature sig = Signature.getInstance("Ed25519");
    sig.initSign(priv);
    sig.update(json.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(sig.sign());
  }

  private static IssueAccessRequest issueReq(
      String svcId,
      String ver,
      String instanceId,
      String sigB64,
      String licenseKey,
      String checksum) {

    String lk =
        (licenseKey != null && licenseKey.length() >= 100)
            ? licenseKey
            : ("L".repeat(100) + "~rnd~" + "A".repeat(64));

    String sig = (sigB64 != null && sigB64.length() >= 60) ? sigB64 : "Q".repeat(60);

    return new IssueAccessRequest(lk, instanceId, checksum, svcId, ver, sig);
  }

  private static ValidateAccessRequest validateReq(
      String svcId, String ver, String instanceId, String sigB64, String checksum) {

    String sig = (sigB64 != null && sigB64.length() >= 60) ? sigB64 : "Q".repeat(60);
    return new ValidateAccessRequest(instanceId, checksum, svcId, ver, sig);
  }

  @Test
  @DisplayName("issue: verifies Ed25519 signature over payload (FULL licenseKey hash)")
  void issue_happyPath_fullLicenseKeyHash() throws Exception {
    KeyPair kp = genEd25519KeyPair();
    String pubB64 = x509PublicKeyB64(kp.getPublic());
    SignatureValidator validator = new SignatureValidatorImpl(pubB64);

    String serviceId = "svcA";
    String serviceVer = "1.2.3";
    String instanceId = "instance-abcdefgh";

    String licenseKey =
        "BSAYLI~X66e_qYlfPxWiIaN2ahPb9tQFyqjMuTih06LCytzjZ0~0aT6lLTZGkO1zHHPHFDzwF7zPiZLRLWSl06HSVQO5z+NqtzzcFCUkkVFuqHTYKcAcI9037sQQQSfBQakQDUoCA==";

    String licHash = sha256B64(licenseKey);
    SignatureData payload =
        SignatureData.builder()
            .serviceId(serviceId)
            .serviceVersion(serviceVer)
            .instanceId(instanceId)
            .encryptedLicenseKeyHash(licHash)
            .build();

    String sigB64 = signPayloadJson(kp.getPrivate(), payload.toJson());

    IssueAccessRequest req = issueReq(serviceId, serviceVer, instanceId, sigB64, licenseKey, null);

    assertDoesNotThrow(() -> validator.validate(req));
  }

  @Test
  @DisplayName("validate: verifies Ed25519 signature for token flow (token hash)")
  void validate_happyPath_tokenHash() throws Exception {
    KeyPair kp = genEd25519KeyPair();
    String pubB64 = x509PublicKeyB64(kp.getPublic());
    SignatureValidator validator = new SignatureValidatorImpl(pubB64);

    String serviceId = "svcA";
    String serviceVer = "3.4.5";
    String instanceId = "instance-ijklmnop";
    String jwtToken = "any.jwt.token.string";

    String tokenHash = sha256B64(jwtToken);
    SignatureData payload =
        SignatureData.builder()
            .serviceId(serviceId)
            .serviceVersion(serviceVer)
            .instanceId(instanceId)
            .licenseTokenHash(tokenHash)
            .build();

    String sigB64 = signPayloadJson(kp.getPrivate(), payload.toJson());
    ValidateAccessRequest req = validateReq(serviceId, serviceVer, instanceId, sigB64, null);

    assertDoesNotThrow(() -> validator.validate(req, jwtToken));
  }

  @Test
  @DisplayName("issue: non-Base64 signature -> SignatureInvalidException")
  void issue_invalidBase64Signature() throws Exception {
    KeyPair kp = genEd25519KeyPair();
    String pubB64 = x509PublicKeyB64(kp.getPublic());
    SignatureValidator validator = new SignatureValidatorImpl(pubB64);

    String serviceId = "svcA", ver = "1.0.0", instanceId = "instance-abcdefgh";

    String licenseKey = "PFX~rnd~SGVsbG8tRW5jcnlwdGVkVXNlcklkPT0=";
    String badSig = "***not-base64***";

    IssueAccessRequest req = issueReq(serviceId, ver, instanceId, badSig, licenseKey, null);

    assertThrows(SignatureInvalidException.class, () -> validator.validate(req));
  }

  @Test
  @DisplayName("validate: signed with a different key -> SignatureInvalidException")
  void validate_wrongKey() throws Exception {
    KeyPair signerKp = genEd25519KeyPair();
    KeyPair verifierKp = genEd25519KeyPair();
    SignatureValidator validator =
        new SignatureValidatorImpl(x509PublicKeyB64(verifierKp.getPublic()));

    String serviceId = "svcA", ver = "1.0.0", instanceId = "instance-qrstuvwx";
    String token = "token-123";

    String tokenHash = sha256B64(token);
    SignatureData payload =
        SignatureData.builder()
            .serviceId(serviceId)
            .serviceVersion(ver)
            .instanceId(instanceId)
            .licenseTokenHash(tokenHash)
            .build();

    String sigB64 = signPayloadJson(signerKp.getPrivate(), payload.toJson());
    ValidateAccessRequest req = validateReq(serviceId, ver, instanceId, sigB64, null);

    assertThrows(SignatureInvalidException.class, () -> validator.validate(req, token));
  }

  @Test
  @DisplayName("issue: tampered request field after signing -> SignatureInvalidException")
  void issue_tamperedRequest() throws Exception {
    KeyPair kp = genEd25519KeyPair();
    SignatureValidator validator = new SignatureValidatorImpl(x509PublicKeyB64(kp.getPublic()));

    String serviceId = "svcA", ver = "1.2.3", instanceId = "instance-abcdefgh";
    String licenseKey = "PFX~rnd~SGVsbG8tRW5jcnlwdGVkVXNlcklkPT0=";

    String licHash = sha256B64(licenseKey);

    SignatureData original =
        SignatureData.builder()
            .serviceId(serviceId)
            .serviceVersion(ver)
            .instanceId(instanceId)
            .encryptedLicenseKeyHash(licHash)
            .build();

    String sigB64 = signPayloadJson(kp.getPrivate(), original.toJson());
    IssueAccessRequest tampered =
        issueReq(serviceId, "9.9.9", instanceId, sigB64, licenseKey, null);

    assertThrows(SignatureInvalidException.class, () -> validator.validate(tampered));
  }

  @Test
  @DisplayName("issue: licenseKey değişmiş (hash değişir) -> SignatureInvalidException")
  void issue_licenseKeyChanged_afterSign() throws Exception {
    KeyPair kp = genEd25519KeyPair();
    SignatureValidator validator = new SignatureValidatorImpl(x509PublicKeyB64(kp.getPublic()));

    String serviceId = "svcA", ver = "1.2.3", instanceId = "instance-abcdefgh";
    String originalKey = "PFX~rnd~SGVsbG8tRW5jcnlwdGVkVXNlcklkPT0=";

    String licHash = sha256B64(originalKey);
    SignatureData payload =
        SignatureData.builder()
            .serviceId(serviceId)
            .serviceVersion(ver)
            .instanceId(instanceId)
            .encryptedLicenseKeyHash(licHash)
            .build();

    String sigB64 = signPayloadJson(kp.getPrivate(), payload.toJson());
    String changedKey = originalKey.replaceFirst("PFX", "PFY");

    IssueAccessRequest req = issueReq(serviceId, ver, instanceId, sigB64, changedKey, null);

    assertThrows(SignatureInvalidException.class, () -> validator.validate(req));
  }
}
