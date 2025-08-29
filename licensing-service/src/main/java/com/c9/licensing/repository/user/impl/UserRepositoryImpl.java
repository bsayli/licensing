package com.c9.licensing.repository.user.impl;

import com.c9.licensing.model.LicenseChecksumVersionInfo;
import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.model.LicenseServiceIdVersionInfo;
import com.c9.licensing.model.errors.LicenseUsageLimitExceededException;
import com.c9.licensing.model.errors.repository.UserNotFoundException;
import com.c9.licensing.repository.user.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.NotFoundException;
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
    Optional<LicenseInfo> userLicenseInfoOptional = Optional.empty();
    UsersResource usersResource = getUsersResource();
    try {
      UserResource userResource = usersResource.get(userId);
      UserRepresentation userRepresentation = userResource.toRepresentation();
      userLicenseInfoOptional = getUserLicenseInfo(userRepresentation);
    } catch (NotFoundException e) {
      logger.error("User Not Found: {}", e.getMessage());
    }
    return userLicenseInfoOptional;
  }

  @Override
  public Optional<LicenseInfo> updateLicenseUsage(String userId, String appInstanceId) {
    Optional<LicenseInfo> userLicenseInfoOptional = Optional.empty();
    UsersResource usersResource = getUsersResource();
    try {
      UserRepresentation userRepresentation = usersResource.get(userId).toRepresentation();
      Map<String, List<String>> attributes = userRepresentation.getAttributes();

      List<String> instanceIds = attributes.getOrDefault(ATTR_INSTANCE_IDS, new ArrayList<>());

      if (!instanceIds.contains(appInstanceId)) {
        int remainingUsageCount =
            Integer.parseInt(getSingleAttribute(attributes, ATTR_REMAINING_USAGE_COUNT));
        if (remainingUsageCount > 0) {
          remainingUsageCount--;
          attributes.put(ATTR_REMAINING_USAGE_COUNT, List.of(String.valueOf(remainingUsageCount)));
        } else {
          throw new LicenseUsageLimitExceededException(
              "No remaining usage available for this license");
        }

        instanceIds.add(appInstanceId);
        attributes.put(ATTR_INSTANCE_IDS, instanceIds);
        keycloak.realm(realm).users().get(userId).update(userRepresentation);
        userLicenseInfoOptional = getUserLicenseInfo(userRepresentation);
      }

      return userLicenseInfoOptional;
    } catch (NotFoundException e) {
      throw new UserNotFoundException("User not found", e);
    }
  }

  private Optional<LicenseInfo> getUserLicenseInfo(UserRepresentation userRepresentation) {
    Optional<LicenseInfo> userLicenseInfoOptional = Optional.empty();
    if (userRepresentation != null) {
      Map<String, List<String>> customAttributes = userRepresentation.getAttributes();
      LicenseInfo userInfo =
          new LicenseInfo.Builder()
              .userId(userRepresentation.getId())
              .licenseTier(getSingleAttribute(customAttributes, ATTR_LICENSE_TIER))
              .licenseStatus(getSingleAttribute(customAttributes, ATTR_LICENSE_STATUS))
              .expirationDate(
                  parseLocalDateTime(getSingleAttribute(customAttributes, ATTR_LICENSE_EXPIRATION)))
              .maxCount(Integer.parseInt(getSingleAttribute(customAttributes, ATTR_MAX_COUNT)))
              .remainingUsageCount(
                  Integer.parseInt(
                      getSingleAttribute(customAttributes, ATTR_REMAINING_USAGE_COUNT)))
              .instanceIds(getAttribute(customAttributes, ATTR_INSTANCE_IDS))
              .allowedServices(getAttribute(customAttributes, ATTR_ALLOWED_SERVICES))
              .allowedServiceVersions(
                  getServiceIdVersionInfo(customAttributes, ATTR_ALLOWED_SERVICE_VERSIONS))
              .checksumsCodegen(getChecksumVersionInfo(customAttributes, ATTR_CHECKSUM_CODEGEN))
              .checksumsTestAutomation(
                  getChecksumVersionInfo(customAttributes, ATTR_CHECKSUM_TEST_AUTO))
              .build();

      userLicenseInfoOptional = Optional.of(userInfo);
    }
    return userLicenseInfoOptional;
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

  private List<LicenseChecksumVersionInfo> getChecksumVersionInfo(
      Map<String, List<String>> attributes, String name) {
    List<String> jsonChecksumWithVersions = attributes.getOrDefault(name, new ArrayList<>());
    return jsonChecksumWithVersions.stream()
        .map(
            jsonChecksumWithVersion ->
                fromJson(jsonChecksumWithVersion, LicenseChecksumVersionInfo.class))
        .toList();
  }

  private List<LicenseServiceIdVersionInfo> getServiceIdVersionInfo(
      Map<String, List<String>> attributes, String name) {
    List<String> jsonServiceIdLicensedMaxVersions =
        attributes.getOrDefault(name, new ArrayList<>());
    return jsonServiceIdLicensedMaxVersions.stream()
        .map(
            jsonServiceIdVersion ->
                fromJson(jsonServiceIdVersion, LicenseServiceIdVersionInfo.class))
        .toList();
  }

  private <T> T fromJson(String json, Class<T> classType) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(json, classType);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return null;
  }
}
