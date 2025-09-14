package io.github.bsayli.license.signature;

import static org.junit.jupiter.api.Assertions.*;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

@Tag("unit")
@DisplayName("Unit Test: SignatureDemo")
class SignatureDemoTest {

  private static final Pattern JSON_ANY = Pattern.compile("\\{[\\s\\S]*?\\}", Pattern.DOTALL);
  private static final Pattern SIG_ANY = Pattern.compile("Signature \\(Base64\\):\\s*(\\S+)");

  private static KeyPair ed25519() throws Exception {
    return KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
  }

  private static String b64(byte[] bytes) {
    return Base64.getEncoder().encodeToString(bytes);
  }

  private static ListAppender<ILoggingEvent> attachAppender() {
    Logger logger = (Logger) LoggerFactory.getLogger(SignatureDemo.class);
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);
    return appender;
  }

  private static String logsToString(ListAppender<ILoggingEvent> appender) {
    StringBuilder sb = new StringBuilder();
    for (ILoggingEvent e : appender.list) sb.append(e.getFormattedMessage()).append('\n');
    return sb.toString();
  }

  @Test
  @DisplayName("sign-sample-key: returns 0, logs JSON with encryptedLicenseKeyHash and signature")
  void sign_sample_key_ok() throws Exception {
    var kp = ed25519();
    String privB64 = b64(kp.getPrivate().getEncoded());

    var app = attachAppender();
    int code =
        SignatureDemo.run(new String[] {"--mode", "sign-sample-key", "--privateKey", privB64});
    String out = logsToString(app);

    assertEquals(0, code);
    assertTrue(out.contains("Signed payload JSON"));
    assertTrue(JSON_ANY.matcher(out).find());
    var m = SIG_ANY.matcher(out);
    assertTrue(m.find());
    assertDoesNotThrow(() -> Base64.getDecoder().decode(m.group(1)));
    assertTrue(out.contains("\"encryptedLicenseKeyHash\""));
    assertFalse(out.contains("\"licenseTokenHash\""));
  }

  @Test
  @DisplayName("sign-sample-token: returns 0, logs JSON with licenseTokenHash and signature")
  void sign_sample_token_ok() throws Exception {
    var kp = ed25519();
    String privB64 = b64(kp.getPrivate().getEncoded());

    var app = attachAppender();
    int code =
        SignatureDemo.run(new String[] {"--mode", "sign-sample-token", "--privateKey", privB64});
    String out = logsToString(app);

    assertEquals(0, code);
    assertTrue(out.contains("Signed payload JSON"));
    assertTrue(JSON_ANY.matcher(out).find());
    var m = SIG_ANY.matcher(out);
    assertTrue(m.find());
    assertDoesNotThrow(() -> Base64.getDecoder().decode(m.group(1)));
    assertTrue(out.contains("\"licenseTokenHash\""));
    assertFalse(out.contains("\"encryptedLicenseKeyHash\""));
  }

  @Test
  @DisplayName("missing args: returns 2 and prints usage")
  void missing_args_exit_2() {
    var app = attachAppender();
    int code = SignatureDemo.run(new String[] {});
    String out = logsToString(app);

    assertEquals(2, code);
    assertTrue(out.contains("Usage:"));
  }

  @Test
  @DisplayName("invalid mode: returns 2, logs error and usage")
  void invalid_mode_exit_2() throws Exception {
    var kp = ed25519();
    String privB64 = b64(kp.getPrivate().getEncoded());

    var app = attachAppender();
    int code = SignatureDemo.run(new String[] {"--mode", "bad-mode", "--privateKey", privB64});
    String out = logsToString(app);

    assertEquals(2, code);
    assertTrue(out.contains("Invalid --mode"));
    assertTrue(out.contains("Usage:"));
  }
}
