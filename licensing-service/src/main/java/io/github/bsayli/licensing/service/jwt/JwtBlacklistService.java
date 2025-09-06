package io.github.bsayli.licensing.service.jwt;

public interface JwtBlacklistService {

  void addCurrentTokenToBlacklist(String clientId);

  boolean isBlackListed(String token);
}
