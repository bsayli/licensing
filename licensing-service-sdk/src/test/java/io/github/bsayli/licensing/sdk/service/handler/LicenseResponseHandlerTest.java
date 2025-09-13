package io.github.bsayli.licensing.sdk.service.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.client.common.contract.ApiClientResponse;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.sdk.common.exception.LicensingSdkRemoteServiceException;
import io.github.bsayli.licensing.sdk.common.i18n.LocalizedMessageResolver;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: LicenseResponseHandler")
class LicenseResponseHandlerTest {

    @Mock private LocalizedMessageResolver messages;
    @InjectMocks private LicenseResponseHandler handler;

    @Test
    @DisplayName("extractTokenOrThrow -> OK + token")
    void extractTokenOrThrow_ok_withToken() {
        @SuppressWarnings("unchecked")
        ApiClientResponse<LicenseAccessResponse> resp = mock(ApiClientResponse.class);
        when(resp.getStatus()).thenReturn(HttpStatus.OK.value());
        LicenseAccessResponse data = mock(LicenseAccessResponse.class);
        when(resp.getData()).thenReturn(data);
        when(data.getLicenseToken()).thenReturn("jwt-token");

        String token = handler.extractTokenOrThrow(resp);

        assertEquals("jwt-token", token);
    }

    @Test
    @DisplayName("extractTokenOrThrow -> OK + empty token -> EMPTY_TOKEN")
    void extractTokenOrThrow_ok_emptyToken_throws() {
        when(messages.getMessage("sdk.remote.empty.token.top")).thenReturn("top");
        when(messages.getMessage("sdk.remote.empty.token.detail")).thenReturn("detail");

        @SuppressWarnings("unchecked")
        ApiClientResponse<LicenseAccessResponse> resp = mock(ApiClientResponse.class);
        when(resp.getStatus()).thenReturn(HttpStatus.OK.value());
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
    @DisplayName("extractTokenIfPresentOrThrow -> OK + no data -> null")
    void extractTokenIfPresentOrThrow_ok_noData_returnsNull() {
        @SuppressWarnings("unchecked")
        ApiClientResponse<LicenseAccessResponse> resp = mock(ApiClientResponse.class);
        when(resp.getStatus()).thenReturn(HttpStatus.OK.value());
        when(resp.getData()).thenReturn(null);

        String token = handler.extractTokenIfPresentOrThrow(resp);

        assertNull(token);
    }

    @Test
    @DisplayName("Non-OK -> throws LicensingSdkRemoteServiceException (defaults)")
    void nonOk_throwsRemoteServiceException() {
        when(messages.getMessage("sdk.remote.call.failed")).thenReturn("remote-failed");
        when(messages.getMessage("sdk.remote.no.payload")).thenReturn("no-payload");

        @SuppressWarnings("unchecked")
        ApiClientResponse<LicenseAccessResponse> resp = mock(ApiClientResponse.class);
        when(resp.getStatus()).thenReturn(HttpStatus.BAD_REQUEST.value());
        when(resp.getMessage()).thenReturn(null);
        when(resp.getErrors()).thenReturn(null);

        LicensingSdkRemoteServiceException ex =
                assertThrows(LicensingSdkRemoteServiceException.class, () -> handler.extractTokenIfPresentOrThrow(resp));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
        assertEquals("REMOTE_ERROR", ex.getErrorCode());
        assertEquals("remote-failed", ex.getTopMessage());
        assertEquals(List.of("no-payload"), ex.getDetails());
    }
}