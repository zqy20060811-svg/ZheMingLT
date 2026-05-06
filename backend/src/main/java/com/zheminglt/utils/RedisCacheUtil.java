package com.zheminglt.utils;

import com.zheminglt.constant.RedisKeyConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisCacheUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // ==================== 点赞缓存 ====================

    /**
     * 添加点赞
     */
    public void addLike(Long postId, Long userId) {
        String likeKey = RedisKeyConstant.POST_LIKE_SET + postId;
        String countKey = RedisKeyConstant.POST_LIKE_COUNT + postId;

        redisTemplate.opsForSet().add(likeKey, userId.toString());
        redisTemplate.opsForValue().increment(countKey);
    }

    /**
     * 取消点赞
     */
    public void removeLike(Long postId, Long userId) {
        String likeKey = RedisKeyConstant.POST_LIKE_SET + postId;
        String countKey = RedisKeyConstant.POST_LIKE_COUNT + postId;

        redisTemplate.opsForSet().remove(likeKey, userId.toString());
        redisTemplate.opsForValue().decrement(countKey);
    }

    /**
     * 检查是否已点赞
     */
    public boolean hasLiked(Long postId, Long userId) {
        String likeKey = RedisKeyConstant.POST_LIKE_SET + postId;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(likeKey, userId.toString()));
    }

    /**
     * 获取点赞数量
     */
    public Long getLikeCount(Long postId) {
        String countKey = RedisKeyConstant.POST_LIKE_COUNT + postId;
        String count = redisTemplate.opsForValue().get(countKey);
        return count != null ? Long.parseLong(count) : null;
    }

    /**
     * 获取点赞用户集合
     */
    public Set<String> getLikeUsers(Long postId) {
        String likeKey = RedisKeyConstant.POST_LIKE_SET + postId;
        return redisTemplate.opsForSet().members(likeKey);
    }

    /**
     * 删除点赞缓存
     */
    public void deleteLikeCache(Long postId) {
        String likeKey = RedisKeyConstant.POST_LIKE_SET + postId;
        String countKey = RedisKeyConstant.POST_LIKE_COUNT + postId;
        redisTemplate.delete(likeKey);
        redisTemplate.delete(countKey);
    }

    // ==================== 收藏缓存 ====================

    /**
     * 添加收藏
     */
    public void addCollect(Long postId, Long userId) {
        String collectKey = RedisKeyConstant.POST_COLLECT_SET + postId;
        String countKey = RedisKeyConstant.POST_COLLECT_COUNT + postId;

        redisTemplate.opsForSet().add(collectKey, userId.toString());
        redisTemplate.opsForValue().increment(countKey);
    }

    /**
     * 取消收藏
     */
    public void removeCollect(Long postId, Long userId) {
        String collectKey = RedisKeyConstant.POST_COLLECT_SET + postId;
        String countKey = RedisKeyConstant.POST_COLLECT_COUNT + postId;

        redisTemplate.opsForSet().remove(collectKey, userId.toString());
        redisTemplate.opsForValue().decrement(countKey);
    }

    /**
     * 检查是否已收藏
     */
    public boolean hasCollected(Long postId, Long userId) {
        String collectKey = RedisKeyConstant.POST_COLLECT_SET + postId;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(collectKey, userId.toString()));
    }

    /**
     * 获取收藏数量
     */
    public Long getCollectCount(Long postId) {
        String countKey = RedisKeyConstant.POST_COLLECT_COUNT + postId;
        String count = redisTemplate.opsForValue().get(countKey);
        return count != null ? Long.parseLong(count) : null;
    }

    /**
     * 获取收藏用户集合
     */
    public Set<String> getCollectUsers(Long postId) {
        String collectKey = RedisKeyConstant.POST_COLLECT_SET + postId;
        return redisTemplate.opsForSet().members(collectKey);
    }

    /**
     * 删除收藏缓存
     */
    public void deleteCollectCache(Long postId) {
        String collectKey = RedisKeyConstant.POST_COLLECT_SET + postId;
        String countKey = RedisKeyConstant.POST_COLLECT_COUNT + postId;
        redisTemplate.delete(collectKey);
        redisTemplate.delete(countKey);
    }

    // ==================== 通用方法 ====================

    /**
     * 设置缓存过期时间
     */
    public void expire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
