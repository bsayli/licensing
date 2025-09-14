package io.github.bsayli.license.cli;

import static org.junit.jupiter.api.Assertions.*;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import java.util.Base64;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("it")
@DisplayName("Integration Test: SignatureCli")
class SignatureCliIT {

  private static final Pattern JSON_ANY = Pattern.compile("\\{[^\\r\\n]*\\}");
  private static final Pattern SIG_ANY = Pattern.compile("Signature \\(Base64\\):\\s*(\\S+)");

  private static String extractFirst(Pattern p, String text, String errorIfMissing) {
    var m = p.matcher(text);
    if (m.find()) return m.groupCount() >= 1 && m.group(1) != null ? m.group(1) : m.group(0);
    fail(errorIfMissing + "\n--- OUTPUT ---\n" + text);
    return null;
  }

  @Test
  @DisplayName("sign (licenseKey) → verify should succeed (exit 0)")
  void sign_then_verify_ok() throws Exception {
    var kpg = java.security.KeyPairGenerator.getInstance("Ed25519");
    var kp = kpg.generateKeyPair();
    String privB64 = Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded());
    String pubB64 = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());

    String[] signArgs = {
      "--mode", "sign",
      "--serviceId", "crm",
      "--serviceVersion", "1.2.3",
      "--instanceId", "crm~mac~00:11:22:33:44:55",
      "--licenseKey", "BSAYLI~RANDOM~encUserId",
      "--privateKey", privB64
    };

    String signOut =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = SignatureCli.run(signArgs);
              assertEquals(0, code, "sign exit code must be 0");
            });
    assertTrue(signOut.contains("Signed payload JSON:"), "sign output should contain header");

    String json = extractFirst(JSON_ANY, signOut, "JSON payload not found in output");
    String signatureB64 = extractFirst(SIG_ANY, signOut, "Signature not found in output");

    String[] verifyArgs = {
      "--mode", "verify",
      "--dataJson", json,
      "--signatureB64", signatureB64,
      "--publicKey", pubB64
    };

    String verifyOut =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = SignatureCli.run(verifyArgs);
              assertEquals(0, code, "verify exit code must be 0");
            });
    assertTrue(verifyOut.contains("Signature is VALID"), "verify should log VALID");
  }

  @Test
  @DisplayName("verify with wrong public key should fail (exit 4)")
  void verify_with_wrong_pubkey_fails() throws Exception {
    var kpg = java.security.KeyPairGenerator.getInstance("Ed25519");
    var kp = kpg.generateKeyPair();
    String privB64 = Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded());

    String[] signArgs = {
      "--mode", "sign",
      "--serviceId", "svc",
      "--serviceVersion", "2.0.0",
      "--instanceId", "svc~node~aa:bb:cc:dd:ee:ff",
      "--licenseKey", "BSAYLI~X~enc",
      "--privateKey", privB64
    };

    String signOut =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = SignatureCli.run(signArgs);
              assertEquals(0, code);
            });

    String json = extractFirst(JSON_ANY, signOut, "JSON payload not found");
    String sig = extractFirst(SIG_ANY, signOut, "Signature not found");

    var wrong = kpg.generateKeyPair();
    String wrongPubB64 = Base64.getEncoder().encodeToString(wrong.getPublic().getEncoded());

    String[] verifyArgs = {
      "--mode", "verify",
      "--dataJson", json,
      "--signatureB64", sig,
      "--publicKey", wrongPubB64
    };

    SystemLambda.tapSystemOutNormalized(
        () -> {
          int code = SignatureCli.run(verifyArgs);
          assertEquals(4, code, "verify with wrong pubkey should exit with 4");
        });
  }
}
