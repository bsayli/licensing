package com.c9.licensing.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;


@Configuration
@EnableCaching
public class CacheConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);

	@Bean
	CacheManager cacheManager() {
		return new ConcurrentMapCacheManager();
	}
	
	@CacheEvict(cacheNames = "userInfoCache", allEntries = true)
	@Scheduled(fixedRateString = "${caching.spring.userInfoTTL}")
	public void emptyUserInfoCache() {
	    logger.info("emptying userInfo cache");
	}
}
