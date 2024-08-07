package license.licensekey.encrypter;

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

public class UserIdEncrypter {

    private static final String ALGORITHM = "AES/GCM/NoPadding"; 
    private static final int GCM_IV_LENGTH = 12;  
    private static final int GCM_TAG_LENGTH = 16;
	private static final String SECRET_KEY = "s/PUyV6Ym/v0A+HehYFZ9GTpatSL1MkcVW1zBLf3cX4=";
    private static final SecretKey encryptionKey = getKey();

    static {
        Security.addProvider(new BouncyCastleProvider()); 
    }

    public UserIdEncrypter() {
        Security.addProvider(new BouncyCastleProvider()); 
    }
    
    public static void main(String[] args) throws Exception {
		String userId = "ba035b3e-d8b6-4a09-89c7-ab0459f2585b";
		System.out.println("User Id:" + userId);
		String userIdEncrypt = encrypt(userId);
		System.out.println("Enc User Id:" + userIdEncrypt);
		String userIdDecrypt = decrypt(userIdEncrypt);
		System.out.println("Dec User Id:" + userIdDecrypt);
	}


    public static String encrypt(String plainText) throws Exception {
        byte[] iv = new SecureRandom().generateSeed(GCM_IV_LENGTH);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, parameterSpec);

        byte[] cipherTextBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        byte[] finalCipherText = concatArrays(iv, cipherTextBytes);
        return Base64.getEncoder().encodeToString(finalCipherText);
    }

    public static String decrypt(String encryptedText) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(encryptedText);
        byte[] iv = Arrays.copyOfRange(decoded, 0, GCM_IV_LENGTH);
        byte[] cipherTextBytes = Arrays.copyOfRange(decoded, GCM_IV_LENGTH, decoded.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey, parameterSpec);

        byte[] decryptedBytes = cipher.doFinal(cipherTextBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private static byte[] concatArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
    
    private static SecretKey getKey() {
		byte[] decodedKey = Base64.getDecoder().decode(SECRET_KEY);
		return new SecretKeySpec(decodedKey, ALGORITHM);
	}
}
