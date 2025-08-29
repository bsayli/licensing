package com.c9.licensing.service.jwt;

public interface JwtBlacklistService {

  void addCurrentTokenToBlacklist(String clientId);

  boolean isBlackListed(String token);
}
