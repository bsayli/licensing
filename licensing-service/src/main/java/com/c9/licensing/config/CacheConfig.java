package com.c9.licensing.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig {
	
	@Value("${caching.spring.userInfoTTL}")
	private Integer userInfoTTL;

	@Value("${caching.spring.userOffLineInfoTTL}")
	private Integer userOffLineInfoTTL;
	
	@Value("${jwt.token.expiration}")
	private Integer jwtTokenExpiration;
	
	@Bean
	CacheManager cacheManager() {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager();
		
		cacheManager.registerCustomCache("userInfoCache",  Caffeine.newBuilder()
				.expireAfterWrite(userInfoTTL, TimeUnit.HOURS).build());
		
		cacheManager.registerCustomCache("userOfflineInfoCache",  Caffeine.newBuilder()
				.expireAfterWrite(userOffLineInfoTTL, TimeUnit.HOURS).build());
		
		cacheManager.registerCustomCache("activeClients",  Caffeine.newBuilder()
				.expireAfterWrite((jwtTokenExpiration * 2), TimeUnit.MINUTES).build());
		
		return cacheManager;
	}
	
}
