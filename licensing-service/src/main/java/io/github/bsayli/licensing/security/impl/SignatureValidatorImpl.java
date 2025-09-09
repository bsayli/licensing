package io.github.bsayli.licensing.security.impl;

import io.github.bsayli.licensing.api.dto.IssueTokenRequest;
import io.github.bsayli.licensing.api.dto.ValidateTokenRequest;
import io.github.bsayli.licensing.domain.model.SignatureData;
import io.github.bsayli.licensing.security.SignatureValidator;
import io.github.bsayli.licensing.service.exception.security.SignatureInvalidException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class SignatureValidatorImpl implements SignatureValidator {

  private static final String ALGORITHM_SHA_256 = "SHA-256";
  private static final String ALGORITHM_DSA = "DSA";
  private static final String ALGORITHM_SHA256WITHDSA = "SHA256withDSA";

  private final byte[] signaturePublicKey;

  public SignatureValidatorImpl(String signaturePublicKeyBase64) {
    this.signaturePublicKey = Base64.getDecoder().decode(signaturePublicKeyBase64);
  }

  @Override
  public void validate(IssueTokenRequest request) throws SignatureInvalidException {
    ensureBase64(request.signature());
    SignatureData data =
        SignatureData.builder()
            .serviceId(request.serviceId())
            .serviceVersion(request.serviceVersion())
            .instanceId(request.instanceId())
            .encryptedLicenseKeyHash(sha256Base64(request.licenseKey()))
            .build();
    verify(request.signature(), data);
  }

  @Override
  public void validate(ValidateTokenRequest request, String token)
      throws SignatureInvalidException {
    ensureBase64(request.signature());
    SignatureData data =
        SignatureData.builder()
            .serviceId(request.serviceId())
            .serviceVersion(request.serviceVersion())
            .instanceId(request.instanceId())
            .licenseTokenHash(sha256Base64(token))
            .build();
    verify(request.signature(), data);
  }

  private void verify(String signatureBase64, SignatureData payload) {
    try {
      byte[] jsonHash = sha256(payload.toJson().getBytes(StandardCharsets.UTF_8));
      Signature sig = Signature.getInstance(ALGORITHM_SHA256WITHDSA);
      sig.initVerify(
          KeyFactory.getInstance(ALGORITHM_DSA)
              .generatePublic(new X509EncodedKeySpec(signaturePublicKey)));
      sig.update(jsonHash);
      boolean ok = sig.verify(Base64.getDecoder().decode(signatureBase64));
      if (!ok) throw new SignatureInvalidException();
    } catch (Exception e) {
      throw new SignatureInvalidException(e);
    }
  }

  private void ensureBase64(String s) {
    if (s == null || !s.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$")) {
      throw new SignatureInvalidException();
    }
  }

  private String sha256Base64(String data) {
    return Base64.getEncoder().encodeToString(sha256(data.getBytes(StandardCharsets.UTF_8)));
  }

  private byte[] sha256(byte[] bytes) {
    try {
      MessageDigest md = MessageDigest.getInstance(ALGORITHM_SHA_256);
      return md.digest(bytes);
    } catch (Exception e) {
      throw new SignatureInvalidException(e);
    }
  }
}
