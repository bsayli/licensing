package com.c9.licensing.service;

import java.util.Optional;

public interface LicenseClientCacheManagementService {

	void addClientInfo(String applicationInstanceId, String token, String encUserId);

	Optional<String> getToken(String applicationInstanceId);

	Optional<String> getUserId(String token);

	Optional<String> getUserIdAndEvictToken(String token);

	Optional<String> getUserIdByClientId(String clientId);


}
