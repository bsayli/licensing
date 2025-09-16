package io.github.bsayli.license.cli;

import static org.junit.jupiter.api.Assertions.*;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.regex.Pattern;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("it")
@DisplayName("Integration Test: LicenseKeyGeneratorCli")
class LicenseKeyGeneratorCliIT {

  private static final Pattern LICENSE_LINE = Pattern.compile("License Key:\\s*(\\S+)");
  private static final Pattern PREFIX_LINE = Pattern.compile("prefix\\s*:\\s*(\\S+)");
  private static final Pattern OPAQUE_LINE =
      Pattern.compile(
          "(?:opaque|opaquePayload\\(Base64URL\\))\\s*:\\s*(\\S+)", Pattern.CASE_INSENSITIVE);

  private static String extractFirst(Pattern p, String text, String errorIfMissing) {
    var m = p.matcher(text);
    if (m.find()) return (m.groupCount() >= 1 && m.group(1) != null) ? m.group(1) : m.group(0);
    fail(errorIfMissing + "\n--- OUTPUT ---\n" + text);
    return null; // unreachable
  }

  private static Path writeTempAesKeyFile() throws Exception {
    KeyGenerator kg = KeyGenerator.getInstance("AES");
    kg.init(256);
    SecretKey key = kg.generateKey();
    String b64 = Base64.getEncoder().encodeToString(key.getEncoded());
    Path tmp = Files.createTempFile("aes-", ".key");
    Files.writeString(tmp, b64);
    tmp.toFile().deleteOnExit();
    return tmp;
  }

  @Test
  @DisplayName("--userId + --secretKeyFile + --printSegments should emit key and segments (exit 0)")
  void generate_with_segments_ok() throws Exception {
    String userId = "11111111-1111-1111-1111-111111111111";
    Path keyFile = writeTempAesKeyFile();

    String out =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code =
                  LicenseKeyGeneratorCli.run(
                      new String[] {
                        "--userId", userId, "--secretKeyFile", keyFile.toString(), "--printSegments"
                      });
              assertEquals(0, code, "exit code should be 0");
            });

    String licenseKey = extractFirst(LICENSE_LINE, out, "License Key line not found");
    String prefix = extractFirst(PREFIX_LINE, out, "prefix line not found");
    String opaque = extractFirst(OPAQUE_LINE, out, "opaque line not found");

    assertNotNull(licenseKey);
    assertNotNull(prefix);
    assertNotNull(opaque);
    assertFalse(licenseKey.isBlank());
    assertFalse(prefix.isBlank());
    assertFalse(opaque.isBlank());
    assertEquals(prefix + "." + opaque, licenseKey, "License key must equal 'prefix.opaque'");
    assertTrue(
        opaque.matches("^[A-Za-z0-9_-]+$"), "Opaque payload must be URL-safe Base64 (no padding)");
  }

  @Test
  @DisplayName("missing args should exit 2 and print usage")
  void usage_errors_missing_args_exit_2() throws Exception {
    String out1 =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = LicenseKeyGeneratorCli.run(new String[] {});
              assertEquals(2, code);
            });
    assertTrue(out1.contains("Usage:"), "should print usage when missing args");
  }

  @Test
  @DisplayName("blank --userId should exit 2 and print usage (with valid --secretKeyFile)")
  void usage_errors_blank_userId_exit_2() throws Exception {
    Path keyFile = writeTempAesKeyFile();

    String out2 =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code =
                  LicenseKeyGeneratorCli.run(
                      new String[] {"--userId", "", "--secretKeyFile", keyFile.toString()});
              assertEquals(2, code);
            });
    assertTrue(out2.contains("must not be blank"), "should mention blank userId");
  }

  @Test
  @DisplayName("missing --secretKeyFile should exit 2 and print usage")
  void usage_errors_missing_secret_key_exit_2() throws Exception {
    String out =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code =
                  LicenseKeyGeneratorCli.run(
                      new String[] {"--userId", "11111111-1111-1111-1111-111111111111"});
              assertEquals(2, code);
            });
    assertTrue(out.contains("Missing --secretKeyFile"), "should mention missing secretKeyFile");
  }
}
