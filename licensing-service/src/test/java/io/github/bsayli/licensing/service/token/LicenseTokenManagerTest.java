package io.github.bsayli.licensing.service.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.domain.model.ClientInfo;
import io.github.bsayli.licensing.domain.model.LicenseStatus;
import io.github.bsayli.licensing.domain.result.LicenseValidationResult;
import io.github.bsayli.licensing.service.ClientSessionCacheService;
import io.github.bsayli.licensing.service.jwt.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: LicenseTokenManager")
class LicenseTokenManagerTest {

  @Test
  @DisplayName("issueAndCache should generate token, cache ClientInfo, and return token")
  void issueAndCache_writesAndReturnsToken() {
    JwtService jwt = mock(JwtService.class);
    ClientSessionCacheService cache = mock(ClientSessionCacheService.class);
    LicenseTokenManager mgr = new LicenseTokenManager(jwt, cache);

    LicenseValidationResult result =
        new LicenseValidationResult.Builder()
            .valid(true)
            .userId("enc-user-1")
            .appInstanceId("inst-9999")
            .licenseStatus(LicenseStatus.ACTIVE)
            .licenseTier("ENTERPRISE")
            .serviceStatus(ServiceErrorCode.TOKEN_CREATED)
            .message("ok")
            .build();

    LicenseTokenIssueRequest req =
        new LicenseTokenIssueRequest.Builder()
            .clientId("client-1")
            .result(result)
            .serviceId("crm")
            .serviceVersion("1.0.0")
            .instanceId("inst-9999")
            .checksum("chk-aaa")
            .signature("sig-xxx")
            .build();

    when(jwt.generateToken("client-1", "ENTERPRISE", LicenseStatus.ACTIVE)).thenReturn("jwt-new");

    String token = mgr.issueAndCache(req);

    assertEquals("jwt-new", token);

    ArgumentCaptor<ClientInfo> infoCap = ArgumentCaptor.forClass(ClientInfo.class);
    verify(cache).put(infoCap.capture());

    ClientInfo info = infoCap.getValue();
    assertEquals("crm", info.serviceId());
    assertEquals("1.0.0", info.serviceVersion());
    assertEquals("inst-9999", info.instanceId());
    assertEquals("chk-aaa", info.checksum());
    assertEquals("sig-xxx", info.signature());
    assertEquals("enc-user-1", info.encUserId());
    assertEquals("jwt-new", info.licenseToken());

    verify(jwt).generateToken("client-1", "ENTERPRISE", LicenseStatus.ACTIVE);
    verifyNoMoreInteractions(jwt, cache);
  }
}
