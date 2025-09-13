package io.github.bsayli.licensing.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.domain.model.ClientInfo;
import io.github.bsayli.licensing.domain.model.ClientSessionSnapshot;
import io.github.bsayli.licensing.generator.ClientIdGenerator;
import io.github.bsayli.licensing.service.ClientSessionCacheService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("ClientSessionCacheServiceImpl")
class ClientSessionCacheServiceImplTest {

  @Mock private Cache cache;
  @Mock private ClientIdGenerator idGen;

  @Test
  @DisplayName("put: builds snapshot correctly and writes to cache")
  void put_writes_snapshot() {
    ClientInfo info = mock(ClientInfo.class);
    when(info.licenseToken()).thenReturn("tok-123");
    when(info.encUserId()).thenReturn("encU");
    when(info.serviceId()).thenReturn("svcA");
    when(info.serviceVersion()).thenReturn("1.2.3");
    when(info.checksum()).thenReturn("sum-xyz");
    when(idGen.getClientId(info)).thenReturn("cid-1");

    ClientSessionCacheService svc = new ClientSessionCacheServiceImpl(cache, idGen);

    svc.put(info);

    ArgumentCaptor<ClientSessionSnapshot> cap =
        ArgumentCaptor.forClass(ClientSessionSnapshot.class);
    verify(cache).put(eq("cid-1"), cap.capture());

    ClientSessionSnapshot v = cap.getValue();
    assertEquals("tok-123", v.licenseToken());
    assertEquals("encU", v.encUserId());
    assertEquals("svcA", v.serviceId());
    assertEquals("1.2.3", v.serviceVersion());
    assertEquals("sum-xyz", v.checksum());

    verifyNoMoreInteractions(cache);
  }

  @Test
  @DisplayName("find: cache hit -> returns value")
  void find_hit() {
    ClientSessionSnapshot stored =
        ClientSessionSnapshot.builder()
            .licenseToken("tok-123")
            .encUserId("encU")
            .serviceId("svcA")
            .serviceVersion("1.2.3")
            .checksum("sum-xyz")
            .build();

    when(cache.get("cid-1", ClientSessionSnapshot.class)).thenReturn(stored);

    ClientSessionCacheService svc = new ClientSessionCacheServiceImpl(cache, idGen);

    ClientSessionSnapshot out = svc.find("cid-1");

    assertNotNull(out);
    assertSame(stored, out);
    verify(cache).get("cid-1", ClientSessionSnapshot.class);
    verifyNoMoreInteractions(cache);
  }

  @Test
  @DisplayName("find: cache miss -> returns null")
  void find_miss() {
    when(cache.get("cid-miss", ClientSessionSnapshot.class)).thenReturn(null);

    ClientSessionCacheService svc = new ClientSessionCacheServiceImpl(cache, idGen);

    ClientSessionSnapshot out = svc.find("cid-miss");

    assertNull(out);
    verify(cache).get("cid-miss", ClientSessionSnapshot.class);
    verifyNoMoreInteractions(cache);
  }
}
