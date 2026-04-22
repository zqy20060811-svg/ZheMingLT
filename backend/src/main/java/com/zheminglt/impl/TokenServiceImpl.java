package com.zheminglt.impl;

import com.zheminglt.constant.BusinessConstant;
import com.zheminglt.service.TokenService;
import com.zheminglt.utils.JWTUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenServiceImpl implements TokenService {

    // AccessToken存储: token -> userId
    private static final Map<String, Long> accessTokenStore = new ConcurrentHashMap<>();
    // AccessToken黑名单: token -> blacklistTime
    private static final Map<String, Long> accessTokenBlacklist = new ConcurrentHashMap<>();

    // RefreshToken存储: token -> userId
    private static final Map<String, Long> refreshTokenStore = new ConcurrentHashMap<>();
    // RefreshToken黑名单: token -> blacklistTime
    private static final Map<String, Long> refreshTokenBlacklist = new ConcurrentHashMap<>();

    // 用户Token映射: userId -> {accessToken, refreshToken}
    private static final Map<Long, Map<String, String>> userTokenMap = new ConcurrentHashMap<>();

    // ==================== AccessToken 相关 ====================

    @Override
    public void storeAccessToken(String token, Long userId) {
        accessTokenStore.put(token, userId);
        // 更新用户Token映射
        userTokenMap.computeIfAbsent(userId, k -> new ConcurrentHashMap<>()).put("accessToken", token);
    }

    @Override
    public boolean isAccessTokenValid(String token) {
        // 先检查是否在黑名单中
        if (isAccessTokenBlacklisted(token)) {
            return false;
        }
        // 再检查token是否存在且JWT有效
        if (!accessTokenStore.containsKey(token)) {
            return false;
        }
        return JWTUtil.validateAccessToken(token);
    }

    @Override
    public void blacklistAccessToken(String token) {
        accessTokenBlacklist.put(token, System.currentTimeMillis());
        removeAccessToken(token);
    }

    @Override
    public boolean isAccessTokenBlacklisted(String token) {
        return accessTokenBlacklist.containsKey(token);
    }

    @Override
    public void removeAccessToken(String token) {
        Long userId = accessTokenStore.remove(token);
        if (userId != null) {
            Map<String, String> tokens = userTokenMap.get(userId);
            if (tokens != null && token.equals(tokens.get("accessToken"))) {
                tokens.remove("accessToken");
            }
        }
    }

    // ==================== RefreshToken 相关 ====================

    @Override
    public void storeRefreshToken(String token, Long userId) {
        refreshTokenStore.put(token, userId);
        // 更新用户Token映射
        userTokenMap.computeIfAbsent(userId, k -> new ConcurrentHashMap<>()).put("refreshToken", token);
    }

    @Override
    public boolean isRefreshTokenValid(String token) {
        // 先检查是否在黑名单中
        if (isRefreshTokenBlacklisted(token)) {
            return false;
        }
        // 再检查token是否存在且JWT有效
        if (!refreshTokenStore.containsKey(token)) {
            return false;
        }
        return JWTUtil.validateRefreshToken(token);
    }

    @Override
    public void blacklistRefreshToken(String token) {
        refreshTokenBlacklist.put(token, System.currentTimeMillis());
        removeRefreshToken(token);
    }

    @Override
    public boolean isRefreshTokenBlacklisted(String token) {
        return refreshTokenBlacklist.containsKey(token);
    }

    @Override
    public void removeRefreshToken(String token) {
        Long userId = refreshTokenStore.remove(token);
        if (userId != null) {
            Map<String, String> tokens = userTokenMap.get(userId);
            if (tokens != null && token.equals(tokens.get("refreshToken"))) {
                tokens.remove("refreshToken");
            }
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
        Map<String, String> oldTokens = userTokenMap.get(userId);
        if (oldTokens != null) {
            String oldAccessToken = oldTokens.get("accessToken");
            if (oldAccessToken != null) {
                blacklistAccessToken(oldAccessToken);
            }
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
        Map<String, String> tokens = userTokenMap.remove(userId);
        if (tokens != null) {
            String accessToken = tokens.get("accessToken");
            String refreshToken = tokens.get("refreshToken");

            if (accessToken != null) {
                blacklistAccessToken(accessToken);
            }
            if (refreshToken != null) {
                blacklistRefreshToken(refreshToken);
            }
        }
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
