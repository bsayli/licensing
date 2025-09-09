package io.github.bsayli.license.signature.generator;

import static io.github.bsayli.license.common.CryptoConstants.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.bsayli.license.signature.model.SignatureData;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;

public final class SignatureGenerator {

  private static final String SAMPLE_SERVICE_ID = "bsayli-licensing";
  private static final String SAMPLE_SERVICE_VERSION = "1.2.2";
  private static final String SAMPLE_INSTANCE_ID = "bsayli-licensing~localdev~00:2A:8D:BE:F1:23";

  private static final String SAMPLE_ENCRYPTED_LICENSE_KEY =
          "v6ZFWUUUDlVaONpVJzzDowezuCkCk6szc4ClvB0ow6V+oyuY2bsJCPdVQErI0F7jiJ44X9xoyRCrMN2Ugz2iK1kekvRkHQdaxREMz8NnQCCIodstpdYqSv+h1lNJqROPzfvj23TxHBSKr0PzlS/OoqulJuHb0rU+9WR/LoAFAr5/L740bToGooZ/KLRKKeGOS3LCJfOApMCVvL9YblYxwPPLTOZC2A==";

  private static final String SAMPLE_LICENSE_TOKEN =
          "eyJhbGciOiJFZERTQSJ9.eyJzdWIiOiJjOWluZUNvZGVnZW4tbWFjYm9va3V5bmprbDV-"
                  + "MDA6MkE6OEQ6QkU6RjE6MjMiLCJsaWNlbnNlU3RhdHVzIjoiQWN0aXZlIiwibGljZW5zZVRpZXIiOiJQcm9mZXNzaW9uYWwiLCJpYXQiOjE3MjIxNzY5MjEsImV4cCI6MTcyMjE3Njk4MX0."
                  + "3Acy39GVi_S5CXUsnKDPmOBWpjD-UoOdfAkyB1QKlDxBuggR4nOhZvGFlNooMNB67f_bp_iySLPmXVPrXIHnDQ";


  private SignatureGenerator() {}

  public static SignatureData sampleSignatureDataWithLicenseKey() throws NoSuchAlgorithmException {
    String encKeyHash = base64Sha256(SAMPLE_ENCRYPTED_LICENSE_KEY);
    return new SignatureData.Builder()
            .serviceId(SAMPLE_SERVICE_ID)
            .serviceVersion(SAMPLE_SERVICE_VERSION)
            .instanceId(SAMPLE_INSTANCE_ID)
            .encryptedLicenseKeyHash(encKeyHash)
            .build();
  }

  public static SignatureData sampleSignatureDataWithLicenseToken() throws NoSuchAlgorithmException {
    String tokenHash = base64Sha256(SAMPLE_LICENSE_TOKEN);
    return new SignatureData.Builder()
            .serviceId(SAMPLE_SERVICE_ID)
            .serviceVersion(SAMPLE_SERVICE_VERSION)
            .instanceId(SAMPLE_INSTANCE_ID)
            .licenseTokenHash(tokenHash)
            .build();
  }

  public static String createSignature(SignatureData payload, String privateKeyPkcs8Base64)
          throws JsonProcessingException, GeneralSecurityException {

    String json = payload.toJson();
    byte[] data = json.getBytes(UTF8);

    PrivateKey privateKey = decodeEd25519PrivateKeyFromBase64(privateKeyPkcs8Base64);

    Signature sig = Signature.getInstance(ED25519_STD_ALGO);
    sig.initSign(privateKey);
    sig.update(data);
    byte[] signature = sig.sign();

    return B64_ENC.encodeToString(signature);
  }


  private static byte[] sha256(byte[] data) throws NoSuchAlgorithmException {
    return MessageDigest.getInstance(SHA_256).digest(data);
  }

  private static String base64Sha256(String text) throws NoSuchAlgorithmException {
    return B64_ENC.encodeToString(sha256(text.getBytes(UTF8)));
  }

  private static PrivateKey decodeEd25519PrivateKeyFromBase64(String pkcs8Base64)
          throws GeneralSecurityException {
    byte[] der = B64_DEC.decode(pkcs8Base64);
    KeyFactory kf = KeyFactory.getInstance(ED25519_STD_ALGO); // "Ed25519"
    return kf.generatePrivate(new PKCS8EncodedKeySpec(der));
  }
}