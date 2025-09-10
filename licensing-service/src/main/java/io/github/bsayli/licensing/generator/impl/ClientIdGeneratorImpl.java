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

  @Override
  public String getClientId(IssueAccessRequest request) {
    return buildClientId(
        request.instanceId(), request.serviceId(), request.serviceVersion(), request.checksum());
  }

  @Override
  public String getClientId(ValidateAccessRequest request) {
    return buildClientId(
        request.instanceId(), request.serviceId(), request.serviceVersion(), request.checksum());
  }

  @Override
  public String getClientId(ClientInfo clientInfo) {
    return buildClientId(
        clientInfo.instanceId(),
        clientInfo.serviceId(),
        clientInfo.serviceVersion(),
        clientInfo.checksum());
  }

  private String buildClientId(
      String instanceId, String serviceId, String serviceVersion, String checksum) {
    StringBuilder raw =
        new StringBuilder().append(instanceId).append(serviceId).append(serviceVersion);

    if (Objects.nonNull(checksum)) {
      raw.append(checksum);
    }

    try {
      MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
      byte[] hashed = digest.digest(raw.toString().getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hashed);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Hash algorithm not found: " + ALGORITHM, e);
    }
  }
}
