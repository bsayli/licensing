package license.securekey.generator;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class SecureDSAKeyPairGenerator {

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final String ALGORITHM = "DSA"; // Use DSA for digital signatures
	private static final int KEY_SIZE = 2048; // Recommended key size for DSA
	
	public static void main(String[] args) throws NoSuchAlgorithmException {
		generateAndPrintKeys();
	}

	private static PrivateKey generateAndPrintKeys() throws NoSuchAlgorithmException {
		Security.addProvider(new BouncyCastleProvider());

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
		keyPairGenerator.initialize(KEY_SIZE, SECURE_RANDOM);

		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		// Get the public key and private key
		PublicKey publicKey = keyPair.getPublic();
		PrivateKey privateKey = keyPair.getPrivate();

		// Encode keys for storage/transmission (optional)
		String encodedPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
		String encodedPrivateKey = Base64.getEncoder().encodeToString(privateKey.getEncoded());

		System.out.println("Generated Public Key: " + encodedPublicKey);
		System.out.println("Generated Private Key: " + encodedPrivateKey);
		
		return privateKey;
	}

}