package com.example.userService.battle.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility class for JWT (JSON Web Token) handling:
 * - Parsing and validating tokens issued by authService
 * - Extracting username (subject) and claims
 * - Generating test tokens for development and tests
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // Initialize HMAC SHA-256 SecretKey using the shared secret
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Extracts username (stored as subject) from a JWT token.
     */
    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * Extracts all claims from a signed JWT token.
     */
    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validates whether the token is properly signed and not expired.
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            if (claims == null) {
                return false;
            }
            return claims.getExpiration() == null || claims.getExpiration().after(new Date());
        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Helper to generate a valid token for testing (same format as authService).
     */
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24)) // 24 hours
                .signWith(key)
                .compact();
    }
}
