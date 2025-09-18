package io.github.bsayli.license.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.bsayli.license.cli.service.SignatureService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SignatureCli {

  private static final Logger log = LoggerFactory.getLogger(SignatureCli.class);

  private static final String ARG_MODE = "--mode";
  private static final String MODE_SIGN = "sign";
  private static final String MODE_VERIFY = "verify";

  private static final String ARG_SERVICE_ID = "--serviceId";
  private static final String ARG_SERVICE_VERSION = "--serviceVersion";
  private static final String ARG_INSTANCE_ID = "--instanceId";
  private static final String ARG_LICENSE_KEY = "--licenseKey";
  private static final String ARG_TOKEN = "--token";

  // changed: now file-based
  private static final String ARG_PRIVATE_KEY_FILE = "--privateKeyFile";
  private static final String ARG_PUBLIC_KEY_FILE = "--publicKeyFile";

  private static final String ARG_DATA_JSON = "--dataJson";
  private static final String ARG_SIGNATURE = "--signatureB64";

  private static final String ARG_HELP_LONG = "--help";
  private static final String ARG_HELP_SHORT = "-h";

  private static final int EXIT_OK = 0;
  private static final int EXIT_USAGE = 2;
  private static final int EXIT_SIGN = 3;
  private static final int EXIT_VERIFY = 4;

  private SignatureCli() {}

  public static void main(String[] args) {
    System.exit(run(args));
  }

  static int run(String[] args) {
    final List<String> argv = Arrays.asList(args);

    if (argv.contains(ARG_HELP_LONG) || argv.contains(ARG_HELP_SHORT)) {
      printUsage();
      return EXIT_OK;
    }

    final String mode = readOptionValue(argv, ARG_MODE).orElse(null);
    if (!MODE_SIGN.equalsIgnoreCase(mode) && !MODE_VERIFY.equalsIgnoreCase(mode)) {
      log.error("Missing or invalid --mode (expected: sign | verify)");
      printUsage();
      return EXIT_USAGE;
    }

    try {
      return MODE_SIGN.equalsIgnoreCase(mode) ? runSign(argv) : runVerify(argv);
    } catch (Exception e) {
      log.error("Unexpected error: {}", e.getMessage(), e);
      return MODE_SIGN.equalsIgnoreCase(mode) ? EXIT_SIGN : EXIT_VERIFY;
    }
  }

  private static int runSign(List<String> argv) {
    String serviceId = readRequired(argv, ARG_SERVICE_ID, "Missing --serviceId");
    String serviceVersion = readRequired(argv, ARG_SERVICE_VERSION, "Missing --serviceVersion");
    String instanceId = readRequired(argv, ARG_INSTANCE_ID, "Missing --instanceId");
    String privateKeyPath = readRequired(argv, ARG_PRIVATE_KEY_FILE, "Missing --privateKeyFile");

    boolean missingRequired =
        isBlank(serviceId)
            || isBlank(serviceVersion)
            || isBlank(instanceId)
            || isBlank(privateKeyPath);
    if (missingRequired) {
      printUsage();
      return EXIT_USAGE;
    }

    Optional<String> licOpt = readOptionValue(argv, ARG_LICENSE_KEY).filter(s -> !s.isBlank());
    Optional<String> tokOpt = readOptionValue(argv, ARG_TOKEN).filter(s -> !s.isBlank());
    if (licOpt.isPresent() == tokOpt.isPresent()) {
      log.error("Provide exactly one of --licenseKey or --token");
      printUsage();
      return EXIT_USAGE;
    }

    try {
      String privateKeyB64 = readFileTrimmed(privateKeyPath, "private key");

      var svc = new SignatureService();
      SignatureService.SignResult result =
          licOpt.isPresent()
              ? svc.signWithLicenseKey(
                  serviceId, serviceVersion, instanceId, licOpt.get(), privateKeyB64)
              : svc.signWithToken(
                  serviceId, serviceVersion, instanceId, tokOpt.get(), privateKeyB64);

      log.info("Signed payload JSON:");
      log.info("{}", result.jsonPayload());
      log.info("Signature (Base64): {}", result.signatureB64());
      return EXIT_OK;

    } catch (GeneralSecurityException | JsonProcessingException e) {
      log.error("Signing error: {}", e.getMessage(), e);
      return EXIT_SIGN;
    } catch (IllegalArgumentException e) {
      log.error(e.getMessage());
      printUsage();
      return EXIT_USAGE;
    } catch (Exception e) {
      log.error("I/O error: {}", e.getMessage(), e);
      return EXIT_SIGN;
    }
  }

  private static int runVerify(List<String> argv) {
    String dataJson = readRequired(argv, ARG_DATA_JSON, "Missing --dataJson");
    String signatureB64 = readRequired(argv, ARG_SIGNATURE, "Missing --signatureB64");
    String publicKeyPath = readRequired(argv, ARG_PUBLIC_KEY_FILE, "Missing --publicKeyFile");

    boolean missingRequired = isBlank(dataJson) || isBlank(signatureB64) || isBlank(publicKeyPath);
    if (missingRequired) {
      printUsage();
      return EXIT_USAGE;
    }

    try {
      String publicKeyB64 = readFileTrimmed(publicKeyPath, "public key");

      var svc = new SignatureService();
      boolean ok = svc.verify(publicKeyB64, dataJson, signatureB64);
      if (ok) {
        log.info("Signature is VALID");
        return EXIT_OK;
      } else {
        log.error("Signature is INVALID");
        return EXIT_VERIFY;
      }
    } catch (GeneralSecurityException e) {
      log.error("Verification error: {}", e.getMessage(), e);
      return EXIT_VERIFY;
    } catch (IllegalArgumentException e) {
      log.error(e.getMessage());
      printUsage();
      return EXIT_USAGE;
    } catch (Exception e) {
      log.error("I/O error: {}", e.getMessage(), e);
      return EXIT_VERIFY;
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
    if (val != null && !val.startsWith("--") && !val.startsWith("-")) {
      return Optional.of(val);
    }
    return Optional.empty();
  }

  private static String readRequired(List<String> argv, String name, String onMissing) {
    return readOptionValue(argv, name)
        .filter(s -> !s.isBlank())
        .orElseGet(
            () -> {
              log.error(onMissing);
              return "";
            });
  }

  private static boolean isBlank(String s) {
    return s == null || s.isBlank();
  }

  private static void printUsage() {
    log.info(
        """
                Usage:

                  # SIGN (Ed25519, private key read from file)
                  java -cp license-generator.jar io.github.bsayli.license.cli.SignatureCli \\
                    --mode sign \\
                    --serviceId <id> --serviceVersion <ver> --instanceId <inst> \\
                    --licenseKey <FULL-license-key> \\
                    --privateKeyFile /secure/keys/signature.private.key

                  # or with token (exactly one of --licenseKey or --token)
                  java -cp license-generator.jar io.github.bsayli.license.cli.SignatureCli \\
                    --mode sign \\
                    --serviceId <id> --serviceVersion <ver> --instanceId <inst> \\
                    --token <jwt> \\
                    --privateKeyFile /secure/keys/signature.private.key

                  # VERIFY (Ed25519, public key read from file)
                  java -cp license-generator.jar io.github.bsayli.license.cli.SignatureCli \\
                    --mode verify \\
                    --dataJson '{...}' \\
                    --signatureB64 <base64-signature> \\
                    --publicKeyFile /secure/keys/signature.public.key

                Options:
                  --mode sign|verify
                  --serviceId <id>
                  --serviceVersion <version>
                  --instanceId <instance>
                  --licenseKey <FULL license key>   (sign)
                  --token <jwt>                     (sign)
                  --privateKeyFile </path/to/pkcs8> (sign)
                  --publicKeyFile  </path/to/spki>  (verify)
                  --dataJson <json>                 (verify)
                  --signatureB64 <base64>           (verify)
                  --help, -h
                """);
  }
}
