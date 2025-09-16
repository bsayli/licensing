package io.github.bsayli.license.common;

import static io.github.bsayli.license.common.CryptoConstants.*;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class CryptoUtils {
  private CryptoUtils() {}

  public static String toBase64(Key key) {
    return B64_ENC.encodeToString(key.getEncoded());
  }

  public static SecretKey loadAesKeyFromBase64(String base64Key) {
    byte[] keyBytes = B64_DEC.decode(base64Key);
    return new SecretKeySpec(keyBytes, AES_KEY_ALGORITHM);
  }

  public static byte[] concat(byte[] a, byte[] b) {
    byte[] out = new byte[a.length + b.length];
    System.arraycopy(a, 0, out, 0, a.length);
    System.arraycopy(b, 0, out, a.length, b.length);
    return out;
  }

  public static byte[] aesGcmEncryptRaw(SecretKey key, byte[] plaintext)
      throws GeneralSecurityException {

    byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
    RNG.nextBytes(iv);

    Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
    cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
    byte[] ctAndTag = cipher.doFinal(plaintext); // CT || TAG

    return concat(iv, ctAndTag);
  }

  public static byte[] aesGcmDecryptRaw(SecretKey key, byte[] ivAndCipherText)
      throws GeneralSecurityException {

    if (ivAndCipherText == null
        || ivAndCipherText.length < GCM_IV_LENGTH_BYTES + GCM_TAG_LENGTH_BYTES) {
      throw new IllegalArgumentException("Invalid GCM payload");
    }

    byte[] iv = Arrays.copyOfRange(ivAndCipherText, 0, GCM_IV_LENGTH_BYTES);
    byte[] ctAndTag =
        Arrays.copyOfRange(ivAndCipherText, GCM_IV_LENGTH_BYTES, ivAndCipherText.length);

    Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
    cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
    return cipher.doFinal(ctAndTag);
  }
}
