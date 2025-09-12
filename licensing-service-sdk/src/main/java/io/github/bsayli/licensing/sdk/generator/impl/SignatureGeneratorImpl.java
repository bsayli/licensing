package io.github.bsayli.licensing.sdk.generator.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.bsayli.licensing.client.generated.dto.IssueAccessRequest;
import io.github.bsayli.licensing.client.generated.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.sdk.domain.model.SignatureData;
import io.github.bsayli.licensing.sdk.generator.SignatureGenerator;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

public class SignatureGeneratorImpl implements SignatureGenerator {

  private static final String ALG_ED25519 = "Ed25519";
  private static final String SHA_256 = "SHA-256";

  private final PrivateKey privateKey;

  public SignatureGeneratorImpl(String privateKeyPkcs8Base64) {
    try {
      byte[] der = Base64.getDecoder().decode(privateKeyPkcs8Base64);
      KeyFactory kf = KeyFactory.getInstance(ALG_ED25519);
      this.privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(der));
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Failed to decode Ed25519 private key", e);
    }
  }

  @Override
  public String generateForIssue(IssueAccessRequest request) {
    String keyHash = base64Sha256(request.getLicenseKey());
    SignatureData payload =
        SignatureData.builder()
            .serviceId(request.getServiceId())
            .serviceVersion(request.getServiceVersion())
            .instanceId(request.getInstanceId())
            .encryptedLicenseKeyHash(keyHash)
            .build();
    return signPayload(payload);
  }

  @Override
  public String generateForValidate(String licenseToken, ValidateAccessRequest request) {
    Objects.requireNonNull(licenseToken, "licenseToken");
    String tokenHash = base64Sha256(licenseToken);
    SignatureData payload =
        SignatureData.builder()
            .serviceId(request.getServiceId())
            .serviceVersion(request.getServiceVersion())
            .instanceId(request.getInstanceId())
            .licenseTokenHash(tokenHash)
            .build();
    return signPayload(payload);
  }

  private String base64Sha256(String text) {
    try {
      MessageDigest md = MessageDigest.getInstance(SHA_256);
      byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  private String signPayload(SignatureData payload) {
    try {
      String json = payload.toJson();
      Signature sig = Signature.getInstance(ALG_ED25519);
      sig.initSign(privateKey);
      sig.update(json.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(sig.sign());
    } catch (JsonProcessingException | GeneralSecurityException e) {
      throw new IllegalStateException("Failed to sign payload", e);
    }
  }
}
