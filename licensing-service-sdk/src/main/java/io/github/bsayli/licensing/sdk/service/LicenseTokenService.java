package io.github.bsayli.licensing.sdk.service;

import org.springframework.stereotype.Service;

@Service
public interface LicenseTokenService {

  void storeLicenseToken(String clientId, String licenseToken);

  String getLicenseToken(String clientId);

  void removeLicenseToken(String clientId);
}
