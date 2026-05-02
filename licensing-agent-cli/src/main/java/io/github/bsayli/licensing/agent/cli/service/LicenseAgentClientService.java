package io.github.bsayli.licensing.agent.cli.service;

public interface LicenseAgentClientService {

  Integer validateLicense(
      String instanceId, String licenseKey, String serviceId, String serviceVersion);
}
