package com.c9.licensing.security;

public interface EncryptionUtil {
	
	String ALGORITHM = "AES/GCM/NoPadding"; 
	int GCM_IV_LENGTH = 12; // Recommended for GCM 
	int GCM_TAG_LENGTH = 16; // 128 bits

	public String encrypt(String licenseCode) throws Exception;

	public String decrypt(String encryptedLicenseCode) throws Exception;
}
