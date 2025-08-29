package com.c9.licensing.sdk.generator.impl;

import com.c9.licensing.sdk.generator.SignatureGenerator;
import com.c9.licensing.sdk.model.SignatureData;
import com.c9.licensing.sdk.model.SignatureData.Builder;
import com.c9.licensing.sdk.model.server.LicenseServerValidationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class SignatureGeneratorImpl implements SignatureGenerator {

  private final PrivateKey privateKey;

  public SignatureGeneratorImpl(String privateKeyStr)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    byte[] decodedPrivateKey = Base64.getDecoder().decode(privateKeyStr);
    KeyFactory keyFactory = KeyFactory.getInstance("DSA");
    this.privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodedPrivateKey));
  }

  @Override
  public String generateSignature(LicenseServerValidationRequest request) {
    Builder builder =
        new SignatureData.Builder()
            .serviceId(request.serviceId())
            .serviceVersion(request.serviceVersion())
            .instanceId(request.instanceId());

    if (request.licenseToken() != null) {
      builder.licenseTokenHash(getDataHash(request.licenseToken()));
    } else {
      builder.encryptedLicenseKeyHash(getDataHash(request.licenseKey()));
    }

    SignatureData signatureData = builder.build();
    try {
      return createSignature(signatureData);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private String createSignature(SignatureData signatureData)
      throws JsonProcessingException,
          InvalidKeyException,
          NoSuchAlgorithmException,
          SignatureException {
    String data = signatureData.toJson();
    byte[] hash = calculateSHA256Hash(data.getBytes());
    byte[] signature = signData(hash);
    return Base64.getEncoder().encodeToString(signature);
  }

  private byte[] calculateSHA256Hash(byte[] data) {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("SHA-256");
      return digest.digest(data);
    } catch (NoSuchAlgorithmException e) {
      return new byte[0];
    }
  }

  private byte[] signData(byte[] data)
      throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    Signature signer = Signature.getInstance("SHA256withDSA");
    signer.initSign(privateKey);
    signer.update(data);
    return signer.sign();
  }

  private String getDataHash(String data) {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      return null;
    }
  }
}
