package io.github.bsayli.licensing.testconfig;

import io.github.bsayli.licensing.common.i18n.LocalizedMessageResolver;
import io.github.bsayli.licensing.service.LicenseOrchestrationService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestControllerMocksConfig {

    @Bean
    public LicenseOrchestrationService licenseOrchestrationService() {
        return Mockito.mock(LicenseOrchestrationService.class);
    }

    @Bean
    public LocalizedMessageResolver messageResolver() {
        var mr = Mockito.mock(LocalizedMessageResolver.class);

        // =========================================================
        // LICENSE API - SUCCESS / TOP-LEVEL MESSAGES (NON-ERROR)
        // =========================================================
        Mockito.when(mr.getMessage("license.validation.success"))
                .thenReturn("License is valid");
        Mockito.when(mr.getMessage("license.key.valid"))
                .thenReturn("License key is valid");
        Mockito.when(mr.getMessage("license.token.valid"))
                .thenReturn("Token is valid");
        Mockito.when(mr.getMessage("license.token.refreshed"))
                .thenReturn("Token has been refreshed");

        // =========================================================
        // LICENSE API - FAILURE / USER-FACING TOP MESSAGE
        // (Used as ProblemDetail.title in ServiceException handler)
        // =========================================================
        Mockito.when(mr.getMessage("license.validation.failed"))
                .thenReturn("License validation failed");
        Mockito.when(mr.getMessage("license.validation.error"))
                .thenReturn("An unexpected error occurred");

        // =========================================================
        // PROBLEM DETAILS (RFC 9457) - TITLES
        // =========================================================
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

        // =========================================================
        // PROBLEM DETAILS (RFC 9457) - GENERIC DETAILS
        // =========================================================
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

        // =========================================================
        // REQUEST / PARAMETER VALIDATION (HTTP 400)
        // =========================================================
        Mockito.when(mr.getMessage("request.validation.failed"))
                .thenReturn("Request validation failed");
        Mockito.when(mr.getMessage("request.invalid.parameter"))
                .thenReturn("Invalid parameter");
        Mockito.when(mr.getMessage("request.missing.parameter"))
                .thenReturn("Missing parameter");
        Mockito.when(mr.getMessage("request.invalid"))
                .thenReturn("Invalid request");

        Mockito.when(mr.getMessage("request.param.missing", "License-Token"))
                .thenReturn("Required request parameter 'License-Token' is missing");
        Mockito.when(mr.getMessage("request.header.missing", "License-Token"))
                .thenReturn("Required request header 'License-Token' is missing");
        Mockito.when(mr.getMessage("request.param.type_mismatch", "String"))
                .thenReturn("Invalid value (expected String).");

        // =========================================================
        // JSON / PAYLOAD (HTTP 400)
        // =========================================================
        Mockito.when(mr.getMessage("request.body.invalid"))
                .thenReturn("Invalid JSON payload.");
        Mockito.when(mr.getMessage("request.body.field.unrecognized", "foo"))
                .thenReturn("Unrecognized field: 'foo'");
        Mockito.when(mr.getMessage("request.body.invalid_format", "String", "123"))
                .thenReturn("Invalid format: expected String, value 123");

        Mockito.when(mr.getMessage("request.method.not_supported", "TRACE"))
                .thenReturn("HTTP method not supported: TRACE");
        Mockito.when(mr.getMessage("request.param.invalid"))
                .thenReturn("One or more parameters are invalid.");
        Mockito.when(mr.getMessage("request.param.required_missing", "License-Token"))
                .thenReturn("Missing required parameter: License-Token");
        Mockito.when(mr.getMessage("request.endpoint.not_found"))
                .thenReturn("Endpoint not found.");

        // =========================================================
        // BEAN VALIDATION (FIELD-LEVEL) - MESSAGE KEYS
        // =========================================================
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

        Mockito.when(mr.getMessage("signature.required"))
                .thenReturn("signature is required");
        Mockito.when(mr.getMessage("signature.size"))
                .thenReturn("signature must be between {min} and {max} characters");

        Mockito.when(mr.getMessage("checksum.size"))
                .thenReturn("checksum must be between {min} and {max} characters");

        Mockito.when(mr.getMessage("license.key.required"))
                .thenReturn("licenseKey is required");
        Mockito.when(mr.getMessage("license.key.size"))
                .thenReturn("licenseKey must be between {min} and {max} characters");

        Mockito.when(mr.getMessage("license.token.required"))
                .thenReturn("licenseToken is required");
        Mockito.when(mr.getMessage("license.token.size"))
                .thenReturn("licenseToken must be between {min} and {max} characters");
        Mockito.when(mr.getMessage("license.token.format"))
                .thenReturn("License token format is invalid");

        // =========================================================
        // LICENSE SERVICE DOMAIN ERRORS (ServiceException message keys)
        // =========================================================
        Mockito.when(mr.getMessage("license.not.found"))
                .thenReturn("License not found");

        Mockito.when(mr.getMessage("license.invalid"))
                .thenReturn("License is invalid");
        Mockito.when(mr.getMessage("license.expired"))
                .thenReturn("License has expired");
        Mockito.when(mr.getMessage("license.inactive"))
                .thenReturn("License is inactive");

        Mockito.when(mr.getMessage("license.usage.limit.exceeded", 10))
                .thenReturn("License usage limit exceeded (max 10)");
        Mockito.when(mr.getMessage("license.service.id.not.supported", "crm"))
                .thenReturn("This license does not support the requested service crm. Please contact support for assistance.");
        Mockito.when(mr.getMessage("license.service.id.invalid", "crm"))
                .thenReturn("The service id crm is invalid. Please check and try again.");
        Mockito.when(mr.getMessage("license.service.version.not.supported", "crm"))
                .thenReturn("Service version is not supported for service crm");
        Mockito.when(mr.getMessage("license.checksum.invalid"))
                .thenReturn("Checksum is invalid");
        Mockito.when(mr.getMessage("license.signature.invalid"))
                .thenReturn("Signature is invalid");

        Mockito.when(mr.getMessage("license.token.created"))
                .thenReturn("Token has been created");
        Mockito.when(mr.getMessage("license.token.active"))
                .thenReturn("Token is active");
        Mockito.when(mr.getMessage("license.token.already.exists"))
                .thenReturn("A valid token already exists. Please use the existing token.");
        Mockito.when(mr.getMessage("license.token.expired"))
                .thenReturn("Token has expired");
        Mockito.when(mr.getMessage("license.token.invalid"))
                .thenReturn("Token is invalid");
        Mockito.when(mr.getMessage("license.token.invalid.access"))
                .thenReturn("Token is invalid because request parameters were changed");
        Mockito.when(mr.getMessage("license.token.too.old"))
                .thenReturn("The token is too old to be refreshed. Please request a new one");

        // =========================================================
        // REPOSITORY ERRORS
        // =========================================================
        Mockito.when(mr.getMessage("repository.operation.failed"))
                .thenReturn("We could not process your request right now");

        Mockito.when(mr.getMessage("user.not.found"))
                .thenReturn("We could not find a license for this account");
        Mockito.when(mr.getMessage("user.attribute.missing", "foo"))
                .thenReturn("A required account setting is missing: foo");
        Mockito.when(mr.getMessage("user.attribute.invalid.format", "foo"))
                .thenReturn("An account setting has an invalid value for foo");

        // =========================================================
        // USER ASYNC OPERATIONS
        // =========================================================
        Mockito.when(mr.getMessage("user.operation.failed"))
                .thenReturn("We could not complete your request right now");
        Mockito.when(mr.getMessage("user.async.already.processing"))
                .thenReturn("Another request is already in progress for this account. Please try again shortly.");
        Mockito.when(mr.getMessage("user.async.max.retry.exceeded"))
                .thenReturn("We could not reach the user store. Please try again later.");

        // =========================================================
        // SERVER ERRORS (FALLBACK)
        // =========================================================
        Mockito.when(mr.getMessage("server.internal.error"))
                .thenReturn("Internal server error. Please try again later.");
        Mockito.when(mr.getMessage("server.unknown.error"))
                .thenReturn("Unknown server error. Please contact support if the problem persists.");

        // =========================================================
        // LICENSE STATUS (DISPLAY / UI MESSAGES)
        // =========================================================
        Mockito.when(mr.getMessage("license.message.active"))
                .thenReturn("Your license is active");
        Mockito.when(mr.getMessage("license.message.inactive"))
                .thenReturn("Your license is inactive");
        Mockito.when(mr.getMessage("license.message.trial"))
                .thenReturn("Your license is a trial license");
        Mockito.when(mr.getMessage("license.message.suspended"))
                .thenReturn("Your license is suspended");
        Mockito.when(mr.getMessage("license.message.grace_period"))
                .thenReturn("Your license is in a grace period");
        Mockito.when(mr.getMessage("license.message.expired"))
                .thenReturn("Your license has expired");
        Mockito.when(mr.getMessage("license.message.revoked"))
                .thenReturn("Your license has been revoked");
        Mockito.when(mr.getMessage("license.message.pending_activation"))
                .thenReturn("Your license is pending activation");

        return mr;
    }
}
