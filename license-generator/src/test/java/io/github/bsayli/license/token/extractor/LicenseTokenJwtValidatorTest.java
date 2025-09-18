package io.github.bsayli.license.token.extractor;

import static io.github.bsayli.license.common.LicenseConstants.*;
import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.license.token.extractor.model.LicenseValidationResult;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: JwtTokenExtractor")
class LicenseTokenJwtValidatorTest {

  private static KeyPair ed25519() throws Exception {
    return KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
  }

  @Test
  @DisplayName("Valid Ed25519-signed token should be verified and claims extracted")
  void validate_happyPath_ok() throws Exception {
    KeyPair kp = ed25519();

    Instant now = Instant.now();
    Instant exp = now.plus(5, ChronoUnit.MINUTES);

    String token =
        Jwts.builder()
            .subject("test-user-123")
            .claim(CLAIM_LICENSE_STATUS, "Active")
            .claim(CLAIM_LICENSE_TIER, "Professional")
            .claim(CLAIM_MESSAGE, "OK")
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .signWith(kp.getPrivate(), Jwts.SIG.EdDSA)
            .compact();

    String pubSpkiBase64 = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
    LicenseTokenJwtValidator extractor = new LicenseTokenJwtValidator(pubSpkiBase64);

    LicenseValidationResult res = extractor.validateAndGetToken(token);

    assertNotNull(res);
    assertEquals("Active", res.licenseStatus());
    assertEquals("Professional", res.licenseTier());
    assertEquals("OK", res.message());
    assertNotNull(res.expirationDate());
    assertTrue(res.expirationDate().after(Date.from(now)));
  }

  @Test
  @DisplayName("Expired token should raise ExpiredJwtException")
  void validate_expiredToken_shouldThrow() throws Exception {
    KeyPair kp = ed25519();

    Instant now = Instant.now();
    Instant exp = now.minus(1, ChronoUnit.MINUTES);

    String token =
        Jwts.builder()
            .subject("test-user-123")
            .claim(CLAIM_LICENSE_STATUS, "Active")
            .claim(CLAIM_LICENSE_TIER, "Professional")
            .issuedAt(Date.from(now.minus(2, ChronoUnit.MINUTES)))
            .expiration(Date.from(exp))
            .signWith(kp.getPrivate(), Jwts.SIG.EdDSA)
            .compact();

    String pubSpkiBase64 = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
    LicenseTokenJwtValidator extractor = new LicenseTokenJwtValidator(pubSpkiBase64);

    assertThrows(ExpiredJwtException.class, () -> extractor.validateAndGetToken(token));
  }

  @Test
  @DisplayName("Blank public key should throw IllegalArgumentException at construction")
  void constructor_blankKey_shouldThrow() {
    assertThrows(IllegalArgumentException.class, () -> new LicenseTokenJwtValidator("  "));
    assertThrows(IllegalArgumentException.class, () -> new LicenseTokenJwtValidator(null));
  }

  @Test
  @DisplayName("Blank token should throw IllegalArgumentException")
  void validate_blankToken_shouldThrow() throws Exception {
    KeyPair kp = ed25519();
    String pubSpkiBase64 = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
    LicenseTokenJwtValidator extractor = new LicenseTokenJwtValidator(pubSpkiBase64);

    assertThrows(IllegalArgumentException.class, () -> extractor.validateAndGetToken(" "));
    assertThrows(IllegalArgumentException.class, () -> extractor.validateAndGetToken(null));
  }

  @Test
  @DisplayName("Token signed with a different key should fail verification (JwtException)")
  void validate_wrongKey_shouldThrow() throws Exception {
    KeyPair signer = ed25519();
    KeyPair verifier = ed25519();

    String token =
        Jwts.builder()
            .subject("user-x")
            .claim(CLAIM_LICENSE_STATUS, "Active")
            .expiration(Date.from(Instant.now().plus(5, ChronoUnit.MINUTES)))
            .signWith(signer.getPrivate(), Jwts.SIG.EdDSA)
            .compact();

    String wrongPubSpki = Base64.getEncoder().encodeToString(verifier.getPublic().getEncoded());
    LicenseTokenJwtValidator extractor = new LicenseTokenJwtValidator(wrongPubSpki);

    assertThrows(JwtException.class, () -> extractor.validateAndGetToken(token));
  }
}
