package io.github.bsayli.licensing.service.validation;

import io.github.bsayli.licensing.model.LicenseChecksumVersionInfo;
import io.github.bsayli.licensing.model.LicenseInfo;
import java.util.List;

public interface ChecksumLookup {
  List<LicenseChecksumVersionInfo> checksumsFor(LicenseInfo info, String serviceId);
}
