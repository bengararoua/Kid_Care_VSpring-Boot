package com.kidcare.insight.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String generateToken(String email) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        System.out.println("🔑 SECRET UTILISÉ (longueur): " + keyBytes.length + " bytes");

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();
    }

    public String extractEmail(String token) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(keyBytes))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(keyBytes))
                    .build()
                    .parseSignedClaims(token);
            System.out.println("✅ TOKEN VALIDE");
            return true;
        } catch (Exception e) {
            System.out.println("❌ TOKEN INVALIDE: " + e.getMessage());
            return false;
        }
    }
}