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
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.service.exception.ConnectionExceptionPredicate;
import com.c9.licensing.service.user.operations.UserAsyncService;
import com.c9.licensing.service.user.operations.errors.AlreadyProcessingException;
import com.c9.licensing.service.user.operations.errors.MaxRetryAttemptsExceededException;
import com.c9.licensing.service.user.repository.UserRepository;

import jakarta.ws.rs.ProcessingException;

@Service
public class UserAsyncServiceImpl implements UserAsyncService {

	private final ConcurrentMap<String, String> ongoingProcesses = new ConcurrentHashMap<>();

	private UserRepository userRepository;

	public UserAsyncServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Async
	@Retryable(recover = "recoverGetUser", 
			retryFor = {SocketException.class, SocketTimeoutException.class, 
						ConnectTimeoutException.class, UnknownHostException.class, HttpHostConnectException.class }, 
						maxAttemptsExpression = "${retry.userServiceAsync.maxAttempts}", 
						backoff = @Backoff(delayExpression = "${retry.userServiceAsync.initialDelay}", 
						maxDelayExpression = "${retry.userServiceAsync.maxDelay}", 
						multiplierExpression = "${retry.userServiceAsync.multiplier}"))
	public CompletableFuture<Optional<LicenseInfo>> getUser(String userId) throws Exception {
		boolean shouldRetryForConnectionError = false;
		boolean processAcquired = processAcquired(userId);
		if (!processAcquired) {
			CompletableFuture<Optional<LicenseInfo>> future = new CompletableFuture<>();
			AlreadyProcessingException alreadyProcessingException = new AlreadyProcessingException(userId);
			future.completeExceptionally(alreadyProcessingException);
			return future;
		}
		logger.info("Processing asynchronously with Thread {}", Thread.currentThread().getName());
		CompletableFuture<Optional<LicenseInfo>> future = new CompletableFuture<>();
		try {
			Optional<LicenseInfo> licenseInfo = userRepository.getUser(userId);
			future.complete(licenseInfo);
		} catch (ProcessingException pe) {
			shouldRetryForConnectionError = ConnectionExceptionPredicate.isConnectionBasedException.test(pe);
			if (shouldRetryForConnectionError) {
				throw pe;
			}
			future.completeExceptionally(pe);
		} catch (Exception e) {
			logger.error("Processing completed with exception", e);
			future.completeExceptionally(e); 
		} finally {
			if(!shouldRetryForConnectionError) {
				ongoingProcesses.remove(userId);
			} 
		}
		logger.info("Processing completed with Thread {}", Thread.currentThread().getName());
		return future;
	}
	
	@Override
	@Recover
	public CompletableFuture<Optional<LicenseInfo>> recoverGetUser(ProcessingException pe, String userId) {
		CompletableFuture<Optional<LicenseInfo>> future = new CompletableFuture<>();
		MaxRetryAttemptsExceededException maxRetryAttemptsExceededException = new MaxRetryAttemptsExceededException(userId);
		future.completeExceptionally(maxRetryAttemptsExceededException);
		ongoingProcesses.remove(userId);
		return future;
	}
	
	private boolean processAcquired(String userId) {
		String currentThreadName = Thread.currentThread().getName();
		String value = ongoingProcesses.putIfAbsent(userId, currentThreadName);
		return value == null || currentThreadName.equals(value);
	}
	
}
