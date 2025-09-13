package io.github.bsayli.licensing.sdk.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.sdk.service.LicenseTokenCacheService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: LicenseTokenCacheServiceImpl")
class LicenseTokenCacheServiceImplTest {

  @Mock private Cache cache;

  private LicenseTokenCacheService newService() {
    return new LicenseTokenCacheServiceImpl(cache);
  }

  @Test
  @DisplayName("put delegates to Cache.put")
  void put_delegates() {
    LicenseTokenCacheService svc = newService();
    svc.put("cid", "jwt-token");
    verify(cache).put("cid", "jwt-token");
    verifyNoMoreInteractions(cache);
  }

  @Test
  @DisplayName("get returns cached value")
  void get_returnsValue() {
    LicenseTokenCacheService svc = newService();
    when(cache.get(eq("cid"), eq(String.class))).thenReturn("jwt-token");
    String v = svc.get("cid");
    assertEquals("jwt-token", v);
    verify(cache).get("cid", String.class);
    verifyNoMoreInteractions(cache);
  }

  @Test
  @DisplayName("get returns null when cache miss")
  void get_returnsNullOnMiss() {
    LicenseTokenCacheService svc = newService();
    when(cache.get(eq("cid"), eq(String.class))).thenReturn(null);
    String v = svc.get("cid");
    assertNull(v);
    verify(cache).get("cid", String.class);
    verifyNoMoreInteractions(cache);
  }

  @Test
  @DisplayName("evict delegates to Cache.evict")
  void evict_delegates() {
    LicenseTokenCacheService svc = newService();
    svc.evict("cid");
    verify(cache).evict("cid");
    verifyNoMoreInteractions(cache);
  }
}
