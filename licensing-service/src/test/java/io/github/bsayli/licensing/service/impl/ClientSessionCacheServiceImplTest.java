package io.github.bsayli.licensing.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.domain.model.ClientInfo;
import io.github.bsayli.licensing.domain.model.ClientSessionSnapshot;
import io.github.bsayli.licensing.generator.ClientIdGenerator;
import io.github.bsayli.licensing.service.ClientSessionCacheService;
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
@DisplayName("ClientSessionCacheServiceImpl")
class ClientSessionCacheServiceImplTest {

  @Mock private CacheManager cacheManager;
  @Mock private Cache cache;
  @Mock private ClientIdGenerator idGen;

  @Test
  @DisplayName("constructor: cache yoksa fail-fast -> IllegalStateException")
  void ctor_cache_missing_failfast() {
    // arrange
    when(cacheManager.getCache("activeClients")).thenReturn(null);

    // act + assert
    assertThrows(
        IllegalStateException.class, () -> new ClientSessionCacheServiceImpl(cacheManager, idGen));
  }

  @Test
  @DisplayName("constructor: cache varsa - put çağrısı snapshot'ı yazar")
  void put_cache_present_stores_snapshot() {
    // arrange
    when(cacheManager.getCache("activeClients")).thenReturn(cache);

    ClientInfo info = mock(ClientInfo.class);
    when(info.licenseToken()).thenReturn("tok-123");
    when(info.encUserId()).thenReturn("encU");
    when(info.serviceId()).thenReturn("svcA");
    when(info.serviceVersion()).thenReturn("1.2.3");
    when(info.checksum()).thenReturn("sum-xyz");

    when(idGen.getClientId(info)).thenReturn("cid-1");

    ClientSessionCacheService svc = new ClientSessionCacheServiceImpl(cacheManager, idGen);

    // act
    svc.put(info);

    // assert
    ArgumentCaptor<ClientSessionSnapshot> valueCap =
        ArgumentCaptor.forClass(ClientSessionSnapshot.class);
    verify(cache).put(eq("cid-1"), valueCap.capture());
    ClientSessionSnapshot v = valueCap.getValue();
    assertEquals("tok-123", v.licenseToken());
    assertEquals("encU", v.encUserId());
    assertEquals("svcA", v.serviceId());
    assertEquals("1.2.3", v.serviceVersion());
    assertEquals("sum-xyz", v.checksum());
  }

  @Test
  @DisplayName("find: cache hit -> Optional.of(value)")
  void find_cache_hit() {
    when(cacheManager.getCache("activeClients")).thenReturn(cache);

    ClientSessionSnapshot stored =
        ClientSessionSnapshot.builder()
            .licenseToken("tok-123")
            .encUserId("encU")
            .serviceId("svcA")
            .serviceVersion("1.2.3")
            .checksum("sum-xyz")
            .build();

    Cache.ValueWrapper wrapper = mock(Cache.ValueWrapper.class);
    when(wrapper.get()).thenReturn(stored);
    when(cache.get("cid-1")).thenReturn(wrapper);

    ClientSessionCacheService svc = new ClientSessionCacheServiceImpl(cacheManager, idGen);
    Optional<ClientSessionSnapshot> out = svc.find("cid-1");

    assertTrue(out.isPresent());
    assertSame(stored, out.get());
  }
}
