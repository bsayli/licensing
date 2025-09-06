package io.github.bsayli.licensing.service.validation;

import io.github.bsayli.licensing.api.dto.LicenseValidationRequest;
import io.github.bsayli.licensing.model.LicenseInfo;
import java.util.List;

public interface LicenseResultServiceDetailValidationService {

  String SERVICE_ID_C9INE_CODEGEN = "c9ineCodegen";
  String SERVICE_ID_C9INE_TEST_AUTOMATION = "c9ineTestAutomation";

  List<String> serviceIds =
      List.of(
          SERVICE_ID_C9INE_CODEGEN,
          SERVICE_ID_C9INE_TEST_AUTOMATION,
          "c9inePlatform",
          "c9ineWeb",
          "c9ineMobile");

  List<String> checksumSupportedServiceIds =
      List.of(SERVICE_ID_C9INE_CODEGEN, SERVICE_ID_C9INE_TEST_AUTOMATION);

  String MESSAGE_LICENSE_SERVICE_ID_NOT_SUPPORTED =
      "Your license does not support this service %s. Please contact support for assistance.";

  String MESSAGE_LICENSE_INVALID_SERVICE_ID =
      "The requested service id  %s is invalid. Please verify the service id and try again.";

  String MESSAGE_LICENSE_INVALID_CHECKSUM =
      "Checksum validation failed. Please contact support for assistance.";

  String MESSAGE_LICENSE_CHECKSUM_SERVICE_VERSION_MISMATCH =
      "Checksum and service version mismatch. Please contact support for assistance.";

  String MESSAGE_LICENSE_SERVICE_VERSION_NOT_SUPPORTED =
      "Your license does not support this %s version. Please contact support for assistance.";

  void validate(LicenseInfo licenseInfo, LicenseValidationRequest request);
}
