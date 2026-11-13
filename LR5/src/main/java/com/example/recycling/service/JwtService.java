package com.example.recycling.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private final SecretKey key;
    private final long accessMinutes;
    private final long refreshDays;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-minutes:15}") long accessMinutes,
            @Value("${jwt.refresh-days:7}") long refreshDays) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessMinutes = accessMinutes;
        this.refreshDays = refreshDays;
    }

    public String createAccessToken(String username) {
        return createToken(username, "access", Instant.now().plusSeconds(accessMinutes * 60));
    }

    public String createRefreshToken(String username) {
        return createToken(username, "refresh", Instant.now().plusSeconds(refreshDays * 24 * 60 * 60));
    }

    private String createToken(String username, String type, Instant expiry) {
        Instant now = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        expiry = expiry.truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        return Jwts.builder()
                .subject(username)
                .id(UUID.randomUUID().toString())
                .claim("type", type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public boolean isTokenOfType(String token, String expectedType) {
        try {
            Claims claims = parse(token);
            return expectedType.equals(claims.get("type", String.class))
                    && claims.getSubject() != null
                    && claims.getExpiration() != null
                    && claims.getId() != null
                    && claims.getIssuedAt() != null;
        } catch (Exception exception) {
            return false;
        }
    }

    public Instant getExpiry(String token) {
        return parse(token).getExpiration().toInstant().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
    }

    public String getUsername(String token) {
        return parse(token).getSubject();
    }

    public long getAccessMinutes() { return accessMinutes; }
    public long getRefreshDays() { return refreshDays; }
}
