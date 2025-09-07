package io.github.bsayli.licensing.generator;

import io.github.bsayli.licensing.api.dto.IssueTokenRequest;
import io.github.bsayli.licensing.api.dto.ValidateTokenRequest;
import io.github.bsayli.licensing.model.ClientInfo;

public interface ClientIdGenerator {

  String getClientId(IssueTokenRequest request);

  String getClientId(ValidateTokenRequest request);

  String getClientId(ClientInfo clientInfo);
}
