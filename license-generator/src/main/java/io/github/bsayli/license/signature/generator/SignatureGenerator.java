package io.github.bsayli.license.signature.generator;

import static io.github.bsayli.license.common.CryptoConstants.B64_DEC;
import static io.github.bsayli.license.common.CryptoConstants.B64_ENC;
import static io.github.bsayli.license.common.CryptoConstants.ED25519_STD_ALGO;
import static io.github.bsayli.license.common.CryptoConstants.UTF8;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.bsayli.license.signature.model.SignatureData;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;

public final class SignatureGenerator {

  private SignatureGenerator() {}

  public static String createSignature(SignatureData payload, String privateKeyPkcs8Base64)
      throws JsonProcessingException, GeneralSecurityException {

    byte[] data = payload.toJson().getBytes(UTF8);
    PrivateKey privateKey = decodeEd25519PrivateKeyFromBase64(privateKeyPkcs8Base64);

    Signature sig = Signature.getInstance(ED25519_STD_ALGO); // "Ed25519"
    sig.initSign(privateKey);
    sig.update(data);
    return B64_ENC.encodeToString(sig.sign());
  }

  private static PrivateKey decodeEd25519PrivateKeyFromBase64(String pkcs8Base64)
      throws GeneralSecurityException {
    byte[] der = B64_DEC.decode(pkcs8Base64);
    return KeyFactory.getInstance(ED25519_STD_ALGO).generatePrivate(new PKCS8EncodedKeySpec(der));
  }
}
