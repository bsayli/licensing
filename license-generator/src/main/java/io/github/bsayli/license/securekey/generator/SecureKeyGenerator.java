package io.github.bsayli.license.securekey.generator;

import static io.github.bsayli.license.common.CryptoConstants.AES_KEY_ALGORITHM;
import static io.github.bsayli.license.common.CryptoConstants.DEFAULT_AES_KEY_BITS;
import static io.github.bsayli.license.common.CryptoConstants.RNG;
import static io.github.bsayli.license.common.CryptoUtils.toBase64;

import java.security.NoSuchAlgorithmException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SecureKeyGenerator {

  private static final Logger log = LoggerFactory.getLogger(SecureKeyGenerator.class);

  private SecureKeyGenerator() {}

  public static void main(String[] args) throws NoSuchAlgorithmException {
    SecretKey key = generateAesKey(DEFAULT_AES_KEY_BITS);
    if (log.isInfoEnabled()) {
      log.info("Generated AES-{} Secret Key (Base64): {}", DEFAULT_AES_KEY_BITS, toBase64(key));
    }
  }

  public static SecretKey generateAesKey(int keySize) throws NoSuchAlgorithmException {
    KeyGenerator keyGen = KeyGenerator.getInstance(AES_KEY_ALGORITHM);
    keyGen.init(keySize, RNG);
    return keyGen.generateKey();
  }
}
