package io.github.bsayli.licensing.service.user.cache.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.domain.model.LicenseStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: UserOnlineCacheServiceImpl")
class UserOnlineCacheServiceImplTest {

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
  @DisplayName("get: değer yoksa null döner")
  void get_valueNull() {
    when(cache.get("u", LicenseInfo.class)).thenReturn(null);

    var service = new UserOnlineCacheServiceImpl(cache);
    LicenseInfo out = service.get("u");

    assertNull(out);
    verify(cache).get("u", LicenseInfo.class);
    verifyNoMoreInteractions(cache);
  }

  @Test
  @DisplayName("get: değer varsa döner")
  void get_valuePresent() {
    LicenseInfo li = sample();
    when(cache.get("u", LicenseInfo.class)).thenReturn(li);

    var service = new UserOnlineCacheServiceImpl(cache);
    LicenseInfo out = service.get("u");

    assertNotNull(out);
    assertEquals(li, out);
    verify(cache).get("u", LicenseInfo.class);
    verifyNoMoreInteractions(cache);
  }

  @Test
  @DisplayName("put: cache.put'e delege eder")
  void put_delegates() {
    LicenseInfo li = sample();
    var service = new UserOnlineCacheServiceImpl(cache);

    service.put("u", li);

    verify(cache).put("u", li);
    verifyNoMoreInteractions(cache);
  }

  @Test
  @DisplayName("evict: cache.evict'e delege eder")
  void evict_delegates() {
    var service = new UserOnlineCacheServiceImpl(cache);

    service.evict("u");

    verify(cache).evict("u");
    verifyNoMoreInteractions(cache);
  }

  @Test
  @DisplayName("exists: değer yoksa false")
  void exists_valueNull() {
    when(cache.get("u")).thenReturn(null);

    var service = new UserOnlineCacheServiceImpl(cache);
    assertFalse(service.exists("u"));

    verify(cache).get("u");
    verifyNoMoreInteractions(cache);
  }

  @Test
  @DisplayName("exists: değer varsa true")
  void exists_valuePresent() {
    when(cache.get("u")).thenReturn(mock(Cache.ValueWrapper.class));

    var service = new UserOnlineCacheServiceImpl(cache);
    assertTrue(service.exists("u"));

    verify(cache).get("u");
    verifyNoMoreInteractions(cache);
  }
}
