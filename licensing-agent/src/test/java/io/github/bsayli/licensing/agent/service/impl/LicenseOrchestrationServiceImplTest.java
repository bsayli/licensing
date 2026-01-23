package io.github.bsayli.licensing.agent.service.impl;

import io.github.bsayli.apicontract.envelope.ServiceResponse;
import io.github.bsayli.licensing.agent.api.dto.LicenseAccessRequest;
import io.github.bsayli.licensing.agent.api.dto.LicenseToken;
import io.github.bsayli.licensing.agent.common.exception.LicensingSdkRemoteServiceException;
import io.github.bsayli.licensing.agent.generator.ClientIdGenerator;
import io.github.bsayli.licensing.agent.generator.SignatureGenerator;
import io.github.bsayli.licensing.agent.service.LicenseTokenCacheService;
import io.github.bsayli.licensing.agent.service.client.LicenseServiceClient;
import io.github.bsayli.licensing.agent.service.handler.LicenseResponseHandler;
import io.github.bsayli.licensing.client.common.problem.ApiProblemException;
import io.github.bsayli.licensing.client.generated.dto.IssueAccessRequest;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.client.generated.dto.ValidateAccessRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: LicenseOrchestrationServiceImpl")
class LicenseOrchestrationServiceImplTest {

    @Mock
    private LicenseServiceClient licenseServiceClient;
    @Mock
    private LicenseTokenCacheService cache;
    @Mock
    private ClientIdGenerator clientIdGenerator;
    @Mock
    private SignatureGenerator signatureGenerator;
    @Mock
    private LicenseResponseHandler responseHandler;

    @InjectMocks
    private LicenseOrchestrationServiceImpl service;

    @Captor
    private ArgumentCaptor<IssueAccessRequest> issueReqCaptor;
    @Captor
    private ArgumentCaptor<ValidateAccessRequest> validateReqCaptor;

    private LicenseAccessRequest sdkReq;

    @BeforeEach
    void setUp() {
        sdkReq = new LicenseAccessRequest("LK_x".repeat(50), "crm~host1~aa:bb", "chk", "crm", "1.5.0");
    }

    @Test
    @DisplayName("Cache miss -> issue + cache + return token")
    void cacheMiss_issueAndCache() {
        when(clientIdGenerator.getClientId(sdkReq)).thenReturn("cid");
        when(cache.get("cid")).thenReturn(null);

        when(signatureGenerator.generateForIssue(any(IssueAccessRequest.class))).thenReturn("sig");

        @SuppressWarnings("unchecked")
        ServiceResponse<LicenseAccessResponse> resp = mock(ServiceResponse.class);
        when(licenseServiceClient.issueAccess(any(IssueAccessRequest.class))).thenReturn(resp);
        when(responseHandler.extractTokenOrThrow(resp)).thenReturn("jwt-1");

        LicenseToken token = service.getLicenseToken(sdkReq);

        assertNotNull(token);
        assertEquals("jwt-1", token.licenseToken());

        verify(licenseServiceClient).issueAccess(issueReqCaptor.capture());
        IssueAccessRequest sent = issueReqCaptor.getValue();
        assertEquals("crm", sent.getServiceId());
        assertEquals("1.5.0", sent.getServiceVersion());
        assertEquals("crm~host1~aa:bb", sent.getInstanceId());
        assertEquals("chk", sent.getChecksum());
        assertNotNull(sent.getLicenseKey());
        assertEquals("sig", sent.getSignature());

        verify(cache).put("cid", "jwt-1");
        verify(licenseServiceClient, never()).validateAccess(anyString(), any());
        verify(signatureGenerator, never()).generateForValidate(anyString(), any());
    }

    @Test
    @DisplayName("Cache hit + validate OK (no refresh token) -> return cached")
    void cacheHit_validate_noRefresh_returnsCached() {
        when(clientIdGenerator.getClientId(sdkReq)).thenReturn("cid");
        when(cache.get("cid")).thenReturn("jwt-old");

        when(signatureGenerator.generateForValidate(eq("jwt-old"), any(ValidateAccessRequest.class)))
                .thenReturn("vsig");

        @SuppressWarnings("unchecked")
        ServiceResponse<LicenseAccessResponse> vResp = mock(ServiceResponse.class);
        when(licenseServiceClient.validateAccess(eq("jwt-old"), any(ValidateAccessRequest.class)))
                .thenReturn(vResp);

        when(responseHandler.extractTokenIfPresent(vResp)).thenReturn(null);

        LicenseToken token = service.getLicenseToken(sdkReq);

        assertNotNull(token);
        assertEquals("jwt-old", token.licenseToken());

        verify(licenseServiceClient).validateAccess(eq("jwt-old"), validateReqCaptor.capture());
        ValidateAccessRequest sent = validateReqCaptor.getValue();
        assertEquals("crm", sent.getServiceId());
        assertEquals("1.5.0", sent.getServiceVersion());
        assertEquals("crm~host1~aa:bb", sent.getInstanceId());
        assertEquals("chk", sent.getChecksum());
        assertEquals("vsig", sent.getSignature());

        verify(cache, never()).put(anyString(), anyString());
        verify(licenseServiceClient, never()).issueAccess(any());
        verify(signatureGenerator, never()).generateForIssue(any());
    }

    @Test
    @DisplayName("Cache hit + validate returns refreshed token -> cache update + return new")
    void cacheHit_validate_refreshed() {
        when(clientIdGenerator.getClientId(sdkReq)).thenReturn("cid");
        when(cache.get("cid")).thenReturn("jwt-old");

        when(signatureGenerator.generateForValidate(eq("jwt-old"), any(ValidateAccessRequest.class)))
                .thenReturn("vsig");

        @SuppressWarnings("unchecked")
        ServiceResponse<LicenseAccessResponse> vResp = mock(ServiceResponse.class);
        when(licenseServiceClient.validateAccess(eq("jwt-old"), any(ValidateAccessRequest.class)))
                .thenReturn(vResp);

        when(responseHandler.extractTokenIfPresent(vResp)).thenReturn("jwt-new");

        LicenseToken token = service.getLicenseToken(sdkReq);

        assertEquals("jwt-new", token.licenseToken());
        verify(cache).put("cid", "jwt-new");
        verify(licenseServiceClient, never()).issueAccess(any());
    }

    @Test
    @DisplayName("Cache hit + validate throws ApiProblemException -> mapped; TOKEN_TOO_OLD -> fallback issue + cache")
    void cacheHit_validate_apiProblemException_tooOld_fallbackIssue() {
        when(clientIdGenerator.getClientId(sdkReq)).thenReturn("cid");
        when(cache.get("cid")).thenReturn("jwt-very-old");

        when(signatureGenerator.generateForValidate(eq("jwt-very-old"), any(ValidateAccessRequest.class)))
                .thenReturn("vsig");

        ApiProblemException validateFail = mock(ApiProblemException.class);

        LicensingSdkRemoteServiceException tooOld =
                new LicensingSdkRemoteServiceException(
                        HttpStatus.UNAUTHORIZED, "TOKEN_IS_TOO_OLD_FOR_REFRESH", "top", List.of("d"));

        when(responseHandler.mapRemoteFailure(validateFail)).thenReturn(tooOld);

        when(licenseServiceClient.validateAccess(eq("jwt-very-old"), any(ValidateAccessRequest.class)))
                .thenThrow(validateFail);

        when(signatureGenerator.generateForIssue(any(IssueAccessRequest.class))).thenReturn("sig");

        @SuppressWarnings("unchecked")
        ServiceResponse<LicenseAccessResponse> iResp = mock(ServiceResponse.class);
        when(licenseServiceClient.issueAccess(any(IssueAccessRequest.class))).thenReturn(iResp);
        when(responseHandler.extractTokenOrThrow(iResp)).thenReturn("jwt-new");

        LicenseToken token = service.getLicenseToken(sdkReq);

        assertEquals("jwt-new", token.licenseToken());
        verify(cache).put("cid", "jwt-new");
        verify(licenseServiceClient).issueAccess(issueReqCaptor.capture());
        assertEquals("crm", issueReqCaptor.getValue().getServiceId());
    }

    @Test
    @DisplayName("Cache miss + issue throws ApiProblemException -> mapped and thrown")
    void cacheMiss_issue_apiProblemException_mappedAndThrown() {
        when(clientIdGenerator.getClientId(sdkReq)).thenReturn("cid");
        when(cache.get("cid")).thenReturn(null);

        when(signatureGenerator.generateForIssue(any(IssueAccessRequest.class))).thenReturn("sig");

        ApiProblemException issueFail = mock(ApiProblemException.class);

        LicensingSdkRemoteServiceException remote =
                new LicensingSdkRemoteServiceException(
                        HttpStatus.BAD_GATEWAY, "REMOTE_ERROR", "top", List.of("d"));

        when(responseHandler.mapRemoteFailure(issueFail)).thenReturn(remote);

        when(licenseServiceClient.issueAccess(any(IssueAccessRequest.class))).thenThrow(issueFail);

        LicensingSdkRemoteServiceException thrown =
                assertThrows(LicensingSdkRemoteServiceException.class, () -> service.getLicenseToken(sdkReq));

        assertSame(remote, thrown);
    }

    @Test
    @DisplayName("Cache hit + validate throws ApiProblemException -> mapped and thrown (not too old)")
    void cacheHit_validate_apiProblemException_mappedAndThrown() {
        when(clientIdGenerator.getClientId(sdkReq)).thenReturn("cid");
        when(cache.get("cid")).thenReturn("jwt-old");

        when(signatureGenerator.generateForValidate(eq("jwt-old"), any(ValidateAccessRequest.class)))
                .thenReturn("vsig");

        ApiProblemException validateFail = mock(ApiProblemException.class);

        LicensingSdkRemoteServiceException remote =
                new LicensingSdkRemoteServiceException(
                        HttpStatus.BAD_REQUEST, "SOME_CODE", "top", List.of("d"));

        when(responseHandler.mapRemoteFailure(validateFail)).thenReturn(remote);

        when(licenseServiceClient.validateAccess(eq("jwt-old"), any(ValidateAccessRequest.class)))
                .thenThrow(validateFail);

        LicensingSdkRemoteServiceException thrown =
                assertThrows(LicensingSdkRemoteServiceException.class, () -> service.getLicenseToken(sdkReq));

        assertSame(remote, thrown);
        verify(licenseServiceClient, never()).issueAccess(any());
    }
}