package io.github.bsayli.license.token.extractor;

import static io.github.bsayli.license.common.CryptoConstants.B64_DEC;
import static io.github.bsayli.license.common.CryptoConstants.EDDSA_BC_ALGO;
import static io.github.bsayli.license.common.LicenseConstants.*;

import io.github.bsayli.license.token.extractor.model.LicenseValidationResult;
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
import java.util.Date;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Verifies and extracts license-related claims from a JWT (EdDSA / Ed25519).
 *
 * <p>Usage:
 *
 * <pre>{@code
 * JwtTokenExtractor extractor = new JwtTokenExtractor(base64SpkiPublicKey);
 * LicenseValidationResult r = extractor.validateAndGetToken(jwt);
 * }</pre>
 *
 * Throws {@link ExpiredJwtException} when the token is expired, and a generic {@link JwtException}
 * for signature/format issues.
 */
public class JwtTokenExtractor {

  private static final String BC_PROVIDER = BouncyCastleProvider.PROVIDER_NAME;

  // ---- Error messages ----
  private static final String ERR_NULL_OR_BLANK_KEY = "Public key must not be null/blank";
  private static final String ERR_INVALID_KEY = "Invalid EdDSA public key (Base64 SPKI expected)";
  private static final String ERR_NULL_OR_BLANK_TOKEN = "JWT token must not be null/blank";
  private static final String ERR_TOKEN_EXPIRED = "Token has expired";

  static {
    // Add BC provider if not present (idempotent)
    if (Security.getProvider(BC_PROVIDER) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  private final PublicKey publicKey;

  /**
   * @param publicKeyBase64 Base64-encoded SubjectPublicKeyInfo (SPKI) Ed25519 public key
   * @throws IllegalArgumentException if the key is null/blank or cannot be parsed
   */
  public JwtTokenExtractor(String publicKeyBase64) {
    if (publicKeyBase64 == null || publicKeyBase64.isBlank()) {
      throw new IllegalArgumentException(ERR_NULL_OR_BLANK_KEY);
    }
    try {
      KeyFactory keyFactory = KeyFactory.getInstance(EDDSA_BC_ALGO, BC_PROVIDER);
      byte[] der = B64_DEC.decode(publicKeyBase64);
      this.publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(der));
    } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
      throw new IllegalArgumentException(ERR_INVALID_KEY, e);
    }
  }

  private static void validateExpirationDate(Claims claims) {
    Date exp = claims.getExpiration();
    if (exp != null && exp.before(Date.from(Instant.now()))) {
      // supply the claims so upstream can inspect if needed
      throw new ExpiredJwtException(null, claims, ERR_TOKEN_EXPIRED);
    }
  }

  /**
   * Parses and verifies the JWT signature, checks {@code exp}, and returns a compact result.
   *
   * @param token compact JWS string
   * @return extracted fields useful for license validation flows
   * @throws ExpiredJwtException if token is expired
   * @throws JwtException for invalid signature/format/claims
   */
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
