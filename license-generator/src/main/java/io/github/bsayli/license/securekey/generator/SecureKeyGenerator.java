package io.github.bsayli.license.securekey.generator;

import static io.github.bsayli.license.common.CryptoConstants.AES_KEY_ALGORITHM;
import static io.github.bsayli.license.common.CryptoConstants.RNG;

import java.security.NoSuchAlgorithmException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public final class SecureKeyGenerator {

  private SecureKeyGenerator() {}

  public static SecretKey generateAesKey(int keySize) throws NoSuchAlgorithmException {
    KeyGenerator keyGen = KeyGenerator.getInstance(AES_KEY_ALGORITHM);
    keyGen.init(keySize, RNG);
    return keyGen.generateKey();
  }
}
