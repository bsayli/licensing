package io.github.bsayli.licensing.sdk.generator;

import io.github.bsayli.licensing.client.generated.dto.IssueAccessRequest;
import io.github.bsayli.licensing.client.generated.dto.ValidateAccessRequest;

public interface SignatureGenerator {

  String generateForIssue(IssueAccessRequest request);

  String generateForValidate(String licenseToken, ValidateAccessRequest request);
}
