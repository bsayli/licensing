package io.github.bsayli.licensing.security.impl;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.licensing.api.dto.IssueTokenRequest;
import io.github.bsayli.licensing.api.dto.ValidateTokenRequest;
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
@DisplayName("SignatureValidatorImpl")
class SignatureValidatorImplTest {

  private static KeyPair genDsaKeyPair() throws Exception {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
    kpg.initialize(2048);
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

  private static String signPayloadJsonHash(PrivateKey priv, String json) throws Exception {
    byte[] jsonHash = sha256(json.getBytes(StandardCharsets.UTF_8));
    Signature sig = Signature.getInstance("SHA256withDSA");
    sig.initSign(priv);
    sig.update(jsonHash);
    return Base64.getEncoder().encodeToString(sig.sign());
  }

  private static IssueTokenRequest issueReq(
      String svcId,
      String ver,
      String instanceId,
      String sigB64,
      String licenseKey,
      String checksum) {
    return new IssueTokenRequest(svcId, ver, instanceId, sigB64, checksum, licenseKey, false);
  }

  private static ValidateTokenRequest validateReq(
      String svcId, String ver, String instanceId, String sigB64, String checksum) {
    return new ValidateTokenRequest(svcId, ver, instanceId, sigB64, checksum);
  }

  @Test
  @DisplayName("validate(IssueTokenRequest): happy path with matching signature")
  void issue_happyPath() throws Exception {
    KeyPair kp = genDsaKeyPair();
    String pubB64 = x509PublicKeyB64(kp.getPublic());
    SignatureValidator validator = new SignatureValidatorImpl(pubB64);

    String serviceId = "svcA";
    String serviceVer = "1.2.3";
    String instanceId = "instance-abcdefgh";
    String licenseKey = "L".repeat(220);

    String encLicHash = sha256B64(licenseKey);
    SignatureData payload =
        SignatureData.builder()
            .serviceId(serviceId)
            .serviceVersion(serviceVer)
            .instanceId(instanceId)
            .encryptedLicenseKeyHash(encLicHash)
            .build();

    String sigB64 = signPayloadJsonHash(kp.getPrivate(), payload.toJson());

    IssueTokenRequest req = issueReq(serviceId, serviceVer, instanceId, sigB64, licenseKey, null);

    assertDoesNotThrow(() -> validator.validate(req));
  }

  @Test
  @DisplayName("validate(ValidateTokenRequest): happy path with matching signature")
  void validate_happyPath() throws Exception {
    KeyPair kp = genDsaKeyPair();
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

    String sigB64 = signPayloadJsonHash(kp.getPrivate(), payload.toJson());

    ValidateTokenRequest req = validateReq(serviceId, serviceVer, instanceId, sigB64, null);

    assertDoesNotThrow(() -> validator.validate(req, jwtToken));
  }

  @Test
  @DisplayName("invalid Base64 signature -> SignatureInvalidException (IssueTokenRequest)")
  void issue_invalidBase64Signature() {
    KeyPair kp;
    try {
      kp = genDsaKeyPair();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    String pubB64 = x509PublicKeyB64(kp.getPublic());
    SignatureValidator validator = new SignatureValidatorImpl(pubB64);

    String serviceId = "svcA", ver = "1.0.0", instanceId = "instance-abcdefgh";
    String badSig = "***not-base64***";
    String licenseKey = "L".repeat(220);

    IssueTokenRequest req = issueReq(serviceId, ver, instanceId, badSig, licenseKey, null);

    assertThrows(SignatureInvalidException.class, () -> validator.validate(req));
  }

  @Test
  @DisplayName(
      "signature does not match payload -> SignatureInvalidException (ValidateTokenRequest)")
  void validate_wrongSignature() throws Exception {
    KeyPair signerKp = genDsaKeyPair();
    KeyPair verifierKp = genDsaKeyPair(); // different key ⇒ verification must fail
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

    String sigB64 = signPayloadJsonHash(signerKp.getPrivate(), payload.toJson());
    ValidateTokenRequest req = validateReq(serviceId, ver, instanceId, sigB64, null);

    assertThrows(SignatureInvalidException.class, () -> validator.validate(req, token));
  }

  @Test
  @DisplayName("tampered request fields (after signing) -> SignatureInvalidException")
  void issue_tamperedRequest() throws Exception {
    KeyPair kp = genDsaKeyPair();
    SignatureValidator validator = new SignatureValidatorImpl(x509PublicKeyB64(kp.getPublic()));

    String serviceId = "svcA", ver = "1.2.3", instanceId = "instance-abcdefgh";
    String licenseKey = "L".repeat(220);
    String encLicHash = sha256B64(licenseKey);

    SignatureData original =
        SignatureData.builder()
            .serviceId(serviceId)
            .serviceVersion(ver)
            .instanceId(instanceId)
            .encryptedLicenseKeyHash(encLicHash)
            .build();

    String sigB64 = signPayloadJsonHash(kp.getPrivate(), original.toJson());

    IssueTokenRequest tampered = issueReq(serviceId, "9.9.9", instanceId, sigB64, licenseKey, null);

    assertThrows(SignatureInvalidException.class, () -> validator.validate(tampered));
  }
}
