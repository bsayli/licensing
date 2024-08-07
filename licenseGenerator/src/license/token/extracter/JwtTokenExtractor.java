package license.token.extracter;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import license.token.extracter.model.LicenseValidationResult;

public class JwtTokenExtractor {

	private final PublicKey publicKey;

	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
		String publicKeyStr = "MCowBQYDK2VwAyEA6lNvY1+qsRSYgo66OFxWZxQymd2wC6LYNr1+q2gHHMo=";
		JwtTokenExtractor licenseKeyResponse = new JwtTokenExtractor(publicKeyStr);
		String token = "eyJhbGciOiJFZERTQSJ9.eyJzdWIiOiJjOWluZUNvZGVnZW5-bWFjYm9va3V5bmprbDV-MDA6MkE6OEQ6QkU6RjE6MjMiLCJsaWNlbnNlU3RhdHVzIjoiQWN0aXZlIiwibGljZW5zZVRpZXIiOiJQcm9mZXNzaW9uYWwiLCJpYXQiOjE3MjIxNzUwMDcsImV4cCI6MTcyMjE3NTA2N30.6W2_0IrwTJqfTp27BsM5Ea88_hhDB_zBuw45CQZ8Z3-XPnfLeUxHfVe5Iq9m4ExT3YggB0TwpvGFLyH2NO0MCg";
		LicenseValidationResult validateToken = licenseKeyResponse.validateAndGetToken(token);
		System.out.println(validateToken);
	}

	public JwtTokenExtractor(String publicKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
		Security.addProvider(new BouncyCastleProvider());
		KeyFactory keyFactory = KeyFactory.getInstance("EdDSA", "BC");
		byte[] decodedPublicKey = Base64.getDecoder().decode(publicKeyStr);
		this.publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(decodedPublicKey));
	}

	public LicenseValidationResult validateAndGetToken(String token) throws JwtException {
		Claims claims = Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token).getPayload();
		
		String licenseStatus = claims.get("licenseStatus", String.class);
		String licenseTier = claims.get("licenseTier", String.class);
		String message = claims.get("message", String.class);
		Date expiration = claims.getExpiration();
		
		validateExpirationDate(claims);
		return new LicenseValidationResult(licenseStatus, licenseTier, message, expiration);

	}

	private void validateExpirationDate(Claims claims) {
        Instant now = Instant.now();
        Date expiration = claims.getExpiration();
        if (expiration.before(Date.from(now))) {
            throw new ExpiredJwtException(null, claims, "Token has expired");
        }
	}
	
	
}