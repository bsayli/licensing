package io.github.bsayli.licensing.generator.impl;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.domain.model.ClientInfo;
import io.github.bsayli.licensing.generator.ClientIdGenerator;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class ClientIdGeneratorImpl implements ClientIdGenerator {

  private static final String ALGORITHM = "SHA-256";
  private static final char SEP = '\u001F';
  private static final Base64.Encoder B64URL_NOPAD_ENC = Base64.getUrlEncoder().withoutPadding();

  private static String trimOrNull(String s) {
    if (s == null) return null;
    String t = s.trim();
    return t.isEmpty() ? null : t;
  }

  @Override
  public String getClientId(IssueAccessRequest request) {
    return buildClientId(
        request.instanceId(),
        request.serviceId(),
        request.serviceVersion(),
        trimOrNull(request.checksum()));
  }

  @Override
  public String getClientId(ValidateAccessRequest request) {
    return buildClientId(
        request.instanceId(),
        request.serviceId(),
        request.serviceVersion(),
        trimOrNull(request.checksum()));
  }

  @Override
  public String getClientId(ClientInfo clientInfo) {
    return buildClientId(
        clientInfo.instanceId(),
        clientInfo.serviceId(),
        clientInfo.serviceVersion(),
        trimOrNull(clientInfo.checksum()));
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
