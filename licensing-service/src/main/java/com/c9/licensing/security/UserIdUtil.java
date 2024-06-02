package com.c9.licensing.security;

public interface UserIdUtil {

    static final String LICENSEKEYPREFIX = "C9";
	static final String ALGORITHM = "AES/CBC/PKCS5Padding";
	static String DELIMITER = "~";
	
	public String extractUserId(String licenseKey);
	
	public String obfuscateUserId(String userId);
	
	public String deobfuscateUserId(String userId);
}
