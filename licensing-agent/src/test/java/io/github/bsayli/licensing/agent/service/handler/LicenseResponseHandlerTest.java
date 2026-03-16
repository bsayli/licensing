package io.github.bsayli.licensing.agent.service.handler;

import io.github.bsayli.apicontract.envelope.ServiceResponse;
import io.github.bsayli.apicontract.error.ErrorItem;
import io.github.bsayli.apicontract.error.ProblemExtensions;
import io.github.bsayli.licensing.agent.common.exception.LicensingAgentRemoteServiceException;
import io.github.bsayli.licensing.agent.common.i18n.LocalizedMessageResolver;
import io.github.bsayli.licensing.client.common.problem.ApiProblemException;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: LicenseResponseHandler")
class LicenseResponseHandlerTest {

    @Mock
    private LocalizedMessageResolver messages;

    @InjectMocks
    private LicenseResponseHandler handler;

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
        when(messages.getMessage("agent.remote.empty.token.top")).thenReturn("top");
        when(messages.getMessage("agent.remote.empty.token.detail")).thenReturn("detail");

        @SuppressWarnings("unchecked")
        ServiceResponse<LicenseAccessResponse> resp = mock(ServiceResponse.class);
        LicenseAccessResponse data = mock(LicenseAccessResponse.class);

        when(resp.getData()).thenReturn(data);
        when(data.getLicenseToken()).thenReturn("  ");

        LicensingAgentRemoteServiceException ex =
                assertThrows(LicensingAgentRemoteServiceException.class, () -> handler.extractTokenOrThrow(resp));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getHttpStatus());
        assertEquals("EMPTY_TOKEN", ex.getErrorCode());
        assertEquals("top", ex.getTopMessage());
        assertEquals(List.of("detail"), ex.getDetails());
    }

    @Test
    @DisplayName("extractTokenOrThrow -> blank i18n messages -> fallbacks used")
    void extractTokenOrThrow_emptyToken_blankMessages_useFallbacks() {
        when(messages.getMessage("agent.remote.empty.token.top")).thenReturn("   ");
        when(messages.getMessage("agent.remote.empty.token.detail")).thenReturn("");

        @SuppressWarnings("unchecked")
        ServiceResponse<LicenseAccessResponse> resp = mock(ServiceResponse.class);
        LicenseAccessResponse data = mock(LicenseAccessResponse.class);

        when(resp.getData()).thenReturn(data);
        when(data.getLicenseToken()).thenReturn(null);

        LicensingAgentRemoteServiceException ex =
                assertThrows(LicensingAgentRemoteServiceException.class, () -> handler.extractTokenOrThrow(resp));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getHttpStatus());
        assertEquals("EMPTY_TOKEN", ex.getErrorCode());
        assertEquals("Remote call failed", ex.getTopMessage());
        assertEquals(List.of("empty-token"), ex.getDetails());
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
        when(messages.getMessage("agent.remote.empty.token.top")).thenReturn("top");
        when(messages.getMessage("agent.remote.empty.token.detail")).thenReturn("detail");

        LicensingAgentRemoteServiceException ex =
                assertThrows(LicensingAgentRemoteServiceException.class, () -> handler.extractTokenOrThrow(null));

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

    @Test
    @DisplayName("mapRemoteFailure -> prefers ApiProblemException.errorCode and formats messages")
    void mapRemoteFailure_prefersErrorCode_andFormatsMessages() {
        when(messages.getMessage("agent.remote.call.failed")).thenReturn("Top Remote Failed");
        when(messages.getMessage("agent.remote.no.payload")).thenReturn("fallback-detail");

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "upstream detail");
        pd.setType(URI.create("urn:test:problem"));
        pd.setTitle("upstream title");
        pd.setProperty("errorCode", "PD_CODE");
        pd.setProperty(
                "extensions",
                ProblemExtensions.ofErrors(
                        List.of(
                                new ErrorItem("NOT_FOUND", "Customer missing", null, null, null),
                                new ErrorItem("X", "Y", null, null, null))));

        ApiProblemException ape = new ApiProblemException(pd, 404);
        LicensingAgentRemoteServiceException mapped = handler.mapRemoteFailure(ape);

        assertEquals(HttpStatus.NOT_FOUND, mapped.getHttpStatus());
        assertEquals("PD_CODE", mapped.getErrorCode());
        assertEquals("Top Remote Failed", mapped.getTopMessage());
        assertEquals(List.of("Customer missing", "Y"), mapped.getDetails());
    }

    @Test
    @DisplayName("mapRemoteFailure -> no errors -> uses ProblemDetail.detail then title then fallback")
    void mapRemoteFailure_noErrors_usesDetailThenTitleThenFallback() {
        when(messages.getMessage("agent.remote.call.failed")).thenReturn("Top Remote Failed");
        when(messages.getMessage("agent.remote.no.payload")).thenReturn("fallback-detail");

        ProblemDetail pd1 = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, "detail-1");
        pd1.setProperty("errorCode", "E");
        ApiProblemException ape1 = new ApiProblemException(pd1, 502);

        LicensingAgentRemoteServiceException m1 = handler.mapRemoteFailure(ape1);
        assertEquals(HttpStatus.BAD_GATEWAY, m1.getHttpStatus());
        assertEquals(List.of("detail-1"), m1.getDetails());

        ProblemDetail pd2 = ProblemDetail.forStatus(HttpStatus.BAD_GATEWAY);
        pd2.setTitle("title-1");
        pd2.setProperty("errorCode", "E");
        ApiProblemException ape2 = new ApiProblemException(pd2, 502);

        LicensingAgentRemoteServiceException m2 = handler.mapRemoteFailure(ape2);
        assertEquals(List.of("title-1"), m2.getDetails());

        ApiProblemException ape3 = new ApiProblemException(null, 502);
        LicensingAgentRemoteServiceException m3 = handler.mapRemoteFailure(ape3);
        assertEquals(List.of("fallback-detail"), m3.getDetails());
    }

    @Test
    @DisplayName("mapRemoteFailure -> null exception -> fallback http/code/details")
    void mapRemoteFailure_nullException_fallbacks() {
        when(messages.getMessage("agent.remote.call.failed")).thenReturn("Top Remote Failed");
        when(messages.getMessage("agent.remote.no.payload")).thenReturn("fallback-detail");

        LicensingAgentRemoteServiceException mapped = handler.mapRemoteFailure(null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, mapped.getHttpStatus());
        assertEquals("REMOTE_ERROR", mapped.getErrorCode());
        assertEquals("Top Remote Failed", mapped.getTopMessage());
        assertEquals(List.of("fallback-detail"), mapped.getDetails());
    }
}