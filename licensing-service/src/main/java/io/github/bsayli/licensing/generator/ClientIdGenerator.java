package io.github.bsayli.licensing.generator;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.domain.model.ClientInfo;

public interface ClientIdGenerator {

  String getClientId(IssueAccessRequest request);

  String getClientId(ValidateAccessRequest request);

  String getClientId(ClientInfo clientInfo);
}
