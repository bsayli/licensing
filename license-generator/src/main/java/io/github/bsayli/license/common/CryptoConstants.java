package io.github.bsayli.license.common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public final class CryptoConstants {

  public static final String AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding";
  public static final String AES_KEY_ALGORITHM = "AES";
  public static final int DEFAULT_AES_KEY_BITS = 256;
  public static final int GCM_IV_LENGTH_BYTES = 12;
  public static final int GCM_TAG_LENGTH_BYTES = 16;
  public static final int GCM_TAG_LENGTH_BITS = GCM_TAG_LENGTH_BYTES * 8;

  public static final String SHA_256 = "SHA-256";
  public static final String SHA_512 = "SHA-512";
  public static final String HMAC_SHA256 = "HmacSHA256";
  public static final String SIG_SHA256_WITH_DSA = "SHA256withDSA";
  public static final String DSA_KEY_ALGORITHM = "DSA";
  public static final String RSA_ALGORITHM = "RSA";

  public static final String ED25519_STD_ALGO = "Ed25519";
  public static final String EDDSA_BC_ALGO = "EdDSA";
  public static final String ED25519_CURVE = "Ed25519";
  public static final String BC_PROVIDER = "BC";

  public static final Charset UTF8 = StandardCharsets.UTF_8;
  public static final SecureRandom RNG = new SecureRandom();
  public static final Base64.Encoder B64_ENC = Base64.getEncoder();
  public static final Base64.Decoder B64_DEC = Base64.getDecoder();

  private CryptoConstants() {}
}
