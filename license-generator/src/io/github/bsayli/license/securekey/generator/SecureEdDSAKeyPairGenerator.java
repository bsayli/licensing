package io.github.bsayli.license.securekey.generator;

import static io.github.bsayli.license.common.CryptoConstants.BC_PROVIDER;
import static io.github.bsayli.license.common.CryptoConstants.ED25519_CURVE;
import static io.github.bsayli.license.common.CryptoConstants.ED25519_STD_ALGO;
import static io.github.bsayli.license.common.CryptoConstants.EDDSA_BC_ALGO;
import static io.github.bsayli.license.common.CryptoConstants.RNG;
import static io.github.bsayli.license.common.CryptoUtils.toBase64;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates an Ed25519 key pair for modern digital signatures (EdDSA).
 *
 * <p>Behavior:
 *
 * <ul>
 *   <li>Tries standard JCA {@code "Ed25519"} first (Java 15+)
 *   <li>Falls back to BouncyCastle ({@code "EdDSA"}) if unavailable
 *   <li>Provides Base64 helpers for storing/transporting keys
 * </ul>
 */
public final class SecureEdDSAKeyPairGenerator {

  private static final Logger log = LoggerFactory.getLogger(SecureEdDSAKeyPairGenerator.class);

  private SecureEdDSAKeyPairGenerator() {
    /* utility */
  }

  public static void main(String[] args) throws GeneralSecurityException {
    KeyPair kp = generateKeyPair();
    log.info("Generated Public Key  (Base64): {}", toBase64(kp.getPublic()));
    log.info("Generated Private Key (Base64): {}", toBase64(kp.getPrivate()));
  }

  /** Generates an Ed25519 key pair; falls back to BC if the JCA provider is missing. */
  public static KeyPair generateKeyPair() throws GeneralSecurityException {
    try {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance(ED25519_STD_ALGO);
      return kpg.generateKeyPair(); // Ed25519 needs no explicit params on standard JCA
    } catch (NoSuchAlgorithmException e) {
      ensureBouncyCastle();
      KeyPairGenerator kpg = KeyPairGenerator.getInstance(EDDSA_BC_ALGO, BC_PROVIDER);
      kpg.initialize(new ECGenParameterSpec(ED25519_CURVE), RNG);
      return kpg.generateKeyPair();
    }
  }

  /** Adds BouncyCastle provider once if not present. */
  private static void ensureBouncyCastle() {
    if (Security.getProvider(BC_PROVIDER) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }
}
