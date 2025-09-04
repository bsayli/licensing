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

public class LicenseKeyEncrypter {

  private static final Logger log = LoggerFactory.getLogger(LicenseKeyEncrypter.class);

  private static final String SECRET_KEY_BASE64 = "SEYhWGm3FwD7g7iXESuM+LuaNgnDtEL5S5HS4td7+SA=";
  private static final SecretKey ENCRYPTION_KEY = loadAesKeyFromBase64(SECRET_KEY_BASE64);

  private static final String SAMPLE_LICENSE_KEY =
      "BSAYLI~3qrruqSQ0Y7PP72FpZu-o6xFyl0DN2yCqorHH6EniiQ~asJ6mTZX46Llv2riV9Eu+FtKM+s7GkxFD/rHxoNHXXwPKLomEmc/c4BXEMApWPSIGTAGL4RjPr1F35yCie31wg==";

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  public static void main(String[] args) throws GeneralSecurityException {
    log.info("License Key (plain): {}", SAMPLE_LICENSE_KEY);
    String enc = encrypt(SAMPLE_LICENSE_KEY);
    log.info("Encrypted: {}", enc);
    String dec = decrypt(enc);
    log.info("Decrypted: {}", dec);
  }

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
