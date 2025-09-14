package io.github.bsayli.license.cli;

import static org.junit.jupiter.api.Assertions.*;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("it")
@DisplayName("Integration Test: EncryptUserIdCli")
class EncryptUserIdCliIT {

  private static final Pattern ENC_LINE = Pattern.compile("Encrypted userId:\\s*(\\S+)");
  private static final Pattern DEC_LINE = Pattern.compile("Decrypted userId:\\s*(\\S+)");

  private static String extractFirst(Pattern p, String text, String errorIfMissing) {
    var m = p.matcher(text);
    if (m.find()) return m.groupCount() >= 1 && m.group(1) != null ? m.group(1) : m.group(0);
    fail(errorIfMissing + "\n--- OUTPUT ---\n" + text);
    return null;
  }

  @Test
  @DisplayName("encrypt -> decrypt roundtrip should succeed (exit 0)")
  void encrypt_then_decrypt_ok() throws Exception {
    String userId = "11111111-1111-1111-1111-111111111111";

    String encOut =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = EncryptUserIdCli.run(new String[] {"encrypt", "--userId", userId});
              assertEquals(0, code);
            });
    String ciphertext = extractFirst(ENC_LINE, encOut, "Encrypted line not found");

    String decOut =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = EncryptUserIdCli.run(new String[] {"decrypt", "--ciphertext", ciphertext});
              assertEquals(0, code);
            });
    String plain = extractFirst(DEC_LINE, decOut, "Decrypted line not found");

    assertEquals(userId, plain, "roundtrip should return original userId");
  }

  @Test
  @DisplayName("missing command or args should exit 2 and print usage")
  void usage_errors_exit_2() throws Exception {
    String out1 =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = EncryptUserIdCli.run(new String[] {});
              assertEquals(2, code);
            });
    assertTrue(out1.contains("Usage:"));

    String out2 =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = EncryptUserIdCli.run(new String[] {"encrypt"});
              assertEquals(2, code);
            });
    assertTrue(out2.contains("Usage:"));

    String out3 =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = EncryptUserIdCli.run(new String[] {"decrypt", "--ciphertext", ""});
              assertEquals(2, code);
            });
    assertTrue(out3.contains("must not be blank"));
  }

  @Test
  @DisplayName("--help should print usage and exit 0")
  void help_exit_0() throws Exception {
    String out =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = EncryptUserIdCli.run(new String[] {"--help"});
              assertEquals(0, code);
            });
    assertTrue(out.contains("Usage:"));
  }
}
