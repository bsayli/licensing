package io.github.bsayli.license.cli;

import io.github.bsayli.license.cli.service.KeygenService;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
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
  private static final String ARG_DIR = "--dir";
  private static final String ARG_HELP_LONG = "--help";
  private static final String ARG_HELP_SHORT = "-h";

  private static final String AES_FILE = "aes.key";
  private static final String PUB_FILE = "signature.public.key";
  private static final String PRIV_FILE = "signature.private.key";

  private KeygenCli() {}

  public static void main(String[] args) {
    System.exit(run(args));
  }

  static int run(String[] args) {
    var argv = Arrays.asList(args);

    if (argv.contains(ARG_HELP_LONG) || argv.contains(ARG_HELP_SHORT)) {
      printUsage();
      return EXIT_OK;
    }

    var mode = read(ARG_MODE, argv).orElse(null);
    if (!MODE_AES.equalsIgnoreCase(mode) && !MODE_ED25519.equalsIgnoreCase(mode)) {
      log.error("Missing or invalid --mode (expected: aes | ed25519)");
      printUsage();
      return EXIT_USAGE;
    }

    var dirOpt = read(ARG_DIR, argv).map(Path::of);
    if (dirOpt.isEmpty()) {
      log.error("Missing --dir <directory> for output files");
      printUsage();
      return EXIT_USAGE;
    }
    var dir = dirOpt.get();

    try {
      ensureDirectory(dir);
    } catch (IOException e) {
      log.error("Failed to create directory: {}", dir, e);
      return EXIT_ERROR;
    }

    try {
      return MODE_AES.equalsIgnoreCase(mode) ? generateAes(dir, argv) : generateEd25519(dir);
    } catch (IllegalArgumentException e) {
      log.error(e.getMessage());
      printUsage();
      return EXIT_USAGE;
    } catch (Exception e) {
      log.error("Key generation error", e);
      return EXIT_ERROR;
    }
  }

  private static int generateAes(Path dir, List<String> argv)
      throws NoSuchAlgorithmException, IOException {
    int size = parseAesSize(read(ARG_SIZE, argv).orElse("256"));
    var service = new KeygenService();
    var out = service.generateAes(size);

    var aesPath = dir.resolve(AES_FILE);
    writeTextFileAtomically(aesPath, out.base64());
    setFilePermissions600(aesPath);

    log.info("AES-{} key written to {}", out.sizeBits(), aesPath.toAbsolutePath());
    return EXIT_OK;
  }

  private static int generateEd25519(Path dir) throws GeneralSecurityException, IOException {
    var service = new KeygenService();
    var pair = service.generateEd25519();

    var pub = dir.resolve(PUB_FILE);
    var prv = dir.resolve(PRIV_FILE);

    writeTextFileAtomically(pub, pair.publicSpkiB64());
    writeTextFileAtomically(prv, pair.privatePkcs8B64());

    setFilePermissions600(pub);
    setFilePermissions600(prv);

    log.info("Ed25519 public key  written to {}", pub.toAbsolutePath());
    log.info("Ed25519 private key written to {}", prv.toAbsolutePath());
    return EXIT_OK;
  }

  private static void ensureDirectory(Path dir) throws IOException {
    Files.createDirectories(dir);
  }

  private static void writeTextFileAtomically(Path dest, String content) throws IOException {
    var tmp = dest.resolveSibling(dest.getFileName().toString() + ".tmp");
    Files.writeString(
        tmp,
        content,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE);
    try {
      Files.move(tmp, dest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } catch (AtomicMoveNotSupportedException e) {
      log.warn("Atomic move not supported; falling back to non-atomic move: {}", dest);
      Files.move(tmp, dest, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private static void setFilePermissions600(Path path) {
    try {
      var perms = EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
      Files.setPosixFilePermissions(path, perms);
    } catch (UnsupportedOperationException e) {
      log.warn("POSIX file permissions not supported for {}. Skipping chmod 600.", path);
    } catch (IOException e) {
      log.warn("Failed to set file permissions for {}: {}", path, e.getMessage());
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
    int idx = argv.indexOf(name);
    if (idx < 0) return Optional.empty();
    int valIdx = idx + 1;
    if (valIdx >= argv.size()) return Optional.empty();
    var val = argv.get(valIdx);
    return (val != null && !val.startsWith("--") && !val.startsWith("-"))
        ? Optional.of(val)
        : Optional.empty();
  }

  private static void printUsage() {
    log.info(
        """
            Usage:
              # AES secret key -> {DIR}/aes.key
              java -cp license-generator.jar io.github.bsayli.license.cli.KeygenCli \\
                  --mode aes --size 256 --dir /secure/keys

              # Ed25519 key pair -> {DIR}/signature.public.key & {DIR}/signature.private.key
              java -cp license-generator.jar io.github.bsayli.license.cli.KeygenCli \\
                  --mode ed25519 --dir /secure/keys

            Notes:
              - Keys are written to files only (never printed).
              - On POSIX systems, files are set to 600 when supported.
            """);
  }
}
