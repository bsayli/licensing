package io.github.bsayli.licensing.api.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.api.dto.IssueTokenRequest;
import io.github.bsayli.licensing.api.dto.LicenseValidationResponse;
import io.github.bsayli.licensing.api.dto.ValidateTokenRequest;
import io.github.bsayli.licensing.common.api.ApiResponse;
import io.github.bsayli.licensing.common.i18n.LocalizedMessageResolver;
import io.github.bsayli.licensing.service.LicenseOrchestrationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: LicenseValidationController")
class LicenseValidationControllerTest {

  @Mock private LicenseOrchestrationService service;
  @Mock private LocalizedMessageResolver messageResolver;

  @InjectMocks private LicenseValidationController controller;

  @Test
  @DisplayName("POST /v1/licenses/tokens -> 200 OK + ApiResponse with created token")
  void issueToken_shouldReturnOkWithToken() {
    var req =
        new IssueTokenRequest(
            "crm",
            "1.2.3",
            "instance-12345678",
            "sig-xxxxx",
            "chk-xxxxx",
            "LK_...base64...",
            false);

    when(service.issueToken(req)).thenReturn(LicenseValidationResponse.created("jwt-token"));
    when(messageResolver.getMessage("license.validation.success")).thenReturn("License is valid");

    ResponseEntity<ApiResponse<LicenseValidationResponse>> resp = controller.issueToken(req);

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertNotNull(resp.getBody());
    assertEquals(200, resp.getBody().status());
    assertEquals("License is valid", resp.getBody().message());
    assertNotNull(resp.getBody().data());
    assertEquals("jwt-token", resp.getBody().data().licenseToken());
    assertTrue(resp.getBody().errors().isEmpty());

    verify(service).issueToken(req);
    verify(messageResolver).getMessage("license.validation.success");
    verifyNoMoreInteractions(service, messageResolver);
  }

  @Test
  @DisplayName("POST /v1/licenses/tokens/validate -> 200 OK + ApiResponse with refreshed token")
  void validateToken_shouldReturnOkWithRefreshedToken() {
    String token = "header.jwt.token";
    var req =
        new ValidateTokenRequest("crm", "1.2.3", "instance-12345678", "sig-xxxxx", "chk-xxxxx");

    when(service.validateToken(req, token))
        .thenReturn(LicenseValidationResponse.refreshed("new-jwt"));
    when(messageResolver.getMessage("license.validation.success")).thenReturn("License is valid");

    ResponseEntity<ApiResponse<LicenseValidationResponse>> resp =
        controller.validateToken(token, req);

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertNotNull(resp.getBody());
    assertEquals(200, resp.getBody().status());
    assertEquals("License is valid", resp.getBody().message());
    assertNotNull(resp.getBody().data());
    assertEquals("new-jwt", resp.getBody().data().licenseToken());
    assertTrue(resp.getBody().errors().isEmpty());

    verify(service).validateToken(req, token);
    verify(messageResolver).getMessage("license.validation.success");
    verifyNoMoreInteractions(service, messageResolver);
  }

  @Test
  @DisplayName("POST /v1/licenses/tokens/validate -> 200 OK + ApiResponse ACTIVE (no token)")
  void validateToken_shouldReturnOkActive() {
    String token = "header.jwt.token";
    var req =
        new ValidateTokenRequest("billing", "2.0.0", "instance-abcdef12", "sig-yyy", "chk-zzz");

    when(service.validateToken(req, token)).thenReturn(LicenseValidationResponse.active());
    when(messageResolver.getMessage("license.validation.success")).thenReturn("License is valid");

    ResponseEntity<ApiResponse<LicenseValidationResponse>> resp =
        controller.validateToken(token, req);

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertNotNull(resp.getBody());
    assertEquals("License is valid", resp.getBody().message());
    assertNotNull(resp.getBody().data());
    assertNull(resp.getBody().data().licenseToken());

    verify(service).validateToken(req, token);
    verify(messageResolver).getMessage("license.validation.success");
    verifyNoMoreInteractions(service, messageResolver);
  }
}
