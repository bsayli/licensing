package io.github.bsayli.license.cli;

import io.github.bsayli.license.token.extractor.JwtTokenExtractor;
import io.github.bsayli.license.token.extractor.model.LicenseValidationResult;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LicenseTokenCli {

  private static final Logger log = LoggerFactory.getLogger(LicenseTokenCli.class);

  private static final String ARG_PUBLIC_KEY = "--publicKey";
  private static final String ARG_TOKEN = "--token";
  private static final String ARG_HELP_LONG = "--help";
  private static final String ARG_HELP_SHORT = "-h";

  private static final int EXIT_OK = 0;
  private static final int EXIT_USAGE = 2;
  private static final int EXIT_VALIDATION = 4;

  private static final String MSG_MISSING_PUBLIC_KEY = "Missing --publicKey <base64-SPKI-Ed25519>";
  private static final String MSG_MISSING_TOKEN = "Missing --token <jwt>";
  private static final String MSG_BLANK_PUBLIC_KEY = "--publicKey must not be blank";
  private static final String MSG_BLANK_TOKEN = "--token must not be blank";

  private LicenseTokenCli() {}

  public static void main(String[] args) {
    List<String> argv = Arrays.asList(args);

    if (argv.contains(ARG_HELP_LONG) || argv.contains(ARG_HELP_SHORT)) {
      printUsage(EXIT_OK);
    }

    String publicKeyB64 =
        readOptionValue(argv, ARG_PUBLIC_KEY)
            .orElseThrow(
                () -> {
                  log.error(MSG_MISSING_PUBLIC_KEY);
                  printUsage(EXIT_USAGE);
                  return new IllegalStateException(MSG_MISSING_PUBLIC_KEY);
                });

    String token =
        readOptionValue(argv, ARG_TOKEN)
            .orElseThrow(
                () -> {
                  log.error(MSG_MISSING_TOKEN);
                  printUsage(EXIT_USAGE);
                  return new IllegalStateException(MSG_MISSING_TOKEN);
                });

    if (publicKeyB64.isBlank()) {
      log.error(MSG_BLANK_PUBLIC_KEY);
      printUsage(EXIT_USAGE);
    }
    if (token.isBlank()) {
      log.error(MSG_BLANK_TOKEN);
      printUsage(EXIT_USAGE);
    }

    try {
      JwtTokenExtractor extractor = new JwtTokenExtractor(publicKeyB64);
      LicenseValidationResult result = extractor.validateAndGetToken(token);

      log.info("License token is VALID");
      log.info("  status     : {}", result.licenseStatus());
      log.info("  tier       : {}", result.licenseTier());
      log.info("  message    : {}", result.message());
      log.info("  expiration : {}", result.expirationDate());

      System.exit(EXIT_OK);
    } catch (ExpiredJwtException e) {
      log.error("License token is EXPIRED: {}", e.getMessage());
      System.exit(EXIT_VALIDATION);
    } catch (JwtException | IllegalArgumentException e) {
      log.error("License token is INVALID: {}", e.getMessage());
      System.exit(EXIT_VALIDATION);
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
                  java -cp license-generator.jar io.github.bsayli.license.cli.LicenseTokenCli \\
                    --publicKey <base64-SPKI-Ed25519> --token <jwt>

                Options:
                  --publicKey <base64>   Base64-encoded SPKI Ed25519 public key used to verify the token
                  --token <jwt>          Compact JWS (EdDSA) license token to validate
                  --help, -h             Show this help
                """);
    System.exit(exitCode);
  }
}
