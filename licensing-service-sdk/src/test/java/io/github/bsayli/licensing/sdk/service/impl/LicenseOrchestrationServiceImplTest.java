package io.github.bsayli.licensing.sdk.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.client.common.contract.ApiClientResponse;
import io.github.bsayli.licensing.client.common.exception.ApiClientException;
import io.github.bsayli.licensing.client.generated.dto.IssueAccessRequest;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.client.generated.dto.ValidateAccessRequest;
import io.github.bsayli.licensing.sdk.api.dto.LicenseAccessRequest;
import io.github.bsayli.licensing.sdk.api.dto.LicenseToken;
import io.github.bsayli.licensing.sdk.common.exception.LicensingSdkHttpTransportException;
import io.github.bsayli.licensing.sdk.common.exception.LicensingSdkRemoteServiceException;
import io.github.bsayli.licensing.sdk.generator.ClientIdGenerator;
import io.github.bsayli.licensing.sdk.generator.SignatureGenerator;
import io.github.bsayli.licensing.sdk.service.LicenseTokenCacheService;
import io.github.bsayli.licensing.sdk.service.client.LicenseServiceClient;
import io.github.bsayli.licensing.sdk.service.handler.LicenseResponseHandler;
import java.util.List;
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
import org.springframework.http.HttpStatusCode;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: LicenseOrchestrationServiceImpl")
class LicenseOrchestrationServiceImplTest {

  @Mock private LicenseServiceClient licenseServiceClient;
  @Mock private LicenseTokenCacheService cache;
  @Mock private ClientIdGenerator clientIdGenerator;
  @Mock private SignatureGenerator signatureGenerator;
  @Mock private LicenseResponseHandler responseHandler;

  @InjectMocks private LicenseOrchestrationServiceImpl service;

  @Captor private ArgumentCaptor<IssueAccessRequest> issueReqCaptor;
  @Captor private ArgumentCaptor<ValidateAccessRequest> validateReqCaptor;

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
    ApiClientResponse<LicenseAccessResponse> resp = mock(ApiClientResponse.class);
    when(licenseServiceClient.issueAccess(any(IssueAccessRequest.class))).thenReturn(resp);
    when(responseHandler.extractTokenOrThrow(resp)).thenReturn("jwt-1");

    LicenseToken token = service.getLicenseToken(sdkReq);

    assertNotNull(token);
    assertEquals("jwt-1", token.licenseToken());
    verify(licenseServiceClient).issueAccess(issueReqCaptor.capture());
    assertEquals("crm", issueReqCaptor.getValue().getServiceId());
    assertEquals("1.5.0", issueReqCaptor.getValue().getServiceVersion());
    assertEquals("crm~host1~aa:bb", issueReqCaptor.getValue().getInstanceId());
    verify(cache).put("cid", "jwt-1");
    verifyNoMoreInteractions(cache);
    verifyNoInteractionsForValidatePath();
  }

  @Test
  @DisplayName("Cache hit + validate OK (ACTIVE, no token) -> return cached")
  void cacheHit_validate_active_noRefresh() {
    when(clientIdGenerator.getClientId(sdkReq)).thenReturn("cid");
    when(cache.get("cid")).thenReturn("jwt-old");
    when(signatureGenerator.generateForValidate(eq("jwt-old"), any(ValidateAccessRequest.class)))
        .thenReturn("vsig");
    @SuppressWarnings("unchecked")
    ApiClientResponse<LicenseAccessResponse> vResp = mock(ApiClientResponse.class);
    when(licenseServiceClient.validateAccess(eq("jwt-old"), any(ValidateAccessRequest.class)))
        .thenReturn(vResp);
    when(responseHandler.extractTokenIfPresentOrThrow(vResp)).thenReturn(null);

    LicenseToken token = service.getLicenseToken(sdkReq);

    assertNotNull(token);
    assertEquals("jwt-old", token.licenseToken());
    verify(licenseServiceClient).validateAccess(eq("jwt-old"), validateReqCaptor.capture());
    assertEquals("crm", validateReqCaptor.getValue().getServiceId());
    assertEquals("1.5.0", validateReqCaptor.getValue().getServiceVersion());
    verify(cache, never()).put(anyString(), anyString());
    verify(cache, never()).evict(anyString());
  }

  @Test
  @DisplayName("Cache hit + validate returns refreshed token -> cache update + return new")
  void cacheHit_validate_refreshed() {
    when(clientIdGenerator.getClientId(sdkReq)).thenReturn("cid");
    when(cache.get("cid")).thenReturn("jwt-old");
    when(signatureGenerator.generateForValidate(eq("jwt-old"), any(ValidateAccessRequest.class)))
        .thenReturn("vsig");
    @SuppressWarnings("unchecked")
    ApiClientResponse<LicenseAccessResponse> vResp = mock(ApiClientResponse.class);
    when(licenseServiceClient.validateAccess(eq("jwt-old"), any(ValidateAccessRequest.class)))
        .thenReturn(vResp);
    when(responseHandler.extractTokenIfPresentOrThrow(vResp)).thenReturn("jwt-new");

    LicenseToken token = service.getLicenseToken(sdkReq);

    assertEquals("jwt-new", token.licenseToken());
    verify(cache).put("cid", "jwt-new");
  }

  @Test
  @DisplayName("Cache hit + validate error TOKEN_IS_TOO_OLD_FOR_REFRESH -> fallback issue + cache")
  void cacheHit_validate_tooOld_fallbackIssue() {
    when(clientIdGenerator.getClientId(sdkReq)).thenReturn("cid");
    when(cache.get("cid")).thenReturn("jwt-very-old");
    when(signatureGenerator.generateForValidate(
            eq("jwt-very-old"), any(ValidateAccessRequest.class)))
        .thenReturn("vsig");
    @SuppressWarnings("unchecked")
    ApiClientResponse<LicenseAccessResponse> vResp = mock(ApiClientResponse.class);
    when(licenseServiceClient.validateAccess(eq("jwt-very-old"), any(ValidateAccessRequest.class)))
        .thenReturn(vResp);
    LicensingSdkRemoteServiceException tooOld =
        new LicensingSdkRemoteServiceException(
            HttpStatus.UNAUTHORIZED, "TOKEN_IS_TOO_OLD_FOR_REFRESH", "top", List.of("d"));
    when(responseHandler.extractTokenIfPresentOrThrow(vResp)).thenThrow(tooOld);

    @SuppressWarnings("unchecked")
    ApiClientResponse<LicenseAccessResponse> iResp = mock(ApiClientResponse.class);
    when(signatureGenerator.generateForIssue(any(IssueAccessRequest.class))).thenReturn("sig");
    when(licenseServiceClient.issueAccess(any(IssueAccessRequest.class))).thenReturn(iResp);
    when(responseHandler.extractTokenOrThrow(iResp)).thenReturn("jwt-new");

    LicenseToken token = service.getLicenseToken(sdkReq);

    assertEquals("jwt-new", token.licenseToken());
    verify(cache).put("cid", "jwt-new");
    verify(licenseServiceClient).issueAccess(issueReqCaptor.capture());
    assertEquals("crm", issueReqCaptor.getValue().getServiceId());
  }

  @Test
  @DisplayName("ApiClientException -> wrapped as LicensingSdkHttpTransportException")
  void apiClientException_wrapped() {
    when(clientIdGenerator.getClientId(sdkReq)).thenReturn("cid");
    when(cache.get("cid")).thenReturn(null);

    ApiClientException apiEx = mock(ApiClientException.class);
    when(apiEx.getStatusCode()).thenReturn(HttpStatusCode.valueOf(502));
    when(apiEx.getMessageKey()).thenReturn("remote.error");
    when(apiEx.getResponseBody()).thenReturn("{err}");

    when(signatureGenerator.generateForIssue(any(IssueAccessRequest.class))).thenReturn("sig");
    when(licenseServiceClient.issueAccess(any(IssueAccessRequest.class))).thenThrow(apiEx);

    LicensingSdkHttpTransportException thrown =
        assertThrows(
            LicensingSdkHttpTransportException.class, () -> service.getLicenseToken(sdkReq));

    assertEquals(502, thrown.getStatusCode().value());
    assertEquals("remote.error", thrown.getMessageKey());
    assertEquals("{err}", thrown.getRawBody());
  }

  private void verifyNoInteractionsForValidatePath() {
    verify(licenseServiceClient, never()).validateAccess(anyString(), any());
    verify(signatureGenerator, never()).generateForValidate(anyString(), any());
  }
}
