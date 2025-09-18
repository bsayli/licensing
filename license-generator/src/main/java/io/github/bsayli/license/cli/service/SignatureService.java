package io.github.bsayli.license.cli.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.bsayli.license.common.CryptoUtils;
import io.github.bsayli.license.signature.generator.SignatureGenerator;
import io.github.bsayli.license.signature.model.SignatureData;
import io.github.bsayli.license.signature.validator.SignatureValidator;
import java.security.GeneralSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SignatureService {

  private static final Logger log = LoggerFactory.getLogger(SignatureService.class);

  private static void validateNotBlank(String v, String name) {
    if (v == null || v.isBlank()) {
      throw new IllegalArgumentException("--" + name + " must not be blank");
    }
  }

  public SignResult signWithLicenseKey(
      String serviceId,
      String serviceVersion,
      String instanceId,
      String fullLicenseKey,
      String privateKeyPkcs8B64)
      throws GeneralSecurityException, JsonProcessingException {

    validateNotBlank(serviceId, "serviceId");
    validateNotBlank(serviceVersion, "serviceVersion");
    validateNotBlank(instanceId, "instanceId");
    validateNotBlank(fullLicenseKey, "licenseKey");
    validateNotBlank(privateKeyPkcs8B64, "privateKey");

    String licenseKeyHashB64 = CryptoUtils.base64Sha256(fullLicenseKey);

    SignatureData data =
        new SignatureData.Builder()
            .serviceId(serviceId)
            .serviceVersion(serviceVersion)
            .instanceId(instanceId)
            .encryptedLicenseKeyHash(licenseKeyHashB64)
            .build();

    String signatureB64 = SignatureGenerator.createSignature(data, privateKeyPkcs8B64);
    String json = data.toJson();

    log.debug(
        "Signed (licenseKeyHash) payloadLen={}, sigLen={}", json.length(), signatureB64.length());
    return new SignResult(json, signatureB64);
  }

  public SignResult signWithToken(
      String serviceId,
      String serviceVersion,
      String instanceId,
      String jwtToken,
      String privateKeyPkcs8B64)
      throws GeneralSecurityException, JsonProcessingException {

    validateNotBlank(serviceId, "serviceId");
    validateNotBlank(serviceVersion, "serviceVersion");
    validateNotBlank(instanceId, "instanceId");
    validateNotBlank(jwtToken, "token");
    validateNotBlank(privateKeyPkcs8B64, "privateKey");

    String tokenHashB64 = CryptoUtils.base64Sha256(jwtToken);

    SignatureData data =
        new SignatureData.Builder()
            .serviceId(serviceId)
            .serviceVersion(serviceVersion)
            .instanceId(instanceId)
            .licenseTokenHash(tokenHashB64)
            .build();

    String signatureB64 = SignatureGenerator.createSignature(data, privateKeyPkcs8B64);
    String json = data.toJson();

    log.debug("Signed (tokenHash) payloadLen={}, sigLen={}", json.length(), signatureB64.length());
    return new SignResult(json, signatureB64);
  }

  public boolean verify(String publicKeySpkiB64, String jsonPayload, String signatureB64)
      throws GeneralSecurityException {

    validateNotBlank(publicKeySpkiB64, "publicKey");
    validateNotBlank(jsonPayload, "dataJson");
    validateNotBlank(signatureB64, "signatureB64");

    SignatureValidator validator = new SignatureValidator(publicKeySpkiB64);
    return validator.validateSignature(signatureB64, jsonPayload);
  }

  public record SignResult(String jsonPayload, String signatureB64) {}
}
