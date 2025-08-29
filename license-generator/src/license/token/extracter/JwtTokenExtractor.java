package license.token.extracter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import license.token.extracter.model.LicenseValidationResult;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtTokenExtractor {

  private static final Logger log = LoggerFactory.getLogger(JwtTokenExtractor.class);

  private final PublicKey publicKey;

  public JwtTokenExtractor(String publicKeyStr)
      throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
    KeyFactory keyFactory = KeyFactory.getInstance("EdDSA", "BC");
    byte[] decodedPublicKey = Base64.getDecoder().decode(publicKeyStr);
    this.publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(decodedPublicKey));
  }

  public static void main(String[] args)
      throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
    String publicKeyStr = "MCowBQYDK2VwAyEA6lNvY1+qsRSYgo66OFxWZxQymd2wC6LYNr1+q2gHHMo=";
    JwtTokenExtractor extractor = new JwtTokenExtractor(publicKeyStr);
    String token =
        "eyJhbGciOiJFZERTQSJ9.eyJzdWIiOiJjOWluZUNvZGVnZW5-bWFjYm9va3V5bmprbDV-MDA6MkE6OEQ6QkU6RjE6MjMiLCJsaWNlbnNlU3RhdHVzIjoiQWN0aXZlIiwibGljZW5zZVRpZXIiOiJQcm9mZXNzaW9uYWwiLCJpYXQiOjE3MjIxNzUwMDcsImV4cCI6MTcyMjE3NTA2N30.6W2_0IrwTJqfTp27BsM5Ea88_hhDB_zBuw45CQZ8Z3-XPnfLeUxHfVe5Iq9m4ExT3YggB0TwpvGFLyH2NO0MCg";

    try {
      LicenseValidationResult result = extractor.validateAndGetToken(token);
      log.info("License token validated successfully: {}", result);
    } catch (ExpiredJwtException e) {
      log.warn("License token is expired: {}", e.getMessage());
    } catch (JwtException e) {
      log.error("License token validation failed: {}", e.getMessage(), e);
    }
  }

  public LicenseValidationResult validateAndGetToken(String token) throws JwtException {
    Claims claims =
        Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token).getPayload();

    validateExpirationDate(claims);

    String licenseStatus = claims.get("licenseStatus", String.class);
    String licenseTier = claims.get("licenseTier", String.class);
    String message = claims.get("message", String.class);
    Date expiration = claims.getExpiration();

    return new LicenseValidationResult(licenseStatus, licenseTier, message, expiration);
  }

  private void validateExpirationDate(Claims claims) {
    Instant now = Instant.now();
    Date expiration = claims.getExpiration();
    if (expiration != null && expiration.before(Date.from(now))) {
      throw new ExpiredJwtException(null, claims, "Token has expired");
    }
  }
}
