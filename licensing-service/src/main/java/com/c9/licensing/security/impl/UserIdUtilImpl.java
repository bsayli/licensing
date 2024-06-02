package com.c9.licensing.security.impl;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.c9.licensing.security.UserIdUtil;

public class UserIdUtilImpl implements UserIdUtil {
    
    private final SecretKey secretKey;

 	public UserIdUtilImpl(String encodedSecretKey) {
 		Security.addProvider(new BouncyCastleProvider());
 		byte[] decodedKey = Base64.getDecoder().decode(encodedSecretKey);
 		secretKey = new SecretKeySpec(decodedKey, ALGORITHM);
 	}
 	
 	public String extractUserId(String licenseKey) {
		String[] components = licenseKey.split(DELIMITER);

		if (components.length != 3 || !components[0].equals(LICENSEKEYPREFIX)) {
			// Handle invalid license key format - log error, throw exception
			throw new IllegalArgumentException("Invalid license key format");
		}

		return deobfuscateUserId(components[2]); // The obfuscated user ID
	}


	public String obfuscateUserId(String userId) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(new byte[16])); // New IV for each
																									// encryption
			byte[] encryptedBytes = cipher.doFinal(userId.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(encryptedBytes);
		} catch (Exception e) {
			throw new RuntimeException("Obfuscation failed", e);
		}
	}

	public String deobfuscateUserId(String obfuscatedUserId) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(new byte[16])); // New IV for each
																									// decryption
			byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(obfuscatedUserId));
			return new String(decryptedBytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new RuntimeException("De-obfuscation failed", e);
		}
	}
	
	

}
