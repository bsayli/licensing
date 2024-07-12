package com.c9.licensing.service.user.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.c9.licensing.errors.LicenseServiceException;
import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.service.exception.ConnectionExceptionPredicate;
import com.c9.licensing.service.user.UserRecoverService;
import com.c9.licensing.service.user.cache.UserCacheService;

import jakarta.ws.rs.ProcessingException;

@Service
public class UserRecoverServiceImpl implements UserRecoverService{
	
	private final UserCacheService userOfflineCacheService;
	
	public UserRecoverServiceImpl(UserCacheService userOfflineCacheService) {
		this.userOfflineCacheService = userOfflineCacheService;
	}

	@Override
	public Optional<LicenseInfo> recoverGetUser(ProcessingException pe, String userId) {
		boolean isConnectionBasedException = ConnectionExceptionPredicate.isConnectionBasedException.test(pe);
		if (isConnectionBasedException && userOfflineCacheService.isUserCached(userId)) {
			return userOfflineCacheService.getUser(userId);
		} else {
			throw new LicenseServiceException("License Service Error", pe);
		}
	}

}
