package com.zheminglt.job;

import com.zheminglt.impl.CollectionServiceImpl;
import com.zheminglt.impl.LikeServiceImpl;
import com.zheminglt.mapper.PostMapper;
import com.zheminglt.model.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Redis 缓存同步任务
 * 定时将 Redis 中的点赞、收藏数据同步到数据库
 */
@Component
public class RedisSyncJob {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private LikeServiceImpl likeService;

    @Autowired
    private CollectionServiceImpl collectionService;

    @Autowired
    private PostMapper postMapper;

    /**
     * 每5分钟同步一次点赞数据到数据库
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5分钟
    public void syncLikesToDatabase() {
        // 获取所有有缓存的帖子ID
        Set<String> keys = redisTemplate.keys("post:like:count:*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            try {
                // 从 key 中提取 postId
                Long postId = Long.parseLong(key.replace("post:like:count:", ""));

                // 同步到数据库
                likeService.syncLikesToDatabase(postId);
            } catch (Exception e) {
                // 同步失败，继续处理下一个
            }
        }
    }

    /**
     * 每5分钟同步一次收藏数据到数据库
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5分钟
    public void syncCollectsToDatabase() {
        // 获取所有有缓存的帖子ID
        Set<String> keys = redisTemplate.keys("post:collect:count:*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            try {
                // 从 key 中提取 postId
                Long postId = Long.parseLong(key.replace("post:collect:count:", ""));

                // 同步到数据库
                collectionService.syncCollectsToDatabase(postId);
            } catch (Exception e) {
                // 同步失败，继续处理下一个
            }
        }
    }

    /**
     * 每天凌晨3点清理过期的缓存数据
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredCache() {
        // 可以在这里添加清理逻辑
    }
}
