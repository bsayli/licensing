package io.github.bsayli.licensing.service.jwt.impl;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.licensing.common.i18n.LocalizedMessageResolver;
import io.github.bsayli.licensing.domain.model.LicenseStatus;
import io.jsonwebtoken.Claims;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: JwtServiceImpl")
class JwtServiceImplTest {

  private static KeyPair genEd25519() throws Exception {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
    return kpg.generateKeyPair();
  }

  private static String b64(byte[] in) {
    return Base64.getEncoder().encodeToString(in);
  }

  /** Basit bir resolver: verilen anahtar için "msg-" + key döndürür. */
  private static LocalizedMessageResolver dummyResolver() {
    return new LocalizedMessageResolver() {
      @Override
      public String getMessage(String messageKey) {
        return "msg-" + messageKey;
      }

      @Override
      public String getMessage(String messageKey, Object... args) {
        return "msg-" + messageKey;
      }

      @Override
      public String getMessage(String messageKey, java.util.Locale locale) {
        return "msg-" + messageKey;
      }

      @Override
      public String getMessage(String messageKey, java.util.Locale locale, Object... args) {
        return "msg-" + messageKey;
      }
    };
  }

  private static JwtServiceImpl svc(
      KeyPair kp, Duration ttl, Duration jitterMax, Clock clock, AtomicLong jitter)
      throws Exception {
    return new JwtServiceImpl(
        b64(kp.getPrivate().getEncoded()),
        b64(kp.getPublic().getEncoded()),
        ttl,
        jitterMax,
        dummyResolver(), // <-- yeni parametre
        clock,
        jitter::get,
        KeyFactory.getInstance("Ed25519"));
  }

  @Test
  @DisplayName("generate -> verify -> claims match (epoch seconds) + message claim")
  void generate_and_verify_now() throws Exception {
    KeyPair kp = genEd25519();
    long baseSec = Instant.now().getEpochSecond() + 1;
    Instant now = Instant.ofEpochSecond(baseSec);
    Clock clk = Clock.fixed(now, ZoneOffset.UTC);
    AtomicLong jitter = new AtomicLong(0);
    JwtServiceImpl s = svc(kp, Duration.ofMinutes(10), Duration.ofSeconds(5), clk, jitter);

    String token = s.generateToken("client-1", "PRO", LicenseStatus.ACTIVE);
    Claims c = s.verifyAndExtractJwtClaims(token);

    assertEquals("client-1", c.getSubject());
    assertEquals("PRO", c.get("licenseTier"));
    assertEquals("ACTIVE", c.get("licenseStatus"));

    // message claim: resolver "msg-license.message.active" döndürür
    assertEquals("msg-license.message.active", c.get("message"));

    assertEquals(now.getEpochSecond(), c.getIssuedAt().toInstant().getEpochSecond());
    assertEquals(
        now.plus(Duration.ofMinutes(10)).getEpochSecond(),
        c.getExpiration().toInstant().getEpochSecond());
  }

  @Test
  @DisplayName("validateTokenFormat: accepts valid EdDSA token, rejects malformed and wrong alg")
  void validate_format() throws Exception {
    KeyPair kp = genEd25519();
    Instant now = Instant.ofEpochSecond(Instant.now().getEpochSecond() + 1);
    Clock clk = Clock.fixed(now, ZoneOffset.UTC);
    JwtServiceImpl s = svc(kp, Duration.ofMinutes(5), Duration.ZERO, clk, new AtomicLong(0));

    String token = s.generateToken("c", "BASIC", LicenseStatus.INACTIVE);
    assertTrue(s.validateTokenFormat(token));

    assertFalse(s.validateTokenFormat(null));
    assertFalse(s.validateTokenFormat("a.b"));
    assertFalse(s.validateTokenFormat("**.**.**"));

    String header =
        Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"HS256\"}".getBytes());
    String payload = Base64.getUrlEncoder().withoutPadding().encodeToString("{}".getBytes());
    String sig = Base64.getUrlEncoder().withoutPadding().encodeToString("x".getBytes());
    assertFalse(s.validateTokenFormat(header + "." + payload + "." + sig));
  }

  @Test
  @DisplayName("expiry uses jitter clamped to max and non-negative (epoch seconds)")
  void expiry_with_jitter_clamped_now() throws Exception {
    KeyPair kp = genEd25519();
    Instant now = Instant.ofEpochSecond(Instant.now().getEpochSecond() + 1);
    Clock clk = Clock.fixed(now, ZoneOffset.UTC);

    AtomicLong jitterBig = new AtomicLong(Duration.ofMinutes(5).toMillis());
    JwtServiceImpl s1 = svc(kp, Duration.ofMinutes(10), Duration.ofMinutes(2), clk, jitterBig);
    Claims c1 = s1.verifyAndExtractJwtClaims(s1.generateToken("x", "TIER", LicenseStatus.ACTIVE));
    assertEquals(
        now.plus(Duration.ofMinutes(12)).getEpochSecond(),
        c1.getExpiration().toInstant().getEpochSecond());

    AtomicLong jitterNeg = new AtomicLong(-1000L);
    JwtServiceImpl s2 = svc(kp, Duration.ofMinutes(10), Duration.ofMinutes(2), clk, jitterNeg);
    Claims c2 = s2.verifyAndExtractJwtClaims(s2.generateToken("y", "TIER", LicenseStatus.ACTIVE));
    assertEquals(
        now.plus(Duration.ofMinutes(10)).getEpochSecond(),
        c2.getExpiration().toInstant().getEpochSecond());
  }
}
