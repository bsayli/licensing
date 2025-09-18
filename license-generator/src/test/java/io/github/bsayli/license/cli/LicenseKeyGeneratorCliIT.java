package io.github.bsayli.license.cli;

import static org.junit.jupiter.api.Assertions.*;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("it")
@DisplayName("Integration Test: LicenseKeyGeneratorCli (writes to file)")
class LicenseKeyGeneratorCliIT {

  private static Path newTempDir() throws Exception {
    Path dir = Files.createTempDirectory("lkgen-");
    dir.toFile().deleteOnExit();
    return dir;
  }

  private static Path writeTempAesKeyFile(Path dir) throws Exception {
    KeyGenerator kg = KeyGenerator.getInstance("AES");
    kg.init(256);
    SecretKey key = kg.generateKey();
    String b64 = Base64.getEncoder().encodeToString(key.getEncoded());
    Path file = dir.resolve("aes.key");
    Files.writeString(file, b64);
    file.toFile().deleteOnExit();
    return file;
  }

  @Test
  @DisplayName("--userId + --secretKeyFile should write {dir}/license.key and log path (exit 0)")
  void generate_writes_file_ok() throws Exception {
    String userId = "11111111-1111-1111-1111-111111111111";
    Path dir = newTempDir();
    Path keyFile = writeTempAesKeyFile(dir);
    Path expectedOut = dir.resolve("license.key");

    String out =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code =
                  LicenseKeyGeneratorCli.run(
                      new String[] {"--userId", userId, "--secretKeyFile", keyFile.toString()});
              assertEquals(0, code, "exit code should be 0");
            });

    assertTrue(Files.exists(expectedOut), "license.key should be created next to AES key file");
    String licenseKey = Files.readString(expectedOut).trim();
    assertFalse(licenseKey.isBlank(), "license.key must not be blank");

    // Basic format check: "<PREFIX>.<opaqueBase64Url>"
    assertTrue(licenseKey.contains("."), "license key must contain a dot separator");
    String[] parts = licenseKey.split("\\.", 2);
    assertEquals(2, parts.length, "license key should have exactly two parts");
    String prefix = parts[0];
    String opaque = parts[1];
    assertFalse(prefix.isBlank(), "prefix must not be blank");
    assertTrue(
        opaque.matches("^[A-Za-z0-9_-]+$"),
        "opaque payload must be URL-safe Base64 (A-Z a-z 0-9 _ -), no padding");

    assertTrue(
        out.contains("License key written to"),
        "stdout should log the target path for the written license key");
  }

  @Test
  @DisplayName("missing args should exit 2 and print usage")
  void usage_errors_missing_args_exit_2() throws Exception {
    String out =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code = LicenseKeyGeneratorCli.run(new String[] {});
              assertEquals(2, code);
            });
    assertTrue(out.contains("Usage:"), "should print usage when missing args");
  }

  @Test
  @DisplayName("blank --userId should exit 2 and print usage (with valid --secretKeyFile)")
  void usage_errors_blank_userId_exit_2() throws Exception {
    Path dir = newTempDir();
    Path keyFile = writeTempAesKeyFile(dir);

    String out =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code =
                  LicenseKeyGeneratorCli.run(
                      new String[] {"--userId", "", "--secretKeyFile", keyFile.toString()});
              assertEquals(2, code);
            });
    assertTrue(out.contains("Missing or invalid --userId"), "should mention invalid userId");
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

  @Test
  @DisplayName("nonexistent --secretKeyFile should exit 2 and not create license.key")
  void nonexistent_secret_key_path_exit_2() throws Exception {
    Path dir = newTempDir();
    Path bogus = dir.resolve("nope.key");
    Path expectedOut = dir.resolve("license.key");

    String out =
        SystemLambda.tapSystemOutNormalized(
            () -> {
              int code =
                  LicenseKeyGeneratorCli.run(
                      new String[] {
                        "--userId",
                        "11111111-1111-1111-1111-111111111111",
                        "--secretKeyFile",
                        bogus.toString()
                      });
              assertEquals(2, code);
            });

    assertTrue(out.contains("Secret key file not found"), "should report missing AES key file");
    assertFalse(Files.exists(expectedOut), "license.key should not be created on error");
  }
}
