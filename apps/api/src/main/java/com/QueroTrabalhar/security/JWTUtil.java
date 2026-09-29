package com.QueroTrabalhar.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Component
public class JWTUtil {

    private static final Logger logger = LoggerFactory.getLogger(JWTUtil.class);

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.secret}")
    private String jwtSecretBase64;

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSecretKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public SecretKey getSecretKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecretBase64);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validToken(String token) {
        return getClaims(token)
                .map(claims -> {
                    String username = claims.getSubject();
                    Date expirationDate = claims.getExpiration();
                    Date now = new Date(System.currentTimeMillis());

                    return username != null
                            && expirationDate != null
                            && now.before(expirationDate);
                })
                .orElse(false);
    }

    public String getUserName(String token) {
        return getClaims(token)
                .map(Claims::getSubject)
                .orElse(null);
    }

    private Optional<Claims> getClaims(String token) {
        if (token == null || token.isBlank()) {
            logger.debug("JWT token ausente ou vazio.");
            return Optional.empty();
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return Optional.of(claims);
        } catch (ExpiredJwtException e) {
            logger.warn("JWT expirado: {}", e.getMessage());
            return Optional.empty();
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("JWT inválido: {}", e.getClass().getSimpleName());
            logger.debug("Detalhes da falha ao validar JWT.", e);
            return Optional.empty();
        }
    }
}