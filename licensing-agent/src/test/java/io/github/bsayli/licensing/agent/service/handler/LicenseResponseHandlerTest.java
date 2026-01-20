package io.github.bsayli.licensing.agent.service.handler;

import io.github.bsayli.apicontract.envelope.ServiceResponse;
import io.github.bsayli.licensing.agent.common.exception.LicensingSdkRemoteServiceException;
import io.github.bsayli.licensing.agent.common.i18n.LocalizedMessageResolver;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: LicenseResponseHandler")
class LicenseResponseHandlerTest {

    @Mock private LocalizedMessageResolver messages;
    @InjectMocks private LicenseResponseHandler handler;

    @Test
    @DisplayName("extractTokenOrThrow -> token present")
    void extractTokenOrThrow_withToken() {
        @SuppressWarnings("unchecked")
        ServiceResponse<LicenseAccessResponse> resp = mock(ServiceResponse.class);
        LicenseAccessResponse data = mock(LicenseAccessResponse.class);
        when(resp.getData()).thenReturn(data);
        when(data.getLicenseToken()).thenReturn("jwt-token");

        String token = handler.extractTokenOrThrow(resp);

        assertEquals("jwt-token", token);
    }

    @Test
    @DisplayName("extractTokenOrThrow -> empty token -> EMPTY_TOKEN")
    void extractTokenOrThrow_emptyToken_throws() {
        when(messages.getMessage("sdk.remote.empty.token.top")).thenReturn("top");
        when(messages.getMessage("sdk.remote.empty.token.detail")).thenReturn("detail");

        @SuppressWarnings("unchecked")
        ServiceResponse<LicenseAccessResponse> resp = mock(ServiceResponse.class);
        LicenseAccessResponse data = mock(LicenseAccessResponse.class);
        when(resp.getData()).thenReturn(data);
        when(data.getLicenseToken()).thenReturn("  ");

        LicensingSdkRemoteServiceException ex =
                assertThrows(LicensingSdkRemoteServiceException.class, () -> handler.extractTokenOrThrow(resp));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getHttpStatus());
        assertEquals("EMPTY_TOKEN", ex.getErrorCode());
        assertEquals("top", ex.getTopMessage());
        assertEquals(List.of("detail"), ex.getDetails());
    }

    @Test
    @DisplayName("extractTokenIfPresent -> no data -> null")
    void extractTokenIfPresent_noData_returnsNull() {
        @SuppressWarnings("unchecked")
        ServiceResponse<LicenseAccessResponse> resp = mock(ServiceResponse.class);
        when(resp.getData()).thenReturn(null);

        String token = handler.extractTokenIfPresent(resp);

        assertNull(token);
    }

    @Test
    @DisplayName("extractTokenOrThrow -> null response -> EMPTY_TOKEN")
    void extractTokenOrThrow_nullResponse_throws() {
        when(messages.getMessage("sdk.remote.empty.token.top")).thenReturn("top");
        when(messages.getMessage("sdk.remote.empty.token.detail")).thenReturn("detail");

        LicensingSdkRemoteServiceException ex =
                assertThrows(
                        LicensingSdkRemoteServiceException.class,
                        () -> handler.extractTokenOrThrow(null));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getHttpStatus());
        assertEquals("EMPTY_TOKEN", ex.getErrorCode());
        assertEquals("top", ex.getTopMessage());
        assertEquals(List.of("detail"), ex.getDetails());
    }

    @Test
    @DisplayName("extractTokenIfPresent -> blank token -> null")
    void extractTokenIfPresent_blankToken_returnsNull() {
        @SuppressWarnings("unchecked")
        ServiceResponse<LicenseAccessResponse> resp = mock(ServiceResponse.class);
        LicenseAccessResponse data = mock(LicenseAccessResponse.class);

        when(resp.getData()).thenReturn(data);
        when(data.getLicenseToken()).thenReturn("   ");

        String token = handler.extractTokenIfPresent(resp);

        assertNull(token);
    }

}
