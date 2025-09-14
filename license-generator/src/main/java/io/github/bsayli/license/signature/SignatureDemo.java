package io.github.bsayli.license.signature;

import io.github.bsayli.license.signature.generator.SignatureGenerator;
import io.github.bsayli.license.signature.model.SignatureData;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SignatureDemo {

  static final int EXIT_OK = 0;
  static final int EXIT_USAGE = 2;
  static final int EXIT_SIGN = 3;
  private static final Logger log = LoggerFactory.getLogger(SignatureDemo.class);
  private static final String ARG_MODE = "--mode";
  private static final String MODE_SAMPLE_KEY = "sign-sample-key";
  private static final String MODE_SAMPLE_TOKEN = "sign-sample-token";
  private static final String ARG_PRIVATE_KEY = "--privateKey";

  private SignatureDemo() {}

  public static void main(String[] args) {
    System.exit(run(args));
  }

  static int run(String[] args) {
    String mode = readOpt(args, ARG_MODE).orElse(null);
    String privateKeyB64 = readOpt(args, ARG_PRIVATE_KEY).orElse(null);

    if (mode == null || privateKeyB64 == null) {
      printUsage();
      return EXIT_USAGE;
    }
    if (!MODE_SAMPLE_KEY.equalsIgnoreCase(mode) && !MODE_SAMPLE_TOKEN.equalsIgnoreCase(mode)) {
      log.error("Invalid --mode. Expected: {} or {}", MODE_SAMPLE_KEY, MODE_SAMPLE_TOKEN);
      printUsage();
      return EXIT_USAGE;
    }

    try {
      if (MODE_SAMPLE_KEY.equalsIgnoreCase(mode)) {
        SignatureData payload = SignatureGenerator.sampleSignatureDataWithLicenseKey();
        String sig = SignatureGenerator.createSignature(payload, privateKeyB64);
        log.info("Signed payload JSON:\n{}", payload.toJson());
        log.info("Signature (Base64): {}", sig);
      } else {
        SignatureData payload = SignatureGenerator.sampleSignatureDataWithLicenseToken();
        String sig = SignatureGenerator.createSignature(payload, privateKeyB64);
        log.info("Signed payload JSON:\n{}", payload.toJson());
        log.info("Signature (Base64): {}", sig);
      }
      return EXIT_OK;
    } catch (Exception e) {
      log.error("Signing failed: {}", e.getMessage(), e);
      return EXIT_SIGN;
    }
  }

  static Optional<String> readOpt(String[] argv, String name) {
    for (int i = 0; i < argv.length; i++) {
      if (name.equals(argv[i]) && i + 1 < argv.length) {
        String v = argv[i + 1];
        if (v != null && !v.startsWith("--") && !v.startsWith("-")) return Optional.of(v);
      }
    }
    return Optional.empty();
  }

  private static void printUsage() {
    log.info(
            """
            Usage:
              # Sign sample payload (encrypted license key variant)
              java -cp license-generator.jar io.github.bsayli.license.signature.SignatureDemo \\
                --mode sign-sample-key \\
                --privateKey <base64-pkcs8-ed25519>
    
              # Sign sample payload (license token variant)
              java -cp license-generator.jar io.github.bsayli.license.signature.SignatureDemo \\
                --mode sign-sample-token \\
                --privateKey <base64-pkcs8-ed25519>
            """);
  }
}