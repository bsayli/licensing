package com.c9.licensing.security;

public interface UserIdUtil {
	
	String LICENSEKEYPREFIX = "C9INE";
	String ALGORITHM = "AES/GCM/NoPadding"; 
	int GCM_IV_LENGTH = 12; // Recommended for GCM 
	int GCM_TAG_LENGTH = 16; // 128 bits
	String DELIMITER = "~";
	
	public String extractDecryptedUserId(String licenseKey) throws Exception;
	
	public String extractEncryptedUserId(String licenseKey) throws Exception;
	 
	public String encrypt(String userId) throws Exception;
	
	public String decrypt(String userId) throws Exception;
}
