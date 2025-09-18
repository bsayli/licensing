package io.github.bsayli.license.cli.service;

import io.github.bsayli.license.licensekey.generator.LicenseKeyGenerator;
import io.github.bsayli.license.licensekey.model.LicenseKeyData;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LicenseKeyGeneratorService {

  private static final Logger log = LoggerFactory.getLogger(LicenseKeyGeneratorService.class);

  public LicenseKeyResult generate(String userId, SecretKey aesKey) {
    if (userId == null || userId.isBlank()) {
      throw new IllegalArgumentException("--userId must not be blank");
    }
    if (aesKey == null) {
      throw new IllegalArgumentException("AES key must not be null");
    }

    LicenseKeyData data = LicenseKeyGenerator.generateLicenseKey(userId, aesKey);
    String licenseKey = data.generateLicenseKey();

    log.debug(
        "License key generated (len={}): prefix={}, opaqueLen={}",
        licenseKey.length(),
        data.prefix(),
        data.opaquePayloadB64Url().length());

    return new LicenseKeyResult(licenseKey, data.prefix(), data.opaquePayloadB64Url());
  }

  public record LicenseKeyResult(String licenseKey, String prefix, String opaquePayloadB64Url) {}
}
