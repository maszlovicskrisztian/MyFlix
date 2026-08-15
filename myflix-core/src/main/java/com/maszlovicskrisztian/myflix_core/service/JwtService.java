package com.maszlovicskrisztian.myflix_core.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtService {

    @Value("${JWT_SECRET}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSignedKey())
                .compact();
    }

    public String extractUsername(String token) {
        if (token == null || token.isEmpty())
            return null;

        var claims  = parseClaims(token);

        if (claims == null)
            return null;

        return claims.getSubject();
    }

    public boolean isTokenValid(String token) {
        Claims claims = parseClaims(token);

        if (claims == null)
            return false;

        return claims.getExpiration().after(new Date());
    }

    private SecretKey getSignedKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private Claims parseClaims(String token) {
        if (token == null || token.isEmpty())
            return null;

        return Jwts.parser()
                .verifyWith(getSignedKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
