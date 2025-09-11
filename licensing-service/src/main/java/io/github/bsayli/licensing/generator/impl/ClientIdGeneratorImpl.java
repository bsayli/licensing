package io.github.bsayli.licensing.generator.impl;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.domain.model.ClientInfo;
import io.github.bsayli.licensing.generator.ClientIdGenerator;
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

  private static String requireAndNorm(String s, String fieldName) {
    Objects.requireNonNull(s, fieldName + " must not be null");
    return s.trim();
  }

  private static String norm(String s) {
    return (s == null) ? null : s.trim();
  }

  @Override
  public String getClientId(IssueAccessRequest request) {
    Objects.requireNonNull(request, "request");
    return buildClientId(
        requireAndNorm(request.instanceId(), FIELD_INSTANCE_ID),
        requireAndNorm(request.serviceId(), FIELD_SERVICE_ID),
        requireAndNorm(request.serviceVersion(), FIELD_SERVICE_VERSION),
        norm(request.checksum()));
  }

  @Override
  public String getClientId(ValidateAccessRequest request) {
    Objects.requireNonNull(request, "request");
    return buildClientId(
        requireAndNorm(request.instanceId(), FIELD_INSTANCE_ID),
        requireAndNorm(request.serviceId(), FIELD_SERVICE_ID),
        requireAndNorm(request.serviceVersion(), FIELD_SERVICE_VERSION),
        norm(request.checksum()));
  }

  @Override
  public String getClientId(ClientInfo clientInfo) {
    Objects.requireNonNull(clientInfo, "clientInfo");
    return buildClientId(
        requireAndNorm(clientInfo.instanceId(), FIELD_INSTANCE_ID),
        requireAndNorm(clientInfo.serviceId(), FIELD_SERVICE_ID),
        requireAndNorm(clientInfo.serviceVersion(), FIELD_SERVICE_VERSION),
        norm(clientInfo.checksum()));
  }

  private String buildClientId(
      String instanceId, String serviceId, String serviceVersion, String checksum) {
    String raw =
        instanceId
            + SEP
            + serviceId
            + SEP
            + serviceVersion
            + SEP
            + (checksum == null ? "" : checksum);

    try {
      MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
      byte[] hashed = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
      return B64URL_NOPAD_ENC.encodeToString(hashed);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Hash algorithm not found: " + ALGORITHM, e);
    }
  }
}
