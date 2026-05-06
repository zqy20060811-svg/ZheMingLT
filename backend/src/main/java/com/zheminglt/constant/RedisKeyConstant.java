package com.zheminglt.constant;

/**
 * Redis Key 常量类
 */
public class RedisKeyConstant {

    // 热门帖子排行榜 Key 前缀
    public static final String HOT_POSTS_DAY = "hot:posts:day:";
    public static final String HOT_POSTS_WEEK = "hot:posts:week:";
    public static final String HOT_POSTS_MONTH = "hot:posts:month:";
    public static final String HOT_POSTS_ALL = "hot:posts:all";

    // 用户Token前缀
    public static final String USER_TOKEN = "user:token:";

    // Token黑名单前缀
    public static final String TOKEN_BLACKLIST = "token:blacklist:";

    // 帖子浏览量前缀
    public static final String POST_VIEWS = "post:views:";

    // 用户浏览历史前缀
    public static final String USER_HISTORY = "user:history:";

    // 防止重复计算锁前缀
    public static final String HOT_POSTS_LOCK = "hot:posts:lock:";

    // 用户帖子列表缓存前缀
    public static final String USER_POSTS = "user:posts:";

    // 点赞相关 Key
    public static final String POST_LIKE_SET = "post:like:";
    public static final String POST_LIKE_COUNT = "post:like:count:";

    // 收藏相关 Key
    public static final String POST_COLLECT_SET = "post:collect:";
    public static final String POST_COLLECT_COUNT = "post:collect:count:";

    // 用户帖子列表缓存过期时间（分钟）
    public static final long USER_POSTS_EXPIRE = 10;
}
