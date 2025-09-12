package io.github.bsayli.licensing.service.user.orchestration.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.domain.model.LicenseStatus;
import io.github.bsayli.licensing.service.user.core.UserService;
import io.github.bsayli.licensing.service.user.orchestration.UserCacheManagementService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("UserOrchestrationServiceImpl")
class UserOrchestrationServiceImplTest {

  @Mock private UserService userService;
  @Mock private UserCacheManagementService cache;

  private static LicenseInfo li() {
    return new LicenseInfo.Builder()
        .userId("u")
        .licenseTier("PRO")
        .licenseStatus(LicenseStatus.ACTIVE)
        .expirationDate(LocalDateTime.now().plusDays(1))
        .instanceIds(List.of("i1"))
        .maxCount(5)
        .remainingUsageCount(5)
        .allowedServices(List.of("crm"))
        .build();
  }

  private UserOrchestrationServiceImpl svc() {
    return new UserOrchestrationServiceImpl(userService, cache);
  }

  @Test
  @DisplayName(
      "getLicenseInfo: offline present & online missing -> returns offline and refreshes async")
  void getLicense_offline_present_triggers_refresh() {
    var info = li();
    when(cache.getOffline("u")).thenReturn(info);
    when(cache.isOnlineMissing("u")).thenReturn(true);

    LicenseInfo out = svc().getLicenseInfo("u");

    assertNotNull(out);
    assertEquals(info, out);
    InOrder in = inOrder(cache);
    in.verify(cache).getOffline("u");
    in.verify(cache).isOnlineMissing("u");
    in.verify(cache).refreshAsync("u");
    in.verifyNoMoreInteractions();
    verifyNoInteractions(userService);
  }

  @Test
  @DisplayName("getLicenseInfo: offline present & online exists -> returns offline, no refresh")
  void getLicense_offline_present_no_refresh() {
    var info = li();
    when(cache.getOffline("u")).thenReturn(info);
    when(cache.isOnlineMissing("u")).thenReturn(false);

    LicenseInfo out = svc().getLicenseInfo("u");

    assertNotNull(out);
    assertEquals(info, out);
    verify(cache).getOffline("u");
    verify(cache).isOnlineMissing("u");
    verify(cache, never()).refreshAsync(anyString());
    verifyNoMoreInteractions(cache);
    verifyNoInteractions(userService);
  }

  @Test
  @DisplayName("getLicenseInfo: offline null -> fetch online, putOffline(value), return value")
  void getLicense_offline_null_online_present() {
    var info = li();
    when(cache.getOffline("u")).thenReturn(null);
    when(userService.getUser("u")).thenReturn(info);

    LicenseInfo out = svc().getLicenseInfo("u");

    assertNotNull(out);
    assertEquals(info, out);
    InOrder in = inOrder(cache, userService);
    in.verify(cache).getOffline("u");
    in.verify(userService).getUser("u");
    in.verify(cache).putOffline("u", info);
    in.verifyNoMoreInteractions();
  }

  @Test
  @DisplayName("getLicenseInfo: offline null & online null -> putOffline(null), return null")
  void getLicense_offline_null_online_null() {
    when(cache.getOffline("u")).thenReturn(null);
    when(userService.getUser("u")).thenReturn(null);

    LicenseInfo out = svc().getLicenseInfo("u");

    assertNull(out);
    InOrder in = inOrder(cache, userService);
    in.verify(cache).getOffline("u");
    in.verify(userService).getUser("u");
    in.verify(cache).putOffline("u", null);
    in.verifyNoMoreInteractions();
  }

  @Test
  @DisplayName("recordUsage: update present -> putBoth(value)")
  void recordUsage_present() {
    var info = li();
    when(userService.updateLicenseUsage("u", "inst")).thenReturn(info);

    svc().recordUsage("u", "inst");

    InOrder in = inOrder(userService, cache);
    in.verify(userService).updateLicenseUsage("u", "inst");
    in.verify(cache).putBoth("u", info);
    in.verifyNoMoreInteractions();
  }

  @Test
  @DisplayName("recordUsage: update null -> putBoth(null)")
  void recordUsage_null() {
    when(userService.updateLicenseUsage("u", "inst")).thenReturn(null);

    svc().recordUsage("u", "inst");

    InOrder in = inOrder(userService, cache);
    in.verify(userService).updateLicenseUsage("u", "inst");
    in.verify(cache).putBoth("u", null);
    in.verifyNoMoreInteractions();
  }
}
