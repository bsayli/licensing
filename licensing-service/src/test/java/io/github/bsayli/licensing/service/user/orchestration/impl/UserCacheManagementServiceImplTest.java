package io.github.bsayli.licensing.service.user.orchestration.impl;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.domain.model.LicenseStatus;
import io.github.bsayli.licensing.service.user.cache.UserCacheService;
import io.github.bsayli.licensing.service.user.core.UserAsyncService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("UserCacheManagementServiceImpl Unit Tests")
class UserCacheManagementServiceImplTest {

  @Mock private UserCacheService offlineCache;
  @Mock private UserCacheService onlineCache;
  @Mock private UserAsyncService userAsyncService;

  private UserCacheManagementServiceImpl service;

  private static LicenseInfo info() {
    return new LicenseInfo.Builder()
        .userId("user-1")
        .licenseTier("PRO")
        .licenseStatus(LicenseStatus.ACTIVE)
        .expirationDate(LocalDateTime.now().plusDays(1))
        .instanceIds(List.of("inst-1"))
        .maxCount(5)
        .remainingUsageCount(5)
        .allowedServices(List.of("crm"))
        .build();
  }

  @BeforeEach
  void setUp() {
    service = new UserCacheManagementServiceImpl(offlineCache, onlineCache, userAsyncService);
  }

  @Test
  @DisplayName("refreshAsync: success + present -> both caches put")
  void refreshAsync_success_present() {
    LicenseInfo li = info();
    when(userAsyncService.getUser("user-1"))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(li)));

    service.refreshAsync("user-1");

    await()
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(
            () -> {
              verify(onlineCache, times(1)).put("user-1", li);
              verify(offlineCache, times(1)).put("user-1", li);
              verifyNoMoreInteractions(onlineCache, offlineCache);
            });
  }

  @Test
  @DisplayName("refreshAsync: success + empty -> both caches evict")
  void refreshAsync_success_empty() {
    when(userAsyncService.getUser("user-2"))
        .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

    service.refreshAsync("user-2");

    await()
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(
            () -> {
              verify(onlineCache, times(1)).evict("user-2");
              verify(offlineCache, times(1)).evict("user-2");
              verifyNoMoreInteractions(onlineCache, offlineCache);
            });
  }

  @Test
  @DisplayName("refreshAsync: failure -> caches untouched")
  void refreshAsync_failure() {
    when(userAsyncService.getUser("user-3"))
        .thenReturn(CompletableFuture.failedFuture(new RuntimeException("x")));

    service.refreshAsync("user-3");

    await()
        .atMost(Duration.ofSeconds(1))
        .untilAsserted(
            () -> {
              verifyNoInteractions(onlineCache);
              verifyNoInteractions(offlineCache);
            });
  }

  @Test
  @DisplayName("getOffline delegates to offline cache")
  void getOffline() {
    Optional<LicenseInfo> expected = Optional.of(info());
    when(offlineCache.get("u")).thenReturn(expected);

    Optional<LicenseInfo> out = service.getOffline("u");

    assertEquals(expected, out);
    verify(offlineCache, times(1)).get("u");
    verifyNoMoreInteractions(offlineCache);
    verifyNoInteractions(onlineCache, userAsyncService);
  }

  @Test
  @DisplayName("isOnlineMissing uses online cache existence")
  void isOnlineMissing() {
    when(onlineCache.exists("u1")).thenReturn(false);
    when(onlineCache.exists("u2")).thenReturn(true);

    assertTrue(service.isOnlineMissing("u1"));
    assertFalse(service.isOnlineMissing("u2"));

    verify(onlineCache, times(1)).exists("u1");
    verify(onlineCache, times(1)).exists("u2");
    verifyNoMoreInteractions(onlineCache);
    verifyNoInteractions(offlineCache, userAsyncService);
  }

  @Test
  @DisplayName("putOffline: null -> evict; non-null -> put")
  void putOffline_behavior() {
    LicenseInfo li = info();

    service.putOffline("u", null);
    service.putOffline("u", li);

    InOrder inOrder = inOrder(offlineCache);
    inOrder.verify(offlineCache).evict("u");
    inOrder.verify(offlineCache).put("u", li);
    inOrder.verifyNoMoreInteractions();

    verifyNoInteractions(onlineCache, userAsyncService);
  }

  @Test
  @DisplayName("putBoth: null -> evict both; non-null -> put both")
  void putBoth_behavior() {
    LicenseInfo li = info();

    service.putBoth("u", null);
    service.putBoth("u", li);

    InOrder inOrder = inOrder(onlineCache, offlineCache);
    inOrder.verify(onlineCache).evict("u");
    inOrder.verify(offlineCache).evict("u");
    inOrder.verify(onlineCache).put("u", li);
    inOrder.verify(offlineCache).put("u", li);
    inOrder.verifyNoMoreInteractions();

    verifyNoInteractions(userAsyncService);
  }

  @Test
  @DisplayName("evict: both caches evict")
  void evict_both() {
    service.evict("u");

    verify(onlineCache, times(1)).evict("u");
    verify(offlineCache, times(1)).evict("u");
    verifyNoMoreInteractions(onlineCache, offlineCache);
    verifyNoInteractions(userAsyncService);
  }
}
