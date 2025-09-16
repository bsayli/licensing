package io.github.bsayli.license.licensekey.encrypter;

import static io.github.bsayli.license.common.CryptoUtils.*;

import java.security.GeneralSecurityException;
import java.security.Security;
import javax.crypto.SecretKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public final class UserIdEncrypter {
  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  private UserIdEncrypter() {}

  public static byte[] encryptRaw(String userId, SecretKey key) throws GeneralSecurityException {
    return aesGcmEncryptRaw(key, userId.getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }

  public static String decryptRaw(byte[] ivAndCipherText, SecretKey key)
      throws GeneralSecurityException {
    byte[] plain = aesGcmDecryptRaw(key, ivAndCipherText);
    return new String(plain, java.nio.charset.StandardCharsets.UTF_8);
  }
}
