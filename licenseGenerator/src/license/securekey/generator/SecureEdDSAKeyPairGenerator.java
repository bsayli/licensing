package license.securekey.generator;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class SecureEdDSAKeyPairGenerator {
	
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	
	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
		generateAndPrintKeys();
	}

	private static PrivateKey generateAndPrintKeys() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
		Security.addProvider(new BouncyCastleProvider());

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EdDSA", "BC");
		ECGenParameterSpec ecSpec = new ECGenParameterSpec("Ed25519");
		keyPairGenerator.initialize(ecSpec, SECURE_RANDOM);

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