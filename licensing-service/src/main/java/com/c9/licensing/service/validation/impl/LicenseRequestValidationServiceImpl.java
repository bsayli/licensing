package com.c9.licensing.service.validation.impl;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.c9.licensing.generator.ClientIdGenerator;
import com.c9.licensing.model.ClientCachedLicenseData;
import com.c9.licensing.model.LicenseValidationRequest;
import com.c9.licensing.model.errors.InvalidRequestException;
import com.c9.licensing.model.errors.TokenAlreadyExistException;
import com.c9.licensing.security.UserIdEncryptor;
import com.c9.licensing.service.LicenseClientCacheManagementService;
import com.c9.licensing.service.validation.LicenseRequestValidationService;

@Service
public class LicenseRequestValidationServiceImpl implements LicenseRequestValidationService{
	
	private final LicenseClientCacheManagementService clientCacheManagementService;
	private final ClientIdGenerator clientIdGenerator;
	private final UserIdEncryptor userIdEncryptor;
	
	public LicenseRequestValidationServiceImpl(LicenseClientCacheManagementService clientCacheManagementService,
			ClientIdGenerator clientIdGenerator, UserIdEncryptor userIdEncryptor) {
		this.clientCacheManagementService = clientCacheManagementService;
		this.clientIdGenerator = clientIdGenerator;
		this.userIdEncryptor = userIdEncryptor;
	}

	@Override
	public void checkLicenseKeyRequestWithCachedData(LicenseValidationRequest request, String userId) {
		String clientId = clientIdGenerator.getClientId(request.serviceId(), request.serviceVersion(), request.instanceId());
		Optional<ClientCachedLicenseData> cachedLicenseDataOpt = clientCacheManagementService.getClientCachedLicenseData(clientId);
		if (cachedLicenseDataOpt.isPresent()) {
			ClientCachedLicenseData cachedData = cachedLicenseDataOpt.get();
			String cachedUserId = userIdEncryptor.decrypt(cachedData.getEncUserId());
			boolean isServiceIdEqual = Objects.equals(cachedData.getServiceId(), request.serviceId());
			boolean isServiceVersionEqual = Objects.equals(cachedData.getServiceVersion(), request.serviceVersion());
			boolean isChecksumEqual = Objects.equals(cachedData.getChecksum(), request.checksum());
			boolean isUserIdEqual = Objects.equals(cachedUserId, userId);
			boolean isValidRequest =  isServiceIdEqual && isServiceVersionEqual && isChecksumEqual && isUserIdEqual;
			
			if(isValidRequest) {
				throw new TokenAlreadyExistException(TOKEN_ALREADY_EXIST);
			}else {
				throw new InvalidRequestException(INVALID_REQUEST);
			}
		}
	}
	
	@Override
	public void checkTokenRequestWithCachedData(LicenseValidationRequest request) {
		String clientId = clientIdGenerator.getClientId(request.serviceId(), request.serviceVersion(), request.instanceId());
		Optional<ClientCachedLicenseData> cachedLicenseDataOpt = clientCacheManagementService.getClientCachedLicenseData(clientId);
		if (cachedLicenseDataOpt.isPresent()) {
			ClientCachedLicenseData cachedData = cachedLicenseDataOpt.get();
			boolean isTokenEqual = Objects.equals(cachedData.getLicenseToken(), request.licenseToken());
			boolean isServiceIdEqual = Objects.equals(cachedData.getServiceId(), request.serviceId());
			boolean isServiceVersionEqual = Objects.equals(cachedData.getServiceVersion(), request.serviceVersion());
			boolean isChecksumEqual = Objects.equals(cachedData.getChecksum(), request.checksum());
			boolean isValidRequest = isTokenEqual && isServiceIdEqual && isServiceVersionEqual && isChecksumEqual;
			
			if(!isValidRequest) {
				throw new InvalidRequestException(INVALID_REQUEST);
			}
		}
	}
}
