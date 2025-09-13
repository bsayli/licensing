package io.github.bsayli.licensing.repository.user.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.domain.model.LicenseStatus;
import io.github.bsayli.licensing.repository.exception.UserAttributeInvalidFormatException;
import io.github.bsayli.licensing.repository.exception.UserAttributeMissingException;
import io.github.bsayli.licensing.repository.exception.UserNotFoundException;
import io.github.bsayli.licensing.service.exception.license.LicenseUsageLimitExceededException;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: UserRepositoryImpl")
class UserRepositoryImplTest {

  @Mock private Keycloak keycloak;
  @Mock private RealmResource realmResource;
  @Mock private UsersResource usersResource;
  @Mock private UserResource userResource;

  @InjectMocks private UserRepositoryImpl repo;

  private static Map<String, List<String>> baseAttrs() {
    Map<String, List<String>> m = new HashMap<>();
    m.put("licenseTier", List.of("PRO"));
    m.put("licenseStatus", List.of(LicenseStatus.ACTIVE.name()));
    m.put(
        "licenseExpiration",
        List.of(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
    m.put("maxCount", List.of("5"));
    m.put("remainingUsageCount", List.of("5"));
    m.put("instanceIds", new ArrayList<>(List.of("inst-A", "inst-B")));
    m.put("allowedServices", new ArrayList<>(List.of("crm", "billing")));
    m.put(
        "allowedServiceVersions",
        List.of(
            json(Map.of("serviceId", "crm", "licensedMaxVersion", "1.2")),
            json(Map.of("serviceId", "billing", "licensedMaxVersion", "2.0"))));
    m.put("checksumCrm", List.of(json(Map.of("version", "1.2", "checksum", "chk-crm"))));
    m.put("checksumBilling", List.of(json(Map.of("version", "2.0", "checksum", "chk-b"))));
    return m;
  }

  private static String json(Object o) {
    try {
      return new ObjectMapper().writeValueAsString(o);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void wireRealm() {
    ReflectionTestUtils.setField(repo, "realm", "test-realm");
    when(keycloak.realm("test-realm")).thenReturn(realmResource);
    when(realmResource.users()).thenReturn(usersResource);
  }

  @Test
  @DisplayName("getUser maps Keycloak attributes to LicenseInfo")
  void getUser_happyPath() {
    wireRealm();
    UserRepresentation rep = new UserRepresentation();
    rep.setId("user-1");
    rep.setAttributes(baseAttrs());

    when(usersResource.get("user-1")).thenReturn(userResource);
    when(userResource.toRepresentation()).thenReturn(rep);

    LicenseInfo info = repo.getUser("user-1");

    assertEquals("user-1", info.userId());
    assertEquals("PRO", info.licenseTier());
    assertEquals(LicenseStatus.ACTIVE, info.licenseStatus());
    assertEquals(5, info.maxCount());
    assertEquals(5, info.remainingUsageCount());
    assertTrue(info.instanceIds().containsAll(List.of("inst-A", "inst-B")));
    assertTrue(info.allowedServices().contains("crm"));
    assertTrue(info.serviceChecksums().containsKey("crm"));
  }

  @Test
  @DisplayName("getUser throws UserNotFoundException when Keycloak returns 404")
  void getUser_notFound() {
    wireRealm();
    when(usersResource.get("missing")).thenThrow(new NotFoundException("404"));

    assertThrows(UserNotFoundException.class, () -> repo.getUser("missing"));
  }

  @Test
  @DisplayName("getUser throws UserAttributeInvalidFormatException for invalid expiration")
  void getUser_invalidExpiration() {
    wireRealm();
    UserRepresentation rep = new UserRepresentation();
    rep.setId("user-2");
    Map<String, List<String>> attrs = baseAttrs();
    attrs.put("licenseExpiration", List.of("bad-date"));
    rep.setAttributes(attrs);

    when(usersResource.get("user-2")).thenReturn(userResource);
    when(userResource.toRepresentation()).thenReturn(rep);

    assertThrows(UserAttributeInvalidFormatException.class, () -> repo.getUser("user-2"));
  }

  @Test
  @DisplayName("getUser throws UserAttributeMissingException when a required attribute is missing")
  void getUser_missingAttribute() {
    wireRealm();
    UserRepresentation rep = new UserRepresentation();
    rep.setId("user-3");
    Map<String, List<String>> attrs = baseAttrs();
    attrs.remove("licenseTier");
    rep.setAttributes(attrs);

    when(usersResource.get("user-3")).thenReturn(userResource);
    when(userResource.toRepresentation()).thenReturn(rep);

    assertThrows(UserAttributeMissingException.class, () -> repo.getUser("user-3"));
  }

  @Test
  @DisplayName("getUser throws UserAttributeInvalidFormatException for invalid checksum JSON")
  void getUser_invalidChecksumJson() {
    wireRealm();
    UserRepresentation rep = new UserRepresentation();
    rep.setId("user-4");
    Map<String, List<String>> attrs = baseAttrs();
    attrs.put("checksumCrm", List.of("{not-json"));
    rep.setAttributes(attrs);

    when(usersResource.get("user-4")).thenReturn(userResource);
    when(userResource.toRepresentation()).thenReturn(rep);

    assertThrows(UserAttributeInvalidFormatException.class, () -> repo.getUser("user-4"));
  }

  @Test
  @DisplayName("updateLicenseUsage decrements remaining, adds instance, and updates user")
  void updateLicenseUsage_updatesAndReturns() {
    wireRealm();
    UserRepresentation rep = new UserRepresentation();
    rep.setId("user-5");
    Map<String, List<String>> attrs = baseAttrs();
    attrs.put("remainingUsageCount", List.of("2"));
    attrs.put("instanceIds", new ArrayList<>(List.of("inst-A")));
    rep.setAttributes(attrs);

    when(usersResource.get("user-5")).thenReturn(userResource);
    when(userResource.toRepresentation()).thenReturn(rep);

    LicenseInfo info = repo.updateLicenseUsage("user-5", "inst-NEW");

    assertEquals(1, info.remainingUsageCount());
    assertTrue(info.instanceIds().contains("inst-NEW"));

    ArgumentCaptor<UserRepresentation> cap = ArgumentCaptor.forClass(UserRepresentation.class);
    verify(userResource).update(cap.capture());
    UserRepresentation updated = cap.getValue();
    assertEquals(List.of("1"), updated.getAttributes().get("remainingUsageCount"));
    assertTrue(updated.getAttributes().get("instanceIds").contains("inst-NEW"));
  }

  @Test
  @DisplayName(
      "updateLicenseUsage throws LicenseUsageLimitExceededException when no remaining usage")
  void updateLicenseUsage_limitExceeded() {
    wireRealm();
    UserRepresentation rep = new UserRepresentation();
    rep.setId("user-6");
    Map<String, List<String>> attrs = baseAttrs();
    attrs.put("remainingUsageCount", List.of("0"));
    attrs.put("instanceIds", new ArrayList<>(List.of("inst-A")));
    rep.setAttributes(attrs);

    when(usersResource.get("user-6")).thenReturn(userResource);
    when(userResource.toRepresentation()).thenReturn(rep);

    assertThrows(
        LicenseUsageLimitExceededException.class,
        () -> repo.updateLicenseUsage("user-6", "inst-Z"));
    verify(userResource, never()).update(any());
  }

  @Test
  @DisplayName("updateLicenseUsage returns info unchanged when instance already present")
  void updateLicenseUsage_instanceAlreadyExists() {
    wireRealm();
    UserRepresentation rep = new UserRepresentation();
    rep.setId("user-7");
    Map<String, List<String>> attrs = baseAttrs();
    attrs.put("remainingUsageCount", List.of("3"));
    attrs.put("instanceIds", new ArrayList<>(List.of("inst-EXIST")));
    rep.setAttributes(attrs);

    when(usersResource.get("user-7")).thenReturn(userResource);
    when(userResource.toRepresentation()).thenReturn(rep);

    LicenseInfo info = repo.updateLicenseUsage("user-7", "inst-EXIST");

    assertEquals(3, info.remainingUsageCount());
    assertTrue(info.instanceIds().contains("inst-EXIST"));
    verify(userResource, never()).update(any());
  }

  @Test
  @DisplayName("updateLicenseUsage throws UserNotFoundException on 404")
  void updateLicenseUsage_notFound() {
    wireRealm();
    when(usersResource.get("missing")).thenThrow(new NotFoundException("404"));
    assertThrows(UserNotFoundException.class, () -> repo.updateLicenseUsage("missing", "inst"));
  }
}
