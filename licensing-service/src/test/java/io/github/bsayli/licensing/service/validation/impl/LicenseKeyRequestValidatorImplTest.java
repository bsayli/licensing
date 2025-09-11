package io.github.bsayli.licensing.service.validation.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.domain.model.ClientSessionSnapshot;
import io.github.bsayli.licensing.generator.ClientIdGenerator;
import io.github.bsayli.licensing.security.SignatureValidator;
import io.github.bsayli.licensing.security.UserIdEncryptor;
import io.github.bsayli.licensing.service.ClientSessionCacheService;
import io.github.bsayli.licensing.service.exception.request.InvalidRequestException;
import io.github.bsayli.licensing.service.exception.token.TokenAlreadyExistsException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: LicenseKeyRequestValidatorImpl")
class LicenseKeyRequestValidatorImplTest {

  @Mock private ClientSessionCacheService cache;
  @Mock private ClientIdGenerator clientIdGenerator;
  @Mock private UserIdEncryptor userIdEncryptor;
  @Mock private SignatureValidator signatureValidator;

  @InjectMocks private LicenseKeyRequestValidatorImpl validator;

  private IssueAccessRequest req(String checksum) {
    String licenseKey = "L".repeat(100) + "~rnd~" + "A".repeat(64); // >=100 and 3 segments
    String instanceId = "inst-1"; // >=10 not required here
    String safeChecksum = checksum != null ? checksum : "c".repeat(40);
    String serviceId = "crm";
    String serviceVersion = "1.2.3";
    String signature = "S".repeat(60); // >=60
    return new IssueAccessRequest(
        licenseKey, instanceId, safeChecksum, serviceId, serviceVersion, signature, false);
  }

  private ClientSessionSnapshot cached(
      String serviceId, String version, String checksum, String encUserId) {
    ClientSessionSnapshot c = mock(ClientSessionSnapshot.class);
    when(c.serviceId()).thenReturn(serviceId);
    when(c.serviceVersion()).thenReturn(version);
    when(c.checksum()).thenReturn(checksum);
    when(c.encUserId()).thenReturn(encUserId);
    return c;
  }

  @Test
  @DisplayName("assertSignatureValid should delegate to SignatureValidator")
  void assertSignatureValid_delegates() {
    IssueAccessRequest request = req("chk");
    assertDoesNotThrow(() -> validator.assertSignatureValid(request));
    verify(signatureValidator).validate(request);
  }

  @Test
  @DisplayName("assertNoConflictingCachedContext should return when cache empty")
  void assertNoConflictingCachedContext_cacheEmpty() {
    IssueAccessRequest request = req("chk");
    when(clientIdGenerator.getClientId(request)).thenReturn("client-1");
    when(cache.find("client-1")).thenReturn(Optional.empty());

    assertDoesNotThrow(() -> validator.assertNoConflictingCachedContext(request, "user-1"));
  }

  @Test
  @DisplayName(
      "assertNoConflictingCachedContext should throw TokenAlreadyExistsException for same context")
  void assertNoConflictingCachedContext_sameContext() {
    IssueAccessRequest request = req("chk");
    when(clientIdGenerator.getClientId(request)).thenReturn("client-1");

    ClientSessionSnapshot c = cached("crm", "1.2.3", "chk", "encU");
    when(cache.find("client-1")).thenReturn(Optional.of(c));
    when(userIdEncryptor.decrypt("encU")).thenReturn("user-1");

    assertThrows(
        TokenAlreadyExistsException.class,
        () -> validator.assertNoConflictingCachedContext(request, "user-1"));
  }

  @Test
  @DisplayName(
      "assertNoConflictingCachedContext should throw InvalidRequestException for different context")
  void assertNoConflictingCachedContext_differentContext() {
    IssueAccessRequest request = req("chk");
    when(clientIdGenerator.getClientId(request)).thenReturn("client-1");

    ClientSessionSnapshot c = cached("crm", "9.9.9", "chk", "encU");
    when(cache.find("client-1")).thenReturn(Optional.of(c));
    when(userIdEncryptor.decrypt("encU")).thenReturn("user-2");

    assertThrows(
        InvalidRequestException.class,
        () -> validator.assertNoConflictingCachedContext(request, "user-1"));
  }
}
