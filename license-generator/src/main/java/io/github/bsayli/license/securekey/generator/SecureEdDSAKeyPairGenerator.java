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

public final class SecureEdDSAKeyPairGenerator {

  private static final Logger log = LoggerFactory.getLogger(SecureEdDSAKeyPairGenerator.class);

  private SecureEdDSAKeyPairGenerator() {}

  public static void main(String[] args) throws GeneralSecurityException {
    KeyPair kp = generateKeyPair();
    if (log.isInfoEnabled()) {
      log.info("Generated Public Key  (Base64): {}", toBase64(kp.getPublic()));
      log.info("Generated Private Key (Base64): {}", toBase64(kp.getPrivate()));
    }
  }

  public static KeyPair generateKeyPair() throws GeneralSecurityException {
    try {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance(ED25519_STD_ALGO);
      return kpg.generateKeyPair();
    } catch (NoSuchAlgorithmException e) {
      ensureBouncyCastle();
      KeyPairGenerator kpg = KeyPairGenerator.getInstance(EDDSA_BC_ALGO, BC_PROVIDER);
      kpg.initialize(new ECGenParameterSpec(ED25519_CURVE), RNG);
      return kpg.generateKeyPair();
    }
  }

  private static void ensureBouncyCastle() {
    if (Security.getProvider(BC_PROVIDER) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }
}
