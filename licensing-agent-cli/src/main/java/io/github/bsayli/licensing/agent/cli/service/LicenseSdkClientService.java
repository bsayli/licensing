package io.github.bsayli.licensing.agent.cli.service;

public interface LicenseSdkClientService {

  Integer validateLicense(
      String instanceId, String licenseKey, String serviceId, String serviceVersion);
}
