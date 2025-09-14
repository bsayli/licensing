package io.github.bsayli.license.cli;

import static org.junit.jupiter.api.Assertions.*;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("it")
@DisplayName("Integration Test: LicenseTokenCli")
class LicenseTokenCliIT {

  @Test
  @DisplayName("valid EdDSA token should pass validation (exit 0)")
  void valid_token_ok() throws Exception {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
    KeyPair kp = kpg.generateKeyPair();

    String token =
        Jwts.builder()
            .subject("user123")
            .claim("licenseStatus", "ACTIVE")
            .claim("licenseTier", "PRO")
            .claim("message", "ok")
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plusSeconds(3600)))
            .signWith(kp.getPrivate())
            .compact();

    String pubB64 = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());

    String out =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code =
                  LicenseTokenCli.run(new String[] {"--publicKey", pubB64, "--token", token});
              assertEquals(0, code, "exit code should be 0 for valid token");
            });

    assertTrue(out.contains("License token is VALID"));
    assertTrue(out.contains("status"));
    assertTrue(out.contains("tier"));
    assertTrue(out.contains("expiration"));
  }

  @Test
  @DisplayName("expired token should exit with 4 and log EXPIRED")
  void expired_token_exits_4() throws Exception {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
    KeyPair kp = kpg.generateKeyPair();

    String expired =
        Jwts.builder()
            .subject("user123")
            .claim("licenseStatus", "ACTIVE")
            .claim("licenseTier", "PRO")
            .issuedAt(Date.from(Instant.now().minusSeconds(7200)))
            .expiration(Date.from(Instant.now().minusSeconds(3600)))
            .signWith(kp.getPrivate())
            .compact();

    String pubB64 = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());

    String out =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code =
                  LicenseTokenCli.run(new String[] {"--publicKey", pubB64, "--token", expired});
              assertEquals(4, code, "expired token should exit with 4");
            });

    assertTrue(out.contains("EXPIRED"));
  }

  @Test
  @DisplayName("usage errors: missing args should exit with 2 and print usage")
  void usage_errors_exit_2() throws Exception {
    String out1 =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = LicenseTokenCli.run(new String[] {"--token", "x"});
              assertEquals(2, code);
            });
    assertTrue(out1.contains("Usage:"));

    String out2 =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = LicenseTokenCli.run(new String[] {"--publicKey", "y"});
              assertEquals(2, code);
            });
    assertTrue(out2.contains("Usage:"));
  }
}
