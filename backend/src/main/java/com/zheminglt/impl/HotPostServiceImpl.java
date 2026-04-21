package com.zheminglt.impl;

import com.zheminglt.constant.RedisKeyConstant;
import com.zheminglt.mapper.PostMapper;
import com.zheminglt.model.Post;
import com.zheminglt.service.HotPostService;
import com.zheminglt.vo.PostVO;
import com.zheminglt.vo.ResponseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class HotPostServiceImpl implements HotPostService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private PostMapper postMapper;

    // 热度计算权重
    private static final double VIEW_WEIGHT = 1.0;
    private static final double LIKE_WEIGHT = 3.0;
    private static final double COMMENT_WEIGHT = 5.0;

    @Override
    public ResponseVO<List<PostVO>> getHotPostsToday(int page, int size) {
        String key = RedisKeyConstant.HOT_POSTS_DAY + LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        return getHotPostsFromRedis(key, page, size);
    }

    @Override
    public ResponseVO<List<PostVO>> getHotPostsWeek(int page, int size) {
        String key = RedisKeyConstant.HOT_POSTS_WEEK + getWeekKey();
        return getHotPostsFromRedis(key, page, size);
    }

    @Override
    public ResponseVO<List<PostVO>> getHotPostsMonth(int page, int size) {
        String key = RedisKeyConstant.HOT_POSTS_MONTH + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return getHotPostsFromRedis(key, page, size);
    }

    @Override
    public ResponseVO<List<PostVO>> getHotPostsAll(int page, int size) {
        return getHotPostsFromRedis(RedisKeyConstant.HOT_POSTS_ALL, page, size);
    }

    /**
     * 从Redis获取热门帖子
     */
    private ResponseVO<List<PostVO>> getHotPostsFromRedis(String key, int page, int size) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        // 计算起始和结束位置
        long start = (page - 1) * size;
        long end = start + size - 1;

        // 获取排序后的帖子ID（按分数倒序）
        Set<ZSetOperations.TypedTuple<String>> tuples = zSetOps.reverseRangeWithScores(key, start, end);

        if (tuples == null || tuples.isEmpty()) {
            // Redis中没有数据，从数据库加载
            return loadHotPostsFromDB(key, page, size);
        }

        // 获取帖子详情
        List<Long> postIds = tuples.stream()
                .map(tuple -> Long.valueOf(tuple.getValue()))
                .collect(Collectors.toList());

        List<PostVO> postVOs = new ArrayList<>();
        for (Long postId : postIds) {
            postMapper.findById(postId).ifPresent(post -> {
                PostVO vo = new PostVO();
                BeanUtils.copyProperties(post, vo);
                // 从Redis获取实时热度分数
                Double score = zSetOps.score(key, String.valueOf(postId));
                if (score != null) {
                    vo.setHeat(score.intValue());
                }
                postVOs.add(vo);
            });
        }

        return ResponseVO.success(postVOs);
    }

    /**
     * 从数据库加载热门帖子并存入Redis
     */
    private ResponseVO<List<PostVO>> loadHotPostsFromDB(String key, int page, int size) {
        List<Post> posts = postMapper.findAll();

        // 计算热度并排序
        List<PostVO> postVOs = posts.stream()
                .map(post -> {
                    PostVO vo = new PostVO();
                    BeanUtils.copyProperties(post, vo);
                    double heat = calculateHeat(
                            post.getViewCount() != null ? post.getViewCount().longValue() : 0L,
                            post.getLikeCount() != null ? post.getLikeCount().longValue() : 0L,
                            post.getCommentCount() != null ? post.getCommentCount().longValue() : 0L
                    );
                    vo.setHeat((int) heat);
                    return vo;
                })
                .sorted((a, b) -> b.getHeat().intValue() - a.getHeat().intValue())
                .skip((page - 1) * size)
                .limit(size)
                .collect(Collectors.toList());

        // 异步存入Redis（这里简化处理，直接存入）
        if (!posts.isEmpty()) {
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
            for (Post post : posts) {
                double heat = calculateHeat(
                        post.getViewCount() != null ? post.getViewCount().longValue() : 0L,
                        post.getLikeCount() != null ? post.getLikeCount().longValue() : 0L,
                        post.getCommentCount() != null ? post.getCommentCount().longValue() : 0L
                );
                zSetOps.add(key, String.valueOf(post.getId()), heat);
            }
            // 设置过期时间
            redisTemplate.expire(key, Duration.ofSeconds(getExpireTime(key)));
        }

        return ResponseVO.success(postVOs);
    }

    @Override
    public void updatePostHeat(Long postId, Long viewCount, Long likeCount, Long commentCount) {
        double heat = calculateHeat(viewCount, likeCount, commentCount);

        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        // 更新总榜
        zSetOps.add(RedisKeyConstant.HOT_POSTS_ALL, String.valueOf(postId), heat);

        // 更新今日榜
        String dayKey = RedisKeyConstant.HOT_POSTS_DAY + LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        zSetOps.add(dayKey, String.valueOf(postId), heat);
        redisTemplate.expire(dayKey, Duration.ofSeconds(2 * 24 * 60 * 60)); // 2天过期

        // 更新本周榜
        String weekKey = RedisKeyConstant.HOT_POSTS_WEEK + getWeekKey();
        zSetOps.add(weekKey, String.valueOf(postId), heat);
        redisTemplate.expire(weekKey, Duration.ofSeconds(8 * 24 * 60 * 60)); // 8天过期

        // 更新本月榜
        String monthKey = RedisKeyConstant.HOT_POSTS_MONTH + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        zSetOps.add(monthKey, String.valueOf(postId), heat);
        redisTemplate.expire(monthKey, Duration.ofSeconds(32 * 24 * 60 * 60)); // 32天过期
    }

    @Override
    public double calculateHeat(Long viewCount, Long likeCount, Long commentCount) {
        double v = viewCount != null ? viewCount : 0;
        double l = likeCount != null ? likeCount : 0;
        double c = commentCount != null ? commentCount : 0;

        return v * VIEW_WEIGHT + l * LIKE_WEIGHT + c * COMMENT_WEIGHT;
    }

    /**
     * 获取本周的Key
     */
    private String getWeekKey() {
        LocalDate now = LocalDate.now();
        // 获取本周一
        LocalDate monday = now.minusDays(now.getDayOfWeek().getValue() - 1);
        return monday.format(DateTimeFormatter.ISO_DATE);
    }

    /**
     * 获取过期时间（秒）
     */
    private long getExpireTime(String key) {
        if (key.startsWith(RedisKeyConstant.HOT_POSTS_DAY)) {
            return 2 * 24 * 60 * 60; // 2天
        } else if (key.startsWith(RedisKeyConstant.HOT_POSTS_WEEK)) {
            return 8 * 24 * 60 * 60; // 8天
        } else if (key.startsWith(RedisKeyConstant.HOT_POSTS_MONTH)) {
            return 32 * 24 * 60 * 60; // 32天
        }
        return 7 * 24 * 60 * 60; // 默认7天
    }
}
