package io.github.bsayli.licensing.sdk.service;

import io.github.bsayli.licensing.sdk.model.server.LicenseServerValidationRequest;
import io.github.bsayli.licensing.sdk.model.server.LicenseServerValidationResponse;

public interface LicenseService {

  String MESSAGE_LICENSE_SERVICE_CLIENT_UNHANDLED_ERROR = "License service client unhandled error";
  String MESSAGE_LICENSE_SERVICE_SERVER_UNHANDLED_ERROR = "License service server unhandled error";
  String URI_LICENSE_VALIDATE = "/api/license/validate";
  String REQUEST_PARAM_SERVICE_ID = "serviceId";
  String REQUEST_PARAM_SERVICE_VERSION = "serviceVersion";
  String REQUEST_PARAM_LICENSE_KEY = "licenseKey";
  String HEADER_PARAM_LICENSE_TOKEN = "X-License-Token";
  String HEADER_PARAM_INSTANCE_ID = "X-Instance-ID";
  String HEADER_PARAM_CHECKSUM = "X-Checksum";
  String HEADER_PARAM_SIGNATURE = "X-Signature";
  String HEADER_PARAM_FORCE_TOKEN_REFRESH = "X-Force-Token-Refresh";

  LicenseServerValidationResponse getLicenseDetails(LicenseServerValidationRequest request);
}
