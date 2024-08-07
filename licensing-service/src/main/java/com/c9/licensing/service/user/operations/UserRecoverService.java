package com.c9.licensing.service.user.operations;

import java.util.Optional;

import com.c9.licensing.model.LicenseInfo;

import jakarta.ws.rs.ProcessingException;

public interface UserRecoverService {

	Optional<LicenseInfo> recoverGetUser(ProcessingException pe, String userId);

}
