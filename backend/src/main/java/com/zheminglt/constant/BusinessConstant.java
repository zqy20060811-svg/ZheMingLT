package com.zheminglt.constant;

/**
 * 业务常量类
 */
public final class BusinessConstant {

    private BusinessConstant() {
        // 私有构造函数，防止实例化
    }

    // ==================== JWT 相关 ====================
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_HEADER = "Authorization";
    public static final String REFRESH_TOKEN_HEADER = "X-Refresh-Token";

    // AccessToken: 短期有效，15分钟
    public static final long ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000; // 15分钟
    public static final long ACCESS_TOKEN_EXPIRATION_SECONDS = 15 * 60; // 15分钟（秒）

    // RefreshToken: 长期有效，7天
    public static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7天
    public static final long REFRESH_TOKEN_EXPIRATION_SECONDS = 7 * 24 * 60 * 60; // 7天（秒）

    // 旧版兼容（已废弃，使用 ACCESS_TOKEN_EXPIRATION）
    @Deprecated
    public static final long TOKEN_EXPIRATION = ACCESS_TOKEN_EXPIRATION;
    @Deprecated
    public static final long TOKEN_EXPIRATION_SECONDS = ACCESS_TOKEN_EXPIRATION_SECONDS;

    // ==================== 用户角色 ====================
    public static final int ROLE_USER = 0;      // 普通用户
    public static final int ROLE_ADMIN = 1;     // 管理员
    public static final int ROLE_SUPER_ADMIN = 2; // 超级管理员

    // ==================== 用户状态 ====================
    public static final int STATUS_DISABLED = 0;    // 禁用
    public static final int STATUS_ENABLED = 1;     // 启用
    public static final int STATUS_LOCKED = 2;      // 锁定

    // ==================== 分页默认值 ====================
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 100;

    // ==================== 文件上传 ====================
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final long MAX_REQUEST_SIZE = 10 * 1024 * 1024; // 10MB
    public static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};

    // ==================== 帖子相关 ====================
    public static final int POST_STATUS_DRAFT = 0;      // 草稿
    public static final int POST_STATUS_PUBLISHED = 1;  // 已发布
    public static final int POST_STATUS_DELETED = 2;    // 已删除

    // ==================== 评论相关 ====================
    public static final int COMMENT_STATUS_NORMAL = 0;   // 正常
    public static final int COMMENT_STATUS_DELETED = 1;  // 已删除
    public static final int COMMENT_STATUS_HIDDEN = 2;   // 隐藏

    // ==================== 缓存 Key 前缀 ====================
    public static final String CACHE_PREFIX = "zheminglt:";
    public static final String CACHE_TOKEN = CACHE_PREFIX + "token:";
    public static final String CACHE_BLACKLIST = CACHE_PREFIX + "blacklist:";
    public static final String CACHE_USER = CACHE_PREFIX + "user:";
    public static final String CACHE_POST = CACHE_PREFIX + "post:";
    public static final String CACHE_HOT_POSTS = CACHE_PREFIX + "hot_posts";
}
