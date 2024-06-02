package com.c9.licensing.security;

public interface EncryptionUtil {
	
	static final String ALGORITHM = "AES/GCM/NoPadding"; 
	static final int GCM_IV_LENGTH = 12; // Recommended for GCM 
	static final int GCM_TAG_LENGTH = 16; // 128 bits

	public String encrypt(String licenseCode) throws Exception;

	public String decrypt(String encryptedLicenseCode) throws Exception;
}
