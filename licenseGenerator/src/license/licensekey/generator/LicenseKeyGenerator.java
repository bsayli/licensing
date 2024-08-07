package license.licensekey.generator;

import java.security.SecureRandom;
import java.util.Base64;

import license.licensekey.encrypter.UserIdEncrypter;
import license.licensekey.model.LicenseKeyData;

public class LicenseKeyGenerator {

	private static final String LICENSEKEYPREFIX = "C9INE";
	private static final SecureRandom random = new SecureRandom();

	public static void main(String[] args) throws Exception {
		String uuid = "ba035b3e-d8b6-4a09-89c7-ab0459f2585b";
		LicenseKeyData licenseKeyData = generateLicenseKey(UserIdEncrypter.encrypt(uuid));
		System.out.println("License Key:" + licenseKeyData.generateLicenseKey());
	}

	public static LicenseKeyData generateLicenseKey(String uuid) throws Exception {
		String randomString = getRandomString();
		return new LicenseKeyData(LICENSEKEYPREFIX, randomString, uuid);
	}

	private static String getRandomString() {
		byte[] randomBytes = new byte[32];
		random.nextBytes(randomBytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
	}
}
