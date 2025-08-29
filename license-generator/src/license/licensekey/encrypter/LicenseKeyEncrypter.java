package license.licensekey.encrypter;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LicenseKeyEncrypter {
  private static final Logger logger = LoggerFactory.getLogger(LicenseKeyEncrypter.class);

  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final int GCM_IV_LENGTH = 12;
  private static final int GCM_TAG_LENGTH = 16;
  private static final String SECRET_KEY = "SEYhWGm3FwD7g7iXESuM+LuaNgnDtEL5S5HS4td7+SA=";
  private static final SecretKey encryptionKey = getKey();

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  public LicenseKeyEncrypter() {
    Security.addProvider(new BouncyCastleProvider());
  }

  public static void main(String[] args) throws Exception {
    String licenseKey =
        "C9INE~3qrruqSQ0Y7PP72FpZu-o6xFyl0DN2yCqorHH6EniiQ~asJ6mTZX46Llv2riV9Eu+FtKM+s7GkxFD/rHxoNHXXwPKLomEmc/c4BXEMApWPSIGTAGL4RjPr1F35yCie31wg==";
    logger.info("License Key: {}", licenseKey);

    String licenseKeyEncrypt = encrypt(licenseKey);
    logger.info("Enc License Key: {}", licenseKeyEncrypt);

    String licenseKeyDecrypt = decrypt(licenseKeyEncrypt);
    logger.info("Dec License Key: {}", licenseKeyDecrypt);
  }

  public static String encrypt(String plainText)
      throws NoSuchPaddingException,
          NoSuchAlgorithmException,
          InvalidAlgorithmParameterException,
          InvalidKeyException,
          IllegalBlockSizeException,
          BadPaddingException {
    byte[] iv = new SecureRandom().generateSeed(GCM_IV_LENGTH);
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
    cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, parameterSpec);

    byte[] cipherTextBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
    byte[] finalCipherText = concatArrays(iv, cipherTextBytes);
    return Base64.getEncoder().encodeToString(finalCipherText);
  }

  public static String decrypt(String encryptedText)
      throws NoSuchPaddingException,
          NoSuchAlgorithmException,
          InvalidAlgorithmParameterException,
          InvalidKeyException,
          IllegalBlockSizeException,
          BadPaddingException {
    byte[] decoded = Base64.getDecoder().decode(encryptedText);
    byte[] iv = Arrays.copyOfRange(decoded, 0, GCM_IV_LENGTH);
    byte[] cipherTextBytes = Arrays.copyOfRange(decoded, GCM_IV_LENGTH, decoded.length);

    Cipher cipher = Cipher.getInstance(ALGORITHM);
    GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
    cipher.init(Cipher.DECRYPT_MODE, encryptionKey, parameterSpec);

    byte[] decryptedBytes = cipher.doFinal(cipherTextBytes);
    return new String(decryptedBytes, StandardCharsets.UTF_8);
  }

  private static byte[] concatArrays(byte[] a, byte[] b) {
    byte[] result = new byte[a.length + b.length];
    System.arraycopy(a, 0, result, 0, a.length);
    System.arraycopy(b, 0, result, a.length, b.length);
    return result;
  }

  private static SecretKey getKey() {
    byte[] decodedKey = Base64.getDecoder().decode(SECRET_KEY);
    return new SecretKeySpec(decodedKey, "AES"); // 🔑 Önemli düzeltme
  }
}
