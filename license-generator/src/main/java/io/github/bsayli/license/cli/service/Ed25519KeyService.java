package io.github.bsayli.license.cli.service;

import static io.github.bsayli.license.common.CryptoConstants.*;

import java.io.IOException;
import java.nio.file.*;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Ed25519KeyService {

  private static final Logger log = LoggerFactory.getLogger(Ed25519KeyService.class);

  public KeyPairB64 generate() throws GeneralSecurityException {
    ensureBouncyCastleIfNeeded();
    KeyPair kp = generateEd25519KeyPair();

    String pub = B64_ENC.encodeToString(kp.getPublic().getEncoded());
    String priv = B64_ENC.encodeToString(kp.getPrivate().getEncoded());

    return new KeyPairB64(pub, priv);
  }

  public void writeString(Path path, String content) throws IOException {
    Path abs = path.toAbsolutePath();
    Path parent = abs.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
    Files.writeString(abs, content);
    log.info("Wrote {} bytes to {}", content.length(), abs);
  }

  private KeyPair generateEd25519KeyPair() throws GeneralSecurityException {
    try {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance(ED25519_STD_ALGO);
      log.debug("Using JDK provider for {} → {}", ED25519_STD_ALGO, kpg.getProvider().getName());
      return kpg.generateKeyPair();
    } catch (NoSuchAlgorithmException e) {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance(EDDSA_BC_ALGO, BC_PROVIDER);
      kpg.initialize(new ECGenParameterSpec(ED25519_CURVE), new SecureRandom());
      log.info(
          "Falling back to BouncyCastle provider for Ed25519 ({}).", kpg.getProvider().getName());
      return kpg.generateKeyPair();
    }
  }

  void ensureBouncyCastleIfNeeded() {
    if (Security.getProvider(BC_PROVIDER) != null) {
      log.debug("BouncyCastle provider already present ({}).", BC_PROVIDER);
      return;
    }
    try {
      Class<?> bc = Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
      Provider p = (Provider) bc.getDeclaredConstructor().newInstance();
      Security.addProvider(p);
      log.info("BouncyCastle provider added: {}", p.getInfo());
    } catch (ClassNotFoundException e) {
      log.warn("BouncyCastle not found on classpath; continuing with JDK provider for Ed25519.", e);
    } catch (ReflectiveOperationException | SecurityException e) {
      log.warn("Failed to initialize BouncyCastle provider; continuing with JDK provider.", e);
    }
  }

  public record KeyPairB64(String publicSpkiB64, String privatePkcs8B64) {}
}
