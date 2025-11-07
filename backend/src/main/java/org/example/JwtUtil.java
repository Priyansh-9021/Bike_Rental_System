package org.example;

// File: JwtUtil.java
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;

public class JwtUtil {

    // A secret key for signing the token. Keep this secret!
    // In a real app, this would be in a config file.
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Token expiration time (e.g., 1 hour)
    private static final long EXPIRATION_TIME = 1000 * 60 * 60;

    // Generates a JWT for a given username
    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    // Validates a token and returns the claims (data)
    public static Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // Token is invalid
            return null;
        }
    }
}