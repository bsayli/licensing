package io.github.bsayli.license.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Ed25519KeygenCli {

    private static final Logger log = LoggerFactory.getLogger(Ed25519KeygenCli.class);

    private static final String ED25519_STD_ALGO = "Ed25519";
    private static final String EDDSA_BC_ALGO = "EdDSA";
    private static final String ED25519_CURVE = "Ed25519";
    private static final String BC_PROVIDER = "BC";

    private Ed25519KeygenCli() {}

    public static void main(String[] args) {
        String outPriv = readOpt(args, "--outPrivate").orElse(null);
        String outPub  = readOpt(args, "--outPublic").orElse(null);

        try {
            ensureBouncyCastleIfNeeded();

            KeyPair kp = generateEd25519KeyPair();

            String privatePkcs8B64 = Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded());
            String publicSpkiB64   = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());

            if (outPriv != null) {
                writeString(Path.of(outPriv), privatePkcs8B64);
            }
            if (outPub != null) {
                writeString(Path.of(outPub), publicSpkiB64);
            }

            log.info("=== Ed25519 Key Pair (Base64) ===");
            log.info("Public  (SPKI, X.509): {}", publicSpkiB64);
            log.info("Private (PKCS#8)     : {}", privatePkcs8B64);

            if (outPriv != null || outPub != null) {
                log.info("Written:");
                if (outPub != null)  log.info("  {} (public, SPKI)", outPub);
                if (outPriv != null) log.info("  {} (private, PKCS#8)", outPriv);
            }

            log.info("");
            log.info("Use with SignatureCli (sign): --privateKey <PKCS8-Base64>");
            log.info("Use with SignatureValidator (verify): pass SPKI public key Base64 to its constructor.");

            System.exit(0);
        } catch (Exception e) {
            log.error("Key generation error: {}", e.getMessage(), e);
            System.exit(2);
        }
    }

    private static KeyPair generateEd25519KeyPair() throws GeneralSecurityException {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(ED25519_STD_ALGO);
            return kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(EDDSA_BC_ALGO, BC_PROVIDER);
            kpg.initialize(new ECGenParameterSpec(ED25519_CURVE), new SecureRandom());
            return kpg.generateKeyPair();
        }
    }

    private static void ensureBouncyCastleIfNeeded() {
        if (Security.getProvider(BC_PROVIDER) == null) {
            try {
                Class<?> bc = Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
                Provider p = (Provider) bc.getDeclaredConstructor().newInstance();
                Security.addProvider(p);
                log.debug("BouncyCastle provider added.");
            } catch (Exception ignored) {
                log.debug("BouncyCastle not on classpath; will use JDK provider if available.");
            }
        }
    }

    private static Optional<String> readOpt(String[] argv, String name) {
        for (int i = 0; i < argv.length; i++) {
            if (name.equals(argv[i]) && i + 1 < argv.length) {
                String v = argv[i + 1];
                if (v != null && !v.startsWith("--") && !v.startsWith("-")) {
                    return Optional.of(v);
                }
            }
        }
        return Optional.empty();
    }

    private static void writeString(Path path, String content) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(path, content);
    }
}