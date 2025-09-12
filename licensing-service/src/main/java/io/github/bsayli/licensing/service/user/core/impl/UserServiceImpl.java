package io.github.bsayli.licensing.service.user.core.impl;

import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.repository.exception.UserNotFoundException;
import io.github.bsayli.licensing.repository.user.UserRepository;
import io.github.bsayli.licensing.service.exception.ConnectionExceptionPredicate;
import io.github.bsayli.licensing.service.exception.internal.LicenseServiceInternalException;
import io.github.bsayli.licensing.service.exception.license.LicenseNotFoundException;
import io.github.bsayli.licensing.service.user.core.UserRecoveryService;
import io.github.bsayli.licensing.service.user.core.UserService;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserRecoveryService userRecoveryService;

  public UserServiceImpl(UserRepository userRepository, UserRecoveryService userRecoveryService) {
    this.userRepository = userRepository;
    this.userRecoveryService = userRecoveryService;
  }

  @Override
  @Cacheable(value = "userInfoCache", key = "#userId")
  @Retryable(
      recover = "recoverUser",
      retryFor = {
        SocketException.class,
        SocketTimeoutException.class,
        ConnectTimeoutException.class,
        UnknownHostException.class,
        HttpHostConnectException.class
      },
      maxAttemptsExpression = "#{@retryProperties.userService.maxAttempts}",
      backoff =
          @Backoff(
              delayExpression = "#{@retryProperties.userService.initialDelay}",
              maxDelayExpression = "#{@retryProperties.userService.maxDelay}",
              multiplierExpression = "#{@retryProperties.userService.multiplier}"))
  public LicenseInfo getUser(String userId) {
    return userRepository.getUser(userId);
  }

  @Override
  @CacheEvict(value = "userInfoCache", key = "#userId")
  public LicenseInfo updateLicenseUsage(String userId, String instanceId) {
    try {
      return userRepository.updateLicenseUsage(userId, instanceId);
    } catch (UserNotFoundException e) {
      throw new LicenseNotFoundException(e, userId);
    }
  }

  @Recover
  public LicenseInfo recoverUser(Throwable cause, String userId) {
    if (cause instanceof jakarta.ws.rs.ProcessingException pe
        && ConnectionExceptionPredicate.isConnectionBasedException.test(pe)) {
      return userRecoveryService.recoverUser(userId, pe);
    }
    if (cause instanceof RuntimeException re) throw re;
    throw new LicenseServiceInternalException(cause);
  }
}
