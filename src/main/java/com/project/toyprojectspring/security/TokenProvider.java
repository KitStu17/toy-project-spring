package com.project.toyprojectspring.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.project.toyprojectspring.entity.MemberEntity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class TokenProvider {
    private static final String SECRET_KEY = "qwabUGR9ag09wq1";

    public String create(MemberEntity memberEntity) {
        Date expireDate = Date.from(
                Instant.now().plus(1, ChronoUnit.DAYS));

        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .setSubject(memberEntity.getId())
                .setIssuer("Toy Project")
                .setIssuedAt(expireDate)
                .compact();
    }

    public String validateAndGetMemberId(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
}
