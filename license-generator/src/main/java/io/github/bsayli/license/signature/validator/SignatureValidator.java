package io.github.bsayli.license.signature.validator;

import static io.github.bsayli.license.common.CryptoConstants.B64_DEC;
import static io.github.bsayli.license.common.CryptoConstants.ED25519_STD_ALGO;
import static io.github.bsayli.license.common.CryptoConstants.UTF8;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;

public final class SignatureValidator {

  private final PublicKey publicKey;

  public SignatureValidator(String publicKeyBase64) {
    if (publicKeyBase64 == null || publicKeyBase64.isBlank()) {
      throw new IllegalArgumentException("Public key must not be null/blank");
    }
    try {
      byte[] der = B64_DEC.decode(publicKeyBase64);
      KeyFactory kf = KeyFactory.getInstance(ED25519_STD_ALGO);
      this.publicKey = kf.generatePublic(new X509EncodedKeySpec(der));
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid Ed25519 public key (Base64 SPKI expected)", e);
    }
  }

  public boolean validateSignature(String signatureB64, String canonicalJson)
      throws GeneralSecurityException {
    byte[] sig = B64_DEC.decode(signatureB64);
    byte[] data = canonicalJson.getBytes(UTF8);

    Signature s = Signature.getInstance(ED25519_STD_ALGO);
    s.initVerify(publicKey);
    s.update(data);
    return s.verify(sig);
  }
}
