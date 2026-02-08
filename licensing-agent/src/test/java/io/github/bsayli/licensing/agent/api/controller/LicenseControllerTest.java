package io.github.bsayli.licensing.agent.api.controller;

import io.github.bsayli.apicontract.envelope.ServiceResponse;
import io.github.bsayli.licensing.agent.api.dto.LicenseAccessRequest;
import io.github.bsayli.licensing.agent.api.dto.LicenseToken;
import io.github.bsayli.licensing.agent.common.exception.LicensingAgentRemoteServiceException;
import io.github.bsayli.licensing.agent.service.LicenseOrchestrationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: LicenseController")
class LicenseControllerTest {

    @Mock
    private LicenseOrchestrationService licenseService;

    @InjectMocks
    private LicenseController controller;

    @Test
    @DisplayName("POST /v1/licenses/access -> 200 OK + ServiceResponse<LicenseToken>")
    void getLicenseToken_ok() {
        var req =
                new LicenseAccessRequest(
                        "L".repeat(100) + "~rnd~" + "A".repeat(64),
                        "crm~host123~00:AA:BB:CC:DD:EE",
                        "c".repeat(40),
                        "crm",
                        "1.5.0");

        when(licenseService.getLicenseToken(req)).thenReturn(new LicenseToken("jwt-abc.def.ghi"));

        ResponseEntity<ServiceResponse<LicenseToken>> resp = controller.getLicenseToken(req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().getData());
        assertEquals("jwt-abc.def.ghi", resp.getBody().getData().licenseToken());

        verify(licenseService).getLicenseToken(req);
        verifyNoMoreInteractions(licenseService);
    }

    @Test
    @DisplayName("POST /v1/licenses/access -> passes request object as-is (no mapping/mutation)")
    void getLicenseToken_delegatesWithSameRequestInstance() {
        var req =
                new LicenseAccessRequest(
                        "L".repeat(100) + "~rnd~" + "A".repeat(64),
                        "crm~host123~00:AA:BB:CC:DD:EE",
                        "c".repeat(40),
                        "crm",
                        "1.5.0");

        when(licenseService.getLicenseToken(any())).thenReturn(new LicenseToken("jwt-x.y.z"));

        controller.getLicenseToken(req);

        verify(licenseService).getLicenseToken(same(req));
        verifyNoMoreInteractions(licenseService);
    }

    @Test
    @DisplayName("POST /v1/licenses/access -> ServiceResponse meta exists (contract)")
    void getLicenseToken_ok_includesMeta() {
        var req =
                new LicenseAccessRequest(
                        "L".repeat(100) + "~rnd~" + "A".repeat(64),
                        "crm~host123~00:AA:BB:CC:DD:EE",
                        "c".repeat(40),
                        "crm",
                        "1.5.0");

        when(licenseService.getLicenseToken(req)).thenReturn(new LicenseToken("jwt-abc.def.ghi"));

        ResponseEntity<ServiceResponse<LicenseToken>> resp = controller.getLicenseToken(req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().getMeta());
        assertNotNull(resp.getBody().getMeta().serverTime());

        verify(licenseService).getLicenseToken(req);
        verifyNoMoreInteractions(licenseService);
    }

    @Test
    @DisplayName("POST /v1/licenses/access -> service exception is not swallowed (propagates)")
    void getLicenseToken_serviceThrows_propagates() {
        var req =
                new LicenseAccessRequest(
                        "L".repeat(100) + "~rnd~" + "A".repeat(64),
                        "crm~host123~00:AA:BB:CC:DD:EE",
                        "c".repeat(40),
                        "crm",
                        "1.5.0");

        var ex =
                new LicensingAgentRemoteServiceException(
                        HttpStatus.BAD_GATEWAY,
                        "REMOTE_ERROR",
                        "Remote call failed",
                        List.of("x : y"));

        when(licenseService.getLicenseToken(req)).thenThrow(ex);

        LicensingAgentRemoteServiceException thrown =
                assertThrows(LicensingAgentRemoteServiceException.class, () -> controller.getLicenseToken(req));

        assertSame(ex, thrown);

        verify(licenseService).getLicenseToken(req);
        verifyNoMoreInteractions(licenseService);
    }
}