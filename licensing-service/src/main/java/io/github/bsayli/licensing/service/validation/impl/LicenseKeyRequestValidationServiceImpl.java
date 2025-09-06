package io.github.bsayli.licensing.service.validation.impl;

import io.github.bsayli.licensing.api.dto.LicenseValidationRequest;
import io.github.bsayli.licensing.generator.ClientIdGenerator;
import io.github.bsayli.licensing.model.ClientCachedLicenseData;
import io.github.bsayli.licensing.model.errors.InvalidRequestException;
import io.github.bsayli.licensing.model.errors.TokenAlreadyExistException;
import io.github.bsayli.licensing.security.SignatureValidator;
import io.github.bsayli.licensing.security.UserIdEncryptor;
import io.github.bsayli.licensing.service.LicenseClientCacheManagementService;
import io.github.bsayli.licensing.service.validation.LicenseKeyRequestValidationService;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class LicenseKeyRequestValidationServiceImpl implements LicenseKeyRequestValidationService {

  private final LicenseClientCacheManagementService clientCacheManagementService;
  private final ClientIdGenerator clientIdGenerator;
  private final UserIdEncryptor userIdEncryptor;
  private final SignatureValidator signatureValidator;

  public LicenseKeyRequestValidationServiceImpl(
      LicenseClientCacheManagementService clientCacheManagementService,
      ClientIdGenerator clientIdGenerator,
      UserIdEncryptor userIdEncryptor,
      SignatureValidator signatureValidator) {
    this.clientCacheManagementService = clientCacheManagementService;
    this.clientIdGenerator = clientIdGenerator;
    this.userIdEncryptor = userIdEncryptor;
    this.signatureValidator = signatureValidator;
  }

  @Override
  public void checkLicenseKeyRequestWithCachedData(
      LicenseValidationRequest request, String userId) {
    String clientId = clientIdGenerator.getClientId(request);
    Optional<ClientCachedLicenseData> cachedLicenseDataOpt =
        clientCacheManagementService.getClientCachedLicenseData(clientId);
    if (cachedLicenseDataOpt.isPresent()) {
      ClientCachedLicenseData cachedData = cachedLicenseDataOpt.get();
      String cachedUserId = userIdEncryptor.decrypt(cachedData.getEncUserId());
      boolean isServiceIdEqual = Objects.equals(cachedData.getServiceId(), request.serviceId());
      boolean isServiceVersionEqual =
          Objects.equals(cachedData.getServiceVersion(), request.serviceVersion());
      boolean isChecksumEqual = Objects.equals(cachedData.getChecksum(), request.checksum());
      boolean isUserIdEqual = Objects.equals(cachedUserId, userId);
      boolean isValidRequest =
          isServiceIdEqual && isServiceVersionEqual && isChecksumEqual && isUserIdEqual;

      if (isValidRequest) {
        throw new TokenAlreadyExistException(MESSAGE_TOKEN_ALREADY_EXIST);
      } else {
        throw new InvalidRequestException(MESSAGE_INVALID_REQUEST);
      }
    }
  }

  @Override
  public void checkSignature(LicenseValidationRequest request) {
    signatureValidator.validateSignature(request);
  }
}
