package io.github.bsayli.license.signature.validator;

import static io.github.bsayli.license.common.CryptoConstants.B64_DEC;

import io.github.bsayli.license.common.CryptoConstants;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * Validates detached signatures produced over the JSON payload of {@code SignatureData}.
 *
 * <p>Expects an input {@code data} string (the exact JSON that was signed) and a Base64-encoded
 * signature generated with SHA256withDSA over the SHA-256 hash of that JSON.
 */
public class SignatureValidator {

  // Preload SHA-256 digest (thread-safe use via clone)
  private static final MessageDigest SHA256_DIGEST;

  // Example public key for validation (DER-encoded SubjectPublicKeyInfo, then Base64)
  private static final String PUBLIC_KEY_B64 =
      "MIIDQjCCAjUGByqGSM44BAEwggIoAoIBAQCPeTXZuarpv6vtiHrPSVG28y7FnjuvNxjo6sSWHz79NgbnQ1GpxBgzObgJ58KuHFObp0dbhdARrbi0eYd1SYRpXKwOjxSzNggooi/6JxEKPWKpk0U0CaD+aWxGWPhL3SCBnDcJoBBXsZWtzQAjPbpUhLYpH51kjviDRIZ3l5zsBLQ0pqwudemYXeI9sCkvwRGMn/qdgYHnM423krcw17njSVkvaAmYchU5Feo9a4tGU8YzRY+AOzKkwuDycpAlbk4/ijsIOKHEUOThjBopo33fXqFD3ktm/wSQPtXPFiPhWNSHxgjpfyEc2B3KI8tuOAdl+CLjQr5ITAV2OTlgHNZnAh0AuvaWpoV499/e5/pnyXfHhe8ysjO65YDAvNVpXQKCAQAWplxYIEhQcE51AqOXVwQNNNo6NHjBVNTkpcAtJC7gT5bmHkvQkEq9rI837rHgnzGC0jyQQ8tkL4gAQWDt+coJsyB2p5wypifyRz6Rh5uixOdEvSCBVEy1W4AsNo0fqD7UielOD6BojjJCilx4xHjGjQUntxyaOrsLC+EsRGiWOefTznTbEBplqiuH9kxoJts+xy9LVZmDS7TtsC98kOmkltOlXVNb6/xF1PYZ9j897buHOSXC8iTgdzEpbaiH7B5HSPh++1/et1SEMWsiMt7lU92vAhErDR8C2jCXMiT+J67ai51LKSLZuovjntnhA6Y8UoELxoi34u1DFuHvF9veA4IBBQACggEAUzTk7qzLnma3DAie/xgibOjNEXFh4ThLOB67Lk0MLs3wkM9giGQRKiwHqdlQLF9ICOZdjti2pp0saQE7W6poXIq03OV46cfvSsbMzAT4OihWJI2UOlQqnbWkn4gHRpgXndPlKKTJ7jNsBy2OAE8B4FCfE74xkjjMqB7qwGAl1vGtRMu2XMKPN/+Aa5WyEv7k9cgv7QjUfDrn6tvutWEnMNm4ZdR3zQV4+wjNHzcJ5+l7BqCY/y6lvUQ/wB6rC6Whq4FNpjjBrZreekoLZbTNUceOd+OnEPeUCDxcG38m/1yNTfV0CBCWDzJ2zl383m6vCQq0asq5Vo2cBp+pA72/gw==";
  private static final byte[] PUBLIC_KEY_DER = B64_DEC.decode(PUBLIC_KEY_B64);

  static {
    try {
      SHA256_DIGEST = MessageDigest.getInstance(CryptoConstants.SHA_256);
    } catch (NoSuchAlgorithmException e) {
      // Should never happen on a sane JRE
      throw new ExceptionInInitializerError(e);
    }
  }

  private static byte[] sha256(byte[] input) {
    // MessageDigest is not thread-safe; use the static instance by cloning
    try {
      MessageDigest md = (MessageDigest) SHA256_DIGEST.clone();
      return md.digest(input);
    } catch (CloneNotSupportedException e) {
      try {
        return MessageDigest.getInstance(CryptoConstants.SHA_256).digest(input);
      } catch (NoSuchAlgorithmException ex) {
        throw new AssertionError(ex);
      }
    }
  }

  /**
   * Verifies a Base64-encoded signature against the given JSON string.
   *
   * @param signatureB64 Base64-encoded signature
   * @param data the exact JSON string that was signed
   * @return true if signature is valid; false otherwise
   */
  public boolean validateSignature(String signatureB64, String data)
      throws NoSuchAlgorithmException,
          InvalidKeySpecException,
          InvalidKeyException,
          SignatureException {

    // Hash JSON bytes with SHA-256 (explicit UTF-8)
    byte[] hash = sha256(data.getBytes(CryptoConstants.UTF8));

    // Prepare public key
    PublicKey publicKey =
        KeyFactory.getInstance(CryptoConstants.DSA_KEY_ALGORITHM)
            .generatePublic(new X509EncodedKeySpec(PUBLIC_KEY_DER));

    // Verify detached signature over the hash
    Signature sig = Signature.getInstance(CryptoConstants.SIG_SHA256_WITH_DSA);
    sig.initVerify(publicKey);
    sig.update(hash);
    return sig.verify(B64_DEC.decode(signatureB64));
  }
}
