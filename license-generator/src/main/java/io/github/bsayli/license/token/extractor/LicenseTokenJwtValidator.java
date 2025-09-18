package io.github.bsayli.license.token.extractor;

import static io.github.bsayli.license.common.CryptoConstants.B64_DEC;
import static io.github.bsayli.license.common.CryptoConstants.ED25519_STD_ALGO;
import static io.github.bsayli.license.common.LicenseConstants.*;

import io.github.bsayli.license.token.extractor.model.LicenseValidationResult;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Date;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class LicenseTokenJwtValidator {

  private static final String BC_PROVIDER = BouncyCastleProvider.PROVIDER_NAME;

  private static final String ERR_NULL_OR_BLANK_KEY = "Public key must not be null/blank";
  private static final String ERR_INVALID_KEY = "Invalid EdDSA public key (Base64 SPKI expected)";
  private static final String ERR_NULL_OR_BLANK_TOKEN = "JWT token must not be null/blank";
  private static final String ERR_TOKEN_EXPIRED = "Token has expired";

  static {
    if (Security.getProvider(BC_PROVIDER) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  private final PublicKey publicKey;

  public LicenseTokenJwtValidator(String publicKeyBase64) {
    if (publicKeyBase64 == null || publicKeyBase64.isBlank()) {
      throw new IllegalArgumentException(ERR_NULL_OR_BLANK_KEY);
    }
    try {
      byte[] der = B64_DEC.decode(publicKeyBase64);
      X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
      KeyFactory keyFactory = resolveKeyFactory();
      this.publicKey = keyFactory.generatePublic(spec);
    } catch (Exception e) {
      throw new IllegalArgumentException(ERR_INVALID_KEY, e);
    }
  }

  private static KeyFactory resolveKeyFactory() throws GeneralSecurityException {
    try {
      return KeyFactory.getInstance(ED25519_STD_ALGO);
    } catch (NoSuchAlgorithmException e) {
      if (Security.getProvider(BC_PROVIDER) == null) {
        Security.addProvider(new BouncyCastleProvider());
      }
      return KeyFactory.getInstance(ED25519_STD_ALGO, BC_PROVIDER);
    }
  }

  private static void validateExpirationDate(Claims claims) {
    Date exp = claims.getExpiration();
    if (exp != null && exp.before(Date.from(Instant.now()))) {
      throw new ExpiredJwtException(null, claims, ERR_TOKEN_EXPIRED);
    }
  }

  public LicenseValidationResult validateAndGetToken(String token) throws JwtException {
    if (token == null || token.isBlank()) {
      throw new IllegalArgumentException(ERR_NULL_OR_BLANK_TOKEN);
    }

    Claims claims =
        Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token).getPayload();

    validateExpirationDate(claims);

    String licenseStatus = claims.get(CLAIM_LICENSE_STATUS, String.class);
    String licenseTier = claims.get(CLAIM_LICENSE_TIER, String.class);
    String message = claims.get(CLAIM_MESSAGE, String.class);
    Date expiration = claims.getExpiration();

    return new LicenseValidationResult(licenseStatus, licenseTier, message, expiration);
  }
}
