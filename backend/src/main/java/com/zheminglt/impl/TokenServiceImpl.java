package com.zheminglt.impl;

import com.zheminglt.constant.BusinessConstant;
import com.zheminglt.service.TokenService;
import com.zheminglt.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class TokenServiceImpl implements TokenService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // Redis Key 前缀
    private static final String ACCESS_TOKEN_PREFIX = "token:access:";
    private static final String REFRESH_TOKEN_PREFIX = "token:refresh:";
    private static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "token:blacklist:access:";
    private static final String REFRESH_TOKEN_BLACKLIST_PREFIX = "token:blacklist:refresh:";
    private static final String USER_TOKEN_PREFIX = "token:user:";

    // ==================== AccessToken 相关 ====================

    @Override
    public void storeAccessToken(String token, Long userId) {
        String key = ACCESS_TOKEN_PREFIX + token;
        // 存储到 Redis，过期时间与 JWT 一致（15 分钟）
        redisTemplate.opsForValue().set(key, userId.toString(), BusinessConstant.ACCESS_TOKEN_EXPIRATION, TimeUnit.MILLISECONDS);
        // 更新用户Token映射
        redisTemplate.opsForHash().put(USER_TOKEN_PREFIX + userId, "accessToken", token);
    }

    @Override
    public boolean isAccessTokenValid(String token) {
        // 先检查是否在黑名单中
        if (isAccessTokenBlacklisted(token)) {
            System.out.println("Token在黑名单中: " + token.substring(0, 20) + "...");
            return false;
        }
        // 再检查token是否存在且JWT有效
        String key = ACCESS_TOKEN_PREFIX + token;
        boolean exists = Boolean.TRUE.equals(redisTemplate.hasKey(key));
        System.out.println("Token是否存在: " + exists + ", key: " + key.substring(0, 30) + "...");
        if (!exists) {
            return false;
        }
        return JWTUtil.validateAccessToken(token);
    }

    @Override
    public void blacklistAccessToken(String token) {
        // 获取剩余过期时间
        long expireTime = JWTUtil.getAccessTokenExpireTime(token);
        if (expireTime > 0) {
            String key = ACCESS_TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "1", expireTime, TimeUnit.MILLISECONDS);
        }
        // 删除原Token
        removeAccessToken(token);
    }

    @Override
    public boolean isAccessTokenBlacklisted(String token) {
        String key = ACCESS_TOKEN_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void removeAccessToken(String token) {
        String key = ACCESS_TOKEN_PREFIX + token;
        String userIdStr = redisTemplate.opsForValue().get(key);
        if (userIdStr != null) {
            redisTemplate.delete(key);
            // 从用户Token映射中移除
            redisTemplate.opsForHash().delete(USER_TOKEN_PREFIX + userIdStr, "accessToken");
        }
    }

    // ==================== RefreshToken 相关 ====================

    @Override
    public void storeRefreshToken(String token, Long userId) {
        String key = REFRESH_TOKEN_PREFIX + token;
        // 存储到 Redis，过期时间与 JWT 一致（7 天）
        redisTemplate.opsForValue().set(key, userId.toString(), BusinessConstant.REFRESH_TOKEN_EXPIRATION, TimeUnit.MILLISECONDS);
        // 更新用户Token映射
        redisTemplate.opsForHash().put(USER_TOKEN_PREFIX + userId, "refreshToken", token);
    }

    @Override
    public boolean isRefreshTokenValid(String token) {
        // 先检查是否在黑名单中
        if (isRefreshTokenBlacklisted(token)) {
            return false;
        }
        // 再检查token是否存在且JWT有效
        String key = REFRESH_TOKEN_PREFIX + token;
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return false;
        }
        return JWTUtil.validateRefreshToken(token);
    }

    @Override
    public void blacklistRefreshToken(String token) {
        // 获取剩余过期时间
        long expireTime = JWTUtil.getRefreshTokenExpireTime(token);
        if (expireTime > 0) {
            String key = REFRESH_TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "1", expireTime, TimeUnit.MILLISECONDS);
        }
        // 删除原Token
        removeRefreshToken(token);
    }

    @Override
    public boolean isRefreshTokenBlacklisted(String token) {
        String key = REFRESH_TOKEN_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void removeRefreshToken(String token) {
        String key = REFRESH_TOKEN_PREFIX + token;
        String userIdStr = redisTemplate.opsForValue().get(key);
        if (userIdStr != null) {
            redisTemplate.delete(key);
            // 从用户Token映射中移除
            redisTemplate.opsForHash().delete(USER_TOKEN_PREFIX + userIdStr, "refreshToken");
        }
    }

    // ==================== Token刷新相关 ====================

    @Override
    public Map<String, String> refreshTokens(String refreshToken) {
        // 1. 验证RefreshToken是否有效
        if (!isRefreshTokenValid(refreshToken)) {
            return null;
        }

        // 2. 从RefreshToken中获取用户ID
        Long userId = JWTUtil.getUserIdFromRefreshToken(refreshToken);

        // 3. 将旧的RefreshToken加入黑名单（实现单点登录，一个refreshToken只能用一次）
        blacklistRefreshToken(refreshToken);

        // 4. 将旧的AccessToken也加入黑名单
        String oldAccessToken = (String) redisTemplate.opsForHash().get(USER_TOKEN_PREFIX + userId, "accessToken");
        if (oldAccessToken != null) {
            blacklistAccessToken(oldAccessToken);
        }

        // 5. 生成新的双Token
        String newAccessToken = JWTUtil.generateAccessToken(userId);
        String newRefreshToken = JWTUtil.generateRefreshToken(userId);

        // 6. 存储新的Token
        storeAccessToken(newAccessToken, userId);
        storeRefreshToken(newRefreshToken, userId);

        // 7. 返回新的Token
        Map<String, String> result = new HashMap<>();
        result.put("accessToken", newAccessToken);
        result.put("refreshToken", newRefreshToken);
        return result;
    }

    @Override
    public void invalidateAllUserTokens(Long userId) {
        // 获取用户的所有Token并加入黑名单
        String oldAccessToken = (String) redisTemplate.opsForHash().get(USER_TOKEN_PREFIX + userId, "accessToken");
        String oldRefreshToken = (String) redisTemplate.opsForHash().get(USER_TOKEN_PREFIX + userId, "refreshToken");

        if (oldAccessToken != null) {
            blacklistAccessToken(oldAccessToken);
        }
        if (oldRefreshToken != null) {
            blacklistRefreshToken(oldRefreshToken);
        }

        // 删除用户Token映射
        redisTemplate.delete(USER_TOKEN_PREFIX + userId);
    }

    // ==================== 兼容旧版方法（已废弃） ====================

    @Override
    @Deprecated
    public void storeToken(String token, Long userId) {
        storeAccessToken(token, userId);
    }

    @Override
    @Deprecated
    public boolean isTokenValid(String token) {
        return isAccessTokenValid(token);
    }

    @Override
    @Deprecated
    public void blacklistToken(String token) {
        blacklistAccessToken(token);
    }

    @Override
    @Deprecated
    public boolean isTokenBlacklisted(String token) {
        return isAccessTokenBlacklisted(token);
    }

    @Override
    @Deprecated
    public void removeToken(String token) {
        removeAccessToken(token);
    }
}
