package com.c9.licensing.service.validation.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.c9.licensing.model.LicenseChecksumVersionInfo;
import com.c9.licensing.model.LicenseInfo;
import com.c9.licensing.model.LicenseServiceIdVersionInfo;
import com.c9.licensing.model.LicenseValidationRequest;
import com.c9.licensing.model.SignatureData;
import com.c9.licensing.model.SignatureData.Builder;
import com.c9.licensing.model.errors.LicenseExpiredException;
import com.c9.licensing.model.errors.LicenseInactiveException;
import com.c9.licensing.model.errors.LicenseInvalidChecksumException;
import com.c9.licensing.model.errors.LicenseInvalidServiceIdException;
import com.c9.licensing.model.errors.LicenseServiceIdNotSupportedException;
import com.c9.licensing.model.errors.LicenseServiceVersionNotSupportedException;
import com.c9.licensing.model.errors.LicenseUsageLimitExceededException;
import com.c9.licensing.security.SignatureValidator;
import com.c9.licensing.service.validation.LicenseValidationService;
import com.fasterxml.jackson.core.Version;

@Service
public class LicenseValidationServiceImpl implements LicenseValidationService {

	private static final String SERVICE_ID_C9INE_CODEGEN = "c9ineCodegen";
	private static final String SERVICE_ID_C9INE_TEST_AUTOMATION = "c9ineTestAutomation";
	
	private static final List<String> serviceIds = List.of(SERVICE_ID_C9INE_CODEGEN, SERVICE_ID_C9INE_TEST_AUTOMATION,
			"c9inePlatform", "c9ineWeb", "c9ineMobile");
	private static final List<String> checksumSupportedServiceIds = List.of(SERVICE_ID_C9INE_CODEGEN,
			SERVICE_ID_C9INE_TEST_AUTOMATION);
	
	private final SignatureValidator signatureValidator;
	
	public LicenseValidationServiceImpl(SignatureValidator signatureValidator) {
		this.signatureValidator = signatureValidator;
	}

	@Override
	public void validate(LicenseInfo licenseInfo, LicenseValidationRequest request) throws Exception {
		validateLicenseExpiration(licenseInfo);
		validateLicenseStatus(licenseInfo);
		validateUsageLimit(licenseInfo, request);
		validateServiceId(licenseInfo, request);
		validateChecksumInfo(licenseInfo, request);
		validateServiceVersion(licenseInfo, request);
		validateSignature(request);
	}
	
	private void validateSignature(LicenseValidationRequest request) {
		boolean isSignatureControlNeeded = request.signature() != null;
		if(isSignatureControlNeeded) {
			String licenseKey =  request.licenseKey();
			boolean requestedByLicenseKey = licenseKey != null  && licenseKey.trim().length() > 0;
			
			Builder signatureDataBuilder = new SignatureData.Builder()
					.serviceId(request.serviceId())
					.instanceId(request.instanceId());
			
			if(requestedByLicenseKey) {
				signatureDataBuilder.encryptedLicenseKeyHash(getDataHash(licenseKey));
			}else {
				signatureDataBuilder.licenseTokenHash(getDataHash(request.licenseToken()));
			}
			SignatureData signatureData = signatureDataBuilder.build();	
			signatureValidator.validateSignature(request.signature(), signatureData);
		}
	}

	@Override
	public boolean isInstanceIdNotExist(String instanceId, List<String> instanceIds) {
		return CollectionUtils.isEmpty(instanceIds) || !instanceIds.contains(instanceId);
	}

	private void validateServiceVersion(LicenseInfo licenseInfo, LicenseValidationRequest request) {
		if (!isVersionInfoValid(licenseInfo, request.serviceId(), request.checksum())) {
			throw new LicenseServiceVersionNotSupportedException(
					String.format(MESSAGE_LICENSE_SERVICE_VERSION_NOT_SUPPORTED, request.serviceId()));
		}
	}

	private void validateChecksumInfo(LicenseInfo licenseInfo, LicenseValidationRequest request) {
		if (!isChecksumInfoValid(licenseInfo, request.serviceId(), request.checksum())) {
			throw new LicenseInvalidChecksumException(MESSAGE_LICENSE_INVALID_CHECKSUM);
		}
	}

	private void validateServiceId(LicenseInfo licenseInfo, LicenseValidationRequest request) {
		boolean serviceIdControlNeeded = request.serviceId() != null;
		if(serviceIdControlNeeded) {
			if (!isServiceIdValid(request.serviceId())) {
				throw new LicenseInvalidServiceIdException(
						String.format(MESSAGE_LICENSE_INVALID_SERVICE_ID, request.serviceId()));
			}
			
			if (!isServiceIdSupported(request.serviceId(), licenseInfo.allowedServices())) {
				throw new LicenseServiceIdNotSupportedException(
						String.format(MESSAGE_LICENSE_SERVICE_ID_NOT_SUPPORTED, request.serviceId()));
			}
		}
	
	}

	private void validateUsageLimit(LicenseInfo licenseInfo, LicenseValidationRequest request) {
		if (!isWithinUserLimit(licenseInfo.remainingUsageCount())
				&& isInstanceIdNotExist(request.instanceId(), licenseInfo.instanceIds())) {
			throw new LicenseUsageLimitExceededException(
					String.format(MESSAGE_LICENSE_LIMIT_EXCEEDED, licenseInfo.maxCount()));
		}
	}

	private void validateLicenseStatus(LicenseInfo licenseInfo) {
		if (!isLicenseStatusActive(licenseInfo.licenseStatus())) {
			throw new LicenseInactiveException(MESSAGE_LICENSE_NOT_ACTIVE);
		}
	}

	private void validateLicenseExpiration(LicenseInfo licenseInfo) {
		if (isLicenseExpired(licenseInfo.expirationDate())) {
			throw new LicenseExpiredException(MESSAGE_LICENSE_EXPIRED);
		}
	}

	private boolean isLicenseExpired(LocalDateTime expirationDate) {
		return LocalDateTime.now().isAfter(expirationDate);
	}

	private boolean isWithinUserLimit(int remainingUsageCount) {
		return remainingUsageCount > 0;
	}

	private boolean isLicenseStatusActive(String licenseStatus) {
		return "Active".equalsIgnoreCase(licenseStatus);
	}

	private boolean isServiceIdValid(String serviceId) {
		boolean isValid = false;
		if (serviceId != null) {
			serviceId = serviceId.trim();
			isValid = serviceIds.contains(serviceId);
		}
		return isValid;
	}

	private boolean isServiceIdSupported(String serviceId, List<String> allowedServices) {
		boolean isSupported = false;
		if (!CollectionUtils.isEmpty(allowedServices) && serviceId != null) {
			serviceId = serviceId.trim();
			isSupported = allowedServices.contains(serviceId);
		}
		return isSupported;
	}

	private boolean isServiceIdChecksumCheckSupports(String serviceId) {
		boolean isSupported = false;
		if (serviceId != null) {
			serviceId = serviceId.trim();
			isSupported = checksumSupportedServiceIds.contains(serviceId);
		}
		return isSupported;
	}

	private boolean isChecksumInfoValid(LicenseInfo licenseInfo, String requestedServiceId, String requestedChecksum) {
		boolean isCheckSumInfoValid = true;
		boolean isChecksumControlNeeded = requestedChecksum != null
				&& isServiceIdChecksumCheckSupports(requestedServiceId);
		if (isChecksumControlNeeded) {
			if (SERVICE_ID_C9INE_CODEGEN.equals(requestedServiceId)) {
				isCheckSumInfoValid = licenseInfo.checksumsCodegen()
						.stream()
						.anyMatch(checksumInfo -> checksumInfo.checksum().equals(requestedChecksum));

			} else if (SERVICE_ID_C9INE_TEST_AUTOMATION.equals(requestedServiceId)) {
				isCheckSumInfoValid = licenseInfo.checksumsTestAutomation()
						.stream()
						.anyMatch(checksumInfo -> checksumInfo.checksum().equals(requestedChecksum));
			}
		}
		return isCheckSumInfoValid;
	}

	private boolean isVersionInfoValid(LicenseInfo licenseInfo, String requestedServiceId, String requestedChecksum) {
		boolean isVersionInfoValid = true;
		List<LicenseServiceIdVersionInfo> allowedServiceVersions = licenseInfo.allowedServiceVersions();
		boolean isVersionControlNeeded = requestedChecksum != null && !allowedServiceVersions.isEmpty()
				&& isServiceIdChecksumCheckSupports(requestedServiceId);
		if (isVersionControlNeeded) {
			Optional<String> version = Optional.empty();

			Optional<String> supportedMaxVersionOpt = allowedServiceVersions.stream()
					.filter(serviceVersionInfo -> serviceVersionInfo.serviceId().equals(requestedServiceId))
					.map(LicenseServiceIdVersionInfo::licensedMaxVersion)
					.findFirst();

			if (SERVICE_ID_C9INE_CODEGEN.equals(requestedServiceId)) {
				version = licenseInfo.checksumsCodegen()
						.stream()
						.filter(checksumInfo -> checksumInfo.checksum().equals(requestedChecksum))
						.map(LicenseChecksumVersionInfo::version)
						.findFirst();

			} else if (SERVICE_ID_C9INE_TEST_AUTOMATION.equals(requestedServiceId)) {
				version = licenseInfo.checksumsTestAutomation()
						.stream()
						.filter(checksumInfo -> checksumInfo.checksum().equals(requestedChecksum))
						.map(LicenseChecksumVersionInfo::version)
						.findFirst();
			}

			if (version.isPresent() && supportedMaxVersionOpt.isPresent()) {
				String supportedMaxVersionStr = supportedMaxVersionOpt.get();
				String clientVersionStr = version.get();

				int[] maxVersionParts = getVersionParts(supportedMaxVersionStr);
				int[] clientVersionParts = getVersionParts(clientVersionStr);

				Version supportedMaxVersion = new Version(maxVersionParts[0], maxVersionParts[1], maxVersionParts[2],
						null, null, null);
				Version clientVersion = new Version(clientVersionParts[0], clientVersionParts[1], clientVersionParts[2],
						null, null, null);

				isVersionInfoValid = clientVersion.compareTo(supportedMaxVersion) <= 0;

			}
		}
		return isVersionInfoValid;
	}

	private int[] getVersionParts(String version) {
		String[] versions = version.split("\\."); // Split by dot (.)
		if (versions.length != 3) { // Ensure three parts (major, minor, patch)
			throw new IllegalArgumentException("Invalid version format: ");
		}
		return new int[] { Integer.parseInt(versions[0]), Integer.parseInt(versions[1]),
				Integer.parseInt(versions[2]) };
	}
	
	private String getDataHash(String data) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(data.getBytes());
			return Base64.getEncoder().encodeToString(hash);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}


}
