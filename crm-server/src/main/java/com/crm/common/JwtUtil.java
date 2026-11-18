package com.crm.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    /**
     * 签名密钥，由 application.yml 的 crm.jwt.secret 注入。
     * 生产环境通过环境变量 CRM_JWT_SECRET 覆盖，避免密钥随代码泄露导致 token 可被伪造。
     */
    @Value("${crm.jwt.secret}")
    private String secret;

    private SecretKey key;

    private static final long EXPIRE = 7 * 24 * 3600 * 1000L;

    @PostConstruct
    void initKey() {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("crm.jwt.secret 长度不足 32 字节，HS256 初始化失败");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    /**
     * 密码指纹：取 BCrypt 哈希的第 8~18 位（跳过固定的 "$2a$10$" 头，落在随机盐里）
     * 改一次密码哈希就变，指纹跟着变，旧 token 便能识别出来
     */
    public static String fingerprint(String passwordHash) {
        if (passwordHash == null || passwordHash.length() < 18) {
            return "";
        }
        return passwordHash.substring(7, 17);
    }

    public String create(Long userId, String empNo, String fingerprint) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("empNo", empNo)
                .claim("fp", fingerprint)
                .expiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(key)
                .compact();
    }

    /** 解析失败（过期/伪造）会抛异常，由拦截器兜住 */
    public TokenInfo parse(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
        return new TokenInfo(
                Long.valueOf(claims.getSubject()),
                claims.get("empNo", String.class),
                claims.get("fp", String.class));
    }

    /**
     * token 里带出来的身份信息
     *
     * @param userId      员工ID
     * @param empNo       工号
     * @param fingerprint 签发时的密码指纹，用来判断改密码后旧 token 是否该作废
     */
    public record TokenInfo(Long userId, String empNo, String fingerprint) {
    }
}
