package com.lei.springbootpractice4.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException; // 引入 ExpiredJwtException
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys; // <--- 引入 Keys
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey; // <--- 引入 SecretKey
import java.io.Serializable;
import java.nio.charset.StandardCharsets; // <--- 引入 StandardCharsets
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import jakarta.annotation.PostConstruct; // <--- 引入 PostConstruct

@Component
public class JwtUtil implements Serializable {

    private static final long serialVersionUID = -2550185165626007488L;

    // 令牌有效期设置为 5 小时
    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;

    // 从 application.yml 读取密钥字符串
    @Value("${jwt.secret}")
    private String secretString;

    // 用于实际签名和验证的 SecretKey 对象
    private SecretKey secretKey;

    // 在 Bean 初始化后执行，根据字符串生成 SecretKey
    @PostConstruct
    public void init() {
        // 使用 HMAC-SHA 算法根据密钥字符串生成安全的 SecretKey
        this.secretKey = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 从 JWT 令牌中获取用户名
     * @param token JWT 令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从 JWT 令牌中获取过期时间
     * @param token JWT 令牌
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从 JWT 令牌中获取指定的 Claim
     * @param token JWT 令牌
     * @param claimsResolver 用于提取 Claim 的函数
     * @param <T> Claim 的类型
     * @return 提取出的 Claim
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从 JWT 令牌中获取所有的 Claims (需要密钥)
     * @param token JWT 令牌
     * @return Claims 对象
     */
    private Claims getAllClaimsFromToken(String token) {
        // 使用 parserBuilder() 和生成的 secretKey 来解析
        return Jwts.parserBuilder()
                .setSigningKey(secretKey) // 使用 SecretKey 对象
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查 JWT 令牌是否已过期
     * @param token JWT 令牌
     * @return 如果已过期则返回 true，否则返回 false
     */
    private Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            // 如果解析时就因为过期而抛出异常，也算过期
            return true;
        }
    }

    /**
     * 为用户生成 JWT 令牌 (原始方法，仅使用 username 作为 subject)
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername());
    }

    /**
     * 为用户生成 JWT 令牌 (重载方法，允许传入额外 Claims)
     */
    public String generateToken(Map<String, Object> claims, String subject) {
        return doGenerateToken(claims, subject);
    }


    /**
     * 执行 JWT 令牌的生成
     * 1. 定义 Claims (签发者, 过期时间, 主题等)
     * 2. 使用 HS512 算法和 SecretKey 签名
     * 3. 压缩成 URL 安全的字符串
     * @param claims 附加的 Claims
     * @param subject 令牌的主题 (通常是用户名)
     * @return JWT 令牌字符串
     */
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000)) // 设置过期时间
                .signWith(secretKey, SignatureAlgorithm.HS256) // 使用 SecretKey 和 HS512 算法签名
                .compact();
    }
    public String getUserIdFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("userId", String.class));
    }

    /**
     * 验证 JWT 令牌是否有效
     * @param token JWT 令牌
     * @param userDetails Spring Security 的 UserDetails 对象
     * @return 如果令牌对该用户有效且未过期则返回 true，否则返回 false
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}