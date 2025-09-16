package io.github.bsayli.license.cli;

import io.github.bsayli.license.cli.service.LicenseKeyService;
import io.github.bsayli.license.common.CryptoUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LicenseKeyGeneratorCli {

  static final int EXIT_OK = 0;
  static final int EXIT_USAGE = 2;
  static final int EXIT_CRYPTO = 3;
  private static final Logger log = LoggerFactory.getLogger(LicenseKeyGeneratorCli.class);

  private static final String ARG_USER_ID = "--userId";
  private static final String ARG_SECRET_KEY_FILE = "--secretKeyFile";
  private static final String ARG_PRINT_SEGMENTS = "--printSegments";
  private static final String ARG_HELP_LONG = "--help";
  private static final String ARG_HELP_SHORT = "-h";

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
      log.error("Missing --userId <uuid>");
      printUsage();
      return EXIT_USAGE;
    }
    if (userId.isBlank()) {
      log.error("--userId must not be blank");
      printUsage();
      return EXIT_USAGE;
    }

    String secretKeyPath = readOptionValue(argv, ARG_SECRET_KEY_FILE).orElse(null);
    if (secretKeyPath == null || secretKeyPath.isBlank()) {
      log.error("Missing --secretKeyFile <path to base64 aes key>");
      printUsage();
      return EXIT_USAGE;
    }

    try {
      Path keyFile = Path.of(secretKeyPath);
      if (!Files.exists(keyFile)) {
        log.error("Secret key file not found: {}", keyFile);
        return EXIT_USAGE;
      }
      String base64 = Files.readString(keyFile).trim();
      SecretKey aesKey = CryptoUtils.loadAesKeyFromBase64(base64);

      var svc = new LicenseKeyService();
      var out = svc.generate(userId, aesKey);

      log.info("License Key: {}", out.licenseKey());

      if (argv.contains(ARG_PRINT_SEGMENTS)) {
        log.info("  prefix              : {}", out.prefix());
        log.info("  opaquePayload(Base64URL) : {}", out.opaquePayloadB64Url());
      }

      return EXIT_OK;
    } catch (IllegalArgumentException e) {
      log.error(e.getMessage());
      printUsage();
      return EXIT_USAGE;
    } catch (Exception e) {
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
                  java -cp license-generator.jar io.github.bsayli.license.cli.LicenseKeyGeneratorCli \\
                    --userId <uuid> --secretKeyFile /path/to/aes.key [--printSegments]

                Options:
                  --userId <uuid>           Keycloak user UUID to bind this license to
                  --secretKeyFile <path>    File containing Base64 AES key (no extra text)
                  --printSegments           Also print prefix and opaque payload (Base64URL)
                  --help, -h                Show this help
                """);
  }
}
