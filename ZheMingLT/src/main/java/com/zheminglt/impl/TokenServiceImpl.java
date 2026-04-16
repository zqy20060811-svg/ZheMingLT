package com.zheminglt.impl;

import com.zheminglt.constant.BusinessConstant;
import com.zheminglt.service.TokenService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenServiceImpl implements TokenService {

    // 使用内存存储token（生产环境应使用Redis）
    private static final Map<String, Long> tokenStore = new ConcurrentHashMap<>();
    private static final Map<String, Long> blacklistStore = new ConcurrentHashMap<>();

    @Override
    public void storeToken(String token, Long userId) {
        tokenStore.put(token, userId);
    }

    @Override
    public boolean isTokenValid(String token) {
        // 先检查是否在黑名单中
        if (isTokenBlacklisted(token)) {
            return false;
        }
        // 再检查token是否存在
        return tokenStore.containsKey(token);
    }

    @Override
    public void blacklistToken(String token) {
        blacklistStore.put(token, System.currentTimeMillis());
        // 同时删除token存储
        removeToken(token);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return blacklistStore.containsKey(token);
    }

    @Override
    public void removeToken(String token) {
        tokenStore.remove(token);
    }
}
