package io.github.bsayli.licensing.service.jwt.impl;

import io.github.bsayli.licensing.domain.model.LicenseStatus;
import io.github.bsayli.licensing.service.jwt.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JwtServiceImpl implements JwtService {

  private static final String KEY_ALG = "EdDSA";
  private static final String KEY_PROVIDER = "BC";
  private static final String JWT_DOT_REGEX = "\\.";
  private static final int JWT_EXPECTED_PARTS = 3;
  private static final String CLAIM_LICENSE_STATUS = "licenseStatus";
  private static final String CLAIM_LICENSE_TIER = "licenseTier";

  private static final Base64.Decoder B64_DEC = Base64.getDecoder();
  private static final Base64.Decoder B64URL_DEC = Base64.getUrlDecoder();
  private static final Pattern ALG_PATTERN = Pattern.compile("\"alg\"\\s*:\\s*\"([^\"]+)\"");

  private final PrivateKey privateKey;
  private final PublicKey publicKey;
  private final Duration tokenTtl;
  private final Duration maxJitter;
  private final Clock clock;
  private final LongSupplier jitterSupplier;

  public JwtServiceImpl(
      String privateKeyBase64, String publicKeyBase64, Duration tokenTtl, Duration maxJitter)
      throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
    this(
        privateKeyBase64,
        publicKeyBase64,
        tokenTtl,
        maxJitter,
        Clock.systemUTC(),
        null,
        KeyFactory.getInstance(KEY_ALG, KEY_PROVIDER));
  }

  public JwtServiceImpl(
      String privateKeyBase64,
      String publicKeyBase64,
      Duration tokenTtl,
      Duration maxJitter,
      Clock clock,
      LongSupplier jitterSupplier,
      KeyFactory keyFactory)
      throws InvalidKeySpecException {

    Objects.requireNonNull(keyFactory, "keyFactory");

    byte[] priv = B64_DEC.decode(privateKeyBase64);
    this.privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(priv));

    byte[] pub = B64_DEC.decode(publicKeyBase64);
    this.publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(pub));

    this.tokenTtl = (tokenTtl == null) ? Duration.ZERO : tokenTtl;
    this.maxJitter = (maxJitter == null) ? Duration.ZERO : maxJitter;
    this.clock = (clock == null) ? Clock.systemUTC() : clock;

    if (jitterSupplier != null) {
      this.jitterSupplier = jitterSupplier;
    } else {
      if (this.maxJitter.isZero() || this.maxJitter.isNegative()) {
        this.jitterSupplier = () -> 0L;
      } else {
        this.jitterSupplier =
            () -> ThreadLocalRandom.current().nextLong(0, this.maxJitter.toMillis() + 1);
      }
    }
  }

  private static String extractAlg(String headerJson) {
    Matcher m = ALG_PATTERN.matcher(headerJson);
    return m.find() ? m.group(1) : null;
  }

  @Override
  public String generateToken(String clientId, String licenseTier, LicenseStatus licenseStatus) {
    Instant now = clock.instant();

    long jitterMs = jitterSupplier.getAsLong();
    if (jitterMs < 0L) jitterMs = 0L;
    if (!maxJitter.isZero() && jitterMs > maxJitter.toMillis()) {
      jitterMs = maxJitter.toMillis();
    }

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

    String[] parts = token.split(JWT_DOT_REGEX, -1);
    if (parts.length != JWT_EXPECTED_PARTS) return false;

    for (String part : parts) {
      try {
        B64URL_DEC.decode(part);
      } catch (IllegalArgumentException e) {
        return false;
      }
    }

    String headerJson = new String(B64URL_DEC.decode(parts[0]), StandardCharsets.UTF_8);
    String alg = extractAlg(headerJson);
    return KEY_ALG.equals(alg);
  }
}
