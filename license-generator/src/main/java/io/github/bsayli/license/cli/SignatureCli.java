package io.github.bsayli.license.cli;

import io.github.bsayli.license.signature.generator.SignatureGenerator;
import io.github.bsayli.license.signature.model.SignatureData;
import io.github.bsayli.license.signature.validator.SignatureValidator;
import java.security.GeneralSecurityException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SignatureCli {

  private static final Logger log = LoggerFactory.getLogger(SignatureCli.class);

  private static final String ARG_MODE = "--mode"; // sign | verify
  private static final String MODE_SIGN = "sign";
  private static final String MODE_VERIFY = "verify";

  // sign mode args
  private static final String ARG_SERVICE_ID = "--serviceId";
  private static final String ARG_SERVICE_VERSION = "--serviceVersion";
  private static final String ARG_INSTANCE_ID = "--instanceId";
  private static final String ARG_ENC_KEY = "--encKey"; // encrypted license key (raw string)
  private static final String ARG_TOKEN = "--token"; // license token (JWT)

  // verify mode args
  private static final String ARG_DATA_JSON = "--dataJson"; // exact JSON used for signing
  private static final String ARG_SIGNATURE = "--signatureB64"; // Base64 detached signature

  private static final String ARG_HELP_LONG = "--help";
  private static final String ARG_HELP_SHORT = "-h";

  private static final int EXIT_OK = 0;
  private static final int EXIT_USAGE = 2;
  private static final int EXIT_SIGN = 3;
  private static final int EXIT_VERIFY = 4;

  private SignatureCli() {}

  public static void main(String[] args) {
    List<String> argv = Arrays.asList(args);

    if (argv.contains(ARG_HELP_LONG) || argv.contains(ARG_HELP_SHORT)) {
      printUsage(EXIT_OK);
    }

    String mode = read(ARG_MODE, argv).orElse(null);
    if (mode == null
        || (!MODE_SIGN.equalsIgnoreCase(mode) && !MODE_VERIFY.equalsIgnoreCase(mode))) {
      log.error("Missing or invalid --mode (expected: sign | verify)");
      printUsage(EXIT_USAGE);
    }

    if (MODE_SIGN.equalsIgnoreCase(mode)) {
      runSign(argv);
    } else {
      runVerify(argv);
    }
  }

  private static void runSign(List<String> argv) {
    String serviceId = require(ARG_SERVICE_ID, argv, "Missing --serviceId");
    String serviceVersion = require(ARG_SERVICE_VERSION, argv, "Missing --serviceVersion");
    String instanceId = require(ARG_INSTANCE_ID, argv, "Missing --instanceId");

    Optional<String> encKeyOpt = read(ARG_ENC_KEY, argv);
    Optional<String> tokenOpt = read(ARG_TOKEN, argv);

    boolean hasEncKey = encKeyOpt.filter(s -> !s.isBlank()).isPresent();
    boolean hasToken = tokenOpt.filter(s -> !s.isBlank()).isPresent();

    if (hasEncKey == hasToken) {
      log.error("Provide exactly one of --encKey or --token");
      printUsage(EXIT_USAGE);
    }

    try {
      SignatureData payload;
      if (hasEncKey) {
        String encKey = encKeyOpt.orElseThrow();
        String encKeyHashB64 = base64Sha256(encKey);
        payload =
            new SignatureData.Builder()
                .serviceId(serviceId)
                .serviceVersion(serviceVersion)
                .instanceId(instanceId)
                .encryptedLicenseKeyHash(encKeyHashB64)
                .build();
      } else {
        String token = tokenOpt.orElseThrow();
        String tokenHashB64 = base64Sha256(token);
        payload =
            new SignatureData.Builder()
                .serviceId(serviceId)
                .serviceVersion(serviceVersion)
                .instanceId(instanceId)
                .licenseTokenHash(tokenHashB64)
                .build();
      }

      String json = payload.toJson();
      String signatureB64 = SignatureGenerator.createSignature(payload);

      log.info("Signed payload JSON:");
      log.info("{}", json);
      log.info("Signature (Base64): {}", signatureB64);
      System.exit(EXIT_OK);
    } catch (Exception e) {
      log.error("Signing error: {}", e.getMessage(), e);
      System.exit(EXIT_SIGN);
    }
  }

  private static void runVerify(List<String> argv) {
    String dataJson = require(ARG_DATA_JSON, argv, "Missing --dataJson");
    String signatureB64 = require(ARG_SIGNATURE, argv, "Missing --signatureB64");

    try {
      SignatureValidator validator = new SignatureValidator();
      boolean ok = validator.validateSignature(signatureB64, dataJson);
      if (ok) {
        log.info("Signature is VALID");
        System.exit(EXIT_OK);
      } else {
        log.error("Signature is INVALID");
        System.exit(EXIT_VERIFY);
      }
    } catch (GeneralSecurityException e) {
      log.error("Verification error: {}", e.getMessage(), e);
      System.exit(EXIT_VERIFY);
    }
  }

  private static Optional<String> read(String name, List<String> argv) {
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

  private static String require(String name, List<String> argv, String onMissing) {
    return read(name, argv)
        .filter(s -> !s.isBlank())
        .orElseGet(
            () -> {
              log.error(onMissing);
              printUsage(EXIT_USAGE);
              return ""; // unreachable
            });
  }

  private static String base64Sha256(String text) throws Exception {
    return java.util.Base64.getEncoder()
        .encodeToString(
            java.security.MessageDigest.getInstance("SHA-256")
                .digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
  }

  private static void printUsage(int exitCode) {
    log.info(
        """
                Usage:
                  # Sign with encrypted license key (detached signature over canonical JSON)
                  java -cp license-generator.jar io.github.bsayli.license.cli.SignatureCli \\
                      --mode sign \\
                      --serviceId <id> --serviceVersion <ver> --instanceId <inst> \\
                      --encKey <encrypted-license-key>

                  # Sign with license token (hash-only)
                  java -cp license-generator.jar io.github.bsayli.license.cli.SignatureCli \\
                      --mode sign \\
                      --serviceId <id> --serviceVersion <ver> --instanceId <inst> \\
                      --token <jwt>

                  # Verify an existing signature against the exact JSON
                  java -cp license-generator.jar io.github.bsayli.license.cli.SignatureCli \\
                      --mode verify \\
                      --dataJson '{...}' \\
                      --signatureB64 <base64-signature>

                Options:
                  --mode sign|verify
                  --serviceId <id>
                  --serviceVersion <version>
                  --instanceId <instance>
                  --encKey <encrypted-license-key>
                  --token <jwt>
                  --dataJson <json>
                  --signatureB64 <base64>
                  --help, -h
                """);
    System.exit(exitCode);
  }
}
