package io.github.bsayli.license.cli;

import io.github.bsayli.license.cli.service.LicenseTokenValidationService;
import io.github.bsayli.license.token.extractor.model.LicenseValidationResult;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LicenseTokenValidationCli {

  static final int EXIT_OK = 0;
  static final int EXIT_USAGE = 2;
  static final int EXIT_VALIDATION = 4;

  private static final Logger log = LoggerFactory.getLogger(LicenseTokenValidationCli.class);

  private static final String ARG_PUBLIC_KEY_FILE = "--publicKeyFile";
  private static final String ARG_TOKEN = "--token";
  private static final String ARG_HELP_LONG = "--help";
  private static final String ARG_HELP_SHORT = "-h";

  // Highlight that this must be the JWT public key (not the detached-signature public key)
  private static final String MSG_MISSING_PUBLIC_KEY =
      "Missing --publicKeyFile <path-to-jwt-public-key (SPKI Base64, Ed25519)>";
  private static final String MSG_MISSING_TOKEN = "Missing --token <jwt>";

  private LicenseTokenValidationCli() {}

  public static void main(String[] args) {
    System.exit(run(args));
  }

  static int run(String[] args) {
    final List<String> argv = Arrays.asList(args);

    if (argv.contains(ARG_HELP_LONG) || argv.contains(ARG_HELP_SHORT)) {
      printUsage();
      return EXIT_OK;
    }

    String publicKeyPath = readOptionValue(argv, ARG_PUBLIC_KEY_FILE).orElse(null);
    if (publicKeyPath == null || publicKeyPath.isBlank()) {
      log.error(MSG_MISSING_PUBLIC_KEY);
      printUsage();
      return EXIT_USAGE;
    }

    String token = readOptionValue(argv, ARG_TOKEN).orElse(null);
    if (token == null || token.isBlank()) {
      log.error(MSG_MISSING_TOKEN);
      printUsage();
      return EXIT_USAGE;
    }

    try {
      String publicKeyB64 = readFileTrimmed(publicKeyPath, "public key");
      var svc = new LicenseTokenValidationService();
      LicenseValidationResult result = svc.validate(publicKeyB64, token);

      log.info("License token is VALID");
      log.info("  status     : {}", result.licenseStatus());
      log.info("  tier       : {}", result.licenseTier());
      log.info("  message    : {}", result.message());
      log.info("  expiration : {}", result.expirationDate());
      return EXIT_OK;

    } catch (ExpiredJwtException e) {
      log.error("License token is EXPIRED: {}", e.getMessage());
      return EXIT_VALIDATION;
    } catch (JwtException | IllegalArgumentException e) {
      log.error("License token is INVALID: {}", e.getMessage());
      return EXIT_VALIDATION;
    }
  }

  private static String readFileTrimmed(String pathStr, String what) {
    Path p = Path.of(pathStr);
    if (!Files.exists(p)) {
      throw new IllegalArgumentException("File not found for " + what + ": " + p.toAbsolutePath());
    }
    try {
      String s = Files.readString(p).trim();
      if (s.isBlank()) {
        throw new IllegalArgumentException("Empty " + what + " file: " + p.toAbsolutePath());
      }
      return s;
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Failed to read " + what + " file: " + p.toAbsolutePath(), e);
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
                      java -cp license-generator.jar io.github.bsayli.license.cli.LicenseTokenValidationCli \\
                        --publicKeyFile /secure/keys/jwt.public.key --token <jwt>

                    Options:
                      --publicKeyFile <path>  File containing Base64 SPKI Ed25519 **JWT** public key
                      --token <jwt>           Compact JWS (EdDSA) license token to validate
                      --help, -h              Show this help

                    Notes:
                      - Do NOT pass the detached-signature public key here. Use the JWT public key (jwt.public.key).
                    """);
  }
}
