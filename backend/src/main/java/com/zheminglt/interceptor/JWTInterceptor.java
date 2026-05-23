package com.zheminglt.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zheminglt.constant.BusinessConstant;
import com.zheminglt.constant.ErrorCodeConstant;
import com.zheminglt.constant.MessageConstant;
import com.zheminglt.service.TokenService;
import com.zheminglt.utils.JWTUtil;
import com.zheminglt.vo.ResponseVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JWTInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求头中的AccessToken
        String authorization = request.getHeader(BusinessConstant.TOKEN_HEADER);
        
        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        // 定义完全公开的路径（不需要任何认证）
        boolean isPublicPath = requestUri.equals("/api/users/login")
                || requestUri.equals("/api/users/register")
                || requestUri.equals("/api/users/refresh")
                || requestUri.startsWith("/api/categories")
                || requestUri.matches("/api/posts/\\d+")
                || requestUri.matches("/api/posts/user/\\d+")
                || (requestUri.equals("/api/posts") && "GET".equals(method))
                || (requestUri.equals("/api/posts/hot") && "GET".equals(method))
                || requestUri.matches("/api/comments/post/\\d+")
                || requestUri.matches("/api/follows/\\d+/following/count")
                || requestUri.matches("/api/follows/\\d+/followers/count")
                || requestUri.matches("/api/users/\\d+")
                || requestUri.matches("/api/users/\\d+/stats")
                || requestUri.matches("/api/users/\\d+/posts")
                || requestUri.startsWith("/swagger-ui")
                || requestUri.startsWith("/v3/api-docs");
        if (isPublicPath) {
            return true;
        }

        // 定义可选验证的路径（有token就解析，没有也不报错）
        boolean isOptionalAuthPath = requestUri.matches("/api/posts/\\d+")
                || requestUri.matches("/api/comments/post/\\d+");

        if (authorization == null || !authorization.startsWith(BusinessConstant.TOKEN_PREFIX)) {
            // 如果是可选验证路径，没有token也放行
            if (isOptionalAuthPath) {
                return true;
            }
            response.setStatus(ErrorCodeConstant.HTTP_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            ResponseVO<String> errorResponse = ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_EMPTY);
            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
            return false;
        }

        String accessToken = authorization.substring(BusinessConstant.TOKEN_PREFIX.length());

        // 1. 验证JWT AccessToken是否有效
        if (!JWTUtil.validateAccessToken(accessToken)) {
            // 如果是可选验证路径，token过期也放行
            if (isOptionalAuthPath) {
                return true;
            }
            // AccessToken已过期，检查是否需要返回特定错误码以便前端刷新
            response.setStatus(ErrorCodeConstant.HTTP_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            // 返回特定的错误码，告诉前端Token已过期，需要使用RefreshToken刷新
            ResponseVO<String> errorResponse = ResponseVO.error(ErrorCodeConstant.CODE_TOKEN_EXPIRED, MessageConstant.TOKEN_EXPIRED);
            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
            return false;
        }

        // 2. 检查AccessToken是否在黑名单中
        if (tokenService.isAccessTokenBlacklisted(accessToken)) {
            // 如果是可选验证路径，token在黑名单也放行
            if (isOptionalAuthPath) {
                return true;
            }
            response.setStatus(ErrorCodeConstant.HTTP_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            ResponseVO<String> errorResponse = ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_BLACKLISTED);
            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
            return false;
        }

        // 3. 检查AccessToken是否在存储中存在（可选，用于单点登录控制）
        boolean isValid = tokenService.isAccessTokenValid(accessToken);
        if (!isValid) {
            // 如果是可选验证路径，token无效也放行
            if (isOptionalAuthPath) {
                return true;
            }
            response.setStatus(ErrorCodeConstant.HTTP_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            ResponseVO<String> errorResponse = ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.UNAUTHORIZED);
            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
            return false;
        }

        // 4. 从AccessToken中提取用户ID并设置到request属性中
        Long userId = JWTUtil.getUserIdFromAccessToken(accessToken);
        request.setAttribute("userId", userId);

        // 5. 检查AccessToken是否即将过期，如果是，在响应头中告知前端
        if (JWTUtil.isAccessTokenExpiringSoon(accessToken)) {
            response.setHeader("X-Token-Expiring-Soon", "true");
        }

        return true;
    }
}
