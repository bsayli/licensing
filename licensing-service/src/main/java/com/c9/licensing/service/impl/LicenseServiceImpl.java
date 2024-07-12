package com.c9.licensing.service.impl;

import org.springframework.stereotype.Service;

import com.c9.licensing.errors.LicenseExpiredException;
import com.c9.licensing.errors.LicenseInactiveException;
import com.c9.licensing.errors.LicenseInvalidException;
import com.c9.licensing.errors.LicenseUsageLimitExceededException;
import com.c9.licensing.errors.TokenExpiredException;
import com.c9.licensing.errors.TokenInvalidException;
import com.c9.licensing.model.LicenseErrorCode;
import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.model.LicenseValidationResult;
import com.c9.licensing.security.UserIdUtil;
import com.c9.licensing.service.LicenseService;
import com.c9.licensing.service.LicenseValidationService;
import com.c9.licensing.service.TokenValidationService;

@Service
public class LicenseServiceImpl implements LicenseService{

	private LicenseValidationService licenseValidationService;
	private TokenValidationService tokenValidationService;
	private UserIdUtil userIdUtil;
	
	public LicenseServiceImpl(LicenseValidationService licenseValidationService,
			TokenValidationService tokenValidationService, UserIdUtil userIdUtil) {
		this.licenseValidationService = licenseValidationService;
		this.tokenValidationService = tokenValidationService;
		this.userIdUtil = userIdUtil;
	}

	public LicenseValidationResult getLicenseResult(String licenseKey, String instanceId) {
		LicenseValidationResult validationResult;
		try {
			LicenseInfo info = licenseValidationService.validateLicense(licenseKey, instanceId);
			validationResult = getValidationResult(instanceId, info, null, LICENSE_KEY_IS_VALID);

		} catch (LicenseInvalidException | LicenseInactiveException | LicenseExpiredException
				| LicenseUsageLimitExceededException e) {
			logger.error(LICENSE_VALIDATION_FAILED, e);
			validationResult = new LicenseValidationResult.Builder()
					.valid(false)
					.errorCode(e.getErrorCode())
					.message(e.getMessage()).build();

		} catch (Exception e) {
			logger.error(ERROR_DURING_LICENSE_VALIDATION, e);
			validationResult = new LicenseValidationResult.Builder()
					.valid(false)
					.errorCode(LicenseErrorCode.UNKNOWN_ERROR)
					.message(ERROR_DURING_LICENSE_VALIDATION).build();
		}
		return validationResult;
	}


	public LicenseValidationResult validateAndRefreshToken(String token, String instanceId) {

		LicenseValidationResult validationResult = null;
		try {
			tokenValidationService.validateToken(token, instanceId);
			validationResult = new LicenseValidationResult.Builder()
					.valid(true)
					.message(TOKEN_IS_VALID)
					.build();

		} catch (TokenInvalidException e) {
			logger.error(LICENSE_VALIDATION_FAILED, e);
			validationResult = new LicenseValidationResult.Builder()
					.valid(false)
					.errorCode(e.getErrorCode())
					.message(e.getMessage()).build();
		} catch (TokenExpiredException e) {
			validationResult = getTokenLicenseValidationResult(e.getTokenSub(), instanceId);
		} catch (Exception e) {
			logger.error(ERROR_DURING_LICENSE_VALIDATION, e);
			validationResult = new LicenseValidationResult.Builder()
					.valid(false)
					.errorCode(LicenseErrorCode.UNKNOWN_ERROR)
					.message(ERROR_DURING_LICENSE_VALIDATION).build();
		}
		return validationResult;
	}

	private LicenseValidationResult getTokenLicenseValidationResult(String tokenSub, String instanceId) {
		LicenseValidationResult validationResult = null;
		try {
			LicenseInfo licenseInfo = licenseValidationService.validateLicenseForToken(tokenSub, instanceId);
			validationResult = getValidationResult(instanceId, licenseInfo, LicenseErrorCode.TOKEN_REFRESHED, TOKEN_REFRESHED);
			
		} catch (LicenseInvalidException | LicenseInactiveException | LicenseExpiredException
				| LicenseUsageLimitExceededException le) {
			logger.error(LICENSE_VALIDATION_FAILED, le);
			validationResult = new LicenseValidationResult.Builder()
					.valid(false)
					.errorCode(le.getErrorCode())
					.message(le.getMessage()).build();

		} catch (Exception ge) {
			logger.error(ERROR_DURING_LICENSE_VALIDATION, ge);
			validationResult = new LicenseValidationResult.Builder()
					.valid(false)
					.errorCode(LicenseErrorCode.UNKNOWN_ERROR)
					.message(ERROR_DURING_LICENSE_VALIDATION).build();
		}
		return validationResult;
	}
	
	private LicenseValidationResult getValidationResult(String instanceId, LicenseInfo info, LicenseErrorCode errorCode, String message) {
		String obfUserID = userIdUtil.obfuscateUserId(info.userId());
		String licenseTier = info.licenseTier();

		return new LicenseValidationResult.Builder().userId(obfUserID).appInstanceId(instanceId)
				.valid(true).licenseStatus(info.licenseStatus()).licenseTier(licenseTier)
				.errorCode(errorCode)
				.message(message).build();
		
	}

}
