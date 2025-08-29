package license.securekey.generator;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecureKeyGenerator {

  private static final Logger logger = LoggerFactory.getLogger(SecureKeyGenerator.class);

  public static void main(String[] args) throws NoSuchAlgorithmException {
    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
    keyGen.init(256);
    SecretKey secretKey = keyGen.generateKey();

    String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
    logger.info("Generated Secret Key: {}", encodedKey);
  }
}
