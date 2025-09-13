package io.github.bsayli.license.cli;

import io.github.bsayli.license.licensekey.encrypter.UserIdEncrypter;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EncryptUserIdCli {

  private static final Logger log = LoggerFactory.getLogger(EncryptUserIdCli.class);

  private static final String CMD_ENCRYPT = "encrypt";
  private static final String CMD_DECRYPT = "decrypt";

  private static final String ARG_USER_ID = "--userId";
  private static final String ARG_CIPHERTEXT = "--ciphertext";
  private static final String ARG_HELP_LONG = "--help";
  private static final String ARG_HELP_SHORT = "-h";

  private static final int EXIT_OK = 0;
  private static final int EXIT_USAGE = 2;
  private static final int EXIT_CRYPTO = 3;

  private static final String MSG_MISSING_CMD = "Missing command: encrypt | decrypt";
  private static final String MSG_MISSING_USER_ID = "Missing --userId <uuid> for encrypt";
  private static final String MSG_BLANK_USER_ID = "--userId must not be blank";
  private static final String MSG_MISSING_CIPHERTEXT = "Missing --ciphertext <base64> for decrypt";
  private static final String MSG_BLANK_CIPHERTEXT = "--ciphertext must not be blank";

  private EncryptUserIdCli() {}

  public static void main(String[] args) {
    List<String> argv = Arrays.asList(args);

    if (argv.isEmpty() || argv.contains(ARG_HELP_LONG) || argv.contains(ARG_HELP_SHORT)) {
      printUsage(argv.isEmpty() ? EXIT_USAGE : EXIT_OK);
    }

    String cmd = argv.getFirst();
    switch (cmd) {
      case CMD_ENCRYPT -> runEncrypt(argv);
      case CMD_DECRYPT -> runDecrypt(argv);
      default -> {
        log.error(MSG_MISSING_CMD);
        printUsage(EXIT_USAGE);
      }
    }
  }

  private static void runEncrypt(List<String> argv) {
    String userId =
        readOptionValue(argv, ARG_USER_ID)
            .orElseGet(
                () -> {
                  log.error(MSG_MISSING_USER_ID);
                  printUsage(EXIT_USAGE);
                  return null;
                });

    if (userId == null || userId.isBlank()) {
      log.error(MSG_BLANK_USER_ID);
      printUsage(EXIT_USAGE);
    }

    try {
      String encrypted = UserIdEncrypter.encrypt(userId);
      log.info("Encrypted userId: {}", encrypted);
      System.exit(EXIT_OK);
    } catch (GeneralSecurityException e) {
      log.error("Encryption error: {}", e.getMessage(), e);
      System.exit(EXIT_CRYPTO);
    } catch (RuntimeException e) {
      log.error("Unexpected error: {}", e.getMessage(), e);
      System.exit(EXIT_CRYPTO);
    }
  }

  private static void runDecrypt(List<String> argv) {
    String ciphertext =
        readOptionValue(argv, ARG_CIPHERTEXT)
            .orElseGet(
                () -> {
                  log.error(MSG_MISSING_CIPHERTEXT);
                  printUsage(EXIT_USAGE);
                  return null;
                });

    if (ciphertext == null || ciphertext.isBlank()) {
      log.error(MSG_BLANK_CIPHERTEXT);
      printUsage(EXIT_USAGE);
    }

    try {
      String plain = UserIdEncrypter.decrypt(ciphertext);
      log.info("Decrypted userId: {}", plain);
      System.exit(EXIT_OK);
    } catch (GeneralSecurityException e) {
      log.error("Decryption error: {}", e.getMessage(), e);
      System.exit(EXIT_CRYPTO);
    } catch (IllegalArgumentException e) {
      log.error("Invalid Base64 ciphertext: {}", e.getMessage(), e);
      System.exit(EXIT_CRYPTO);
    } catch (RuntimeException e) {
      log.error("Unexpected error: {}", e.getMessage(), e);
      System.exit(EXIT_CRYPTO);
    }
  }

  private static Optional<String> readOptionValue(List<String> argv, String name) {
    int idx = argv.indexOf(name);
    if (idx < 0) return Optional.empty();
    int valIdx = idx + 1;
    if (valIdx >= argv.size()) return Optional.empty();
    String val = argv.get(valIdx);
    if (val != null && !val.startsWith("--") && !val.startsWith("-")) {
      return Optional.of(val);
    }
    return Optional.empty();
  }

  private static void printUsage(int exitCode) {
    log.info(
        """
                Usage:
                  java -cp license-generator.jar io.github.bsayli.license.cli.EncryptUserIdCli encrypt  --userId <uuid>
                  java -cp license-generator.jar io.github.bsayli.license.cli.EncryptUserIdCli decrypt  --ciphertext <base64>

                Commands:
                  encrypt                 Encrypt a Keycloak user UUID (AES-GCM, Base64 output)
                  decrypt                 Decrypt a previously encrypted Base64 payload

                Options:
                  --userId <uuid>        Required for 'encrypt'
                  --ciphertext <base64>   Required for 'decrypt'
                  --help, -h             Show this help
                """);
    System.exit(exitCode);
  }
}
