package com.immo.config;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtils {
    
    @Value("${app.secret-key}")
    private String secretKey;

    @Value("${app.expiration-time}")
    private long expirationTime;

    @Value("${app.clock-skew-seconds}")
    private long clockSkewSeconds;

    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();
       return createToken(claims, email); 
    }

    private String createToken(Map<String, Object> claims, String email){
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(email)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
            .signWith(getSignKey(), SignatureAlgorithm.HS256)
            .compact();  
    }
    
    private Key getSignKey() {
        byte[] keyBytes = secretKey.getBytes(); 
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    public Boolean validationToken(String token, UserDetails userDetails) {
        String email = extractEmail(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));  
    }
                
    private boolean isTokenExpired(String token) {
       return extractExpirationDate(token).before(new Date());
    }
 
    private Date extractExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
        
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
        
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims); 
    }
       
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .setAllowedClockSkewSeconds(clockSkewSeconds) // Utilise le décalage d'horloge configuré
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
  