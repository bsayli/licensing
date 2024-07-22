package com.c9.licensing.service.user.impl;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.c9.licensing.errors.LicenseServiceUnexpectedException;
import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.service.exception.ConnectionExceptionPredicate;
import com.c9.licensing.service.user.UserCacheManagementService;
import com.c9.licensing.service.user.operations.UserRecoverService;

import jakarta.ws.rs.ProcessingException;

@Service
public class UserRecoverServiceImpl implements UserRecoverService {

	private final UserCacheManagementService userCacheManagementService;

	public UserRecoverServiceImpl(UserCacheManagementService userCacheManagementService) {
		this.userCacheManagementService = userCacheManagementService;
	}

	@Override
	public Optional<LicenseInfo> recoverGetUser(ProcessingException pe, String userId) {
		boolean isConnectionBasedException = ConnectionExceptionPredicate.isConnectionBasedException.test(pe);
		if (isConnectionBasedException) {
			Map<String, Optional<LicenseInfo>> dataInOffline = userCacheManagementService.getDataInOffline(userId);
			if (dataInOffline.containsKey(userId)) {
				return dataInOffline.get(userId);
			}
		} 
		throw new LicenseServiceUnexpectedException("License Service Unexpected Error", pe);
	}

}
