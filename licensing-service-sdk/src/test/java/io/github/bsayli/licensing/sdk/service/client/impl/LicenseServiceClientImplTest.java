package io.github.bsayli.licensing.sdk.service.client.impl;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

import io.github.bsayli.licensing.client.adapter.LicensingServiceClientAdapter;
import io.github.bsayli.licensing.client.common.contract.ApiClientResponse;
import io.github.bsayli.licensing.client.generated.dto.IssueAccessRequest;
import io.github.bsayli.licensing.client.generated.dto.LicenseAccessResponse;
import io.github.bsayli.licensing.client.generated.dto.ValidateAccessRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: LicenseServiceClientImpl")
class LicenseServiceClientImplTest {

    @Mock
    private LicensingServiceClientAdapter adapter;

    @InjectMocks
    private LicenseServiceClientImpl client;

    @Test
    @DisplayName("issueAccess delegates to adapter and returns response")
    void issueAccess_delegates() {
        IssueAccessRequest req = new IssueAccessRequest()
                .serviceId("crm")
                .serviceVersion("1.2.3")
                .instanceId("inst-1")
                .checksum("chk")
                .licenseKey("LK_xxx");
        @SuppressWarnings("unchecked")
        ApiClientResponse<LicenseAccessResponse> expected = mock(ApiClientResponse.class);
        when(adapter.issueAccess(any(IssueAccessRequest.class))).thenReturn(expected);

        ApiClientResponse<LicenseAccessResponse> actual = client.issueAccess(req);

        assertSame(expected, actual);
        ArgumentCaptor<IssueAccessRequest> captor = ArgumentCaptor.forClass(IssueAccessRequest.class);
        verify(adapter).issueAccess(captor.capture());
        IssueAccessRequest passed = captor.getValue();
        verify(adapter, times(1)).issueAccess(any(IssueAccessRequest.class));
        verifyNoMoreInteractions(adapter);

        org.junit.jupiter.api.Assertions.assertEquals("crm", passed.getServiceId());
        org.junit.jupiter.api.Assertions.assertEquals("1.2.3", passed.getServiceVersion());
        org.junit.jupiter.api.Assertions.assertEquals("inst-1", passed.getInstanceId());
        org.junit.jupiter.api.Assertions.assertEquals("chk", passed.getChecksum());
        org.junit.jupiter.api.Assertions.assertEquals("LK_xxx", passed.getLicenseKey());
    }

    @Test
    @DisplayName("validateAccess delegates to adapter and returns response")
    void validateAccess_delegates() {
        String token = "jwt-token";
        ValidateAccessRequest req = new ValidateAccessRequest()
                .serviceId("crm")
                .serviceVersion("1.2.3")
                .instanceId("inst-1")
                .checksum("chk");
        @SuppressWarnings("unchecked")
        ApiClientResponse<LicenseAccessResponse> expected = mock(ApiClientResponse.class);
        when(adapter.validateAccess(anyString(), any(ValidateAccessRequest.class))).thenReturn(expected);

        ApiClientResponse<LicenseAccessResponse> actual = client.validateAccess(token, req);

        assertSame(expected, actual);
        ArgumentCaptor<String> tokenCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ValidateAccessRequest> reqCap = ArgumentCaptor.forClass(ValidateAccessRequest.class);
        verify(adapter).validateAccess(tokenCap.capture(), reqCap.capture());
        verify(adapter, times(1)).validateAccess(anyString(), any(ValidateAccessRequest.class));
        verifyNoMoreInteractions(adapter);
        org.junit.jupiter.api.Assertions.assertEquals("jwt-token", tokenCap.getValue());
        ValidateAccessRequest passed = reqCap.getValue();
        org.junit.jupiter.api.Assertions.assertEquals("crm", passed.getServiceId());
        org.junit.jupiter.api.Assertions.assertEquals("1.2.3", passed.getServiceVersion());
        org.junit.jupiter.api.Assertions.assertEquals("inst-1", passed.getInstanceId());
        org.junit.jupiter.api.Assertions.assertEquals("chk", passed.getChecksum());
    }
}