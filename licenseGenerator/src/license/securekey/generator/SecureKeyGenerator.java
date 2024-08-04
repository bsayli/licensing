package license.securekey.generator;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class SecureKeyGenerator {

	public static void main(String[] args) throws NoSuchAlgorithmException {
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(256); 
		SecretKey secretKey = keyGen.generateKey();

		String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
		System.out.println("Generated Secret Key: " + encodedKey);
	}
}
