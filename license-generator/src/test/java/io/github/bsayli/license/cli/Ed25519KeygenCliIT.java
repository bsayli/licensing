package io.github.bsayli.license.cli;

import static org.junit.jupiter.api.Assertions.*;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("it")
@DisplayName("Integration Test: Ed25519KeygenCli")
class Ed25519KeygenCliIT {

  private static final Pattern PUB_LINE =
      Pattern.compile("Public\\s*\\(SPKI, X\\.509\\):\\s*(\\S+)");
  private static final Pattern PRIV_LINE =
      Pattern.compile("Private\\s*\\(PKCS#8\\)\\s*:\\s*(\\S+)");

  private static boolean isBase64(String s) {
    try {
      Base64.getDecoder().decode(s);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private static String extractFirst(Pattern p, String text, String errorIfMissing) {
    var m = p.matcher(text);
    if (m.find()) return m.groupCount() >= 1 && m.group(1) != null ? m.group(1) : m.group(0);
    fail(errorIfMissing + "\n--- OUTPUT ---\n" + text);
    return null;
  }

  @Test
  @DisplayName("no args → prints Base64 keys (exit 0)")
  void print_keys_ok() throws Exception {
    String out =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = Ed25519KeygenCli.run(new String[] {});
              assertEquals(0, code);
            });

    String pub = extractFirst(PUB_LINE, out, "Public key line not found");
    String priv = extractFirst(PRIV_LINE, out, "Private key line not found");

    assertTrue(isBase64(pub), "public key must be Base64");
    assertTrue(isBase64(priv), "private key must be Base64");
  }

  @Test
  @DisplayName("--outPublic/--outPrivate → writes files and logs paths (exit 0)")
  void write_files_ok() throws Exception {
    Path tmpDir = Files.createTempDirectory("ed25519-it-");
    Path pubFile = tmpDir.resolve("pub.b64");
    Path privFile = tmpDir.resolve("priv.b64");

    String out =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code =
                  Ed25519KeygenCli.run(
                      new String[] {
                        "--outPublic", pubFile.toString(),
                        "--outPrivate", privFile.toString()
                      });
              assertEquals(0, code);
            });

    assertTrue(Files.exists(pubFile), "public file should exist");
    assertTrue(Files.exists(privFile), "private file should exist");

    String pub = Files.readString(pubFile).trim();
    String priv = Files.readString(privFile).trim();

    assertTrue(isBase64(pub));
    assertTrue(isBase64(priv));
    assertTrue(out.contains("Written:"));
    assertTrue(out.contains(pubFile.toString()));
    assertTrue(out.contains(privFile.toString()));
  }

  @Test
  @DisplayName("--help prints usage and exits 0")
  void help_ok() throws Exception {
    String out =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = Ed25519KeygenCli.run(new String[] {"--help"});
              assertEquals(0, code);
            });
    assertTrue(out.contains("Usage:"));
  }
}
