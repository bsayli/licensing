package io.github.bsayli.license.common;

import static io.github.bsayli.license.common.CryptoConstants.*;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Small crypto helpers to remove duplication across modules. - No secrets here; only stateless
 * helpers using {@link CryptoConstants}.
 */
public final class CryptoUtils {
  private CryptoUtils() {}

  /** Encodes any SecretKey into Base64 (no line breaks). */
  public static String toBase64(SecretKey key) {
    return B64_ENC.encodeToString(key.getEncoded());
  }

  /** Encodes any {@link Key} (public/private/secret) into Base64 (no line breaks). */
  public static String toBase64(Key key) {
    return B64_ENC.encodeToString(key.getEncoded());
  }

  /** Loads an AES {@link SecretKey} from a Base64-encoded key string. */
  public static SecretKey loadAesKeyFromBase64(String base64Key) {
    byte[] keyBytes = B64_DEC.decode(base64Key);
    return new SecretKeySpec(keyBytes, AES_KEY_ALGORITHM);
  }

  /** Concatenates two byte arrays (utility used by AES-GCM IV || CIPHERTEXT). */
  public static byte[] concat(byte[] a, byte[] b) {
    byte[] out = new byte[a.length + b.length];
    System.arraycopy(a, 0, out, 0, a.length);
    System.arraycopy(b, 0, out, a.length, b.length);
    return out;
  }

  /* =========================
   * AES-GCM one-shot helpers
   * ========================= */

  public static String aesGcmEncryptToBase64(SecretKey key, String plainText)
      throws GeneralSecurityException {
    return aesGcmEncryptToBase64(key, plainText.getBytes(UTF8));
  }

  public static String aesGcmEncryptToBase64(SecretKey key, byte[] plaintext)
      throws GeneralSecurityException {
    byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
    RNG.nextBytes(iv);

    Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
    cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

    byte[] cipherBytes = cipher.doFinal(plaintext);
    return B64_ENC.encodeToString(concat(iv, cipherBytes));
  }

  /** Decrypts Base64( IV || CIPHERTEXT ) into a UTF-8 string. */
  public static String aesGcmDecryptFromBase64(SecretKey key, String encryptedBase64)
      throws GeneralSecurityException {
    byte[] plain = aesGcmDecryptBytesFromBase64(key, encryptedBase64);
    return new String(plain, UTF8);
  }

  /** Decrypts Base64( IV || CIPHERTEXT ) into raw bytes. */
  public static byte[] aesGcmDecryptBytesFromBase64(SecretKey key, String encryptedBase64)
      throws GeneralSecurityException {
    byte[] all = B64_DEC.decode(encryptedBase64);
    byte[] iv = Arrays.copyOfRange(all, 0, GCM_IV_LENGTH_BYTES);
    byte[] cipherBytes = Arrays.copyOfRange(all, GCM_IV_LENGTH_BYTES, all.length);

    Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
    cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

    return cipher.doFinal(cipherBytes);
  }
}
