package com.zheminglt.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Token 黑名单工具类
 * 用于管理已登出或失效的 Token
 */
@Component
public class TokenBlacklistUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    /**
     * 将 Token 加入黑名单
     *
     * @param token     JWT Token
     * @param expireTime 过期时间（毫秒）
     */
    public void addToBlacklist(String token, long expireTime) {
        String key = BLACKLIST_PREFIX + token;
        // 存储到 Redis，设置过期时间与 Token 一致
        redisTemplate.opsForValue().set(key, "1", expireTime, TimeUnit.MILLISECONDS);
    }

    /**
     * 检查 Token 是否在黑名单中
     *
     * @param token JWT Token
     * @return true 表示在黑名单中
     */
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 从黑名单中移除 Token（一般不需要手动调用）
     *
     * @param token JWT Token
     */
    public void removeFromBlacklist(String token) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.delete(key);
    }
}
