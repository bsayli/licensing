package io.github.bsayli.license.signature.validator;

import static io.github.bsayli.license.common.CryptoConstants.ED25519_STD_ALGO;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class SignatureValidator {

  private final PublicKey publicKey;

  public SignatureValidator(String publicKeyBase64) {
    if (publicKeyBase64 == null || publicKeyBase64.isBlank()) {
      throw new IllegalArgumentException("Public key must not be null/blank");
    }
    try {
      byte[] der = Base64.getDecoder().decode(publicKeyBase64);
      KeyFactory kf = KeyFactory.getInstance(ED25519_STD_ALGO);
      this.publicKey = kf.generatePublic(new X509EncodedKeySpec(der));
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid Ed25519 public key (Base64 SPKI expected)", e);
    }
  }

  public boolean validateSignature(String signatureB64, String canonicalJson)
      throws GeneralSecurityException {
    byte[] sig = Base64.getDecoder().decode(signatureB64);
    byte[] data = canonicalJson.getBytes(StandardCharsets.UTF_8);

    Signature s = Signature.getInstance(ED25519_STD_ALGO);
    s.initVerify(publicKey);
    s.update(data);
    return s.verify(sig);
  }
}
