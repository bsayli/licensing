package io.github.bsayli.licensing.security.impl;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.domain.model.SignatureData;
import io.github.bsayli.licensing.security.SignatureValidator;
import io.github.bsayli.licensing.service.exception.security.SignatureInvalidException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class SignatureValidatorImpl implements SignatureValidator {

  private static final String ALG_ED25519 = "Ed25519";
  private static final String ALG_EDDSA_BC = "EdDSA";
  private static final String PROVIDER_BC = "BC";

  private final PublicKey publicKey;

  public SignatureValidatorImpl(String signaturePublicKeyBase64) {
    if (signaturePublicKeyBase64 == null || signaturePublicKeyBase64.isBlank()) {
      throw new SignatureInvalidException();
    }
    try {
      byte[] der = Base64.getDecoder().decode(signaturePublicKeyBase64);
      this.publicKey = loadEd25519PublicKey(der);
    } catch (Exception e) {
      throw new SignatureInvalidException(e);
    }
  }

  @Override
  public void validate(IssueAccessRequest request) throws SignatureInvalidException {
    ensureStdBase64(request.signature());

    SignatureData data =
        SignatureData.builder()
            .serviceId(request.serviceId())
            .serviceVersion(request.serviceVersion())
            .instanceId(request.instanceId())
            .encryptedLicenseKeyHash(sha256Base64(request.licenseKey()))
            .build();

    verifyEd25519(request.signature(), data);
  }

  @Override
  public void validate(ValidateAccessRequest request, String token)
      throws SignatureInvalidException {
    ensureStdBase64(request.signature());

    SignatureData data =
        SignatureData.builder()
            .serviceId(request.serviceId())
            .serviceVersion(request.serviceVersion())
            .instanceId(request.instanceId())
            .licenseTokenHash(sha256Base64(token))
            .build();

    verifyEd25519(request.signature(), data);
  }

  private PublicKey loadEd25519PublicKey(byte[] spkiDer) throws GeneralSecurityException {
    try {
      KeyFactory kf = KeyFactory.getInstance(ALG_ED25519);
      return kf.generatePublic(new X509EncodedKeySpec(spkiDer));
    } catch (NoSuchAlgorithmException e) {
      KeyFactory kf = KeyFactory.getInstance(ALG_EDDSA_BC, PROVIDER_BC);
      return kf.generatePublic(new X509EncodedKeySpec(spkiDer));
    }
  }

  private void verifyEd25519(String signatureBase64, SignatureData payload) {
    try {
      byte[] data = payload.toJson().getBytes(StandardCharsets.UTF_8);
      byte[] sigBytes = Base64.getDecoder().decode(signatureBase64);

      Signature sig;
      try {
        sig = Signature.getInstance(ALG_ED25519);
      } catch (NoSuchAlgorithmException e) {
        sig = Signature.getInstance(ALG_EDDSA_BC, PROVIDER_BC);
      }

      sig.initVerify(publicKey);
      sig.update(data);
      boolean ok = sig.verify(sigBytes);
      if (!ok) throw new SignatureInvalidException();
    } catch (Exception e) {
      throw new SignatureInvalidException(e);
    }
  }

  private String sha256Base64(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      return Base64.getEncoder().encodeToString(md.digest(s.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new SignatureInvalidException(e);
    }
  }

  private void ensureStdBase64(String s) {
    if (s == null || s.isBlank()) throw new SignatureInvalidException();
    if (!s.matches("^[A-Za-z0-9+/]+={0,2}$")) {
      throw new SignatureInvalidException();
    }
  }
}
