package io.github.bsayli.licensing.agent.testconfig;

import io.github.bsayli.licensing.agent.common.i18n.LocalizedMessageResolver;
import io.github.bsayli.licensing.agent.service.LicenseOrchestrationService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestControllerMocksConfig {

    @Bean
    @Primary
    public LicenseOrchestrationService licenseOrchestrationService() {
        return Mockito.mock(LicenseOrchestrationService.class);
    }

    @Bean
    @Primary
    public LocalizedMessageResolver messageResolver() {
        var mr = Mockito.mock(LocalizedMessageResolver.class);

        Mockito.when(mr.getMessage("problem.title.not_found"))
                .thenReturn("Resource not found");
        Mockito.when(mr.getMessage("problem.title.internal_error"))
                .thenReturn("Internal server error");
        Mockito.when(mr.getMessage("problem.title.bad_request"))
                .thenReturn("Bad request");
        Mockito.when(mr.getMessage("problem.title.validation_failed"))
                .thenReturn("Validation failed");
        Mockito.when(mr.getMessage("problem.title.method_not_allowed"))
                .thenReturn("Method not allowed");
        Mockito.when(mr.getMessage("problem.title.service_error"))
                .thenReturn("Service error");
        Mockito.when(mr.getMessage("problem.title.conflict"))
                .thenReturn("Conflict");
        Mockito.when(mr.getMessage("problem.title.too_many_requests"))
                .thenReturn("Too many requests");

        Mockito.when(mr.getMessage("problem.detail.not_found"))
                .thenReturn("Requested resource was not found.");
        Mockito.when(mr.getMessage("problem.detail.internal_error"))
                .thenReturn("Unexpected error occurred.");
        Mockito.when(mr.getMessage("problem.detail.bad_request"))
                .thenReturn("Malformed request body.");
        Mockito.when(mr.getMessage("problem.detail.validation_failed"))
                .thenReturn("One or more fields are invalid.");
        Mockito.when(mr.getMessage("problem.detail.method_not_allowed"))
                .thenReturn("The request method is not supported for this resource.");
        Mockito.when(mr.getMessage("problem.detail.service_error"))
                .thenReturn("The request could not be processed.");
        Mockito.when(mr.getMessage("problem.detail.conflict"))
                .thenReturn("The request could not be completed due to a conflict.");
        Mockito.when(mr.getMessage("problem.detail.too_many_requests"))
                .thenReturn("Too many requests. Please try again later.");

        Mockito.when(mr.getMessage("request.validation.failed"))
                .thenReturn("Request validation failed");
        Mockito.when(mr.getMessage("request.param.invalid"))
                .thenReturn("One or more parameters are invalid.");
        Mockito.when(mr.getMessage("request.param.required_missing", "License-Token"))
                .thenReturn("Missing required parameter: License-Token");
        Mockito.when(mr.getMessage("request.param.type_mismatch", "String"))
                .thenReturn("Invalid value (expected String).");
        Mockito.when(mr.getMessage("request.header.missing", "License-Token"))
                .thenReturn("Required request header 'License-Token' is missing");

        Mockito.when(mr.getMessage("request.body.invalid"))
                .thenReturn("Invalid JSON payload.");
        Mockito.when(mr.getMessage("request.body.field.unrecognized", "foo"))
                .thenReturn("Unrecognized field: 'foo'");
        Mockito.when(mr.getMessage("request.body.invalid_format", "String", "123"))
                .thenReturn("Invalid format: expected String, value 123");

        Mockito.when(mr.getMessage("request.method.not_supported", "GET"))
                .thenReturn("HTTP method not supported: GET");
        Mockito.when(mr.getMessage("request.method.not_supported", "TRACE"))
                .thenReturn("HTTP method not supported: TRACE");
        Mockito.when(mr.getMessage("request.endpoint.not_found"))
                .thenReturn("Endpoint not found.");

        Mockito.when(mr.getMessage("service.id.required"))
                .thenReturn("serviceId is required");
        Mockito.when(mr.getMessage("service.id.size"))
                .thenReturn("serviceId must be between {min} and {max} characters");

        Mockito.when(mr.getMessage("service.version.required"))
                .thenReturn("serviceVersion is required");
        Mockito.when(mr.getMessage("service.version.size"))
                .thenReturn("serviceVersion must be between {min} and {max} characters");

        Mockito.when(mr.getMessage("instance.id.required"))
                .thenReturn("instanceId is required");
        Mockito.when(mr.getMessage("instance.id.size"))
                .thenReturn("instanceId must be between {min} and {max} characters");

        Mockito.when(mr.getMessage("checksum.size"))
                .thenReturn("checksum must be between {min} and {max} characters");

        Mockito.when(mr.getMessage("license.key.required"))
                .thenReturn("licenseKey is required");
        Mockito.when(mr.getMessage("license.key.size"))
                .thenReturn("licenseKey must be between {min} and {max} characters");

        Mockito.when(mr.getMessage("signature.required"))
                .thenReturn("signature is required");
        Mockito.when(mr.getMessage("signature.size"))
                .thenReturn("signature must be between {min} and {max} characters");

        Mockito.when(mr.getMessage("license.token.required"))
                .thenReturn("licenseToken is required");
        Mockito.when(mr.getMessage("license.token.size"))
                .thenReturn("licenseToken must be between {min} and {max} characters");
        Mockito.when(mr.getMessage("license.token.format"))
                .thenReturn("License token format is invalid");

        Mockito.when(mr.getMessage("license.key.valid"))
                .thenReturn("License key is valid");
        Mockito.when(mr.getMessage("license.token.valid"))
                .thenReturn("Token is valid");
        Mockito.when(mr.getMessage("license.token.refreshed"))
                .thenReturn("Token has been refreshed");

        Mockito.when(mr.getMessage("license.validation.failed"))
                .thenReturn("License validation failed");
        Mockito.when(mr.getMessage("license.validation.error"))
                .thenReturn("An unexpected error occurred");

        Mockito.when(mr.getMessage("agent.remote.call.failed"))
                .thenReturn("Licensing service request failed");
        Mockito.when(mr.getMessage("agent.remote.empty.token.top"))
                .thenReturn("Licensing service returned success but no token was included");
        Mockito.when(mr.getMessage("agent.remote.empty.token.detail"))
                .thenReturn("Missing license token in response");
        Mockito.when(mr.getMessage("agent.remote.no.payload"))
                .thenReturn("No additional error details");

        Mockito.when(mr.getMessage("server.internal.error"))
                .thenReturn("Internal server error. Please try again later.");
        Mockito.when(mr.getMessage("server.unknown.error"))
                .thenReturn("Unknown server error. Please contact support if the problem persists.");

        return mr;
    }
}