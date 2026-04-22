package com.zheminglt.service;

import java.util.Map;

public interface TokenService {

    // ==================== AccessToken 相关 ====================

    void storeAccessToken(String token, Long userId);

    boolean isAccessTokenValid(String token);

    void blacklistAccessToken(String token);

    boolean isAccessTokenBlacklisted(String token);

    void removeAccessToken(String token);

    // ==================== RefreshToken 相关 ====================

    void storeRefreshToken(String token, Long userId);

    boolean isRefreshTokenValid(String token);

    void blacklistRefreshToken(String token);

    boolean isRefreshTokenBlacklisted(String token);

    void removeRefreshToken(String token);

    /**
     * 使用RefreshToken刷新双Token
     * @param refreshToken 刷新令牌
     * @return 包含新accessToken和refreshToken的Map，如果刷新失败返回null
     */
    Map<String, String> refreshTokens(String refreshToken);

    /**
     * 使某个用户的所有Token失效（用于密码修改、账号禁用等场景）
     * @param userId 用户ID
     */
    void invalidateAllUserTokens(Long userId);

    // ==================== 兼容旧版方法（已废弃） ====================

    @Deprecated
    void storeToken(String token, Long userId);

    @Deprecated
    boolean isTokenValid(String token);

    @Deprecated
    void blacklistToken(String token);

    @Deprecated
    boolean isTokenBlacklisted(String token);

    @Deprecated
    void removeToken(String token);
}
