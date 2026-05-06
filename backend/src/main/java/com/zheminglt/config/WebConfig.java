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
        System.out.println("========== WebConfig.addInterceptors 被调用 ==========");
        System.out.println("JWTInterceptor is null: " + (jwtInterceptor == null));
        registry.addInterceptor(jwtInterceptor)
                // 拦截所有路径（context-path已经是/api，所以这里用/**）
                .addPathPatterns("/**")
                // 不需要拦截的公开接口
                .excludePathPatterns("/users/login", "/users/register", "/users/refresh")
                .excludePathPatterns("/posts", "/posts/hot", "/posts/{id:[\\d+]}", "/posts/user/{userId:[\\d+]}")
                .excludePathPatterns("/categories", "/categories/**")
                .excludePathPatterns("/comments/post/{postId:[\\d+]}")
                .excludePathPatterns("/follows/{userId:[\\d+]}/following/count", "/follows/{userId:[\\d+]}/followers/count")
                .excludePathPatterns("/users/{userId:[\\d+]}", "/users/{userId:[\\d+]}/stats", "/users/{userId:[\\d+]}/posts")
                .excludePathPatterns("/swagger-ui/**", "/v3/api-docs/**");
    }
}
