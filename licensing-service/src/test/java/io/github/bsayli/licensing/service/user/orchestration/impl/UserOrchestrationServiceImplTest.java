package io.github.bsayli.licensing.service.user.orchestration.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.domain.model.LicenseStatus;
import io.github.bsayli.licensing.service.user.core.UserService;
import io.github.bsayli.licensing.service.user.orchestration.UserCacheManagementService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    when(cache.getOffline("u")).thenReturn(Optional.of(info));
    when(cache.isOnlineMissing("u")).thenReturn(true);

    Optional<LicenseInfo> out = svc().getLicenseInfo("u");

    assertTrue(out.isPresent());
    assertEquals(info, out.get());
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
    when(cache.getOffline("u")).thenReturn(Optional.of(info));
    when(cache.isOnlineMissing("u")).thenReturn(false);

    Optional<LicenseInfo> out = svc().getLicenseInfo("u");

    assertTrue(out.isPresent());
    assertEquals(info, out.get());
    verify(cache).getOffline("u");
    verify(cache).isOnlineMissing("u");
    verify(cache, never()).refreshAsync(anyString());
    verifyNoMoreInteractions(cache);
    verifyNoInteractions(userService);
  }

  @Test
  @DisplayName("getLicenseInfo: offline empty -> fetch online, putOffline(value), return value")
  void getLicense_offline_empty_online_present() {
    var info = li();
    when(cache.getOffline("u")).thenReturn(Optional.empty());
    when(userService.getUser("u")).thenReturn(Optional.of(info));

    Optional<LicenseInfo> out = svc().getLicenseInfo("u");

    assertTrue(out.isPresent());
    assertEquals(info, out.get());
    InOrder in = inOrder(cache, userService);
    in.verify(cache).getOffline("u");
    in.verify(userService).getUser("u");
    in.verify(cache).putOffline("u", info);
    in.verifyNoMoreInteractions();
  }

  @Test
  @DisplayName("getLicenseInfo: offline empty & online empty -> putOffline(null), return empty")
  void getLicense_offline_empty_online_empty() {
    when(cache.getOffline("u")).thenReturn(Optional.empty());
    when(userService.getUser("u")).thenReturn(Optional.empty());

    Optional<LicenseInfo> out = svc().getLicenseInfo("u");

    assertTrue(out.isEmpty());
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
    when(userService.updateLicenseUsage("u", "inst")).thenReturn(Optional.of(info));

    svc().recordUsage("u", "inst");

    InOrder in = inOrder(userService, cache);
    in.verify(userService).updateLicenseUsage("u", "inst");
    in.verify(cache).putBoth("u", info);
    in.verifyNoMoreInteractions();
  }

  @Test
  @DisplayName("recordUsage: update empty -> putBoth(null)")
  void recordUsage_empty() {
    when(userService.updateLicenseUsage("u", "inst")).thenReturn(Optional.empty());

    svc().recordUsage("u", "inst");

    InOrder in = inOrder(userService, cache);
    in.verify(userService).updateLicenseUsage("u", "inst");
    in.verify(cache).putBoth("u", null);
    in.verifyNoMoreInteractions();
  }
}
