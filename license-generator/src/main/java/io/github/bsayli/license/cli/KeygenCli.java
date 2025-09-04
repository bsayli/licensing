package io.github.bsayli.license.cli;

import io.github.bsayli.license.common.CryptoUtils;
import io.github.bsayli.license.securekey.generator.SecureEdDSAKeyPairGenerator;
import io.github.bsayli.license.securekey.generator.SecureKeyGenerator;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.*;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KeygenCli {

  private static final Logger log = LoggerFactory.getLogger(KeygenCli.class);

  private static final String ARG_MODE = "--mode"; // aes | ed25519
  private static final String MODE_AES = "aes";
  private static final String MODE_ED25519 = "ed25519";

  private static final String ARG_SIZE = "--size"; // for AES: 128|192|256

  private static final String ARG_HELP_LONG = "--help";
  private static final String ARG_HELP_SHORT = "-h";

  private static final int EXIT_OK = 0;
  private static final int EXIT_USAGE = 2;
  private static final int EXIT_ERROR = 3;

  private KeygenCli() {}

  public static void main(String[] args) {
    List<String> argv = Arrays.asList(args);

    if (argv.contains(ARG_HELP_LONG) || argv.contains(ARG_HELP_SHORT)) {
      printUsage(EXIT_OK);
    }

    String mode = read(ARG_MODE, argv).orElse(null);
    if (mode == null
        || (!MODE_AES.equalsIgnoreCase(mode) && !MODE_ED25519.equalsIgnoreCase(mode))) {
      log.error("Missing or invalid --mode (expected: aes | ed25519)");
      printUsage(EXIT_USAGE);
    }

    try {
      if (MODE_AES.equalsIgnoreCase(mode)) {
        runAes(argv);
      } else {
        runEd25519();
      }
      System.exit(EXIT_OK);
    } catch (Exception e) {
      log.error("Key generation error: {}", e.getMessage(), e);
      System.exit(EXIT_ERROR);
    }
  }

  private static void runAes(List<String> argv) throws Exception {
    int size = parseAesSize(read(ARG_SIZE, argv).orElse("256"));
    SecretKey key = SecureKeyGenerator.generateAesKey(size);
    log.info("AES-{} SecretKey (Base64): {}", size, CryptoUtils.toBase64(key));
  }

  private static void runEd25519() throws GeneralSecurityException {
    KeyPair kp = SecureEdDSAKeyPairGenerator.generateKeyPair();
    log.info("Ed25519 PublicKey  (Base64): {}", CryptoUtils.toBase64(kp.getPublic()));
    log.info("Ed25519 PrivateKey (Base64): {}", CryptoUtils.toBase64(kp.getPrivate()));
  }

  private static int parseAesSize(String raw) {
    try {
      int v = Integer.parseInt(raw.trim());
      if (v == 128 || v == 192 || v == 256) return v;
    } catch (NumberFormatException ignored) {
    }
    throw new IllegalArgumentException("--size must be one of 128, 192, 256");
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

  private static void printUsage(int exitCode) {
    log.info(
        """
                Usage:
                  # AES secret key (Base64)
                  java -cp license-generator.jar io.github.bsayli.license.cli.KeygenCli \\
                      --mode aes --size 256

                  # Ed25519 key pair (Base64)
                  java -cp license-generator.jar io.github.bsayli.license.cli.KeygenCli \\
                      --mode ed25519

                Options:
                  --mode aes|ed25519
                  --size 128|192|256     (AES only, default: 256)
                  --help, -h
                """);
    System.exit(exitCode);
  }
}
