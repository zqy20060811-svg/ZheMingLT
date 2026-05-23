package com.zheminglt.impl;

import com.zheminglt.constant.BusinessConstant;
import com.zheminglt.service.TokenService;
import com.zheminglt.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
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
    }

    @Override
    public boolean isAccessTokenValid(String token) {
        // 先检查是否在黑名单中
        if (isAccessTokenBlacklisted(token)) {
            return false;
        }
        // 再检查token是否存在且JWT有效
        String key = ACCESS_TOKEN_PREFIX + token;
        boolean exists = Boolean.TRUE.equals(redisTemplate.hasKey(key));
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
        redisTemplate.delete(key);
    }

    // ==================== RefreshToken 相关 ====================

    @Override
    public void storeRefreshToken(String token, Long userId) {
        String key = REFRESH_TOKEN_PREFIX + token;
        // 存储到 Redis，过期时间与 JWT 一致（7 天）
        redisTemplate.opsForValue().set(key, userId.toString(), BusinessConstant.REFRESH_TOKEN_EXPIRATION, TimeUnit.MILLISECONDS);
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
        redisTemplate.delete(key);
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

        // 3. 生成新的双Token
        String newAccessToken = JWTUtil.generateAccessToken(userId);
        String newRefreshToken = JWTUtil.generateRefreshToken(userId);

        // 4. 存储新的Token
        storeAccessToken(newAccessToken, userId);
        storeRefreshToken(newRefreshToken, userId);

        // 5. 将旧的RefreshToken加入黑名单（实现单点登录，一个refreshToken只能用一次）
        blacklistRefreshToken(refreshToken);

        // 6. 返回新的Token
        Map<String, String> result = new HashMap<>();
        result.put("accessToken", newAccessToken);
        result.put("refreshToken", newRefreshToken);
        return result;
    }

    @Override
    public void invalidateAllUserTokens(Long userId) {
        redisTemplate.delete(USER_TOKEN_PREFIX + userId);
    }
}
