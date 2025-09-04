package io.github.bsayli.license.cli;

import io.github.bsayli.license.licensekey.encrypter.UserIdEncrypter;
import io.github.bsayli.license.licensekey.generator.LicenseKeyGenerator;
import io.github.bsayli.license.licensekey.model.LicenseKeyData;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LicenseKeyGeneratorCli {

  private static final Logger log = LoggerFactory.getLogger(LicenseKeyGeneratorCli.class);

  private static final String ARG_USER_ID = "--userId";
  private static final String ARG_PRINT_SEGMENTS = "--printSegments";
  private static final String ARG_HELP_LONG = "--help";
  private static final String ARG_HELP_SHORT = "-h";

  private static final int EXIT_OK = 0;
  private static final int EXIT_USAGE = 2;
  private static final int EXIT_CRYPTO = 3;

  private static final String MSG_MISSING_USER_ID = "Missing --userId <uuid>";
  private static final String MSG_BLANK_USER_ID = "--userId must not be blank";

  private LicenseKeyGeneratorCli() {}

  public static void main(String[] args) {
    List<String> argv = Arrays.asList(args);

    if (argv.contains(ARG_HELP_LONG) || argv.contains(ARG_HELP_SHORT)) {
      printUsage(EXIT_OK);
    }

    String userId =
        readOptionValue(argv, ARG_USER_ID)
            .orElseThrow(
                () -> {
                  log.error(MSG_MISSING_USER_ID);
                  printUsage(EXIT_USAGE);
                  return new IllegalStateException(MSG_MISSING_USER_ID);
                });

    if (userId.isBlank()) {
      log.error(MSG_BLANK_USER_ID);
      printUsage(EXIT_USAGE);
    }

    try {
      // 1) Encrypt Keycloak user UUID
      String encryptedUserId = UserIdEncrypter.encrypt(userId);

      // 2) Build license key (PREFIX ~ random ~ encryptedUserId)
      LicenseKeyData licenseKeyData = LicenseKeyGenerator.generateLicenseKey(encryptedUserId);
      String licenseKey = licenseKeyData.generateLicenseKey();

      // 3) Output
      log.info("License Key: {}", licenseKey);

      if (hasFlag(argv, ARG_PRINT_SEGMENTS)) {
        log.info("  prefix         : {}", licenseKeyData.prefix());
        log.info("  randomString   : {}", licenseKeyData.randomString());
        log.info("  encryptedUserId: {}", licenseKeyData.uuid());
      }

      System.exit(EXIT_OK);
    } catch (GeneralSecurityException e) {
      log.error("License key generation failed: {}", e.getMessage(), e);
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

  private static boolean hasFlag(List<String> argv, String flag) {
    return argv.contains(flag);
  }

  private static void printUsage(int exitCode) {
    log.info(
        """
                Usage:
                  java -cp license-generator.jar io.github.bsayli.license.cli.LicenseKeyGeneratorCli --userId <uuid> [--printSegments]

                Options:
                  --userId <uuid>     Keycloak user UUID to bind this license to
                  --printSegments     Also print prefix, random and encryptedUserId parts
                  --help, -h          Show this help
                """);
    System.exit(exitCode);
  }
}
