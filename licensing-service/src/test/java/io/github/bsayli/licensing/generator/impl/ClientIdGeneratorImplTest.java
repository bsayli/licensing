package io.github.bsayli.licensing.generator.impl;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
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

  private static final char SEP = '\u001F';
  private static final Base64.Encoder B64URL_NOPAD_ENC = Base64.getUrlEncoder().withoutPadding();

  private final ClientIdGeneratorImpl generator = new ClientIdGeneratorImpl();

  private String expectedUrlSafe(
      String instanceId, String serviceId, String version, String checksum) throws Exception {
    String raw =
        instanceId.trim()
            + SEP
            + serviceId.trim()
            + SEP
            + version.trim()
            + SEP
            + (checksum == null ? "" : checksum.trim());

    byte[] hashed =
        MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
    return B64URL_NOPAD_ENC.encodeToString(hashed);
  }

  @Test
  @DisplayName(
      "getClientId(IssueAccessRequest) deterministic ve checksum var/yok farklı sonuç üretir")
  void issueTokenRequest_idStable() throws Exception {
    var reqWithChecksum =
        new IssueAccessRequest("LK", "inst-12345678", "chk", "crm", "1.2.3", "sig", false);
    var reqNoChecksum =
        new IssueAccessRequest("LK", "inst-12345678", null, "crm", "1.2.3", "sig", false);

    String id1 = generator.getClientId(reqWithChecksum);
    String id2 = generator.getClientId(reqNoChecksum);

    assertEquals(expectedUrlSafe("inst-12345678", "crm", "1.2.3", "chk"), id1);
    assertEquals(expectedUrlSafe("inst-12345678", "crm", "1.2.3", null), id2);
    assertNotEquals(id1, id2);
  }

  @Test
  @DisplayName("ValidateAccessRequest ile IssueAccessRequest aynı girdilerde aynı ID’yi verir")
  void validate_vs_issue_sameResult() throws Exception {
    var issue =
        new IssueAccessRequest("LK", "inst-ABCD1234", "cs", "billing", "2.0.0", "sig", false);
    var valid = new ValidateAccessRequest("inst-ABCD1234", "cs", "billing", "2.0.0", "sig");

    String idIssue = generator.getClientId(issue);
    String idValid = generator.getClientId(valid);

    String exp = expectedUrlSafe("inst-ABCD1234", "billing", "2.0.0", "cs");
    assertEquals(exp, idIssue);
    assertEquals(exp, idValid);
  }

  @Test
  @DisplayName("ClientInfo ile üretilen ID, aynı alanlarla beklenenle eşleşir")
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
    assertEquals(expectedUrlSafe("inst-XYZ00000", "reporting", "3.1.0", "cshm"), id);
  }
}
