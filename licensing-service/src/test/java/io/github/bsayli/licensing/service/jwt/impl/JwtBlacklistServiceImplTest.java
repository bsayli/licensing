package io.github.bsayli.licensing.service.jwt.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.domain.model.ClientSessionSnapshot;
import io.github.bsayli.licensing.service.ClientSessionCacheService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: JwtBlacklistServiceImpl")
class JwtBlacklistServiceImplTest {

  @Test
  @DisplayName("addCurrentTokenToBlacklist -> oturumdaki mevcut token blackliste yazılır")
  void addCurrentTokenToBlacklist_putsToken() {
    var sessionCache = mock(ClientSessionCacheService.class);
    var blacklist = mock(Cache.class);

    var snapshot = mock(ClientSessionSnapshot.class);
    when(snapshot.licenseToken()).thenReturn("jwt-abc");
    when(sessionCache.find("client-1")).thenReturn(snapshot);

    var svc = new JwtBlacklistServiceImpl(sessionCache, blacklist);

    svc.addCurrentTokenToBlacklist("client-1");

    ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Boolean> val = ArgumentCaptor.forClass(Boolean.class);
    verify(blacklist).put(key.capture(), val.capture());
    assertEquals("jwt-abc", key.getValue());
    assertEquals(Boolean.TRUE, val.getValue());
  }

  @Test
  @DisplayName("addCurrentTokenToBlacklist -> snapshot yoksa put çağrılmaz")
  void addCurrentTokenToBlacklist_noToken_noPut() {
    var sessionCache = mock(ClientSessionCacheService.class);
    var blacklist = mock(Cache.class);

    when(sessionCache.find("client-2")).thenReturn(null);

    var svc = new JwtBlacklistServiceImpl(sessionCache, blacklist);

    svc.addCurrentTokenToBlacklist("client-2");

    verify(blacklist, never()).put(any(), any());
  }

  @Test
  @DisplayName("isBlacklisted -> yalnızca cache TRUE dönerse true")
  void isBlacklisted_behaviour() {
    var sessionCache = mock(ClientSessionCacheService.class);
    var blacklist = mock(Cache.class);

    var svc = new JwtBlacklistServiceImpl(sessionCache, blacklist);

    when(blacklist.get("tok-1", Boolean.class)).thenReturn(Boolean.TRUE);
    assertTrue(svc.isBlacklisted("tok-1"));

    when(blacklist.get("tok-2", Boolean.class)).thenReturn(Boolean.FALSE);
    assertFalse(svc.isBlacklisted("tok-2"));

    when(blacklist.get("tok-3", Boolean.class)).thenReturn(null);
    assertFalse(svc.isBlacklisted("tok-3"));

    assertFalse(svc.isBlacklisted(null));
  }
}
