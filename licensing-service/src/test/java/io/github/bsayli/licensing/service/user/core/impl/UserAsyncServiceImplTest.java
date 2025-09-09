package io.github.bsayli.licensing.service.user.core.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.domain.model.LicenseStatus;
import io.github.bsayli.licensing.repository.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: UserAsyncServiceImpl")
class UserAsyncServiceImplTest {

  @Mock private UserRepository userRepository;

  private static LicenseInfo sample() {
    return new LicenseInfo.Builder()
        .userId("u")
        .licenseTier("PRO")
        .licenseStatus(LicenseStatus.ACTIVE)
        .expirationDate(LocalDateTime.now().plusDays(3))
        .instanceIds(List.of("i1"))
        .maxCount(5)
        .remainingUsageCount(5)
        .allowedServices(List.of("crm"))
        .build();
  }

  private UserAsyncServiceImpl service() {
    return new UserAsyncServiceImpl(userRepository);
  }

  @Test
  @DisplayName("success: repository returns value -> completed future with result")
  void success() {
    var svc = service();
    var info = sample();
    when(userRepository.getUser("u")).thenReturn(Optional.of(info));

    CompletableFuture<Optional<LicenseInfo>> f = svc.getUser("u");

    assertTrue(f.isDone());
    assertFalse(f.isCompletedExceptionally());
    assertTrue(f.join().isPresent());
    assertEquals(info, f.join().get());
    verify(userRepository, times(1)).getUser("u");
  }
}
