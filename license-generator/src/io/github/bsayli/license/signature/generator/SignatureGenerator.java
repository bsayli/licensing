package io.github.bsayli.license.signature.generator;

import static io.github.bsayli.license.common.CryptoConstants.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.bsayli.license.common.CryptoConstants;
import io.github.bsayli.license.signature.model.SignatureData;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * Generates detached digital signatures over a canonicalized {@link SignatureData} JSON payload.
 *
 * <p>Notes:
 *
 * <ul>
 *   <li>Uses SHA-256 for hashing and DSA (SHA256withDSA) for signing.
 *   <li>All fixed literals are centralized as constants in {@link CryptoConstants}.
 *   <li>Sample data below is for demonstration only; replace with runtime values in production.
 * </ul>
 */
public final class SignatureGenerator {

  /* ==== Sample payload fields (demo only) ==== */
  private static final String SAMPLE_SERVICE_ID = "bsayli-licensing";
  private static final String SAMPLE_SERVICE_VERSION = "1.2.2";
  private static final String SAMPLE_INSTANCE_ID = "bsayli-licensing~localdev~00:2A:8D:BE:F1:23";
  private static final String SAMPLE_ENCRYPTED_LICENSE_KEY =
      "v6ZFWUUUDlVaONpVJzzDowezuCkCk6szc4ClvB0ow6V+oyuY2bsJCPdVQErI0F7jiJ44X9xoyRCrMN2Ugz2iK1kekvRkHQdaxREMz8NnQCCIodstpdYqSv+h1lNJqROPzfvj23TxHBSKr0PzlS/OoqulJuHb0rU+9WR/LoAFAr5/L740bToGooZ/KLRKKeGOS3LCJfOApMCVvL9YblYxwPPLTOZC2A==";
  private static final String SAMPLE_LICENSE_TOKEN =
      "eyJhbGciOiJFZERTQSJ9.eyJzdWIiOiJjOWluZUNvZGVnZW5-bWFjYm9va3V5bmprbDV-MDA6MkE6OEQ6QkU6RjE6MjMiLCJsaWNlbnNlU3RhdHVzIjoiQWN0aXZlIiwibGljZW5zZVRpZXIiOiJQcm9mZXNzaW9uYWwiLCJpYXQiOjE3MjIxNzY5MjEsImV4cCI6MTcyMjE3Njk4MX0.3Acy39GVi_S5CXUsnKDPmOBWpjD-UoOdfAkyB1QKlDxBuggR4nOhZvGFlNooMNB67f_bp_iySLPmXVPrXIHnDQ";

  /* ==== Private key (PKCS#8, Base64) for demo signing only ==== */
  private static final String PRIVATE_KEY_B64 =
      "MIICXQIBADCCAjUGByqGSM44BAEwggIoAoIBAQCPeTXZuarpv6vtiHrPSVG28y7FnjuvNxjo6sSWHz79NgbnQ1GpxBgzObgJ58KuHFObp0dbhdARrbi0eYd1SYRpXKwOjxSzNggooi/6JxEKPWKpk0U0CaD+aWxGWPhL3SCBnDcJoBBXsZWtzQAjPbpUhLYpH51kjviDRIZ3l5zsBLQ0pqwudemYXeI9sCkvwRGMn/qdgYHnM423krcw17njSVkvaAmYchU5Feo9a4tGU8YzRY+AOzKkwuDycpAlbk4/ijsIOKHEUOThjBopo33fXqFD3ktm/wSQPtXPFiPhWNSHxgjpfyEc2B3KI8tuOAdl+CLjQr5ITAV2OTlgHNZnAh0AuvaWpoV499/e5/pnyXfHhe8ysjO65YDAvNVpXQKCAQAWplxYIEhQcE51AqOXVwQNNNo6NHjBVNTkpcAtJC7gT5bmHkvQkEq9rI837rHgnzGC0jyQQ8tkL4gAQWDt+coJsyB2p5wypifyRz6Rh5uixOdEvSCBVEy1W4AsNo0fqD7UielOD6BojjJCilx4xHjGjQUntxyaOrsLC+EsRGiWOefTznTbEBplqiuH9kxoJts+xy9LVZmDS7TtsC98kOmkltOlXVNb6/xF1PYZ9j897buHOSXC8iTgdzEpbaiH7B5HSPh++1/et1SEMWsiMt7lU92vAhErDR8C2jCXMiT+J67ai51LKSLZuovjntnhA6Y8UoELxoi34u1DFuHvF9veBB8CHQCfi8GVDJbIyuLMMAd9STvVWIwMfApIDrFmB2EW";

  private SignatureGenerator() {
    // utility class
  }

  /* -------------------------------- API (demo helpers) -------------------------------- */

  /** Builds a sample payload that signs an encrypted license key (hash only). */
  public static SignatureData sampleSignatureDataWithLicenseKey() throws NoSuchAlgorithmException {
    final String encKeyHash = base64Sha256(SAMPLE_ENCRYPTED_LICENSE_KEY);
    return new SignatureData.Builder()
        .serviceId(SAMPLE_SERVICE_ID)
        .serviceVersion(SAMPLE_SERVICE_VERSION)
        .instanceId(SAMPLE_INSTANCE_ID)
        .encryptedLicenseKeyHash(encKeyHash)
        .build();
  }

  /** Builds a sample payload that signs a license token (hash only). */
  public static SignatureData sampleSignatureDataWithLicenseToken()
      throws NoSuchAlgorithmException {
    final String tokenHash = base64Sha256(SAMPLE_LICENSE_TOKEN);
    return new SignatureData.Builder()
        .serviceId(SAMPLE_SERVICE_ID)
        .serviceVersion(SAMPLE_SERVICE_VERSION)
        .instanceId(SAMPLE_INSTANCE_ID)
        .licenseTokenHash(tokenHash)
        .build();
  }

  /** Creates a Base64-encoded detached signature for the given payload. */
  public static String createSignature(SignatureData payload)
      throws JsonProcessingException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          SignatureException,
          InvalidKeyException {
    final String json = payload.toJson();
    final byte[] hash = sha256(json.getBytes(UTF8));
    final PrivateKey pk = decodePrivateKeyFromBase64(PRIVATE_KEY_B64);
    final byte[] sig = signHash(hash, pk);
    return Base64.getEncoder().encodeToString(sig);
  }

  /* -------------------------------- Internals -------------------------------- */

  private static byte[] sha256(byte[] data) throws NoSuchAlgorithmException {
    return MessageDigest.getInstance(SHA_256).digest(data);
  }

  private static String base64Sha256(String text) throws NoSuchAlgorithmException {
    return Base64.getEncoder().encodeToString(sha256(text.getBytes(UTF8)));
  }

  private static byte[] signHash(byte[] hash, PrivateKey privateKey)
      throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    final Signature signer = Signature.getInstance(SIG_SHA256_WITH_DSA);
    signer.initSign(privateKey);
    signer.update(hash);
    return signer.sign();
  }

  private static PrivateKey decodePrivateKeyFromBase64(String pkcs8Base64)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    final byte[] keyBytes = Base64.getDecoder().decode(pkcs8Base64);
    final KeyFactory kf = KeyFactory.getInstance(DSA_KEY_ALGORITHM);
    return kf.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
  }
}
