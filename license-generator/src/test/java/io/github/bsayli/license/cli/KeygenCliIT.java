package io.github.bsayli.license.cli;

import static org.junit.jupiter.api.Assertions.*;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import java.util.Base64;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("it")
@DisplayName("Integration Test: KeygenCli")
class KeygenCliIT {

  private static final Pattern AES_LINE =
      Pattern.compile("AES-(128|192|256) SecretKey \\(Base64\\):\\s*(\\S+)");
  private static final Pattern PUB_LINE =
      Pattern.compile("Ed25519 PublicKey\\s*\\(Base64\\):\\s*(\\S+)");
  private static final Pattern PRIV_LINE =
      Pattern.compile("Ed25519 PrivateKey\\s*\\(Base64\\):\\s*(\\S+)");

  private static boolean isBase64(String s) {
    try {
      Base64.getDecoder().decode(s);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Test
  @DisplayName("--mode aes (default 256) should print Base64 key and exit 0")
  void aes_default_256_ok() throws Exception {
    String out =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = KeygenCli.run(new String[] {"--mode", "aes"});
              assertEquals(0, code);
            });

    var m = AES_LINE.matcher(out);
    assertTrue(m.find(), "AES output not found");
    assertEquals("256", m.group(1));
    assertTrue(isBase64(m.group(2)));
  }

  @Test
  @DisplayName("--mode aes --size 128 and 192 should work")
  void aes_explicit_sizes_ok() throws Exception {
    String out128 =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = KeygenCli.run(new String[] {"--mode", "aes", "--size", "128"});
              assertEquals(0, code);
            });
    var m128 = AES_LINE.matcher(out128);
    assertTrue(m128.find());
    assertEquals("128", m128.group(1));
    assertTrue(isBase64(m128.group(2)));

    String out192 =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = KeygenCli.run(new String[] {"--mode", "aes", "--size", "192"});
              assertEquals(0, code);
            });
    var m192 = AES_LINE.matcher(out192);
    assertTrue(m192.find());
    assertEquals("192", m192.group(1));
    assertTrue(isBase64(m192.group(2)));
  }

  @Test
  @DisplayName("--mode ed25519 should print public and private keys and exit 0")
  void ed25519_ok() throws Exception {
    String out =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = KeygenCli.run(new String[] {"--mode", "ed25519"});
              assertEquals(0, code);
            });

    var mpub = PUB_LINE.matcher(out);
    var mprv = PRIV_LINE.matcher(out);
    assertTrue(mpub.find(), "Public key line not found");
    assertTrue(mprv.find(), "Private key line not found");
    assertTrue(isBase64(mpub.group(1)));
    assertTrue(isBase64(mprv.group(1)));
  }

  @Test
  @DisplayName("invalid mode or size should exit 2 and print usage")
  void usage_errors_exit_2() throws Exception {
    String outMode =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = KeygenCli.run(new String[] {"--mode", "rsa"});
              assertEquals(2, code);
            });
    assertTrue(outMode.contains("Usage:"));

    String outSize =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = KeygenCli.run(new String[] {"--mode", "aes", "--size", "111"});
              assertEquals(2, code);
            });
    assertTrue(outSize.contains("Usage:"));
    assertTrue(outSize.contains("--size must be one of"));
  }

  @Test
  @DisplayName("--help should print usage and exit 0")
  void help_exit_0() throws Exception {
    String out =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = KeygenCli.run(new String[] {"--help"});
              assertEquals(0, code);
            });
    assertTrue(out.contains("Usage:"));
  }
}
