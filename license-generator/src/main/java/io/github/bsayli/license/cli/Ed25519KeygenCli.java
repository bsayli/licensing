package io.github.bsayli.license.cli;

import io.github.bsayli.license.cli.service.Ed25519KeyService;
import java.nio.file.Path;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Ed25519KeygenCli {

  static final int EXIT_OK = 0;
  static final int EXIT_USAGE = 2;
  static final int EXIT_ERROR = 3;
  private static final Logger log = LoggerFactory.getLogger(Ed25519KeygenCli.class);
  private static final String ARG_OUT_PRIVATE = "--outPrivate";
  private static final String ARG_OUT_PUBLIC = "--outPublic";
  private static final String ARG_HELP_LONG = "--help";
  private static final String ARG_HELP_SHORT = "-h";

  private Ed25519KeygenCli() {}

  public static void main(String[] args) {
    System.exit(run(args));
  }

  static int run(String[] args) {
    try {
      if (hasFlag(args, ARG_HELP_LONG) || hasFlag(args, ARG_HELP_SHORT)) {
        printUsage();
        return EXIT_OK;
      }

      String outPriv = readOpt(args, ARG_OUT_PRIVATE).orElse(null);
      String outPub = readOpt(args, ARG_OUT_PUBLIC).orElse(null);

      var service = new Ed25519KeyService();
      var keys = service.generate();

      if (outPub != null) service.writeString(Path.of(outPub), keys.publicSpkiB64());
      if (outPriv != null) service.writeString(Path.of(outPriv), keys.privatePkcs8B64());

      log.info("=== Ed25519 Key Pair (Base64) ===");
      log.info("Public  (SPKI, X.509): {}", keys.publicSpkiB64());
      log.info("Private (PKCS#8)     : {}", keys.privatePkcs8B64());

      if (outPriv != null || outPub != null) {
        log.info("Written:");
        if (outPub != null) log.info("  {} (public, SPKI)", outPub);
        if (outPriv != null) log.info("  {} (private, PKCS#8)", outPriv);
      }

      return EXIT_OK;
    } catch (IllegalArgumentException e) {
      log.error(e.getMessage());
      printUsage();
      return EXIT_USAGE;
    } catch (Exception e) {
      log.error("Key generation error", e);
      return EXIT_ERROR;
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

  private static boolean hasFlag(String[] argv, String flag) {
    for (String s : argv) if (flag.equals(s)) return true;
    return false;
  }

  private static void printUsage() {
    log.info(
        """
            Usage:
              java -cp license-generator.jar io.github.bsayli.license.cli.Ed25519KeygenCli [--outPublic <path>] [--outPrivate <path>] [--help]

            Options:
              --outPublic  <path>   Write Base64 SPKI public key to file
              --outPrivate <path>   Write Base64 PKCS#8 private key to file
              --help, -h            Show this help
            """);
  }
}
