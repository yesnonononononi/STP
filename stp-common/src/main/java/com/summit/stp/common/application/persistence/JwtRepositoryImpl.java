package com.summit.stp.common.application.persistence;

import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.constants.UserAuthConstants;
import com.summit.stp.common.application.domain.repository.JwtRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
@ConditionalOnExpression("'${spring.application.name}' == 'stp-auth-service' || '${spring.application.name}' == 'stp-gateway'")
@Repository
public class JwtRepositoryImpl implements JwtRepository {
    private final SecretKey secretKey;
    public  JwtRepositoryImpl( @Value("${stp.auth.jwt.secret-key}")String key) {
        this.secretKey = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }
    @Override
    public String generateToken(UserSession userSession) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userSession.getId().toString())
                .claim("username", userSession.getUsername())
                .claim("ip", userSession.getIp())
                .claim("loginTime",Instant.ofEpochMilli(now))
                .issuedAt(new Date(now))
                .expiration(new Date(now + UserAuthConstants.Business.DEFAULT_TOKEN_EXPIRE_SECONDS))
                .signWith(this.secretKey)
                .compact();
    }

    @Override
    public UserSession parseToken(String token) {
        Claims payload = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return UserSession.builder()
                .id(Long.valueOf(payload.getSubject()))
                .username(payload.get("username", String.class))
                .ip(payload.get("ip", String.class))
                .token(token)
                .loginTime(LocalDateTime.from(payload.get("loginTime", Instant.class)))
                .tokenType(UserSession.TokenType.JWT)
                .build();
    }

}

