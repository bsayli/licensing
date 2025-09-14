package io.github.bsayli.license.cli;

import static org.junit.jupiter.api.Assertions.*;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("it")
@DisplayName("Integration Test: LicenseKeyGeneratorCli")
class LicenseKeyGeneratorCliIT {

  private static final Pattern LICENSE_LINE = Pattern.compile("License Key:\\s*(\\S+)");
  private static final Pattern PREFIX_LINE = Pattern.compile("prefix\\s*:\\s*(\\S+)");
  private static final Pattern RANDOM_LINE = Pattern.compile("randomString\\s*:\\s*(\\S+)");
  private static final Pattern ENCUID_LINE = Pattern.compile("encryptedUserId\\s*:\\s*(\\S+)");

  private static String extractFirst(Pattern p, String text, String errorIfMissing) {
    var m = p.matcher(text);
    if (m.find()) return m.groupCount() >= 1 && m.group(1) != null ? m.group(1) : m.group(0);
    fail(errorIfMissing + "\n--- OUTPUT ---\n" + text);
    return null;
  }

  @Test
  @DisplayName("--userId with --printSegments should emit key and segments (exit 0)")
  void generate_with_segments_ok() throws Exception {
    String userId = "11111111-1111-1111-1111-111111111111";

    String out =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code =
                  LicenseKeyGeneratorCli.run(new String[] {"--userId", userId, "--printSegments"});
              assertEquals(0, code, "exit code should be 0");
            });

    String licenseKey = extractFirst(LICENSE_LINE, out, "License Key line not found");
    String prefix = extractFirst(PREFIX_LINE, out, "prefix line not found");
    String random = extractFirst(RANDOM_LINE, out, "randomString line not found");
    String encUserId = extractFirst(ENCUID_LINE, out, "encryptedUserId line not found");

    assertNotNull(licenseKey);
    assertTrue(licenseKey.startsWith(prefix + "~"), "license should start with prefix + '~'");
    assertTrue(licenseKey.contains("~" + random + "~"), "license should contain random segment");
    assertNotNull(encUserId);
    assertTrue(licenseKey.endsWith(encUserId), "license should end with encryptedUserId");
    assertEquals("BSAYLI", prefix, "prefix should be BSAYLI");
    assertNotNull(random);
    assertFalse(random.isBlank());
    assertFalse(encUserId.isBlank());
  }

  @Test
  @DisplayName("missing/blank --userId should exit 2 and print usage")
  void usage_errors_exit_2() throws Exception {
    String out1 =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = LicenseKeyGeneratorCli.run(new String[] {});
              assertEquals(2, code);
            });
    assertTrue(out1.contains("Usage:"), "should print usage when missing args");

    String out2 =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = LicenseKeyGeneratorCli.run(new String[] {"--userId", ""});
              assertEquals(2, code);
            });
    assertTrue(out2.contains("must not be blank"), "should mention blank userId");
  }
}
