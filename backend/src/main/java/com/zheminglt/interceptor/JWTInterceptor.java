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
        // 获取请求头中的token
        String authorization = request.getHeader(BusinessConstant.TOKEN_HEADER);

        if (authorization == null || !authorization.startsWith(BusinessConstant.TOKEN_PREFIX)) {
            response.setStatus(ErrorCodeConstant.HTTP_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            ResponseVO<String> errorResponse = ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_EMPTY);
            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
            return false;
        }

        String token = authorization.substring(BusinessConstant.TOKEN_PREFIX.length());

        // 1. 验证JWT token是否有效
        if (!JWTUtil.validateToken(token)) {
            response.setStatus(ErrorCodeConstant.HTTP_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            ResponseVO<String> errorResponse = ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_INVALID);
            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
            return false;
        }

        // 2. 检查token是否在Redis黑名单中
        if (tokenService.isTokenBlacklisted(token)) {
            response.setStatus(ErrorCodeConstant.HTTP_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            ResponseVO<String> errorResponse = ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_BLACKLISTED);
            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
            return false;
        }

        // 3. 检查token是否在Redis中存在（可选，用于单点登录控制）
        if (!tokenService.isTokenValid(token)) {
            response.setStatus(ErrorCodeConstant.HTTP_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            ResponseVO<String> errorResponse = ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.UNAUTHORIZED);
            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
            return false;
        }

        // 4. 从token中提取用户ID并设置到request属性中
        Long userId = JWTUtil.getUserIdFromToken(token);
        request.setAttribute("userId", userId);

        return true;
    }
}
