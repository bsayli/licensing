package com.c9.licensing.service.user.operations;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.c9.licensing.model.LicenseInfo;

import jakarta.ws.rs.ProcessingException;

public interface UserService {
	
	Logger logger = LoggerFactory.getLogger(UserService.class);

	Optional<LicenseInfo> updateLicenseUsage(String userId, String appInstanceId);

	Optional<LicenseInfo> getUser(String userId) throws Exception;

	Optional<LicenseInfo> recoverGetUser(ProcessingException pe, String userId);

}
