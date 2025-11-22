package com.meetingroom.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil 
{
    private final SecretKey secretKey;
    private final long expiration = 1000 * 60 * 60 * 24; // 24h

    public JwtUtil(@Value("${jwt.secret}") String secret) 
    {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, String role) 
    {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    public String extractEmail(String token) 
    {
        return Jwts.parserBuilder().setSigningKey(secretKey).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public String extractRole(String token) 
    {
        return (String) Jwts.parserBuilder().setSigningKey(secretKey).build()
                .parseClaimsJws(token).getBody().get("role");
    }

    public boolean validateToken(String token) 
    {
        try 
        {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } 
        catch (JwtException e) 
        {
            return false;
        }
    }
}
