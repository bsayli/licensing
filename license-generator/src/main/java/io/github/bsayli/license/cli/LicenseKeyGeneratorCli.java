package io.github.bsayli.license.cli;

import io.github.bsayli.license.cli.service.LicenseKeyGeneratorService;
import io.github.bsayli.license.common.CryptoUtils;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.EnumSet;
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
  private static final String ARG_HELP_LONG = "--help";
  private static final String ARG_HELP_SHORT = "-h";

  private static final String OUTPUT_FILE_NAME = "license.key";

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
    if (userId == null || userId.isBlank()) {
      log.error("Missing or invalid --userId <uuid>");
      printUsage();
      return EXIT_USAGE;
    }

    String secretKeyPathStr = readOptionValue(argv, ARG_SECRET_KEY_FILE).orElse(null);
    if (secretKeyPathStr == null || secretKeyPathStr.isBlank()) {
      log.error("Missing --secretKeyFile <path to base64 AES key>");
      printUsage();
      return EXIT_USAGE;
    }

    try {
      Path secretKeyPath = Path.of(secretKeyPathStr);
      if (!Files.exists(secretKeyPath)) {
        log.error("Secret key file not found: {}", secretKeyPath.toAbsolutePath());
        return EXIT_USAGE;
      }

      String base64 = Files.readString(secretKeyPath).trim();
      SecretKey aesKey = CryptoUtils.loadAesKeyFromBase64(base64);

      var svc = new LicenseKeyGeneratorService();
      var out = svc.generate(userId, aesKey);

      Path outDir = secretKeyPath.getParent() != null ? secretKeyPath.getParent() : Path.of(".");
      Path outFile = outDir.resolve(OUTPUT_FILE_NAME);

      writeTextFileAtomically(outFile, out.licenseKey());
      setFilePermissions600(outFile);

      log.info("License key written to {}", outFile.toAbsolutePath());
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

  private static void writeTextFileAtomically(Path dest, String content) throws IOException {
    Path tmp = dest.resolveSibling(dest.getFileName().toString() + ".tmp");
    try {
      Files.writeString(
          tmp,
          content,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING,
          StandardOpenOption.WRITE);
      moveFile(dest, tmp);
    } catch (IOException ioe) {
      try {
        Files.deleteIfExists(tmp);
      } catch (IOException cleanup) {
        log.warn("Failed to cleanup temp file {}: {}", tmp, cleanup.getMessage());
      }
      throw ioe;
    }
  }

  private static void moveFile(Path dest, Path tmp) throws IOException {
    try {
      Files.move(tmp, dest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } catch (AtomicMoveNotSupportedException e) {
      Files.move(tmp, dest, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private static void setFilePermissions600(Path path) {
    try {
      PosixFileAttributeView posixView =
          Files.getFileAttributeView(path, PosixFileAttributeView.class);
      if (posixView == null) {
        log.info("POSIX permissions not supported for {} (skipping chmod 600).", path);
        return;
      }
      var perms = EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
      Files.setPosixFilePermissions(path, perms);
    } catch (UnsupportedOperationException e) {
      log.info("POSIX permissions not supported for {} (skipping chmod 600).", path);
    } catch (IOException e) {
      log.warn("Failed to set file permissions (600) for {}: {}", path, e.getMessage());
    }
  }

  private static void printUsage() {
    log.info(
        """
            Usage:
              java -cp license-generator.jar io.github.bsayli.license.cli.LicenseKeyGeneratorCli \\
                --userId <uuid> --secretKeyFile /path/to/aes.key

            Behavior:
              - Generates a license key bound to the given userId.
              - Writes result to {dir(secretKeyFile)}/license.key (never prints secrets).
              - Applies chmod 600 on POSIX systems when supported.

            Options:
              --userId <uuid>         Keycloak user UUID to bind this license to
              --secretKeyFile <path>  File containing Base64 AES key (no extra text)
              --help, -h              Show this help
            """);
  }
}
