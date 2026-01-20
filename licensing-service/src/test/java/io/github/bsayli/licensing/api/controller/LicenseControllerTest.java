package io.github.bsayli.licensing.api.controller;

import io.github.bsayli.apicontract.envelope.ServiceResponse;
import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.api.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.api.dto.ValidateAccessRequest;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: LicenseController")
class LicenseControllerTest {

    @Mock
    private LicenseOrchestrationService service;

    @InjectMocks
    private LicenseController controller;

    @Test
    @DisplayName("POST /v1/licenses/access -> 200 OK + ServiceResponse with created token")
    void createAccess() {
        var req =
                new IssueAccessRequest(
                        "crm", "1.2.3", "instance-12345678", "sig-xxxxx", "chk-xxxxx", "LK_...base64...");

        when(service.issueAccess(req)).thenReturn(LicenseAccessResponse.created("jwt-token"));

        ResponseEntity<ServiceResponse<LicenseAccessResponse>> resp = controller.createAccess(req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().getData());

        assertEquals("jwt-token", resp.getBody().getData().licenseToken());

        verify(service).issueAccess(req);
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("POST /v1/licenses/access/validate -> 200 OK + ServiceResponse with refreshed token")
    void validateToken_shouldReturnOkWithRefreshedAccess() {
        String token = "header.jwt.token";
        var req =
                new ValidateAccessRequest("crm", "1.2.3", "instance-12345678", "sig-xxxxx", "chk-xxxxx");

        when(service.validateAccess(req, token)).thenReturn(LicenseAccessResponse.refreshed("new-jwt"));

        ResponseEntity<ServiceResponse<LicenseAccessResponse>> resp = controller.validateAccess(token, req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().getData());

        assertEquals("new-jwt", resp.getBody().getData().licenseToken());

        verify(service).validateAccess(req, token);
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("POST /v1/licenses/access/validate -> 200 OK + ServiceResponse ACTIVE (no token)")
    void validateAccess_shouldReturnOkActive() {
        String token = "header.jwt.token";
        var req =
                new ValidateAccessRequest("billing", "2.0.0", "instance-abcdef12", "sig-yyy", "chk-zzz");

        when(service.validateAccess(req, token)).thenReturn(LicenseAccessResponse.active());

        ResponseEntity<ServiceResponse<LicenseAccessResponse>> resp = controller.validateAccess(token, req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().getData());

        assertNull(resp.getBody().getData().licenseToken());

        verify(service).validateAccess(req, token);
        verifyNoMoreInteractions(service);
    }
}