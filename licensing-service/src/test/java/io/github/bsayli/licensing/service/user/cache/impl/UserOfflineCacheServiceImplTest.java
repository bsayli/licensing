package io.github.bsayli.licensing.service.user.cache.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.domain.model.LicenseStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: UserOfflineCacheServiceImpl")
class UserOfflineCacheServiceImplTest {

  private static final String CACHE_NAME = "userOfflineInfoCache";

  @Mock private CacheManager cacheManager;
  @Mock private Cache cache;

  private static LicenseInfo sample() {
    return new LicenseInfo.Builder()
        .userId("user-1")
        .licenseTier("PRO")
        .licenseStatus(LicenseStatus.ACTIVE)
        .expirationDate(LocalDateTime.now().plusDays(2))
        .instanceIds(List.of("inst-1"))
        .maxCount(3)
        .remainingUsageCount(2)
        .allowedServices(List.of("crm"))
        .build();
  }

  @Test
  @DisplayName("ctor: cache yoksa fail-fast -> IllegalStateException")
  void ctor_cache_missing_failfast() {
    when(cacheManager.getCache(CACHE_NAME)).thenReturn(null);
    assertThrows(IllegalStateException.class, () -> new UserOfflineCacheServiceImpl(cacheManager));
    verify(cacheManager).getCache(CACHE_NAME);
    verifyNoMoreInteractions(cacheManager);
    verifyNoInteractions(cache);
  }

  @Test
  @DisplayName("get: değer yoksa Optional.empty döner")
  void get_valueNull() {
    when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);
    when(cache.get("u", LicenseInfo.class)).thenReturn(null);

    UserOfflineCacheServiceImpl service = new UserOfflineCacheServiceImpl(cacheManager);
    Optional<LicenseInfo> out = service.get("u");

    assertTrue(out.isEmpty());
    verify(cacheManager).getCache(CACHE_NAME);
    verify(cache).get("u", LicenseInfo.class);
    verifyNoMoreInteractions(cacheManager, cache);
  }

  @Test
  @DisplayName("get: değer varsa Optional.of döner")
  void get_valuePresent() {
    LicenseInfo li = sample();
    when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);
    when(cache.get("u", LicenseInfo.class)).thenReturn(li);

    UserOfflineCacheServiceImpl service = new UserOfflineCacheServiceImpl(cacheManager);
    Optional<LicenseInfo> out = service.get("u");

    assertTrue(out.isPresent());
    assertEquals(li, out.get());
    verify(cacheManager).getCache(CACHE_NAME);
    verify(cache).get("u", LicenseInfo.class);
    verifyNoMoreInteractions(cacheManager, cache);
  }

  @Test
  @DisplayName("put: cache.put'e delege eder")
  void put_cachePresent() {
    LicenseInfo li = sample();
    when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);

    UserOfflineCacheServiceImpl service = new UserOfflineCacheServiceImpl(cacheManager);
    service.put("u", li);

    verify(cacheManager).getCache(CACHE_NAME);
    verify(cache).put("u", li);
    verifyNoMoreInteractions(cacheManager, cache);
  }

  @Test
  @DisplayName("evict: cache.evict'e delege eder")
  void evict_cachePresent() {
    when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);

    UserOfflineCacheServiceImpl service = new UserOfflineCacheServiceImpl(cacheManager);
    service.evict("u");

    verify(cacheManager).getCache(CACHE_NAME);
    verify(cache).evict("u");
    verifyNoMoreInteractions(cacheManager, cache);
  }

  @Test
  @DisplayName("exists: değer yoksa false")
  void exists_valueNull() {
    when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);
    when(cache.get("u")).thenReturn(null);

    UserOfflineCacheServiceImpl service = new UserOfflineCacheServiceImpl(cacheManager);
    assertFalse(service.exists("u"));

    verify(cacheManager).getCache(CACHE_NAME);
    verify(cache).get("u");
    verifyNoMoreInteractions(cacheManager, cache);
  }

  @Test
  @DisplayName("exists: değer varsa true")
  void exists_valuePresent() {
    when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);
    Cache.ValueWrapper wrapper = mock(Cache.ValueWrapper.class);
    when(cache.get("u")).thenReturn(wrapper);

    UserOfflineCacheServiceImpl service = new UserOfflineCacheServiceImpl(cacheManager);
    assertTrue(service.exists("u"));

    verify(cacheManager).getCache(CACHE_NAME);
    verify(cache).get("u");
    verifyNoMoreInteractions(cacheManager, cache);
  }
}
