package io.github.bsayli.license.common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Shared cryptographic constants and reusable utilities (no secrets).
 *
 * <p>Only algorithm names, default sizes, and generic utilities belong here. Do NOT store secrets
 * or runtime keys in this class.
 */
public final class CryptoConstants {

  /* ==============================
   * AES / Symmetric crypto
   * ============================== */
  public static final String AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding";
  public static final String AES_KEY_ALGORITHM = "AES";
  public static final int DEFAULT_AES_KEY_BITS = 256;
  public static final int GCM_IV_LENGTH_BYTES = 12; // 96-bit IV
  public static final int GCM_TAG_LENGTH_BYTES = 16; // 128-bit tag
  public static final int GCM_TAG_LENGTH_BITS = GCM_TAG_LENGTH_BYTES * 8;
  /* ==============================
   * Hash / Signature algorithms
   * ============================== */
  public static final String SHA_256 = "SHA-256";
  public static final String SHA_512 = "SHA-512";
  public static final String HMAC_SHA256 = "HmacSHA256";
  public static final String SIG_SHA256_WITH_DSA = "SHA256withDSA";
  public static final String DSA_KEY_ALGORITHM = "DSA";
  public static final String RSA_ALGORITHM = "RSA";
  /* ==============================
   * EdDSA / Ed25519
   * ============================== */
  public static final String ED25519_STD_ALGO = "Ed25519"; // JCA standard
  public static final String EDDSA_BC_ALGO = "EdDSA"; // BouncyCastle alias
  public static final String ED25519_CURVE = "Ed25519";
  public static final String BC_PROVIDER = "BC";
  /* ==============================
   * Charset & Utilities
   * ============================== */
  public static final Charset UTF8 = StandardCharsets.UTF_8;
  public static final SecureRandom RNG = new SecureRandom();
  // Shared Base64 codecs
  public static final Base64.Encoder B64_ENC = Base64.getEncoder();
  public static final Base64.Decoder B64_DEC = Base64.getDecoder();

  private CryptoConstants() {
    // utility class
  }
}
