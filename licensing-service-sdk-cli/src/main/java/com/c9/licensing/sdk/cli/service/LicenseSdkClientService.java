package com.c9.licensing.sdk.cli.service;

public interface LicenseSdkClientService {

  Integer validateLicense(
      String instanceId, String licenseKey, String serviceId, String serviceVersion);
}
