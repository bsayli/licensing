package io.github.bsayli.licensing.service.jwt.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.domain.model.ClientSessionSnapshot;
import io.github.bsayli.licensing.service.ClientSessionCacheService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: JwtBlacklistServiceImpl")
class JwtBlacklistServiceImplTest {

  private static final String CACHE_NAME = "blacklistedTokens";

  @Test
  @DisplayName("addCurrentTokenToBlacklist should put current token into cache")
  void addCurrentTokenToBlacklist_putsToken() {
    var sessionCache = mock(ClientSessionCacheService.class);
    var cacheManager = mock(CacheManager.class);
    var cache = mock(Cache.class);

    when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);

    var cached = mock(ClientSessionSnapshot.class);
    when(cached.licenseToken()).thenReturn("jwt-abc");
    when(sessionCache.find("client-1")).thenReturn(Optional.of(cached));

    var svc = new JwtBlacklistServiceImpl(sessionCache, cacheManager);

    svc.addCurrentTokenToBlacklist("client-1");

    ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Boolean> val = ArgumentCaptor.forClass(Boolean.class);
    verify(cache).put(key.capture(), val.capture());
    assertEquals("jwt-abc", key.getValue());
    assertEquals(Boolean.TRUE, val.getValue());
  }

  @Test
  @DisplayName("addCurrentTokenToBlacklist should not call put when token missing")
  void addCurrentTokenToBlacklist_noToken_noPut() {
    var sessionCache = mock(ClientSessionCacheService.class);
    var cacheManager = mock(CacheManager.class);
    var cache = mock(Cache.class);

    when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);
    when(sessionCache.find("client-2")).thenReturn(Optional.empty());

    var svc = new JwtBlacklistServiceImpl(sessionCache, cacheManager);

    svc.addCurrentTokenToBlacklist("client-2");

    verify(cache, never()).put(any(), any());
  }

  @Test
  @DisplayName("isBlacklisted should return true only when cache returns TRUE")
  void isBlacklisted_behaviour() {
    var sessionCache = mock(ClientSessionCacheService.class);
    var cacheManager = mock(CacheManager.class);
    var cache = mock(Cache.class);

    when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);

    var svc = new JwtBlacklistServiceImpl(sessionCache, cacheManager);

    when(cache.get("tok-1", Boolean.class)).thenReturn(Boolean.TRUE);
    assertTrue(svc.isBlacklisted("tok-1"));

    when(cache.get("tok-2", Boolean.class)).thenReturn(Boolean.FALSE);
    assertFalse(svc.isBlacklisted("tok-2"));

    when(cache.get("tok-3", Boolean.class)).thenReturn(null);
    assertFalse(svc.isBlacklisted("tok-3"));

    assertFalse(svc.isBlacklisted(null));
  }
}
