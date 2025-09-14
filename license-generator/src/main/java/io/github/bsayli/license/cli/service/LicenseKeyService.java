package io.github.bsayli.license.cli.service;

import io.github.bsayli.license.licensekey.encrypter.UserIdEncrypter;
import io.github.bsayli.license.licensekey.generator.LicenseKeyGenerator;
import io.github.bsayli.license.licensekey.model.LicenseKeyData;
import java.security.GeneralSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LicenseKeyService {

  private static final Logger log = LoggerFactory.getLogger(LicenseKeyService.class);

  public LicenseKeyResult generate(String userId) throws GeneralSecurityException {
    if (userId == null || userId.isBlank()) {
      throw new IllegalArgumentException("--userId must not be blank");
    }

    String encryptedUserId = UserIdEncrypter.encrypt(userId);
    LicenseKeyData data = LicenseKeyGenerator.generateLicenseKey(encryptedUserId);
    String licenseKey = data.generateLicenseKey();

    log.debug(
        "License key generated (len={}): prefix={}, randomLen={}, encryptedLen={}",
        licenseKey.length(),
        data.prefix(),
        data.randomString().length(),
        data.uuid().length());

    return new LicenseKeyResult(licenseKey, data.prefix(), data.randomString(), data.uuid());
  }

  public record LicenseKeyResult(
      String licenseKey, String prefix, String randomString, String encryptedUserId) {}
}
