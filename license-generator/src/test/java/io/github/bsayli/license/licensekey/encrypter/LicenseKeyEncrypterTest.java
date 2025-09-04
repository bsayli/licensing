package io.github.bsayli.license.licensekey.encrypter;

import static org.junit.jupiter.api.Assertions.*;

import java.security.GeneralSecurityException;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: LicenseKeyEncrypter")
class LicenseKeyEncrypterTest {

  private static final String SAMPLE_KEY = "BSAYLI~randomSegment~encryptedUserId123";

  @Test
  @DisplayName("Encrypt + Decrypt should return original license key")
  void encryptDecrypt_roundTrip_ok() throws GeneralSecurityException {
    String encrypted = LicenseKeyEncrypter.encrypt(SAMPLE_KEY);
    assertNotNull(encrypted);
    assertNotEquals(SAMPLE_KEY, encrypted);

    String decrypted = LicenseKeyEncrypter.decrypt(encrypted);
    assertEquals(SAMPLE_KEY, decrypted);
  }

  @Test
  @DisplayName("Decrypting a tampered ciphertext should fail with GeneralSecurityException")
  void decrypt_tamperedCipher_shouldFail() throws GeneralSecurityException {
    String encrypted = LicenseKeyEncrypter.encrypt(SAMPLE_KEY);

    byte[] all = Base64.getDecoder().decode(encrypted);
    all[all.length - 1] ^= 0x01; // bit flip
    String tampered = Base64.getEncoder().encodeToString(all);

    assertThrows(GeneralSecurityException.class, () -> LicenseKeyEncrypter.decrypt(tampered));
  }
}
