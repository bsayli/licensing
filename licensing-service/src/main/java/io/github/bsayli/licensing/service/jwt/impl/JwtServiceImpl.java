package io.github.bsayli.licensing.service.jwt.impl;

import io.github.bsayli.licensing.domain.model.LicenseStatus;
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
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class JwtServiceImpl implements JwtService {

  private static final String KEY_ALG = "EdDSA";
  private static final String KEY_PROVIDER = "BC";
  private static final String HEADER_REQUIRED_FIELD = "alg";
  private static final String JWT_DOT_REGEX = "\\.";
  private static final int JWT_EXPECTED_PARTS = 3;
  private static final String CLAIM_LICENSE_STATUS = "licenseStatus";
  private static final String CLAIM_LICENSE_TIER = "licenseTier";
  private static final Base64.Decoder B64_DEC = Base64.getDecoder();
  private static final Base64.Decoder B64URL_DEC = Base64.getUrlDecoder();
  private final PrivateKey privateKey;
  private final PublicKey publicKey;
  private final Duration tokenTtl;
  private final Duration maxJitter;

  public JwtServiceImpl(
      String privateKeyBase64, String publicKeyBase64, Duration tokenTtl, Duration maxJitter)
      throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {

    Security.addProvider(new BouncyCastleProvider());

    KeyFactory kf = KeyFactory.getInstance(KEY_ALG, KEY_PROVIDER);

    byte[] priv = B64_DEC.decode(privateKeyBase64);
    this.privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(priv));

    byte[] pub = B64_DEC.decode(publicKeyBase64);
    this.publicKey = kf.generatePublic(new X509EncodedKeySpec(pub));

    this.tokenTtl = (tokenTtl == null) ? Duration.ZERO : tokenTtl;
    this.maxJitter = (maxJitter == null) ? Duration.ZERO : maxJitter;
  }

  @Override
  public String generateToken(String clientId, String licenseTier, LicenseStatus licenseStatus) {
    Instant now = Instant.now();

    long jitterMs =
        (maxJitter.isZero() || maxJitter.isNegative())
            ? 0L
            : ThreadLocalRandom.current().nextLong(0, maxJitter.toMillis() + 1);

    Instant expiry = now.plus(tokenTtl).plusMillis(jitterMs);

    return Jwts.builder()
        .subject(clientId)
        .claim(CLAIM_LICENSE_STATUS, licenseStatus.name())
        .claim(CLAIM_LICENSE_TIER, licenseTier)
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

    String[] parts = token.split(JWT_DOT_REGEX);
    if (parts.length != JWT_EXPECTED_PARTS) return false;

    for (String part : parts) {
      try {
        B64URL_DEC.decode(part);
      } catch (IllegalArgumentException e) {
        return false;
      }
    }

    String headerJson = new String(B64URL_DEC.decode(parts[0]));
    return headerJson.contains(HEADER_REQUIRED_FIELD);
  }
}
