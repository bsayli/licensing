package io.github.bsayli.licensing.agent.service;

import org.springframework.stereotype.Service;

@Service
public interface LicenseTokenCacheService {

    void put(String clientId, String licenseToken);

    String get(String clientId);

    void evict(String clientId);
}
