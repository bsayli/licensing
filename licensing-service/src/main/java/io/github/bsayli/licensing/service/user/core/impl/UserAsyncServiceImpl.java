package io.github.bsayli.licensing.service.user.core.impl;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.repository.user.UserRepository;
import io.github.bsayli.licensing.service.exception.ConnectionExceptionPredicate;
import io.github.bsayli.licensing.service.user.core.UserAsyncService;
import io.github.bsayli.licensing.service.user.exception.AlreadyProcessingException;
import io.github.bsayli.licensing.service.user.exception.MaxRetryAttemptsExceededException;
import jakarta.ws.rs.ProcessingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class UserAsyncServiceImpl implements UserAsyncService {

  private static final Logger log = LoggerFactory.getLogger(UserAsyncServiceImpl.class);

  private final Set<String> ongoingUsers = ConcurrentHashMap.newKeySet();
  private final UserRepository userRepository;

  public UserAsyncServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Async
  @Retryable(
      recover = "recoverUser",
      retryFor = {
        SocketException.class,
        SocketTimeoutException.class,
        ConnectTimeoutException.class,
        UnknownHostException.class,
        HttpHostConnectException.class
      },
      maxAttemptsExpression = "#{@retryProperties.userServiceAsync.maxAttempts}",
      backoff =
          @Backoff(
              delayExpression = "#{@retryProperties.userServiceAsync.initialDelay}",
              maxDelayExpression = "#{@retryProperties.userServiceAsync.maxDelay}",
              multiplierExpression = "#{@retryProperties.userServiceAsync.multiplier}"))
  public CompletableFuture<Optional<LicenseInfo>> getUser(String userId) {
    if (!ongoingUsers.add(userId)) {
      return CompletableFuture.failedFuture(new AlreadyProcessingException(userId));
    }

    boolean releaseSlot = true;
    try {
      log.debug("Async fetch start userId={} thread={}", userId, Thread.currentThread().getName());
      Optional<LicenseInfo> result = userRepository.getUser(userId);
      return CompletableFuture.completedFuture(result);

    } catch (ProcessingException pe) {
      if (ConnectionExceptionPredicate.isConnectionBasedException.test(pe)) {
        releaseSlot = false;
        throw pe;
      }
      return CompletableFuture.failedFuture(pe);

    } catch (Exception e) {
      log.error("Async fetch failed userId={}", userId, e);
      return CompletableFuture.failedFuture(e);

    } finally {
      if (releaseSlot) {
        ongoingUsers.remove(userId);
        log.debug("Async fetch end userId={} thread={}", userId, Thread.currentThread().getName());
      }
    }
  }

  @Recover
  public CompletableFuture<Optional<LicenseInfo>> recoverUser(
      ProcessingException pe, String userId) {
    ongoingUsers.remove(userId);
    return CompletableFuture.failedFuture(new MaxRetryAttemptsExceededException(userId));
  }
}
