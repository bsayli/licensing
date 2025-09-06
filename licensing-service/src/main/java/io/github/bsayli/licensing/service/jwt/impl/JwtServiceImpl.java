package io.github.bsayli.licensing.service.jwt.impl;

import io.github.bsayli.licensing.service.jwt.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class JwtServiceImpl implements JwtService {

  private static final Random random = new Random();

  private final PrivateKey privateKey;
  private final PublicKey publicKey;
  private final Integer tokenExpirationInMinute;
  private final long tokenMaxJitter;

  public JwtServiceImpl(
      String privateKeyStr,
      String publicKeyStr,
      Integer tokenExpirationInMinute,
      long tokenMaxJitter)
      throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
    Security.addProvider(new BouncyCastleProvider());
    byte[] decodedPrivateKey = Base64.getDecoder().decode(privateKeyStr);
    KeyFactory keyFactory = KeyFactory.getInstance("EdDSA", "BC");
    this.privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodedPrivateKey));

    byte[] decodedPublicKey = Base64.getDecoder().decode(publicKeyStr);
    this.publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(decodedPublicKey));
    this.tokenExpirationInMinute = tokenExpirationInMinute;
    this.tokenMaxJitter = tokenMaxJitter;
  }

  @Override
  public String generateToken(String clientId, String licenseTier, String licenseStatus) {
    Instant now = Instant.now();
    long jitter = random.nextLong(tokenMaxJitter);
    Integer tokenExpirationWithJitter = (tokenExpirationInMinute * 60) + (int) (jitter / 1000);
    Instant expiry = now.plus(tokenExpirationWithJitter, ChronoUnit.SECONDS);

    return Jwts.builder()
        .subject(clientId)
        .claim("licenseStatus", licenseStatus)
        .claim("licenseTier", licenseTier)
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiry))
        .signWith(privateKey)
        .compact();
  }

  @Override
  public Claims verifyAndExtractJwtClaims(String token) {
    return Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token).getPayload();
  }

  @Override
  public boolean validateTokenFormat(String token) {
    if (token == null) return false;

    String[] parts = token.split("\\.");
    if (parts.length != 3) {
      return false;
    }

    for (String part : parts) {
      try {
        Base64.getUrlDecoder().decode(part);
      } catch (IllegalArgumentException e) {
        return false;
      }
    }

    String header = new String(Base64.getUrlDecoder().decode(parts[0]));
    return header.contains("alg");
  }
}
