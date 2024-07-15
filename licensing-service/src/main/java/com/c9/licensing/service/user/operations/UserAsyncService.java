package com.c9.licensing.service.user.operations;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.c9.licensing.model.LicenseInfo;

import jakarta.ws.rs.ProcessingException;

public interface UserAsyncService {
	
	Logger logger = LoggerFactory.getLogger(UserAsyncService.class);
	
	CompletableFuture<Optional<LicenseInfo>> getUser(String userId) throws Exception;

	CompletableFuture<Optional<LicenseInfo>> recoverGetUser(ProcessingException pe, String userId);

}
