package io.github.bsayli.license.cli;

import io.github.bsayli.license.cli.service.LicenseKeyService;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LicenseKeyGeneratorCli {

  static final int EXIT_OK = 0;
  static final int EXIT_USAGE = 2;
  static final int EXIT_CRYPTO = 3;
  private static final Logger log = LoggerFactory.getLogger(LicenseKeyGeneratorCli.class);
  private static final String ARG_USER_ID = "--userId";
  private static final String ARG_PRINT_SEGMENTS = "--printSegments";
  private static final String ARG_HELP_LONG = "--help";
  private static final String ARG_HELP_SHORT = "-h";
  private static final String MSG_MISSING_USER_ID = "Missing --userId <uuid>";
  private static final String MSG_BLANK_USER_ID = "--userId must not be blank";

  private LicenseKeyGeneratorCli() {}

  public static void main(String[] args) {
    System.exit(run(args));
  }

  static int run(String[] args) {
    final List<String> argv = Arrays.asList(args);

    if (argv.contains(ARG_HELP_LONG) || argv.contains(ARG_HELP_SHORT)) {
      printUsage();
      return EXIT_OK;
    }

    String userId = readOptionValue(argv, ARG_USER_ID).orElse(null);
    if (userId == null) {
      log.error(MSG_MISSING_USER_ID);
      printUsage();
      return EXIT_USAGE;
    }
    if (userId.isBlank()) {
      log.error(MSG_BLANK_USER_ID);
      printUsage();
      return EXIT_USAGE;
    }

    try {
      var svc = new LicenseKeyService();
      var out = svc.generate(userId);

      log.info("License Key: {}", out.licenseKey());

      if (argv.contains(ARG_PRINT_SEGMENTS)) {
        log.info("  prefix         : {}", out.prefix());
        log.info("  randomString   : {}", out.randomString());
        log.info("  encryptedUserId: {}", out.encryptedUserId());
      }

      return EXIT_OK;
    } catch (GeneralSecurityException e) {
      log.error("License key generation failed: {}", e.getMessage(), e);
      return EXIT_CRYPTO;
    }
  }

  private static Optional<String> readOptionValue(List<String> argv, String name) {
    int idx = argv.indexOf(name);
    if (idx < 0) return Optional.empty();
    int valIdx = idx + 1;
    if (valIdx >= argv.size()) return Optional.empty();
    String val = argv.get(valIdx);
    if (val != null && !val.startsWith("--") && !val.startsWith("-")) return Optional.of(val);
    return Optional.empty();
  }

  private static void printUsage() {
    log.info(
        """
            Usage:
              java -cp license-generator.jar io.github.bsayli.license.cli.LicenseKeyGeneratorCli --userId <uuid> [--printSegments]

            Options:
              --userId <uuid>     Keycloak user UUID to bind this license to
              --printSegments     Also print prefix, random and encryptedUserId parts
              --help, -h          Show this help
            """);
  }
}
