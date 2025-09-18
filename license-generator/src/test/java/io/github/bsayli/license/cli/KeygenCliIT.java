package io.github.bsayli.license.cli;

import static org.junit.jupiter.api.Assertions.*;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Tag("it")
@DisplayName("Integration Test: KeygenCli (file outputs)")
class KeygenCliIT {

  private static final String AES_FILE = "aes.key";

  private static final String SIGN_PUB_FILE = "signature.public.key";
  private static final String SIGN_PRIV_FILE = "signature.private.key";

  private static final String JWT_PUB_FILE = "jwt.public.key";
  private static final String JWT_PRIV_FILE = "jwt.private.key";

  private static boolean isBase64(String s) {
    try {
      Base64.getDecoder().decode(s);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private static String readTrim(Path p) throws Exception {
    return Files.readString(p).trim();
  }

  @Test
  @DisplayName("--mode aes writes aes.key (256-bit by default) and exits 0")
  void aes_default256_writesFile_ok(@TempDir Path dir) throws Exception {
    String out =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = KeygenCli.run(new String[] {"--mode", "aes", "--dir", dir.toString()});
              assertEquals(0, code);
            });

    Path aes = dir.resolve(AES_FILE);
    assertTrue(Files.exists(aes), "aes.key should be created");

    String b64 = readTrim(aes);
    assertTrue(isBase64(b64), "aes.key should contain Base64");
    byte[] raw = Base64.getDecoder().decode(b64);
    assertEquals(32, raw.length, "Default AES key should be 256-bit (32 bytes)");

    assertTrue(out.contains("AES-256 key written to"), "should log file path");
  }

  @Test
  @DisplayName("--mode aes --size 128 and 192 write correct key sizes")
  void aes_explicitSizes_writeCorrectLengths(@TempDir Path dir) throws Exception {
    // 128-bit
    {
      Path d = Files.createDirectory(dir.resolve("k128"));
      int code =
          KeygenCli.run(new String[] {"--mode", "aes", "--size", "128", "--dir", d.toString()});
      assertEquals(0, code);

      String b64 = readTrim(d.resolve(AES_FILE));
      byte[] raw = Base64.getDecoder().decode(b64);
      assertEquals(16, raw.length, "AES-128 should be 16 bytes");
    }

    // 192-bit
    {
      Path d = Files.createDirectory(dir.resolve("k192"));
      int code =
          KeygenCli.run(new String[] {"--mode", "aes", "--size", "192", "--dir", d.toString()});
      assertEquals(0, code);

      String b64 = readTrim(d.resolve(AES_FILE));
      byte[] raw = Base64.getDecoder().decode(b64);
      assertEquals(24, raw.length, "AES-192 should be 24 bytes");
    }
  }

  @Test
  @DisplayName("--mode ed25519 (default purpose=signature) writes signature.* files and exits 0")
  void ed25519_signature_default_writesFiles_ok(@TempDir Path dir) throws Exception {
    String out =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = KeygenCli.run(new String[] {"--mode", "ed25519", "--dir", dir.toString()});
              assertEquals(0, code);
            });

    Path pub = dir.resolve(SIGN_PUB_FILE);
    Path prv = dir.resolve(SIGN_PRIV_FILE);

    assertTrue(Files.exists(pub), "signature.public.key should exist");
    assertTrue(Files.exists(prv), "signature.private.key should exist");

    String pubB64 = readTrim(pub);
    String prvB64 = readTrim(prv);
    assertTrue(isBase64(pubB64), "public key should be Base64 (SPKI)");
    assertTrue(isBase64(prvB64), "private key should be Base64 (PKCS#8)");

    assertTrue(out.contains("Signature public key"), "should log signature public key write");
    assertTrue(out.contains("Signature private key"), "should log signature private key write");
  }

  @Test
  @DisplayName("--mode ed25519 --purpose jwt writes jwt.* files and exits 0")
  void ed25519_jwt_purpose_writesFiles_ok(@TempDir Path dir) throws Exception {
    String out =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code =
                  KeygenCli.run(
                      new String[] {
                        "--mode", "ed25519", "--purpose", "jwt", "--dir", dir.toString()
                      });
              assertEquals(0, code);
            });

    Path pub = dir.resolve(JWT_PUB_FILE);
    Path prv = dir.resolve(JWT_PRIV_FILE);

    assertTrue(Files.exists(pub), "jwt.public.key should exist");
    assertTrue(Files.exists(prv), "jwt.private.key should exist");

    String pubB64 = readTrim(pub);
    String prvB64 = readTrim(prv);
    assertTrue(
        isBase64(pubB64),
        "jwt public key should be Base64 (SPKI)"); // typo? ensure correct variable
    assertTrue(isBase64(prvB64), "jwt private key should be Base64 (PKCS#8)");

    assertTrue(out.contains("JWT public key"), "should log jwt public key write");
    assertTrue(out.contains("JWT private key"), "should log jwt private key write");
  }

  @Test
  @DisplayName("invalid mode/size/purpose should exit 2 and print usage")
  void usage_errors_exit2(@TempDir Path dir) throws Exception {
    String outMode =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = KeygenCli.run(new String[] {"--mode", "rsa", "--dir", dir.toString()});
              assertEquals(2, code);
            });
    assertTrue(outMode.contains("Usage:"));

    String outSize =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code =
                  KeygenCli.run(
                      new String[] {"--mode", "aes", "--size", "111", "--dir", dir.toString()});
              assertEquals(2, code);
            });
    assertTrue(outSize.contains("Usage:"));
    assertTrue(outSize.contains("--size must be one of"));

    String outPurpose =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code =
                  KeygenCli.run(
                      new String[] {
                        "--mode", "ed25519", "--purpose", "foo", "--dir", dir.toString()
                      });
              assertEquals(2, code);
            });
    assertTrue(outPurpose.contains("Usage:"), "invalid purpose should print usage");
    assertTrue(outPurpose.contains("--purpose (expected: signature | jwt)"));
  }

  @Test
  @DisplayName("missing --dir should exit 2 and print usage")
  void missing_dir_exit2() throws Exception {
    String out =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = KeygenCli.run(new String[] {"--mode", "aes"});
              assertEquals(2, code);
            });
    assertTrue(out.contains("Missing --dir <directory>"), "should mention missing --dir");
  }

  @Test
  @DisplayName("--help prints usage and exits 0")
  void help_exit0() throws Exception {
    String out =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = KeygenCli.run(new String[] {"--help"});
              assertEquals(0, code);
            });
    assertTrue(out.contains("Usage:"));
  }
}
