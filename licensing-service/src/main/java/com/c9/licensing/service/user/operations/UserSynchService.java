package com.c9.licensing.service.user.operations;

import java.util.Optional;

import com.c9.licensing.model.LicenseInfo;

import jakarta.ws.rs.ProcessingException;

public interface UserSynchService {

	Optional<LicenseInfo> recoverGetUser(ProcessingException pe, String userId);

	void updateLicenseUsage(String userId, String appInstanceId);

	Optional<LicenseInfo> getUser(String userId) throws Exception;

}
