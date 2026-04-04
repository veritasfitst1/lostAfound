package com.example.server.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Component
//jwt的生成 解析 和校验
public class JwtUtil {

    private final SecretKey secretKey; //密钥
    private final long expirationMs;  //过期时间

    //构造，设置密钥和过期时间
    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms:604800000}") long expirationMs) {
        // JJWT 要求 HMAC 密钥 ≥256 位；对任意长度配置串做 SHA-256 派生为 32 字节
        this.secretKey = hmacKeyFromSecret(secret);
        this.expirationMs = expirationMs;
    }

    private static SecretKey hmacKeyFromSecret(String secret) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = md.digest(secret.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    //生成token  根据userid和role
    public String generateToken(Long userId, String role) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .issuedAt(new Date())//签发时间
                .expiration(new Date(System.currentTimeMillis() + expirationMs)) //过期时间
                .signWith(secretKey)
                .compact();
    }

    //解析token 获得用户id
    public Long getUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }
    //解析token 获得role字段
    public String getRole(String token) {
        return (String) getClaims(token).get("role");
    }

    //验证token bool
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    //解码 成功时返回claims 即token中的载荷数据
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
