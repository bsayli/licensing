package io.github.bsayli.license.cli;

import io.github.bsayli.license.cli.service.UserIdCryptoService;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EncryptUserIdCli {

  static final int EXIT_OK = 0;
  static final int EXIT_USAGE = 2;
  static final int EXIT_CRYPTO = 3;
  private static final Logger log = LoggerFactory.getLogger(EncryptUserIdCli.class);
  private static final String CMD_ENCRYPT = "encrypt";
  private static final String CMD_DECRYPT = "decrypt";
  private static final String ARG_USER_ID = "--userId";
  private static final String ARG_CIPHERTEXT = "--ciphertext";
  private static final String ARG_HELP_LONG = "--help";
  private static final String ARG_HELP_SHORT = "-h";

  private EncryptUserIdCli() {}

  public static void main(String[] args) {
    System.exit(run(args));
  }

  static int run(String[] args) {
    final List<String> argv = Arrays.asList(args);

    if (argv.isEmpty() || argv.contains(ARG_HELP_LONG) || argv.contains(ARG_HELP_SHORT)) {
      printUsage();
      return argv.isEmpty() ? EXIT_USAGE : EXIT_OK;
    }

    final String cmd = argv.getFirst();
    return switch (cmd) {
      case CMD_ENCRYPT ->
          runWithArg(
              argv,
              ARG_USER_ID,
              "Missing --userId <uuid> for encrypt",
              "--userId must not be blank",
              UserIdCryptoService::encrypt,
              "Encrypted userId: {}");
      case CMD_DECRYPT ->
          runWithArg(
              argv,
              ARG_CIPHERTEXT,
              "Missing --ciphertext <base64> for decrypt",
              "--ciphertext must not be blank",
              UserIdCryptoService::decrypt,
              "Decrypted userId: {}");
      default -> {
        log.error("Missing command: encrypt | decrypt");
        printUsage();
        yield EXIT_USAGE;
      }
    };
  }

  private static int runWithArg(
      List<String> argv,
      String requiredFlag,
      String missingMsg,
      String blankMsg,
      CryptoOp op,
      String successLogPattern) {

    String value = readOptionValue(argv, requiredFlag).orElse(null);
    if (value == null) {
      log.error(missingMsg);
      printUsage();
      return EXIT_USAGE;
    }
    if (value.isBlank()) {
      log.error(blankMsg);
      printUsage();
      return EXIT_USAGE;
    }

    try {
      var svc = new UserIdCryptoService();
      String result = op.apply(svc, value);
      log.info(successLogPattern, result);
      return EXIT_OK;
    } catch (GeneralSecurityException e) {
      log.error("Crypto error: {}", e.getMessage(), e);
      return EXIT_CRYPTO;
    } catch (IllegalArgumentException e) {
      log.error("Invalid input: {}", e.getMessage(), e);
      return EXIT_CRYPTO;
    } catch (RuntimeException e) {
      log.error("Unexpected error: {}", e.getMessage(), e);
      return EXIT_CRYPTO;
    }
  }

  static Optional<String> readOptionValue(List<String> argv, String name) {
    int idx = argv.indexOf(name);
    if (idx < 0) return Optional.empty();
    int valIdx = idx + 1;
    if (valIdx >= argv.size()) return Optional.empty();
    String val = argv.get(valIdx);
    return (val != null && !val.startsWith("--") && !val.startsWith("-"))
        ? Optional.of(val)
        : Optional.empty();
  }

  private static void printUsage() {
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
              --ciphertext <base64>  Required for 'decrypt'
              --help, -h             Show this help
            """);
  }

  @FunctionalInterface
  private interface CryptoOp {
    String apply(UserIdCryptoService svc, String value) throws GeneralSecurityException;
  }
}
