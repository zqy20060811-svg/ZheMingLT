package com.zheminglt.constant;

/**
 * 错误码常量类
 */
public final class ErrorCodeConstant {

    private ErrorCodeConstant() {
        // 私有构造函数，防止实例化
    }

    // HTTP 状态码
    public static final int HTTP_OK = 200;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_CONFLICT = 409;
    public static final int HTTP_INTERNAL_ERROR = 500;

    // 通用错误码
    public static final int CODE_SUCCESS = 200;
    public static final int CODE_ERROR = 500;
    public static final int CODE_UNAUTHORIZED = 401;
    public static final int CODE_NOT_FOUND = 404;
    public static final int CODE_CONFLICT = 409;
}
