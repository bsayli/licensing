package io.github.bsayli.licensing.agent.api.controller;

import io.github.bsayli.licensing.agent.api.dto.LicenseAccessRequest;
import io.github.bsayli.licensing.agent.api.dto.LicenseToken;
import io.github.bsayli.licensing.agent.common.api.ApiResponse;
import io.github.bsayli.licensing.agent.common.i18n.LocalizedMessageResolver;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: LicenseController")
class LicenseControllerTest {

    @Mock
    private LicenseOrchestrationService licenseService;
    @Mock
    private LocalizedMessageResolver messageResolver;

    @InjectMocks
    private LicenseController controller;

    @Test
    @DisplayName("POST /v1/licenses/access -> 200 OK + ApiResponse with LicenseToken")
    void getLicenseToken_ok() {

        var req =
                new LicenseAccessRequest(
                        "L".repeat(100) + "~rnd~" + "A".repeat(64),
                        "crm~host123~00:AA:BB:CC:DD:EE",
                        "c".repeat(40),
                        "crm",
                        "1.5.0");

        when(licenseService.getLicenseToken(req)).thenReturn(new LicenseToken("jwt-abc.def.ghi"));
        when(messageResolver.getMessage("license.validation.success")).thenReturn("License is valid");

        ResponseEntity<ApiResponse<LicenseToken>> resp = controller.getLicenseToken(req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(200, resp.getBody().status());
        assertEquals("License is valid", resp.getBody().message());
        assertNotNull(resp.getBody().data());
        assertEquals("jwt-abc.def.ghi", resp.getBody().data().licenseToken());
        assertTrue(resp.getBody().errors().isEmpty());

        verify(licenseService).getLicenseToken(req);
        verify(messageResolver).getMessage("license.validation.success");
        verifyNoMoreInteractions(licenseService, messageResolver);
    }
}
