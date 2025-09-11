package io.github.bsayli.licensing.sdk.generator.impl;

import io.github.bsayli.licensing.sdk.api.dto.LicenseAccessRequest;
import io.github.bsayli.licensing.sdk.generator.ClientIdGenerator;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class ClientIdGeneratorImpl implements ClientIdGenerator {

  private static final String ALGORITHM = "SHA-256";
  private static final char SEP = '\u001F';
  private static final Base64.Encoder B64URL_NOPAD_ENC = Base64.getUrlEncoder().withoutPadding();

  private static final String FIELD_INSTANCE_ID = "instanceId";
  private static final String FIELD_SERVICE_ID = "serviceId";
  private static final String FIELD_SERVICE_VERSION = "serviceVersion";

  private static String requireAndNorm(String value, String fieldName) {
    Objects.requireNonNull(value, fieldName + " must not be null");
    return value.trim();
  }

  @Override
  public String getClientId(LicenseAccessRequest request) {
    Objects.requireNonNull(request, "request");
    String raw =
        requireAndNorm(request.instanceId(), FIELD_INSTANCE_ID)
            + SEP
            + requireAndNorm(request.serviceId(), FIELD_SERVICE_ID)
            + SEP
            + requireAndNorm(request.serviceVersion(), FIELD_SERVICE_VERSION)
            + SEP
            + (request.checksum() == null ? "" : request.checksum().trim());

    try {
      MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
      byte[] hashed = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
      return B64URL_NOPAD_ENC.encodeToString(hashed);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Hash algorithm not found: " + ALGORITHM, e);
    }
  }
}
