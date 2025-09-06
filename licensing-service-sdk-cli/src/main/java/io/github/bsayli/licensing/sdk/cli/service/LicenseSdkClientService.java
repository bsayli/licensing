package io.github.bsayli.licensing.sdk.cli.service;

public interface LicenseSdkClientService {

  Integer validateLicense(
      String instanceId, String licenseKey, String serviceId, String serviceVersion);
}
