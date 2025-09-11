package io.github.bsayli.licensing.sdk.generator.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.bsayli.licensing.client.generated.dto.IssueAccessRequest;
import io.github.bsayli.licensing.client.generated.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.sdk.generator.SignatureGenerator;
import io.github.bsayli.licensing.sdk.model.SignatureData;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

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

  private static String base64Sha256(String text) {
    try {
      MessageDigest md = MessageDigest.getInstance(SHA_256);
      byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  private static boolean isBlank(String s) {
    return s == null || s.isBlank();
  }

  @Override
  public String generateForIssue(IssueAccessRequest request) {
    if (request == null) throw new IllegalArgumentException("request is null");
    if (isBlank(request.getServiceId())
        || isBlank(request.getServiceVersion())
        || isBlank(request.getInstanceId())) {
      throw new IllegalArgumentException("serviceId, serviceVersion, instanceId are required");
    }
    if (isBlank(request.getLicenseKey())) {
      throw new IllegalArgumentException("licenseKey is required for issue signature");
    }

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
    if (request == null) throw new IllegalArgumentException("request is null");
    if (isBlank(licenseToken)) {
      throw new IllegalArgumentException("licenseToken is required for validate signature");
    }
    if (isBlank(request.getServiceId())
        || isBlank(request.getServiceVersion())
        || isBlank(request.getInstanceId())) {
      throw new IllegalArgumentException("serviceId, serviceVersion, instanceId are required");
    }

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

  private String signPayload(SignatureData payload) {
    try {
      String json = payload.toJson();
      Signature sig = Signature.getInstance(ALG_ED25519);
      sig.initSign(privateKey);
      sig.update(json.getBytes(StandardCharsets.UTF_8));
      byte[] signature = sig.sign();
      return Base64.getEncoder().encodeToString(signature);
    } catch (JsonProcessingException | GeneralSecurityException e) {
      throw new IllegalStateException("Failed to sign payload", e);
    }
  }
}
