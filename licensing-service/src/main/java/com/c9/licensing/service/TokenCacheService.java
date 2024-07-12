package com.c9.licensing.service;

public interface TokenCacheService {

	boolean isTokenValidAndInvalidate(String token);

	String addValidToken(String token);

}
