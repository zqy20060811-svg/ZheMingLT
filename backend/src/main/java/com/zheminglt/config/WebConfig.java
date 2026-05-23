package com.zheminglt.config;

import com.zheminglt.interceptor.JWTInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JWTInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                // 拦截所有路径（context-path已经是/api，所以这里用/**）
                .addPathPatterns("/**")
                // 不需要拦截的公开接口
                .excludePathPatterns("/api/users/login", "/api/users/register", "/api/users/refresh")
                .excludePathPatterns("/api/posts/{id:[\\d+]}", "/api/posts/user/{userId:[\\d+]}")
                .excludePathPatterns("/api/categories", "/api/categories/**")
                .excludePathPatterns("/api/comments/post/{postId:[\\d+]}")
                .excludePathPatterns("/api/follows/{userId:[\\d+]}/following/count", "/api/follows/{userId:[\\d+]}/followers/count")
                .excludePathPatterns("/api/users/{userId:[\\d+]}", "/api/users/{userId:[\\d+]}/stats", "/api/users/{userId:[\\d+]}/posts")
                .excludePathPatterns("/swagger-ui/**", "/v3/api-docs/**");
    }
}
