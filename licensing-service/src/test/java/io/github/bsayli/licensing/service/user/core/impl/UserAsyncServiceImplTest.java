package io.github.bsayli.licensing.service.user.core.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.domain.model.LicenseStatus;
import io.github.bsayli.licensing.repository.user.UserRepository;
import io.github.bsayli.licensing.service.user.exception.AlreadyProcessingException;
import io.github.bsayli.licensing.service.user.exception.MaxRetryAttemptsExceededException;
import jakarta.ws.rs.ProcessingException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
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
    when(userRepository.getUser("u")).thenReturn(info);

    CompletableFuture<LicenseInfo> f = svc.getUser("u");

    assertTrue(f.isDone());
    assertFalse(f.isCompletedExceptionally());
    assertEquals(info, f.join());
    verify(userRepository, times(1)).getUser("u");
  }

  @Test
  @DisplayName(
      "concurrency: second call for same user -> AlreadyProcessingException; after first finishes, next call succeeds")
  void alreadyProcessing_then_release_after_finish() throws Exception {
    var svc = service();
    var info = sample();

    CountDownLatch started = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);

    when(userRepository.getUser("u"))
        .thenAnswer(
            inv -> {
              started.countDown(); // first call has entered repository
              assertTrue(release.await(2, TimeUnit.SECONDS));
              return info;
            });

    try (ExecutorService exec = Executors.newSingleThreadExecutor()) {
      Future<CompletableFuture<LicenseInfo>> first = exec.submit(() -> svc.getUser("u"));

      assertTrue(started.await(1, TimeUnit.SECONDS), "first call did not reach repo in time");

      // second concurrent call for the same userId -> AlreadyProcessingException as failed future
      CompletableFuture<LicenseInfo> second = svc.getUser("u");
      assertTrue(second.isCompletedExceptionally());
      ExecutionException ex2 = assertThrows(ExecutionException.class, second::get);
      assertInstanceOf(AlreadyProcessingException.class, ex2.getCause());

      // now let the first call complete
      release.countDown();
      assertEquals(info, first.get(2, TimeUnit.SECONDS).join());

      // after slot is released, a new call should succeed
      when(userRepository.getUser("u")).thenReturn(info);
      CompletableFuture<LicenseInfo> third = svc.getUser("u");
      assertTrue(third.isDone());
      assertEquals(info, third.join());
    }
  }

  @Test
  @DisplayName(
      "ProcessingException with connection-based cause -> method rethrows; recover -> MaxRetryAttemptsExceededException")
  void processingException_connectionBased_then_recoverToMaxRetryExceeded() {
    var svc = service();
    ProcessingException pe = new ProcessingException(new SocketTimeoutException("read timed out"));
    when(userRepository.getUser("u")).thenThrow(pe);

    // method rethrows for connection-based errors (retry mekanizması için)
    ProcessingException thrown = assertThrows(ProcessingException.class, () -> svc.getUser("u"));
    assertSame(pe, thrown);

    // recover -> MaxRetryAttemptsExceededException
    CompletableFuture<LicenseInfo> recovered = svc.recoverUser(pe, "u");
    ExecutionException ex = assertThrows(ExecutionException.class, recovered::get);
    assertInstanceOf(MaxRetryAttemptsExceededException.class, ex.getCause());
  }

  @Test
  @DisplayName(
      "ProcessingException with NON-connection cause -> exceptional future; recover propagates same ProcessingException")
  void processingException_nonConnection_then_recoverReturnsSame() {
    var svc = service();

    ProcessingException pe = new ProcessingException(new IllegalStateException("oops"));
    when(userRepository.getUser("u")).thenThrow(pe);

    CompletableFuture<LicenseInfo> f = svc.getUser("u");
    assertTrue(f.isCompletedExceptionally());
    ExecutionException ex0 = assertThrows(ExecutionException.class, f::get);
    assertSame(pe, ex0.getCause());

    CompletableFuture<LicenseInfo> recovered = svc.recoverUser(pe, "u");
    ExecutionException ex = assertThrows(ExecutionException.class, recovered::get);
    assertSame(pe, ex.getCause());
  }

  @Test
  @DisplayName("repository throws RuntimeException -> exceptional future with same cause")
  void repository_generalException() {
    var svc = service();
    RuntimeException boom = new RuntimeException("boom");
    when(userRepository.getUser("u")).thenThrow(boom);

    CompletableFuture<LicenseInfo> f = svc.getUser("u");
    ExecutionException ex = assertThrows(ExecutionException.class, f::get);
    assertSame(boom, ex.getCause());
  }

  @Test
  @DisplayName("recover with non-Exception Throwable -> wraps into LicenseServiceInternalException")
  void recover_nonExceptionThrowable_wraps() {
    var svc = service();
    Throwable t = new Throwable("low-level error");

    CompletableFuture<LicenseInfo> recovered = svc.recoverUser(t, "u");
    ExecutionException ex = assertThrows(ExecutionException.class, recovered::get);
    assertEquals(
        "io.github.bsayli.licensing.service.exception.internal.LicenseServiceInternalException",
        ex.getCause().getClass().getName());
  }

  @Test
  @DisplayName(
      "ProcessingException with UnknownHostException cause (connection-based) -> method rethrows; recover -> MaxRetryAttemptsExceededException")
  void processingException_unknownHost_then_recoverToMaxRetryExceeded() {
    var svc = service();

    ProcessingException pe = new ProcessingException(new UnknownHostException("dns"));
    when(userRepository.getUser("u")).thenThrow(pe);

    ProcessingException thrown = assertThrows(ProcessingException.class, () -> svc.getUser("u"));
    assertSame(pe, thrown);

    CompletableFuture<LicenseInfo> recovered = svc.recoverUser(pe, "u");
    ExecutionException ex = assertThrows(ExecutionException.class, recovered::get);
    assertInstanceOf(MaxRetryAttemptsExceededException.class, ex.getCause());
  }
}
