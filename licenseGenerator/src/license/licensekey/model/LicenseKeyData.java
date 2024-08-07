package license.licensekey.model;

public class LicenseKeyData {
	
	private static final String DELIMITER = "~";

	private final String prefix;
	private final String randomString;
	private final String uuid;

	public LicenseKeyData(String prefix, String randomString, String uuid) {
		this.prefix = prefix;
		this.randomString = randomString;
		this.uuid = uuid;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getRandomString() {
		return randomString;
	}

	public String getUuid() {
		return uuid;
	}

	public String generateLicenseKey() {
		return prefix + DELIMITER + randomString + DELIMITER + uuid;
	}

}
