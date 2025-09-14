package io.github.bsayli.license.cli.service;

import io.github.bsayli.license.licensekey.encrypter.UserIdEncrypter;
import java.security.GeneralSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UserIdCryptoService {

  private static final Logger log = LoggerFactory.getLogger(UserIdCryptoService.class);

  public String encrypt(String userId) throws GeneralSecurityException {
    if (userId == null || userId.isBlank()) {
      throw new IllegalArgumentException("--userId must not be blank");
    }
    String enc = UserIdEncrypter.encrypt(userId);
    log.debug("Encrypted userId ({} chars) -> {} chars base64", userId.length(), enc.length());
    return enc;
  }

  public String decrypt(String ciphertextBase64) throws GeneralSecurityException {
    if (ciphertextBase64 == null || ciphertextBase64.isBlank()) {
      throw new IllegalArgumentException("--ciphertext must not be blank");
    }
    String plain = UserIdEncrypter.decrypt(ciphertextBase64);
    log.debug(
        "Decrypted ciphertext ({} chars base64) -> plain ({} chars)",
        ciphertextBase64.length(),
        plain.length());
    return plain;
  }
}
