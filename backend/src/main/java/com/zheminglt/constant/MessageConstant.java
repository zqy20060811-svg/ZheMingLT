package com.zheminglt.constant;

/**
 * 消息提示常量类
 */
public final class MessageConstant {

    private MessageConstant() {
        // 私有构造函数，防止实例化
    }

    // ==================== 通用消息 ====================
    public static final String SUCCESS = "操作成功";
    public static final String ERROR = "操作失败";
    public static final String PARAM_ERROR = "参数错误";
    public static final String SERVER_ERROR = "服务器内部错误";

    // ==================== 用户相关消息 ====================
    // 成功消息
    public static final String USER_REGISTER_SUCCESS = "注册成功";
    public static final String USER_LOGIN_SUCCESS = "登录成功";
    public static final String USER_LOGOUT_SUCCESS = "退出登录成功";
    public static final String USER_UPDATE_SUCCESS = "用户信息更新成功";

    // 错误消息
    public static final String USER_NOT_FOUND = "用户不存在";
    public static final String USER_ALREADY_EXISTS = "用户已存在";
    public static final String USERNAME_ALREADY_EXISTS = "用户名已被注册";
    public static final String EMAIL_ALREADY_EXISTS = "邮箱已被注册";
    public static final String PHONE_ALREADY_EXISTS = "手机号已被注册";
    public static final String USERNAME_OR_PASSWORD_ERROR = "用户名或密码错误";
    public static final String PASSWORD_ERROR = "密码错误";
    public static final String TOKEN_EMPTY = "Token不能为空";
    public static final String TOKEN_INVALID = "Token无效或已过期";
    public static final String TOKEN_EXPIRED = "Token已过期";
    public static final String TOKEN_BLACKLISTED = "Token已被加入黑名单";
    public static final String UNAUTHORIZED = "未登录或登录已过期";
    public static final String FORBIDDEN = "没有权限执行此操作";

    // ==================== 帖子相关消息 ====================
    public static final String POST_CREATE_SUCCESS = "帖子发布成功";
    public static final String POST_UPDATE_SUCCESS = "帖子更新成功";
    public static final String POST_DELETE_SUCCESS = "帖子删除成功";
    public static final String POST_NOT_FOUND = "帖子不存在";
    public static final String POST_ALREADY_EXISTS = "帖子已存在";

    // ==================== 评论相关消息 ====================
    public static final String COMMENT_CREATE_SUCCESS = "评论发表成功";
    public static final String COMMENT_UPDATE_SUCCESS = "评论更新成功";
    public static final String COMMENT_DELETE_SUCCESS = "评论删除成功";
    public static final String COMMENT_NOT_FOUND = "评论不存在";

    // ==================== 分类相关消息 ====================
    public static final String CATEGORY_CREATE_SUCCESS = "分类创建成功";
    public static final String CATEGORY_UPDATE_SUCCESS = "分类更新成功";
    public static final String CATEGORY_DELETE_SUCCESS = "分类删除成功";
    public static final String CATEGORY_NOT_FOUND = "分类不存在";

    // ==================== 互动相关消息 ====================
    public static final String LIKE_SUCCESS = "点赞成功";
    public static final String UNLIKE_SUCCESS = "取消点赞成功";
    public static final String COLLECT_SUCCESS = "收藏成功";
    public static final String UNCOLLECT_SUCCESS = "取消收藏成功";
    public static final String ALREADY_LIKED = "已经点赞过了";
    public static final String NOT_LIKED = "还没有点赞";
    public static final String ALREADY_COLLECTED = "已经收藏过了";
    public static final String NOT_COLLECTED = "还没有收藏";
}
