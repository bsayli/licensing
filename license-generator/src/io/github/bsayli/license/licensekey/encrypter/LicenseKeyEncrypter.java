package io.github.bsayli.license.licensekey.encrypter;

import static io.github.bsayli.license.common.CryptoConstants.*;
import static io.github.bsayli.license.common.CryptoUtils.concat;
import static io.github.bsayli.license.common.CryptoUtils.loadAesKeyFromBase64;

import io.github.bsayli.license.common.CryptoConstants;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to encrypt/decrypt a composed license key string with AES/GCM.
 *
 * <p>Output format: Base64( IV(12 bytes) || CIPHERTEXT ) <strong>Security note:</strong>
 * SECRET_KEY_BASE64 is inline for demo usage. In production, load the key from a secure runtime
 * source (vault/KMS/env).
 */
public class LicenseKeyEncrypter {

  private static final Logger LOG = LoggerFactory.getLogger(LicenseKeyEncrypter.class);

  // Demo key — replace at runtime in real deployments.
  private static final String SECRET_KEY_BASE64 = "SEYhWGm3FwD7g7iXESuM+LuaNgnDtEL5S5HS4td7+SA=";
  private static final SecretKey ENCRYPTION_KEY = loadAesKeyFromBase64(SECRET_KEY_BASE64);

  // Demo sample (for the main method)
  private static final String SAMPLE_LICENSE_KEY =
      "BSAYLI~3qrruqSQ0Y7PP72FpZu-o6xFyl0DN2yCqorHH6EniiQ~asJ6mTZX46Llv2riV9Eu+FtKM+s7GkxFD/rHxoNHXXwPKLomEmc/c4BXEMApWPSIGTAGL4RjPr1F35yCie31wg==";

  static {
    // Not strictly required for AES/GCM on modern JDKs, but harmless and consistent with project.
    Security.addProvider(new BouncyCastleProvider());
  }

  public static void main(String[] args) throws GeneralSecurityException {
    LOG.info("License Key (plain): {}", SAMPLE_LICENSE_KEY);

    String enc = encrypt(SAMPLE_LICENSE_KEY);
    LOG.info("Encrypted: {}", enc);

    String dec = decrypt(enc);
    LOG.info("Decrypted: {}", dec);
  }

  /** Encrypts plain text using AES/GCM, returns Base64( IV || CIPHERTEXT ). */
  public static String encrypt(String plainText) throws GeneralSecurityException {
    byte[] iv = new byte[CryptoConstants.GCM_IV_LENGTH_BYTES];
    RNG.nextBytes(iv);

    Cipher cipher = Cipher.getInstance(CryptoConstants.AES_GCM_TRANSFORMATION);
    cipher.init(
        Cipher.ENCRYPT_MODE,
        ENCRYPTION_KEY,
        new GCMParameterSpec(CryptoConstants.GCM_TAG_LENGTH_BITS, iv));

    byte[] cipherBytes = cipher.doFinal(plainText.getBytes(CryptoConstants.UTF8));
    byte[] out = concat(iv, cipherBytes);
    return B64_ENC.encodeToString(out);
  }

  /** Decrypts Base64( IV || CIPHERTEXT ) produced by {@link #encrypt(String)}. */
  public static String decrypt(String encryptedBase64) throws GeneralSecurityException {
    byte[] all = B64_DEC.decode(encryptedBase64);
    byte[] iv = Arrays.copyOfRange(all, 0, CryptoConstants.GCM_IV_LENGTH_BYTES);
    byte[] cipherBytes = Arrays.copyOfRange(all, CryptoConstants.GCM_IV_LENGTH_BYTES, all.length);

    Cipher cipher = Cipher.getInstance(CryptoConstants.AES_GCM_TRANSFORMATION);
    cipher.init(
        Cipher.DECRYPT_MODE,
        ENCRYPTION_KEY,
        new GCMParameterSpec(CryptoConstants.GCM_TAG_LENGTH_BITS, iv));

    byte[] plain = cipher.doFinal(cipherBytes);
    return new String(plain, CryptoConstants.UTF8);
  }
}
