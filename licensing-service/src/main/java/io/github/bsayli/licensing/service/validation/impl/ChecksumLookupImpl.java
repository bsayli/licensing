package io.github.bsayli.licensing.service.validation.impl;

import io.github.bsayli.licensing.model.LicenseChecksumVersionInfo;
import io.github.bsayli.licensing.model.LicenseInfo;
import io.github.bsayli.licensing.service.validation.ChecksumLookup;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ChecksumLookupImpl implements ChecksumLookup {

  @Override
  public List<LicenseChecksumVersionInfo> checksumsFor(LicenseInfo info, String serviceId) {
    // Yeni isimler:
    return switch (serviceId) {
      case "crm" -> info.checksumsCrm();
      case "billing" -> info.checksumsBilling();
      case "reporting" -> info.checksumsReporting();
      // İsterseniz eski isimler için geriye dönük destek:
      // case "c9ineCodegen"        -> info.checksumsCodegen();
      // case "c9ineTestAutomation" -> info.checksumsTestAutomation();
      default -> List.of();
    };
  }
}
