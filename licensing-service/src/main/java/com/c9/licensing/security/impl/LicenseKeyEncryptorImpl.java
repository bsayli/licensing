package com.c9.licensing.security.impl;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Arrays;

import com.c9.licensing.model.errors.LicenseInvalidException;
import com.c9.licensing.security.LicenseKeyEncryptor;

public class LicenseKeyEncryptorImpl implements LicenseKeyEncryptor {

    private final SecretKey secretKey;

	public LicenseKeyEncryptorImpl(String encodedSecretKey) {
		Security.addProvider(new BouncyCastleProvider());
		byte[] decodedKey = Base64.getDecoder().decode(encodedSecretKey);
		secretKey = new SecretKeySpec(decodedKey, ALGORITHM);
	}

	public String encrypt(String plainText) throws LicenseInvalidException {
		byte[] iv = new SecureRandom().generateSeed(GCM_IV_LENGTH);
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

			byte[] cipherTextBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
			byte[] finalCipherText = concatArrays(iv, cipherTextBytes);
			return Base64.getEncoder().encodeToString(finalCipherText);
		}catch (Exception e) {
			throw new LicenseInvalidException("License Key invalid", e);
		}
	}

	public String decrypt(String encryptedText) throws LicenseInvalidException {
		boolean isValidBase64 = isValidBase64(encryptedText);
		if(!isValidBase64) {
			throw new LicenseInvalidException("License Key not found or invalid");
		}
		try {
			byte[] decoded = Base64.getDecoder().decode(encryptedText);
			byte[] iv = Arrays.copyOfRange(decoded, 0, GCM_IV_LENGTH);
			byte[] cipherTextBytes = Arrays.copyOfRange(decoded, GCM_IV_LENGTH, decoded.length);

			Cipher cipher = Cipher.getInstance(ALGORITHM);
			GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

			byte[] decryptedBytes = cipher.doFinal(cipherTextBytes);
			return new String(decryptedBytes, StandardCharsets.UTF_8);
		}catch (Exception e) {
			throw new LicenseInvalidException("License Key not found or invalid", e);
		}		
	}

	private byte[] concatArrays(byte[] a, byte[] b) {
		byte[] result = new byte[a.length + b.length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}
	
	private boolean isValidBase64(String str) {
	    String base64Regex = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$";
	    return str != null && str.matches(base64Regex);
	}
}
