package io.github.bsayli.license.licensekey.generator;

import static io.github.bsayli.license.common.CryptoConstants.RNG;
import static io.github.bsayli.license.common.LicenseConstants.LICENSE_KEY_PREFIX;
import static io.github.bsayli.license.common.LicenseConstants.RANDOM_BYTES_FOR_KEY;
import static io.github.bsayli.license.licensekey.encrypter.UserIdEncrypter.SAMPLE_USER_ID;

import io.github.bsayli.license.licensekey.encrypter.UserIdEncrypter;
import io.github.bsayli.license.licensekey.model.LicenseKeyData;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LicenseKeyGenerator {

  private static final Logger log = LoggerFactory.getLogger(LicenseKeyGenerator.class);

  private LicenseKeyGenerator() {}

  public static void main(String[] args) throws Exception {
    String encryptedUserId = UserIdEncrypter.encrypt(SAMPLE_USER_ID);
    LicenseKeyData key = generateLicenseKey(encryptedUserId);
    if (log.isInfoEnabled()) {
      log.info("Generated License Key: {}", key.generateLicenseKey());
    }
  }

  public static LicenseKeyData generateLicenseKey(String encryptedUserId) {
    String randomSegment = urlSafeRandom();
    return new LicenseKeyData(LICENSE_KEY_PREFIX, randomSegment, encryptedUserId);
  }

  private static String urlSafeRandom() {
    byte[] buf = new byte[RANDOM_BYTES_FOR_KEY];
    RNG.nextBytes(buf);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
  }
}
