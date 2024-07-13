package com.c9.licensing.service.user.operations.impl;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.service.exception.ConnectionExceptionPredicate;
import com.c9.licensing.service.user.operations.UserAsyncService;
import com.c9.licensing.service.user.operations.errors.AlreadyProcessingException;
import com.c9.licensing.service.user.repository.UserRepository;

import jakarta.ws.rs.ProcessingException;

@Service
public class UserAsyncServiceImpl implements UserAsyncService {

	private final ConcurrentMap<String, Boolean> ongoingProcesses = new ConcurrentHashMap<>();

	private UserRepository userRepository;

	public UserAsyncServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Async
	@Retryable(retryFor = { SocketException.class, SocketTimeoutException.class, ConnectTimeoutException.class,
			UnknownHostException.class,
			HttpHostConnectException.class }, maxAttemptsExpression = "${retry.licenseServiceAsync.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.licenseServiceAsync.initialDelay}", maxDelayExpression = "${retry.licenseServiceAsync.maxDelay}", multiplierExpression = "${retry.licenseServiceAsync.multiplier}"))
	public CompletableFuture<Optional<LicenseInfo>> getUser(String userId) throws Exception {
		boolean processAcquired = ongoingProcesses.putIfAbsent(userId, true) == null;
		if (!processAcquired) {
			CompletableFuture<Optional<LicenseInfo>> future = new CompletableFuture<>();
			future.completeExceptionally(new AlreadyProcessingException(userId));
			return future;
		}

		logger.info("Processing asynchronously with Thread {}", Thread.currentThread().getName());
		CompletableFuture<Optional<LicenseInfo>> future = new CompletableFuture<>();
		try {
			Optional<LicenseInfo> licenseInfo = userRepository.getUser(userId);
			logger.info("licenseInfo: {}", licenseInfo);
			future.complete(licenseInfo);
		} catch (ProcessingException pe) {
			boolean isConnectionBasedException = ConnectionExceptionPredicate.isConnectionBasedException.test(pe);
			if (isConnectionBasedException) {
				throw pe;
			}
			future.completeExceptionally(pe);
		} catch (Exception e) {
			logger.error("Error retrieving user information", e);
			future.completeExceptionally(e); // Handle exceptions in the future
		} finally {
			ongoingProcesses.remove(userId);
		}
		logger.info("Completed async method with Thread {}", Thread.currentThread().getName());
		return future;
	}

}
