package io.github.bsayli.license.cli.service;

import io.github.bsayli.license.token.extractor.JwtTokenExtractor;
import io.github.bsayli.license.token.extractor.model.LicenseValidationResult;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LicenseTokenService {

  private static final Logger log = LoggerFactory.getLogger(LicenseTokenService.class);

  public LicenseValidationResult validate(String publicKeyB64, String token)
      throws JwtException, IllegalArgumentException {

    if (publicKeyB64 == null || publicKeyB64.isBlank()) {
      throw new IllegalArgumentException("--publicKey must not be blank");
    }
    if (token == null || token.isBlank()) {
      throw new IllegalArgumentException("--token must not be blank");
    }

    log.debug("Validating token (jwtLen={}, pkLen={})", token.length(), publicKeyB64.length());

    JwtTokenExtractor extractor = new JwtTokenExtractor(publicKeyB64);
    return extractor.validateAndGetToken(token);
  }
}
