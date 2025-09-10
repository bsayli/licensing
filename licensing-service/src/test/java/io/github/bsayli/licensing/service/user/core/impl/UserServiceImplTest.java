package io.github.bsayli.licensing.service.user.core.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.repository.exception.UserNotFoundException;
import io.github.bsayli.licensing.repository.user.UserRepository;
import io.github.bsayli.licensing.service.exception.license.LicenseNotFoundException;
import io.github.bsayli.licensing.service.user.core.UserRecoveryService;
import jakarta.ws.rs.ProcessingException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: UserServiceImpl")
class UserServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private UserRecoveryService userRecoveryService;

  @InjectMocks private UserServiceImpl service;

  private static LicenseInfo sample(String userId) {
    return new LicenseInfo.Builder()
        .userId(userId)
        .licenseTier("PRO")
        .licenseStatus(io.github.bsayli.licensing.domain.model.LicenseStatus.ACTIVE)
        .expirationDate(LocalDateTime.now().plusDays(3))
        .maxCount(5)
        .remainingUsageCount(5)
        .build();
  }

  @Test
  @DisplayName("getUser returns Optional with value when repository finds user")
  void getUser_present() {
    when(userRepository.getUser("u1")).thenReturn(Optional.of(sample("u1")));

    Optional<LicenseInfo> out = service.getUser("u1");

    assertTrue(out.isPresent());
    assertEquals("u1", out.get().userId());
    verify(userRepository).getUser("u1");
    verifyNoMoreInteractions(userRepository, userRecoveryService);
  }

  @Test
  @DisplayName("getUser returns Optional.empty when repository returns empty")
  void getUser_empty() {
    when(userRepository.getUser("missing")).thenReturn(Optional.empty());

    Optional<LicenseInfo> out = service.getUser("missing");

    assertTrue(out.isEmpty());
    verify(userRepository).getUser("missing");
    verifyNoMoreInteractions(userRepository, userRecoveryService);
  }

  @Test
  @DisplayName("updateLicenseUsage returns updated LicenseInfo")
  void updateLicenseUsage_ok() {
    var updated = Optional.of(sample("u2"));
    when(userRepository.updateLicenseUsage("u2", "inst-1")).thenReturn(updated);

    Optional<LicenseInfo> out = service.updateLicenseUsage("u2", "inst-1");

    assertEquals(updated, out);
    verify(userRepository).updateLicenseUsage("u2", "inst-1");
    verifyNoMoreInteractions(userRepository, userRecoveryService);
  }

  @Test
  @DisplayName(
      "updateLicenseUsage throws LicenseNotFoundException when repository throws UserNotFoundException")
  void updateLicenseUsage_userNotFound() {
    when(userRepository.updateLicenseUsage("u3", "inst-2"))
        .thenThrow(new UserNotFoundException(new RuntimeException()));

    assertThrows(LicenseNotFoundException.class, () -> service.updateLicenseUsage("u3", "inst-2"));
    verify(userRepository).updateLicenseUsage("u3", "inst-2");
    verifyNoMoreInteractions(userRepository, userRecoveryService);
  }

  @Test
  @DisplayName("@Recover recoverUser delegates to UserRecoveryService")
  void recoverUser_delegates() {
    ProcessingException pe =
        new ProcessingException(new java.net.SocketTimeoutException("read timed out"));

    Optional<LicenseInfo> expected = Optional.of(sample("u4"));
    when(userRecoveryService.recoverUser("u4", pe)).thenReturn(expected);

    Optional<LicenseInfo> out = service.recoverUser(pe, "u4");

    assertEquals(expected, out);
    verify(userRecoveryService).recoverUser("u4", pe);
    verifyNoMoreInteractions(userRepository, userRecoveryService);
  }
}
