package io.github.bsayli.licensing.service.validation.impl;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.domain.model.ClientCachedLicenseData;
import io.github.bsayli.licensing.generator.ClientIdGenerator;
import io.github.bsayli.licensing.security.SignatureValidator;
import io.github.bsayli.licensing.security.UserIdEncryptor;
import io.github.bsayli.licensing.service.ClientSessionCache;
import io.github.bsayli.licensing.service.exception.request.InvalidRequestException;
import io.github.bsayli.licensing.service.exception.token.TokenAlreadyExistsException;
import io.github.bsayli.licensing.service.validation.LicenseKeyRequestValidator;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class LicenseKeyRequestValidatorImpl implements LicenseKeyRequestValidator {

  private final ClientSessionCache cacheService;
  private final ClientIdGenerator clientIdGenerator;
  private final UserIdEncryptor userIdEncryptor;
  private final SignatureValidator signatureValidator;

  public LicenseKeyRequestValidatorImpl(
      ClientSessionCache cacheService,
      ClientIdGenerator clientIdGenerator,
      UserIdEncryptor userIdEncryptor,
      SignatureValidator signatureValidator) {
    this.cacheService = cacheService;
    this.clientIdGenerator = clientIdGenerator;
    this.userIdEncryptor = userIdEncryptor;
    this.signatureValidator = signatureValidator;
  }

  @Override
  public void assertSignatureValid(IssueAccessRequest request) {
    signatureValidator.validate(request);
  }

  @Override
  public void assertNoConflictingCachedContext(IssueAccessRequest request, String userId) {
    String clientId = clientIdGenerator.getClientId(request);
    Optional<ClientCachedLicenseData> cachedOpt = cacheService.find(clientId);

    if (cachedOpt.isEmpty()) {
      return;
    }

    ClientCachedLicenseData cached = cachedOpt.get();
    String cachedUserId = userIdEncryptor.decrypt(cached.getEncUserId());

    boolean sameServiceId = Objects.equals(cached.getServiceId(), request.serviceId());
    boolean sameServiceVersion =
        Objects.equals(cached.getServiceVersion(), request.serviceVersion());
    boolean sameChecksum = Objects.equals(cached.getChecksum(), request.checksum());
    boolean sameUser = Objects.equals(cachedUserId, userId);

    boolean sameContext = sameServiceId && sameServiceVersion && sameChecksum && sameUser;

    if (sameContext) {
      throw new TokenAlreadyExistsException();
    }
    throw new InvalidRequestException();
  }
}
