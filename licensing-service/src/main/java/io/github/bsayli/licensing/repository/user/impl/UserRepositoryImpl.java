package io.github.bsayli.licensing.repository.user.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bsayli.licensing.domain.model.LicenseChecksumVersionInfo;
import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.domain.model.LicenseServiceIdVersionInfo;
import io.github.bsayli.licensing.domain.model.LicenseStatus;
import io.github.bsayli.licensing.repository.exception.UserAttributeInvalidFormatException;
import io.github.bsayli.licensing.repository.exception.UserAttributeMissingException;
import io.github.bsayli.licensing.repository.exception.UserNotFoundException;
import io.github.bsayli.licensing.repository.user.UserRepository;
import io.github.bsayli.licensing.service.exception.license.LicenseUsageLimitExceededException;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {

  private static final String ATTR_LICENSE_TIER = "licenseTier";
  private static final String ATTR_LICENSE_STATUS = "licenseStatus";
  private static final String ATTR_LICENSE_EXPIRATION = "licenseExpiration";
  private static final String ATTR_INSTANCE_IDS = "instanceIds";
  private static final String ATTR_MAX_COUNT = "maxCount";
  private static final String ATTR_REMAINING_USAGE_COUNT = "remainingUsageCount";
  private static final String ATTR_ALLOWED_SERVICES = "allowedServices";
  private static final String ATTR_ALLOWED_SERVICE_VERSIONS = "allowedServiceVersions";
  private static final String ATTR_CHECKSUM_CRM = "checksumCrm";
  private static final String ATTR_CHECKSUM_BILLING = "checksumBilling";
  private static final String ATTR_CHECKSUM_REPORTING = "checksumReporting";

  private static final String SERVICE_CRM = "crm";
  private static final String SERVICE_BILLING = "billing";
  private static final String SERVICE_REPORTING = "reporting";

  private final Keycloak keycloak;

  @Value("${keycloak.realm}")
  private String realm;

  public UserRepositoryImpl(Keycloak keycloak) {
    this.keycloak = keycloak;
  }

  @Override
  public LicenseInfo getUser(String userId) {
    UsersResource users = getUsersResource();
    try {
      UserResource userRes = users.get(userId);
      UserRepresentation rep = userRes.toRepresentation();
      return toLicenseInfo(rep);
    } catch (NotFoundException e) {
      throw new UserNotFoundException(e);
    }
  }

  @Override
  public LicenseInfo updateLicenseUsage(String userId, String appInstanceId) {
    UsersResource users = getUsersResource();
    try {
      UserResource ur = users.get(userId);
      UserRepresentation rep = ur.toRepresentation();
      Map<String, List<String>> attrs = rep.getAttributes();

      List<String> instanceIds = new ArrayList<>(attrs.getOrDefault(ATTR_INSTANCE_IDS, List.of()));

      if (!instanceIds.contains(appInstanceId)) {
        int remaining = Integer.parseInt(getSingleAttribute(attrs, ATTR_REMAINING_USAGE_COUNT));
        if (remaining > 0) {
          remaining--;
          attrs.put(ATTR_REMAINING_USAGE_COUNT, List.of(String.valueOf(remaining)));
        } else {
          throw new LicenseUsageLimitExceededException();
        }

        instanceIds.add(appInstanceId);
        attrs.put(ATTR_INSTANCE_IDS, instanceIds);
        rep.setAttributes(attrs);
        ur.update(rep);
      }

      return toLicenseInfo(rep);

    } catch (NotFoundException e) {
      throw new UserNotFoundException(e);
    }
  }

  private LicenseInfo toLicenseInfo(UserRepresentation rep) {
    if (rep == null) {
      throw new UserNotFoundException();
    }

    Map<String, List<String>> attrs = rep.getAttributes();
    Map<String, List<LicenseChecksumVersionInfo>> checksums = new HashMap<>();

    List<LicenseChecksumVersionInfo> crm = getChecksumVersionInfo(attrs, ATTR_CHECKSUM_CRM);
    if (!crm.isEmpty()) checksums.put(SERVICE_CRM, crm);

    List<LicenseChecksumVersionInfo> billing = getChecksumVersionInfo(attrs, ATTR_CHECKSUM_BILLING);
    if (!billing.isEmpty()) checksums.put(SERVICE_BILLING, billing);

    List<LicenseChecksumVersionInfo> reporting =
        getChecksumVersionInfo(attrs, ATTR_CHECKSUM_REPORTING);
    if (!reporting.isEmpty()) checksums.put(SERVICE_REPORTING, reporting);

    return new LicenseInfo.Builder()
        .userId(rep.getId())
        .licenseTier(getSingleAttribute(attrs, ATTR_LICENSE_TIER))
        .licenseStatus(LicenseStatus.from(getSingleAttribute(attrs, ATTR_LICENSE_STATUS)))
        .expirationDate(parseLocalDateTime(getSingleAttribute(attrs, ATTR_LICENSE_EXPIRATION)))
        .maxCount(Integer.parseInt(getSingleAttribute(attrs, ATTR_MAX_COUNT)))
        .remainingUsageCount(
            Integer.parseInt(getSingleAttribute(attrs, ATTR_REMAINING_USAGE_COUNT)))
        .instanceIds(getAttribute(attrs, ATTR_INSTANCE_IDS))
        .allowedServices(getAttribute(attrs, ATTR_ALLOWED_SERVICES))
        .allowedServiceVersions(getServiceIdVersionInfo(attrs, ATTR_ALLOWED_SERVICE_VERSIONS))
        .serviceChecksums(checksums)
        .build();
  }

  private UsersResource getUsersResource() {
    RealmResource realmResource = keycloak.realm(realm);
    return realmResource.users();
  }

  private String getSingleAttribute(Map<String, List<String>> attrs, String name) {
    List<String> values = (attrs != null) ? attrs.get(name) : null;
    if (values == null || values.isEmpty()) {
      throw new UserAttributeMissingException(name);
    }
    return values.getFirst();
  }

  private List<String> getAttribute(Map<String, List<String>> attrs, String name) {
    return (attrs != null)
        ? new ArrayList<>(attrs.getOrDefault(name, List.of()))
        : new ArrayList<>();
  }

  private LocalDateTime parseLocalDateTime(String str) {
    try {
      return LocalDateTime.parse(str, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    } catch (Exception ex) {
      throw new UserAttributeInvalidFormatException(ATTR_LICENSE_EXPIRATION, str, ex);
    }
  }

  private List<LicenseChecksumVersionInfo> getChecksumVersionInfo(
      Map<String, List<String>> attrs, String name) {
    List<String> vals = attrs.getOrDefault(name, List.of());
    List<LicenseChecksumVersionInfo> result = new ArrayList<>(vals.size());
    for (String json : vals) {
      result.add(fromJson(json, LicenseChecksumVersionInfo.class, name));
    }
    return result;
  }

  private List<LicenseServiceIdVersionInfo> getServiceIdVersionInfo(
      Map<String, List<String>> attrs, String name) {
    List<String> vals = attrs.getOrDefault(name, List.of());
    List<LicenseServiceIdVersionInfo> result = new ArrayList<>(vals.size());
    for (String json : vals) {
      result.add(fromJson(json, LicenseServiceIdVersionInfo.class, name));
    }
    return result;
  }

  private <T> T fromJson(String json, Class<T> type, String attrName) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(json, type);
    } catch (JsonProcessingException e) {
      throw new UserAttributeInvalidFormatException(attrName, json, e);
    }
  }
}
