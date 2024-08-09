package com.c9.licensing.service.validation.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.c9.licensing.model.LicenseChecksumVersionInfo;
import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.model.LicenseServiceIdVersionInfo;
import com.c9.licensing.model.LicenseValidationRequest;
import com.c9.licensing.model.errors.LicenseInvalidChecksumException;
import com.c9.licensing.model.errors.LicenseInvalidServiceIdException;
import com.c9.licensing.model.errors.LicenseServiceIdNotSupportedException;
import com.c9.licensing.model.errors.LicenseServiceVersionNotSupportedException;
import com.c9.licensing.service.validation.LicenseResultServiceDetailValidationService;
import com.fasterxml.jackson.core.Version;

@Service
public class LicenseResultServiceDetailValidationServiceImpl implements LicenseResultServiceDetailValidationService {

	@Override
	public void validate(LicenseInfo licenseInfo, LicenseValidationRequest request) {
		validateServiceId(licenseInfo, request);
		validateChecksumInfo(licenseInfo, request);
		validateServiceVersion(licenseInfo, request);
	}
	
	private void validateServiceId(LicenseInfo licenseInfo, LicenseValidationRequest request) {
		if (!isServiceIdValid(request.serviceId())) {
			throw new LicenseInvalidServiceIdException(
					String.format(MESSAGE_LICENSE_INVALID_SERVICE_ID, request.serviceId()));
		}

		if (!isServiceIdSupported(request.serviceId(), licenseInfo.allowedServices())) {
			throw new LicenseServiceIdNotSupportedException(
					String.format(MESSAGE_LICENSE_SERVICE_ID_NOT_SUPPORTED, request.serviceId()));
		}
	}
	
	private boolean isServiceIdValid(String serviceId) {
		return serviceIds.contains(serviceId);
	}

	private boolean isServiceIdSupported(String serviceId, List<String> allowedServices) {
		boolean isSupported = false;
		if (!CollectionUtils.isEmpty(allowedServices)) {
			isSupported = allowedServices.contains(serviceId);
		}
		return isSupported;
	}

	private void validateChecksumInfo(LicenseInfo licenseInfo, LicenseValidationRequest request) {
		if (!isChecksumInfoValid(licenseInfo, request.serviceId(), request.checksum())) {
			throw new LicenseInvalidChecksumException(MESSAGE_LICENSE_INVALID_CHECKSUM);
		}
	}
	
	private boolean isChecksumInfoValid(LicenseInfo licenseInfo, String requestedServiceId, String requestedChecksum) {
		boolean isChecksumControlNeeded = requestedChecksum != null
				&& isServiceIdChecksumCheckSupports(requestedServiceId);
		if (isChecksumControlNeeded) {
			if (SERVICE_ID_C9INE_CODEGEN.equals(requestedServiceId)) {
				return licenseInfo.checksumsCodegen()
						.stream()
						.anyMatch(checksumInfo -> checksumInfo.checksum().equals(requestedChecksum));

			} else if (SERVICE_ID_C9INE_TEST_AUTOMATION.equals(requestedServiceId)) {
				return licenseInfo.checksumsTestAutomation()
						.stream()
						.anyMatch(checksumInfo -> checksumInfo.checksum().equals(requestedChecksum));
			}
		}
		return true;
	}
	
	private boolean isServiceIdChecksumCheckSupports(String serviceId) {
		return checksumSupportedServiceIds.contains(serviceId);
	}

	private void validateServiceVersion(LicenseInfo licenseInfo, LicenseValidationRequest request) {
		boolean isValid = isServiceVersionValid(licenseInfo, request.serviceVersion(), request.serviceId());
		if (!isValid) {
			throw new LicenseServiceVersionNotSupportedException(
					String.format(MESSAGE_LICENSE_SERVICE_VERSION_NOT_SUPPORTED, request.serviceId()));
		}
		isValid = isVersionInfoCompatibleWithChecksum(licenseInfo, request.serviceVersion(), request.serviceId(),
				request.checksum());
		if (!isValid) {
			throw new LicenseServiceVersionNotSupportedException(
					String.format(MESSAGE_LICENSE_CHECKSUM_SERVICE_VERSION_MISMATCH, request.serviceId()));
		}
	}
	
	private boolean isServiceVersionValid(LicenseInfo licenseInfo, String requestedServiceVersion,
			String requestedServiceId) {
		List<LicenseServiceIdVersionInfo> allowedServiceVersions = licenseInfo.allowedServiceVersions();
		boolean isVersionControlNeeded = !CollectionUtils.isEmpty(allowedServiceVersions);
		if (isVersionControlNeeded) {
			Optional<String> supportedMaxVersionOpt = allowedServiceVersions.stream()
					.filter(serviceVersionInfo -> serviceVersionInfo.serviceId().equals(requestedServiceId))
					.map(LicenseServiceIdVersionInfo::licensedMaxVersion)
					.findFirst();

			if (supportedMaxVersionOpt.isPresent()) {
				String supportedMaxVersionStr = supportedMaxVersionOpt.get();
				String clientVersionStr = requestedServiceVersion;

				int[] maxVersionParts = getServiceVersionParts(supportedMaxVersionStr);
				int[] clientVersionParts = getServiceVersionParts(clientVersionStr);

				Version supportedMaxVersion = new Version(maxVersionParts[0], maxVersionParts[1], maxVersionParts[2],
						null, null, null);
				Version clientVersion = new Version(clientVersionParts[0], clientVersionParts[1], clientVersionParts[2],
						null, null, null);

				return clientVersion.compareTo(supportedMaxVersion) <= 0;
			}
		}
		return true;
	}
	
	private boolean isVersionInfoCompatibleWithChecksum(LicenseInfo licenseInfo, String requestedServiceVersion,
			String requestedServiceId, String requestedChecksum) {
		if (requestedChecksum != null) {
			Optional<String> checksumServiceVersion = getChecksumServiceVersion(licenseInfo, requestedServiceId,
					requestedChecksum);

			if (checksumServiceVersion.isPresent()) {
				return checksumServiceVersion.get().equals(requestedServiceVersion);
			}
		}
		return true;
	}

	private Optional<String> getChecksumServiceVersion(LicenseInfo licenseInfo, String requestedServiceId,
			String requestedChecksum) {
		Optional<String> checksumServiceVersion = Optional.empty();
		if (Objects.nonNull(requestedChecksum)) {
			if (SERVICE_ID_C9INE_CODEGEN.equals(requestedServiceId)) {
				checksumServiceVersion = licenseInfo.checksumsCodegen()
						.stream()
						.filter(checksumInfo -> checksumInfo.checksum().equals(requestedChecksum))
						.map(LicenseChecksumVersionInfo::version)
						.findFirst();

			} else if (SERVICE_ID_C9INE_TEST_AUTOMATION.equals(requestedServiceId)) {
				checksumServiceVersion = licenseInfo.checksumsTestAutomation()
						.stream()
						.filter(checksumInfo -> checksumInfo.checksum().equals(requestedChecksum))
						.map(LicenseChecksumVersionInfo::version)
						.findFirst();
			}
		}
		return checksumServiceVersion;
	}

	private int[] getServiceVersionParts(String serviceVersion) {
		String[] versions = serviceVersion.split("\\."); 
		if (versions.length != 3) { 
			throw new IllegalArgumentException("Invalid version format: ");
		}
		return new int[] { Integer.parseInt(versions[0]), Integer.parseInt(versions[1]),
				Integer.parseInt(versions[2]) };
	}
}
