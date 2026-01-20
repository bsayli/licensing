package io.github.bsayli.licensing.service.validation.impl;

import io.github.bsayli.licensing.domain.model.LicenseChecksumVersionInfo;
import io.github.bsayli.licensing.domain.model.LicenseInfo;
import io.github.bsayli.licensing.service.validation.ChecksumLookup;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChecksumLookupImpl implements ChecksumLookup {
    @Override
    public List<LicenseChecksumVersionInfo> checksumsFor(LicenseInfo info, String serviceId) {
        if (info == null || serviceId == null) return List.of();
        return info.serviceChecksums().getOrDefault(serviceId, List.of());
    }
}
