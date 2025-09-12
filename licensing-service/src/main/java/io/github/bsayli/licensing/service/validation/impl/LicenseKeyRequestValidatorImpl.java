package io.github.bsayli.licensing.service.validation.impl;

import io.github.bsayli.licensing.api.dto.IssueAccessRequest;
import io.github.bsayli.licensing.domain.model.ClientSessionSnapshot;
import io.github.bsayli.licensing.generator.ClientIdGenerator;
import io.github.bsayli.licensing.security.SignatureValidator;
import io.github.bsayli.licensing.security.UserIdEncryptor;
import io.github.bsayli.licensing.service.ClientSessionCacheService;
import io.github.bsayli.licensing.service.exception.request.InvalidRequestException;
import io.github.bsayli.licensing.service.validation.LicenseKeyRequestValidator;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class LicenseKeyRequestValidatorImpl implements LicenseKeyRequestValidator {

  private final ClientSessionCacheService cacheService;
  private final ClientIdGenerator clientIdGenerator;
  private final UserIdEncryptor userIdEncryptor;
  private final SignatureValidator signatureValidator;

  public LicenseKeyRequestValidatorImpl(
      ClientSessionCacheService cacheService,
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
    final String clientId = clientIdGenerator.getClientId(request);
    final ClientSessionSnapshot cached = cacheService.find(clientId);
    if (cached == null) {
      return;
    }

    final String cachedUserId = userIdEncryptor.decrypt(cached.encUserId());

    final boolean sameContext =
        Objects.equals(cached.serviceId(), request.serviceId())
            && Objects.equals(cached.serviceVersion(), request.serviceVersion())
            && Objects.equals(cached.checksum(), request.checksum())
            && Objects.equals(cachedUserId, userId);

    if (sameContext) {
      return;
    }

    throw new InvalidRequestException();
  }
}
