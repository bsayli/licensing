package com.c9.licensing.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.c9.licensing.errors.LicenseInvalidException;
import com.c9.licensing.model.LicenseInfo;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;

@Service
public class UserService {

	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	@Value("${keycloak.realm}")
	private String realm;

	@Autowired
	private Keycloak keycloak;

	@Cacheable(cacheNames = "userInfoCache", key = "#userId")
	public Optional<LicenseInfo> getUser(String userId) {

		UsersResource usersResource = getUsersResource();
		UserResource userResource;
		
		Optional<LicenseInfo> userInfoOpt = Optional.empty();

		try {
			userResource = usersResource.get(userId);
			UserRepresentation userRepresentation = userResource.toRepresentation();
			if (userRepresentation != null) {
				Map<String, List<String>> customAttributes = userRepresentation.getAttributes();
				LicenseInfo userInfo = new LicenseInfo.Builder()
						.userId(userRepresentation.getId())
						.licenseTier(getSingleAttribute(customAttributes, "licenseTier"))
						.licenseStatus(getSingleAttribute(customAttributes, "licenseStatus"))
						.expirationDate(parseLocalDateTime(getSingleAttribute(customAttributes, "licenseExpiration")))
						.maxCount(Integer.parseInt(getSingleAttribute(customAttributes, "maxCount")))
						.remainingUsageCount(Integer.parseInt(getSingleAttribute(customAttributes, "remainingUsageCount")))
						.instanceIds(getAttribute(customAttributes, "instanceIds"))
						.build();
						
				userInfoOpt = Optional.of(userInfo);
			}
			// ... process user information ...
		} catch (NotFoundException | ProcessingException e) {
			logger.error("User Not Found", e);
			throw new LicenseInvalidException("License Key not found", e);
		} 

		return userInfoOpt;
	}
	
	
	@CacheEvict(cacheNames = "userInfoCache", key = "#userId")
	public void updateLicenseUsage(String userId, String appInstanceId) {
		UsersResource usersResource = getUsersResource();
		
		try {
			UserRepresentation user = usersResource.get(userId).toRepresentation();
			Map<String, List<String>> attributes = user.getAttributes();

			// Get or create instance_ids list
			List<String> instanceIds = attributes.getOrDefault("instanceIds", new ArrayList<>());
		
			// Check if appInstanceId already exists
			if (!instanceIds.contains(appInstanceId)) {
				int remainingUsageCount = Integer.parseInt(getSingleAttribute(attributes, "remainingUsageCount"));
			    if (remainingUsageCount > 0) {
			        remainingUsageCount--;
			        attributes.put("remainingUsageCount", List.of(String.valueOf(remainingUsageCount)));
			    } else {
			        // Handle case where there are no remaining uses (e.g., log, throw exception)
			        throw new RuntimeException("No remaining usage available for this license");
			    }
			    
			    instanceIds.add(appInstanceId);
			    attributes.put("instanceIds", instanceIds);
			    keycloak.realm(realm).users().get(userId).update(user);
			}
			
		} catch (NotFoundException e) {
			logger.error("User Not Found", e);
			throw e;
		} catch (ProcessingException e) {
			logger.error("User Not Found ProcessingException ", e);
			throw e;
		}
		
	}

	private UsersResource getUsersResource() {
		RealmResource realmResource = keycloak.realm(realm);
		return realmResource.users();
	}

	private String getSingleAttribute(Map<String, List<String>> attributes, String name) {
		List<String> values = attributes.get(name);
		if (values == null || values.isEmpty()) {
			throw new IllegalArgumentException("Custom attribute '" + name + "' not found");
		}
		return values.get(0); // Assuming your attribute has a single value
	}
	
	private List<String> getAttribute(Map<String, List<String>> attributes, String name) {
		return attributes.getOrDefault(name, new ArrayList<>());
	}

	// Adapt this to the format Keycloak stores the expiration date
	private LocalDateTime parseLocalDateTime(String dateString) {
		// ... Implementation to parse the date string
		return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}
}
