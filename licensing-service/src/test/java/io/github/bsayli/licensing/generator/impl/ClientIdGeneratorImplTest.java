package io.github.bsayli.licensing.generator.impl;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.licensing.api.dto.IssueTokenRequest;
import io.github.bsayli.licensing.api.dto.ValidateTokenRequest;
import io.github.bsayli.licensing.domain.model.ClientInfo;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: ClientIdGeneratorImpl")
class ClientIdGeneratorImplTest {

  private final ClientIdGeneratorImpl generator = new ClientIdGeneratorImpl();

  private String expected(String instanceId, String serviceId, String version, String checksum)
      throws Exception {
    String raw = (instanceId + serviceId + version) + (checksum == null ? "" : checksum);
    byte[] hashed =
        MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(hashed);
  }

  @Test
  @DisplayName(
      "getClientId(IssueTokenRequest) should produce deterministic value with/without checksum")
  void issueTokenRequest_idStable() throws Exception {
    var reqWithChecksum =
        new IssueTokenRequest("crm", "1.2.3", "inst-12345678", "sig", "chk", "LK", false);
    var reqNoChecksum =
        new IssueTokenRequest("crm", "1.2.3", "inst-12345678", "sig", null, "LK", false);

    String id1 = generator.getClientId(reqWithChecksum);
    String id2 = generator.getClientId(reqNoChecksum);

    assertEquals(expected("inst-12345678", "crm", "1.2.3", "chk"), id1);
    assertEquals(expected("inst-12345678", "crm", "1.2.3", null), id2);
    assertNotEquals(id1, id2);
  }

  @Test
  @DisplayName("getClientId(ValidateTokenRequest) should match IssueTokenRequest for same inputs")
  void validate_vs_issue_sameResult() throws Exception {
    var issue =
        new IssueTokenRequest("billing", "2.0.0", "inst-ABCD1234", "sig", "cs", "LK", false);
    var valid = new ValidateTokenRequest("billing", "2.0.0", "inst-ABCD1234", "sig", "cs");

    String idIssue = generator.getClientId(issue);
    String idValid = generator.getClientId(valid);

    String exp = expected("inst-ABCD1234", "billing", "2.0.0", "cs");
    assertEquals(exp, idIssue);
    assertEquals(exp, idValid);
  }

  @Test
  @DisplayName("getClientId(ClientInfo) should generate same ID as requests")
  void clientInfo_sameResult() throws Exception {
    var info =
        new ClientInfo.Builder()
            .serviceId("reporting")
            .serviceVersion("3.1.0")
            .instanceId("inst-XYZ00000")
            .checksum("cshm")
            .signature("sig")
            .encUserId("encU")
            .licenseToken("tok")
            .build();

    String id = generator.getClientId(info);
    assertEquals(expected("inst-XYZ00000", "reporting", "3.1.0", "cshm"), id);
  }
}
