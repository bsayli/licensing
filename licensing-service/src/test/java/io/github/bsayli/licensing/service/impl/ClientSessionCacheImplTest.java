package io.github.bsayli.licensing.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.domain.model.ClientCachedLicenseData;
import io.github.bsayli.licensing.domain.model.ClientInfo;
import io.github.bsayli.licensing.generator.ClientIdGenerator;
import io.github.bsayli.licensing.service.ClientSessionCache;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("ClientSessionCacheImpl")
class ClientSessionCacheImplTest {

  @Mock private CacheManager cacheManager;
  @Mock private Cache cache;
  @Mock private ClientIdGenerator idGen;

  @Test
  @DisplayName("put: cache not found -> no-op (no id generation)")
  void put_cache_null_noop() {
    when(cacheManager.getCache("activeClients")).thenReturn(null);

    ClientSessionCache svc = new ClientSessionCacheImpl(cacheManager, idGen);

    ClientInfo info = mock(ClientInfo.class);
    svc.put(info);

    verify(cacheManager).getCache("activeClients");
    verifyNoMoreInteractions(cacheManager);
    verifyNoInteractions(idGen);
  }

  @Test
  @DisplayName("put: cache present -> stores snapshot built from ClientInfo")
  void put_cache_present_stores_snapshot() {
    when(cacheManager.getCache("activeClients")).thenReturn(cache);

    ClientInfo info = mock(ClientInfo.class);
    when(info.licenseToken()).thenReturn("tok-123");
    when(info.encUserId()).thenReturn("encU");
    when(info.serviceId()).thenReturn("svcA");
    when(info.serviceVersion()).thenReturn("1.2.3");
    when(info.checksum()).thenReturn("sum-xyz");

    when(idGen.getClientId(info)).thenReturn("cid-1");

    ClientSessionCache svc = new ClientSessionCacheImpl(cacheManager, idGen);
    svc.put(info);

    ArgumentCaptor<ClientCachedLicenseData> valueCap =
        ArgumentCaptor.forClass(ClientCachedLicenseData.class);

    verify(cache).put(eq("cid-1"), valueCap.capture());
    ClientCachedLicenseData v = valueCap.getValue();

    assertEquals("tok-123", v.getLicenseToken());
    assertEquals("encU", v.getEncUserId());
    assertEquals("svcA", v.getServiceId());
    assertEquals("1.2.3", v.getServiceVersion());
    assertEquals("sum-xyz", v.getChecksum());
  }

  @Test
  @DisplayName("find: cache not found -> Optional.empty")
  void find_cache_null() {
    when(cacheManager.getCache("activeClients")).thenReturn(null);

    ClientSessionCache svc = new ClientSessionCacheImpl(cacheManager, idGen);
    Optional<ClientCachedLicenseData> out = svc.find("cid-1");

    assertTrue(out.isEmpty());
  }

  @Test
  @DisplayName("find: cache present but miss -> Optional.empty")
  void find_cache_miss() {
    when(cacheManager.getCache("activeClients")).thenReturn(cache);
    when(cache.get("cid-1")).thenReturn(null);

    ClientSessionCache svc = new ClientSessionCacheImpl(cacheManager, idGen);
    Optional<ClientCachedLicenseData> out = svc.find("cid-1");

    assertTrue(out.isEmpty());
  }

  @Test
  @DisplayName("find: cache present and hit -> Optional.of(value)")
  void find_cache_hit() {
    when(cacheManager.getCache("activeClients")).thenReturn(cache);

    ClientCachedLicenseData stored =
        new ClientCachedLicenseData.Builder()
            .licenseToken("tok-123")
            .encUserId("encU")
            .serviceId("svcA")
            .serviceVersion("1.2.3")
            .checksum("sum-xyz")
            .build();

    Cache.ValueWrapper wrapper = mock(Cache.ValueWrapper.class);
    when(wrapper.get()).thenReturn(stored);
    when(cache.get("cid-1")).thenReturn(wrapper);

    ClientSessionCache svc = new ClientSessionCacheImpl(cacheManager, idGen);
    Optional<ClientCachedLicenseData> out = svc.find("cid-1");

    assertTrue(out.isPresent());
    assertSame(stored, out.get());
  }
}
