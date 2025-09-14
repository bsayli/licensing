package io.github.bsayli.license.cli.service;

import io.github.bsayli.license.common.CryptoUtils;
import io.github.bsayli.license.securekey.generator.SecureEdDSAKeyPairGenerator;
import io.github.bsayli.license.securekey.generator.SecureKeyGenerator;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KeygenService {

  private static final Logger log = LoggerFactory.getLogger(KeygenService.class);

  public AesKeyB64 generateAes(int sizeBits) throws NoSuchAlgorithmException {
    if (sizeBits != 128 && sizeBits != 192 && sizeBits != 256) {
      throw new IllegalArgumentException("--size must be one of 128, 192, 256");
    }
    SecretKey key = SecureKeyGenerator.generateAesKey(sizeBits);
    String b64 = CryptoUtils.toBase64(key);
    log.debug("Generated AES-{} key ({} chars base64)", sizeBits, b64.length());
    return new AesKeyB64(b64, sizeBits);
  }

  public Ed25519KeysB64 generateEd25519() throws GeneralSecurityException {
    KeyPair kp = SecureEdDSAKeyPairGenerator.generateKeyPair();
    String pub = CryptoUtils.toBase64(kp.getPublic());
    String prv = CryptoUtils.toBase64(kp.getPrivate());
    log.debug("Generated Ed25519 pair (pub {} chars, priv {} chars)", pub.length(), prv.length());
    return new Ed25519KeysB64(pub, prv);
  }

  public record AesKeyB64(String base64, int sizeBits) {}

  public record Ed25519KeysB64(String publicSpkiB64, String privatePkcs8B64) {}
}
