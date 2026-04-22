package com.zheminglt.utils;

import com.zheminglt.constant.BusinessConstant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JWTUtil {

    private static final String SECRET_KEY = "zheminglt_secret_key_zheminglt_secret_key_zheminglt_secret_key";
    private static final String REFRESH_SECRET_KEY = "zheminglt_refresh_secret_key_zheminglt_refresh_secret_key_zheminglt_refresh";

    private static SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    private static SecretKey getRefreshSigningKey() {
        return Keys.hmacShaKeyFor(REFRESH_SECRET_KEY.getBytes());
    }

    /**
     * 生成AccessToken（短期有效，15分钟）
     */
    public static String generateAccessToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "access");
        claims.put("jti", UUID.randomUUID().toString()); // JWT ID，用于唯一标识

        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + BusinessConstant.ACCESS_TOKEN_EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成RefreshToken（长期有效，7天）
     */
    public static String generateRefreshToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");
        claims.put("jti", UUID.randomUUID().toString()); // JWT ID，用于唯一标识

        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + BusinessConstant.REFRESH_TOKEN_EXPIRATION))
                .signWith(getRefreshSigningKey())
                .compact();
    }

    /**
     * 验证AccessToken
     */
    public static boolean validateAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            // 检查token类型
            return "access".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证RefreshToken
     */
    public static boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getRefreshSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            // 检查token类型
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从AccessToken中提取用户ID
     */
    public static Long getUserIdFromAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.get("userId").toString());
    }

    /**
     * 从RefreshToken中提取用户ID
     */
    public static Long getUserIdFromRefreshToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getRefreshSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.get("userId").toString());
    }

    /**
     * 获取Token的过期时间
     */
    public static Date getExpirationDateFromAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getExpiration();
    }

    /**
     * 获取Token的JWT ID
     */
    public static String getJtiFromToken(String token, boolean isRefreshToken) {
        SecretKey key = isRefreshToken ? getRefreshSigningKey() : getSigningKey();
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("jti", String.class);
    }

    /**
     * 检查AccessToken是否即将过期（剩余时间小于5分钟）
     */
    public static boolean isAccessTokenExpiringSoon(String token) {
        try {
            Date expiration = getExpirationDateFromAccessToken(token);
            long remainingTime = expiration.getTime() - System.currentTimeMillis();
            return remainingTime < 5 * 60 * 1000; // 小于5分钟
        } catch (Exception e) {
            return true;
        }
    }

    // ==================== 兼容旧版方法（已废弃） ====================

    @Deprecated
    public static String generateToken(Long userId) {
        return generateAccessToken(userId);
    }

    @Deprecated
    public static Long getUserIdFromToken(String token) {
        return getUserIdFromAccessToken(token);
    }

    @Deprecated
    public static boolean validateToken(String token) {
        return validateAccessToken(token);
    }
}
