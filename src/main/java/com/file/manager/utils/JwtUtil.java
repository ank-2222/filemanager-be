package com.file.manager.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtUtil {

  private final SecretKey key;

  public JwtUtil(@Value("${jwt.secret}") String secret) {
    byte[] decodedKey = Base64.getUrlDecoder().decode(secret);
    this.key = Keys.hmacShaKeyFor(decodedKey);
  }

  // Token validity duration (e.g., 24 hours)
  private final long validityInMilliseconds = 3600000L * 24;

  // Generate JWT token using subject (typically user email or ID)
  public String generateToken(String subject) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + validityInMilliseconds);

    return Jwts.builder()
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key)
            .compact();
  }

  // Parse token claims
  public Claims getClaims(String token) {
    return Jwts.parser()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
  }

  // Extract subject (e.g., email) from token
  public String getSubject(String token) {
    return getClaims(token).getSubject();
  }

  // Validate token (checks signature and expiration)
  public boolean isTokenValid(String token) {
    try {
      Claims claims = getClaims(token);
      return !claims.getExpiration().before(new Date());
    } catch (Exception e) {
      return false;
    }
  }

  public static String getJwtTokenFromCurrentRequest() {
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

    if (attributes != null) {
      HttpServletRequest request = attributes.getRequest();
      String bearerToken = request.getHeader("Authorization");

      if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
        return bearerToken.substring(7);
      }
    }
    return null;
  }
}
