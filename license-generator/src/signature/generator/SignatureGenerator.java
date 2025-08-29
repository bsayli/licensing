package signature.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import signature.model.SignatureData;

public class SignatureGenerator {

  private static final Logger logger = LoggerFactory.getLogger(SignatureGenerator.class);
  private static final String ALGORITHM = "SHA256withDSA";

  public static void main(String[] args) throws Exception {
    SignatureData signatureData = getSignatureDataWithLicenseKey();
    String signature = createSignature(signatureData);
    logger.info("Generated Signature for the encLicenseKey: {}", signature);

    signatureData = getSignatureDataWithLicenseToken();
    signature = createSignature(signatureData);
    logger.info("Generated Signature for the licenseToken: {}", signature);
  }

  public static SignatureData getSignatureDataWithLicenseKey() throws NoSuchAlgorithmException {
    String serviceId = "c9ineCodegen";
    String serviceVersion = "1.2.2";
    String encryptedLicenseKey =
        "v6ZFWUUUDlVaONpVJzzDowezuCkCk6szc4ClvB0ow6V+oyuY2bsJCPdVQErI0F7jiJ44X9xoyRCrMN2Ugz2iK1kekvRkHQdaxREMz8NnQCCIodstpdYqSv+h1lNJqROPzfvj23TxHBSKr0PzlS/OoqulJuHb0rU+9WR/LoAFAr5/L740bToGooZ/KLRKKeGOS3LCJfOApMCVvL9YblYxwPPLTOZC2A==";
    String encryptedLicenseKeyHash = getDataHash(encryptedLicenseKey);
    String instanceId = "c9ineCodegen~macbookuynjkl5~00:2A:8D:BE:F1:23";
    return new SignatureData.Builder()
        .serviceId(serviceId)
        .serviceVersion(serviceVersion)
        .instanceId(instanceId)
        .encryptedLicenseKeyHash(encryptedLicenseKeyHash)
        .build();
  }

  public static SignatureData getSignatureDataWithLicenseToken() throws NoSuchAlgorithmException {
    String serviceId = "c9ineCodegen";
    String serviceVersion = "1.2.2";
    String token =
        "eyJhbGciOiJFZERTQSJ9.eyJzdWIiOiJjOWluZUNvZGVnZW5-bWFjYm9va3V5bmprbDV-MDA6MkE6OEQ6QkU6RjE6MjMiLCJsaWNlbnNlU3RhdHVzIjoiQWN0aXZlIiwibGljZW5zZVRpZXIiOiJQcm9mZXNzaW9uYWwiLCJpYXQiOjE3MjIxNzY5MjEsImV4cCI6MTcyMjE3Njk4MX0.3Acy39GVi_S5CXUsnKDPmOBWpjD-UoOdfAkyB1QKlDxBuggR4nOhZvGFlNooMNB67f_bp_iySLPmXVPrXIHnDQ";
    String tokenHash = getDataHash(token);
    String instanceId = "c9ineCodegen~macbookuynjkl5~00:2A:8D:BE:F1:23";
    return new SignatureData.Builder()
        .serviceId(serviceId)
        .serviceVersion(serviceVersion)
        .instanceId(instanceId)
        .licenseTokenHash(tokenHash)
        .build();
  }

  public static String createSignature(SignatureData signatureData)
      throws JsonProcessingException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          SignatureException,
          InvalidKeyException {
    String data = signatureData.toJson();

    // 1. Generate Hash
    byte[] hash = calculateSHA256Hash(data.getBytes());

    // 2. Sign the Hash
    byte[] signature = signData(hash);

    return Base64.getEncoder().encodeToString(signature); // Encode signature for transport
  }

  private static byte[] calculateSHA256Hash(byte[] data) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    return digest.digest(data);
  }

  private static String getDataHash(String data) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(data.getBytes());
    return Base64.getEncoder().encodeToString(hash);
  }

  private static byte[] signData(byte[] data)
      throws NoSuchAlgorithmException,
          InvalidKeySpecException,
          InvalidKeyException,
          SignatureException {
    PrivateKey privateKey = getPrivateKey();

    Signature signer = Signature.getInstance(ALGORITHM);
    signer.initSign(privateKey);
    signer.update(data);
    return signer.sign();
  }

  private static PrivateKey getPrivateKey()
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    String privateKeyInString =
        "MIICXQIBADCCAjUGByqGSM44BAEwggIoAoIBAQCPeTXZuarpv6vtiHrPSVG28y7FnjuvNxjo6sSWHz79NgbnQ1GpxBgzObgJ58KuHFObp0dbhdARrbi0eYd1SYRpXKwOjxSzNggooi/6JxEKPWKpk0U0CaD+aWxGWPhL3SCBnDcJoBBXsZWtzQAjPbpUhLYpH51kjviDRIZ3l5zsBLQ0pqwudemYXeI9sCkvwRGMn/qdgYHnM423krcw17njSVkvaAmYchU5Feo9a4tGU8YzRY+AOzKkwuDycpAlbk4/ijsIOKHEUOThjBopo33fXqFD3ktm/wSQPtXPFiPhWNSHxgjpfyEc2B3KI8tuOAdl+CLjQr5ITAV2OTlgHNZnAh0AuvaWpoV499/e5/pnyXfHhe8ysjO65YDAvNVpXQKCAQAWplxYIEhQcE51AqOXVwQNNNo6NHjBVNTkpcAtJC7gT5bmHkvQkEq9rI837rHgnzGC0jyQQ8tkL4gAQWDt+coJsyB2p5wypifyRz6Rh5uixOdEvSCBVEy1W4AsNo0fqD7UielOD6BojjJCilx4xHjGjQUntxyaOrsLC+EsRGiWOefTznTbEBplqiuH9kxoJts+xy9LVZmDS7TtsC98kOmkltOlXVNb6/xF1PYZ9j897buHOSXC8iTgdzEpbaiH7B5HSPh++1/et1SEMWsiMt7lU92vAhErDR8C2jCXMiT+J67ai51LKSLZuovjntnhA6Y8UoELxoi34u1DFuHvF9veBB8CHQCfi8GVDJbIyuLMMAd9STvVWIwMfApIDrFmB2EW";
    byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyInString);
    KeyFactory keyFactory = KeyFactory.getInstance("DSA");
    return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
  }
}
