package io.github.bsayli.licensing.service.token;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.common.exception.ServiceErrorCode;
import io.github.bsayli.licensing.domain.model.ClientInfo;
import io.github.bsayli.licensing.domain.model.LicenseStatus;
import io.github.bsayli.licensing.domain.result.LicenseValidationResult;
import io.github.bsayli.licensing.service.ClientSessionCache;
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
    var jwt = mock(JwtService.class);
    var cache = mock(ClientSessionCache.class);
    var mgr = new LicenseTokenManager(jwt, cache);

    var result =
        new LicenseValidationResult.Builder()
            .valid(true)
            .userId("enc-user-1")
            .appInstanceId("inst-9999")
            .licenseStatus(LicenseStatus.ACTIVE)
            .licenseTier("ENTERPRISE")
            .serviceStatus(ServiceErrorCode.TOKEN_CREATED)
            .message("ok")
            .build();

    when(jwt.generateToken("client-1", "ENTERPRISE", LicenseStatus.ACTIVE)).thenReturn("jwt-new");

    String token =
        mgr.issueAndCache("client-1", result, "crm", "1.0.0", "inst-9999", "chk-aaa", "sig-xxx");

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
