package io.github.bsayli.licensing.service.user.core.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.domain.model.LicenseStatus;
import io.github.bsayli.licensing.repository.exception.UserNotFoundException;
import io.github.bsayli.licensing.repository.user.UserRepository;
import io.github.bsayli.licensing.service.exception.license.LicenseNotFoundException;
import io.github.bsayli.licensing.service.user.core.UserRecoveryService;
import jakarta.ws.rs.ProcessingException;
import java.time.LocalDateTime;
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
        .licenseStatus(LicenseStatus.ACTIVE)
        .expirationDate(LocalDateTime.now().plusDays(3))
        .maxCount(5)
        .remainingUsageCount(5)
        .build();
  }

  @Test
  @DisplayName("getUser returns LicenseInfo when repository finds user")
  void getUser_present() {
    when(userRepository.getUser("u1")).thenReturn(sample("u1"));

    LicenseInfo out = service.getUser("u1");

    assertEquals("u1", out.userId());
    verify(userRepository).getUser("u1");
    verifyNoMoreInteractions(userRepository, userRecoveryService);
  }

  @Test
  @DisplayName("getUser propagates UserNotFoundException when repo throws 404")
  void getUser_notFound() {
    when(userRepository.getUser("missing"))
        .thenThrow(new UserNotFoundException(new RuntimeException()));

    assertThrows(UserNotFoundException.class, () -> service.getUser("missing"));
    verify(userRepository).getUser("missing");
    verifyNoMoreInteractions(userRepository, userRecoveryService);
  }

  @Test
  @DisplayName("updateLicenseUsage returns updated LicenseInfo")
  void updateLicenseUsage_ok() {
    var updated = sample("u2");
    when(userRepository.updateLicenseUsage("u2", "inst-1")).thenReturn(updated);

    LicenseInfo out = service.updateLicenseUsage("u2", "inst-1");

    assertEquals(updated, out);
    verify(userRepository).updateLicenseUsage("u2", "inst-1");
    verifyNoMoreInteractions(userRepository, userRecoveryService);
  }

  @Test
  @DisplayName("updateLicenseUsage maps UserNotFoundException to LicenseNotFoundException")
  void updateLicenseUsage_userNotFound() {
    when(userRepository.updateLicenseUsage("u3", "inst-2"))
        .thenThrow(new UserNotFoundException(new RuntimeException()));

    assertThrows(LicenseNotFoundException.class, () -> service.updateLicenseUsage("u3", "inst-2"));
    verify(userRepository).updateLicenseUsage("u3", "inst-2");
    verifyNoMoreInteractions(userRepository, userRecoveryService);
  }

  @Test
  @DisplayName("@Recover recoverUser delegates to UserRecoveryService and returns LicenseInfo")
  void recoverUser_delegates() {
    ProcessingException pe =
        new ProcessingException(new java.net.SocketTimeoutException("read timed out"));

    LicenseInfo expected = sample("u4");
    when(userRecoveryService.recoverUser("u4", pe)).thenReturn(expected);

    LicenseInfo out = service.recoverUser(pe, "u4");

    assertEquals(expected, out);
    verify(userRecoveryService).recoverUser("u4", pe);
    verifyNoMoreInteractions(userRepository, userRecoveryService);
  }
}
