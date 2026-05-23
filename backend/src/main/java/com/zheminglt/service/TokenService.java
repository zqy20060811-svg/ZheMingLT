package com.zheminglt.service;

import java.util.Map;

public interface TokenService {

    void storeAccessToken(String token, Long userId);

    boolean isAccessTokenValid(String token);

    void blacklistAccessToken(String token);

    boolean isAccessTokenBlacklisted(String token);

    void removeAccessToken(String token);

    void storeRefreshToken(String token, Long userId);

    boolean isRefreshTokenValid(String token);

    void blacklistRefreshToken(String token);

    boolean isRefreshTokenBlacklisted(String token);

    void removeRefreshToken(String token);

    Map<String, String> refreshTokens(String refreshToken);

    void invalidateAllUserTokens(Long userId);
}
