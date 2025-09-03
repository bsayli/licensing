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

/**
 * Generates strong symmetric keys for encryption (e.g., AES-GCM).
 *
 * <p>Design notes:
 *
 * <ul>
 *   <li>Defaults to 256-bit AES, which is widely recommended.
 *   <li>Returns Base64-encoded material for easy storage/transport.
 * </ul>
 */
public final class SecureKeyGenerator {

  private static final Logger log = LoggerFactory.getLogger(SecureKeyGenerator.class);

  private SecureKeyGenerator() {
    // utility class
  }

  public static void main(String[] args) throws NoSuchAlgorithmException {
    SecretKey key = generateAesKey(DEFAULT_AES_KEY_BITS);
    log.info("Generated AES-{} Secret Key (Base64): {}", DEFAULT_AES_KEY_BITS, toBase64(key));
  }

  /** Generates an AES secret key with the given key size (e.g., 128/192/256). */
  public static SecretKey generateAesKey(int keySize) throws NoSuchAlgorithmException {
    KeyGenerator keyGen = KeyGenerator.getInstance(AES_KEY_ALGORITHM);
    keyGen.init(keySize, RNG);
    return keyGen.generateKey();
  }
}
