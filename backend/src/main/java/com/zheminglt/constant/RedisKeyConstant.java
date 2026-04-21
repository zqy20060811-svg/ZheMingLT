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
}
