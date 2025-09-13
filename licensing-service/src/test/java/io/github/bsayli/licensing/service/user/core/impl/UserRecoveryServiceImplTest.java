package io.github.bsayli.licensing.service.user.core.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.domain.model.LicenseStatus;
import io.github.bsayli.licensing.service.exception.internal.LicenseServiceInternalException;
import io.github.bsayli.licensing.service.user.orchestration.UserCacheManagementService;
import jakarta.ws.rs.ProcessingException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: UserRecoveryServiceImpl")
class UserRecoveryServiceImplTest {

  @Mock private UserCacheManagementService cache;

  @InjectMocks private UserRecoveryServiceImpl service;

  private static LicenseInfo sample(String userId) {
    return new LicenseInfo.Builder()
        .userId(userId)
        .licenseTier("PRO")
        .licenseStatus(LicenseStatus.ACTIVE)
        .expirationDate(LocalDateTime.now().plusDays(2))
        .instanceIds(List.of("inst-1"))
        .maxCount(5)
        .remainingUsageCount(5)
        .allowedServices(List.of("crm"))
        .build();
  }

  @Test
  @DisplayName("recoverUser: connection-based error + cache hit => returns cached LicenseInfo")
  void recoverUser_connectionBased_cacheHit() {
    var cause = new ProcessingException(new SocketTimeoutException("read timed out"));
    var cached = sample("user-1");
    when(cache.getOffline("user-1")).thenReturn(cached);

    LicenseInfo out = service.recoverUser("user-1", cause);

    assertNotNull(out);
    assertSame(cached, out);
    verify(cache, times(1)).getOffline("user-1");
    verifyNoMoreInteractions(cache);
  }

  @Test
  @DisplayName(
      "recoverUser: connection-based error + cache miss => throws LicenseServiceInternalException (with cause)")
  void recoverUser_connectionBased_cacheMiss() {
    var cause = new ProcessingException(new SocketTimeoutException("connect timed out"));
    when(cache.getOffline("user-2")).thenReturn(null);

    LicenseServiceInternalException ex =
        assertThrows(
            LicenseServiceInternalException.class, () -> service.recoverUser("user-2", cause));

    assertSame(cause, ex.getCause());
    verify(cache, times(1)).getOffline("user-2");
    verifyNoMoreInteractions(cache);
  }

  @Test
  @DisplayName(
      "recoverUser: non-connection error => throws LicenseServiceInternalException without cache access")
  void recoverUser_nonConnectionBased_throws() {
    var cause = new ProcessingException(new IllegalStateException("boom"));

    LicenseServiceInternalException ex =
        assertThrows(
            LicenseServiceInternalException.class, () -> service.recoverUser("user-3", cause));

    assertSame(cause, ex.getCause());
    verifyNoInteractions(cache);
  }
}
