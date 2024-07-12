package com.c9.licensing.service.user.repository.impl;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.c9.licensing.errors.LicenseUsageLimitExceededException;
import com.c9.licensing.errors.repository.UserNotFoundException;
import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.service.user.repository.UserRepository;

import jakarta.ws.rs.NotFoundException;

@Repository
public class UserRepositoryImpl implements UserRepository {

	@Value("${keycloak.realm}")
	private String realm;
	private Keycloak keycloak;

	public UserRepositoryImpl(Keycloak keycloak) {
		this.keycloak = keycloak;
	}

	@Override
	public Optional<LicenseInfo> getUser(String userId) {
		Optional<LicenseInfo> userInfoOpt = Optional.empty();
		UsersResource usersResource = getUsersResource();
		UserResource userResource;
		try {
			userResource = usersResource.get(userId);
			UserRepresentation userRepresentation = userResource.toRepresentation();
			if (userRepresentation != null) {
				Map<String, List<String>> customAttributes = userRepresentation.getAttributes();
				LicenseInfo userInfo = new LicenseInfo.Builder().userId(userRepresentation.getId())
						.licenseTier(getSingleAttribute(customAttributes, ATTR_LICENSE_TIER))
						.licenseStatus(getSingleAttribute(customAttributes, ATTR_LICENSE_STATUS))
						.expirationDate(parseLocalDateTime(getSingleAttribute(customAttributes, ATTR_LICENSE_EXPIRATION)))
						.maxCount(Integer.parseInt(getSingleAttribute(customAttributes, ATTR_MAX_COUNT)))
						.remainingUsageCount(
								Integer.parseInt(getSingleAttribute(customAttributes, ATTR_REMAINING_USAGE_COUNT)))
						.instanceIds(getAttribute(customAttributes, ATTR_INSTANCE_IDS)).build();

				userInfoOpt = Optional.of(userInfo);
			}

		} catch (NotFoundException e) {
			logger.error("User Not Found", e);
		}

		return userInfoOpt;
	}

	@Override
	public void updateLicenseUsage(String userId, String appInstanceId) {
		UsersResource usersResource = getUsersResource();
		try {
			UserRepresentation user = usersResource.get(userId).toRepresentation();
			Map<String, List<String>> attributes = user.getAttributes();

			List<String> instanceIds = attributes.getOrDefault(ATTR_INSTANCE_IDS, new ArrayList<>());

			if (!instanceIds.contains(appInstanceId)) {
				int remainingUsageCount = Integer.parseInt(getSingleAttribute(attributes, ATTR_REMAINING_USAGE_COUNT));
				if (remainingUsageCount > 0) {
					remainingUsageCount--;
					attributes.put(ATTR_REMAINING_USAGE_COUNT, List.of(String.valueOf(remainingUsageCount)));
				} else {
					throw new LicenseUsageLimitExceededException("No remaining usage available for this license");
				}

				instanceIds.add(appInstanceId);
				attributes.put(ATTR_INSTANCE_IDS, instanceIds);
				keycloak.realm(realm).users().get(userId).update(user);
			}

		} catch (NotFoundException e) {
			throw new UserNotFoundException("User not found", e);
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
		return values.get(0); 
	}

	private List<String> getAttribute(Map<String, List<String>> attributes, String name) {
		return attributes.getOrDefault(name, new ArrayList<>());
	}

	private LocalDateTime parseLocalDateTime(String dateString) {
		return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

}
