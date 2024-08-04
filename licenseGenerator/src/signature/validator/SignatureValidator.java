package signature.validator;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import signature.generator.SignatureGenerator;
import signature.model.SignatureData;

public class SignatureValidator {

	private static final String PUBLIC_KEY = "MIIDQjCCAjUGByqGSM44BAEwggIoAoIBAQCPeTXZuarpv6vtiHrPSVG28y7FnjuvNxjo6sSWHz79NgbnQ1GpxBgzObgJ58KuHFObp0dbhdARrbi0eYd1SYRpXKwOjxSzNggooi/6JxEKPWKpk0U0CaD+aWxGWPhL3SCBnDcJoBBXsZWtzQAjPbpUhLYpH51kjviDRIZ3l5zsBLQ0pqwudemYXeI9sCkvwRGMn/qdgYHnM423krcw17njSVkvaAmYchU5Feo9a4tGU8YzRY+AOzKkwuDycpAlbk4/ijsIOKHEUOThjBopo33fXqFD3ktm/wSQPtXPFiPhWNSHxgjpfyEc2B3KI8tuOAdl+CLjQr5ITAV2OTlgHNZnAh0AuvaWpoV499/e5/pnyXfHhe8ysjO65YDAvNVpXQKCAQAWplxYIEhQcE51AqOXVwQNNNo6NHjBVNTkpcAtJC7gT5bmHkvQkEq9rI837rHgnzGC0jyQQ8tkL4gAQWDt+coJsyB2p5wypifyRz6Rh5uixOdEvSCBVEy1W4AsNo0fqD7UielOD6BojjJCilx4xHjGjQUntxyaOrsLC+EsRGiWOefTznTbEBplqiuH9kxoJts+xy9LVZmDS7TtsC98kOmkltOlXVNb6/xF1PYZ9j897buHOSXC8iTgdzEpbaiH7B5HSPh++1/et1SEMWsiMt7lU92vAhErDR8C2jCXMiT+J67ai51LKSLZuovjntnhA6Y8UoELxoi34u1DFuHvF9veA4IBBQACggEAUzTk7qzLnma3DAie/xgibOjNEXFh4ThLOB67Lk0MLs3wkM9giGQRKiwHqdlQLF9ICOZdjti2pp0saQE7W6poXIq03OV46cfvSsbMzAT4OihWJI2UOlQqnbWkn4gHRpgXndPlKKTJ7jNsBy2OAE8B4FCfE74xkjjMqB7qwGAl1vGtRMu2XMKPN/+Aa5WyEv7k9cgv7QjUfDrn6tvutWEnMNm4ZdR3zQV4+wjNHzcJ5+l7BqCY/y6lvUQ/wB6rC6Whq4FNpjjBrZreekoLZbTNUceOd+OnEPeUCDxcG38m/1yNTfV0CBCWDzJ2zl383m6vCQq0asq5Vo2cBp+pA72/gw==";	
	private static final byte [] BYTES_PUBLIC_KEY = Base64.getDecoder().decode(PUBLIC_KEY);
	
	public static void main(String[] args) throws Exception {
		
		String signatureForLicenseKey = "MD0CHEnxz/R0Yja5brlsGl2a8zMZhmspVLhc/T/ClsUCHQCcBjr3H5fgmS0HelUXYHSEFmdZFc5QdlppII5J";
		
		SignatureData signatureData = SignatureGenerator.getSignatureDataWithLicenseKey();
		SignatureValidator signatureValidator = new SignatureValidator();
		boolean validateSignature = signatureValidator.validateSignature(signatureForLicenseKey, signatureData.toJson());
		System.out.println("Signature verication result with ServiceId, InstanceId and License Key Parameters:" + validateSignature);
		
		String signatureForLicenseToken = "MD0CHDnHnB88kxW6v7cEQlSnjLMg/lSxUjDLJf4ayWoCHQC10rnrDlQ0a3vXhgLeUbe5NnoaF5VyHD/t2KmT";
		
		signatureData = SignatureGenerator.getSignatureDataWithLicenseToken();
		validateSignature = signatureValidator.validateSignature(signatureForLicenseToken, signatureData.toJson());
		System.out.println("Signature verication result with ServiceId, InstanceId and License Token Parameters:" + validateSignature);
		
	}
	
	public boolean validateSignature(String signature, String data) throws Exception {
		byte[] hash = calculateSHA256Hash(data.getBytes());

		// 3. Verify the signature
		Signature signatureObject = Signature.getInstance("SHA256withDSA"); // Assuming DSA was used for signing
		signatureObject.initVerify(KeyFactory.getInstance("DSA").generatePublic(new X509EncodedKeySpec(BYTES_PUBLIC_KEY)));
		signatureObject.update(hash);
		return signatureObject.verify(Base64.getDecoder().decode(signature));
	}

	private byte[] calculateSHA256Hash(byte[] data) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		return digest.digest(data);
	}
}
