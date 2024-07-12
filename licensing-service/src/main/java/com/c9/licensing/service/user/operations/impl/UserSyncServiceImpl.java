package com.c9.licensing.service.user.operations.impl;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Optional;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.c9.licensing.errors.LicenseInvalidException;
import com.c9.licensing.errors.repository.UserNotFoundException;
import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.service.user.UserRecoverService;
import com.c9.licensing.service.user.operations.UserSynchService;
import com.c9.licensing.service.user.repository.UserRepository;

import jakarta.ws.rs.ProcessingException;

@Service
public class UserSyncServiceImpl implements UserSynchService {

	private final UserRepository userRepository;
	private final UserRecoverService userRecoverService;

	public UserSyncServiceImpl(UserRepository userRepository, UserRecoverService userRecoverService) {
		this.userRepository = userRepository;
		this.userRecoverService = userRecoverService;
	}

	@Override
	@Retryable(recover = "recoverGetUser", retryFor = { SocketTimeoutException.class, ConnectTimeoutException.class,
			UnknownHostException.class,
			HttpHostConnectException.class }, maxAttemptsExpression = "${retry.licenseService.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.licenseService.initialDelay}", maxDelayExpression = "${retry.licenseService.maxDelay}", multiplierExpression = "${retry.licenseService.multiplier}"))
	public Optional<LicenseInfo> getUser(String userId) throws Exception {
		return userRepository.getUser(userId);
	}

	@Override
	public void updateLicenseUsage(String userId, String appInstanceId) {
		try {
			userRepository.updateLicenseUsage(userId, appInstanceId);
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
