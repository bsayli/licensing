package io.github.bsayli.license.cli;

import io.github.bsayli.license.cli.service.KeygenService;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KeygenCli {

  static final int EXIT_OK = 0;
  static final int EXIT_USAGE = 2;
  static final int EXIT_ERROR = 3;
  private static final Logger log = LoggerFactory.getLogger(KeygenCli.class);
  private static final String ARG_MODE = "--mode";
  private static final String MODE_AES = "aes";
  private static final String MODE_ED25519 = "ed25519";
  private static final String ARG_SIZE = "--size";
  private static final String ARG_HELP_LONG = "--help";
  private static final String ARG_HELP_SHORT = "-h";

  private KeygenCli() {}

  public static void main(String[] args) {
    System.exit(run(args));
  }

  static int run(String[] args) {
    List<String> argv = Arrays.asList(args);

    if (argv.contains(ARG_HELP_LONG) || argv.contains(ARG_HELP_SHORT)) {
      printUsage();
      return EXIT_OK;
    }

    String mode = read(ARG_MODE, argv).orElse(null);
    if (!MODE_AES.equalsIgnoreCase(mode) && !MODE_ED25519.equalsIgnoreCase(mode)) {
      log.error("Missing or invalid --mode (expected: aes | ed25519)");
      printUsage();
      return EXIT_USAGE;
    }

    try {
      var service = new KeygenService();

      if (MODE_AES.equalsIgnoreCase(mode)) {
        int size = parseAesSize(read(ARG_SIZE, argv).orElse("256"));
        var out = service.generateAes(size);
        log.info("AES-{} SecretKey (Base64): {}", out.sizeBits(), out.base64());
      } else {
        var pair = service.generateEd25519();
        log.info("Ed25519 PublicKey  (Base64): {}", pair.publicSpkiB64());
        log.info("Ed25519 PrivateKey (Base64): {}", pair.privatePkcs8B64());
      }

      return EXIT_OK;
    } catch (IllegalArgumentException e) {
      log.error(e.getMessage());
      printUsage();
      return EXIT_USAGE;
    } catch (Exception e) {
      log.error("Key generation error: {}", e.getMessage(), e);
      return EXIT_ERROR;
    }
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
    return EncryptUserIdCli.readOptionValue(argv, name);
  }

  private static void printUsage() {
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
  }
}
