package io.github.bsayli.licensing.service.user.cache.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.domain.model.LicenseStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: UserOnlineCacheServiceImpl")
class UserOnlineCacheServiceImplTest {

  private static final String CACHE_NAME = "userInfoCache";

  @Mock private CacheManager cacheManager;
  @Mock private Cache cache;

  private UserOnlineCacheServiceImpl service;

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

  @BeforeEach
  void setUp() {
    service = new UserOnlineCacheServiceImpl(cacheManager);
  }

  @Test
  void get_cacheNull() {
    when(cacheManager.getCache(CACHE_NAME)).thenReturn(null);

    Optional<LicenseInfo> out = service.get("u");

    assertTrue(out.isEmpty());
    verify(cacheManager).getCache(CACHE_NAME);
    verifyNoMoreInteractions(cacheManager);
    verifyNoInteractions(cache);
  }

  @Test
  void get_valueNull() {
    when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);
    when(cache.get("u", LicenseInfo.class)).thenReturn(null);

    Optional<LicenseInfo> out = service.get("u");

    assertTrue(out.isEmpty());
    verify(cacheManager).getCache(CACHE_NAME);
    verify(cache).get("u", LicenseInfo.class);
    verifyNoMoreInteractions(cacheManager, cache);
  }

  @Test
  void get_valuePresent() {
    LicenseInfo li = sample();
    when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);
    when(cache.get("u", LicenseInfo.class)).thenReturn(li);

    Optional<LicenseInfo> out = service.get("u");

    assertTrue(out.isPresent());
    assertEquals(li, out.get());
    verify(cacheManager).getCache(CACHE_NAME);
    verify(cache).get("u", LicenseInfo.class);
    verifyNoMoreInteractions(cacheManager, cache);
  }

  @Test
  void put_cacheNull() {
    when(cacheManager.getCache(CACHE_NAME)).thenReturn(null);

    service.put("u", sample());

    verify(cacheManager).getCache(CACHE_NAME);
    verifyNoMoreInteractions(cacheManager);
    verifyNoInteractions(cache);
  }

  @Test
  void put_cachePresent() {
    LicenseInfo li = sample();
    when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);

    service.put("u", li);

    verify(cacheManager).getCache(CACHE_NAME);
    verify(cache).put("u", li);
    verifyNoMoreInteractions(cacheManager, cache);
  }

  @Test
  void evict_cacheNull() {
    when(cacheManager.getCache(CACHE_NAME)).thenReturn(null);

    service.evict("u");

    verify(cacheManager).getCache(CACHE_NAME);
    verifyNoMoreInteractions(cacheManager);
    verifyNoInteractions(cache);
  }

  @Test
  void evict_cachePresent() {
    when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);

    service.evict("u");

    verify(cacheManager).getCache(CACHE_NAME);
    verify(cache).evict("u");
    verifyNoMoreInteractions(cacheManager, cache);
  }

  @Test
  void exists_cacheNull() {
    when(cacheManager.getCache(CACHE_NAME)).thenReturn(null);

    assertFalse(service.exists("u"));

    verify(cacheManager).getCache(CACHE_NAME);
    verifyNoMoreInteractions(cacheManager);
    verifyNoInteractions(cache);
  }

  @Test
  void exists_valueNull() {
    when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);
    when(cache.get("u")).thenReturn(null);

    assertFalse(service.exists("u"));

    verify(cacheManager).getCache(CACHE_NAME);
    verify(cache).get("u");
    verifyNoMoreInteractions(cacheManager, cache);
  }

  @Test
  void exists_valuePresent() {
    when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);
    when(cache.get("u")).thenReturn(mock(Cache.ValueWrapper.class));

    assertTrue(service.exists("u"));

    verify(cacheManager).getCache(CACHE_NAME);
    verify(cache).get("u");
    verifyNoMoreInteractions(cacheManager, cache);
  }
}
