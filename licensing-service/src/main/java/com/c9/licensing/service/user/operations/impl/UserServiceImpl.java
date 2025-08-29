package com.c9.licensing.service.user.operations.impl;

import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.model.errors.LicenseInvalidException;
import com.c9.licensing.model.errors.repository.UserNotFoundException;
import com.c9.licensing.repository.user.UserRepository;
import com.c9.licensing.service.user.operations.UserRecoverService;
import com.c9.licensing.service.user.operations.UserService;
import jakarta.ws.rs.ProcessingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Optional;
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
  private final UserRecoverService userRecoverService;

  public UserServiceImpl(UserRepository userRepository, UserRecoverService userRecoverService) {
    this.userRepository = userRepository;
    this.userRecoverService = userRecoverService;
  }

  @Override
  @Cacheable(value = "userInfoCache", key = "#userId")
  @Retryable(
      recover = "recoverGetUser",
      retryFor = {
        SocketException.class,
        SocketTimeoutException.class,
        ConnectTimeoutException.class,
        UnknownHostException.class,
        HttpHostConnectException.class
      },
      maxAttemptsExpression = "${retry.userService.maxAttempts}",
      backoff =
          @Backoff(
              delayExpression = "${retry.userService.initialDelay}",
              maxDelayExpression = "${retry.userService.maxDelay}",
              multiplierExpression = "${retry.userService.multiplier}"))
  public Optional<LicenseInfo> getUser(String userId) throws Exception {
    return userRepository.getUser(userId);
  }

  @Override
  @CacheEvict(value = "userInfoCache", key = "#userId")
  public Optional<LicenseInfo> updateLicenseUsage(String userId, String appInstanceId) {
    try {
      return userRepository.updateLicenseUsage(userId, appInstanceId);
    } catch (UserNotFoundException e) {
      throw new LicenseInvalidException("License key not found", e);
    }
  }

  @Override
  @Recover
  public Optional<LicenseInfo> recoverGetUser(ProcessingException pe, String userId) {
    return userRecoverService.recoverGetUser(pe, userId);
  }
}
