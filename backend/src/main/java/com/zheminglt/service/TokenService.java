package com.zheminglt.service;

public interface TokenService {
    void storeToken(String token, Long userId);
    boolean isTokenValid(String token);
    void blacklistToken(String token);
    boolean isTokenBlacklisted(String token);
    void removeToken(String token);
}
