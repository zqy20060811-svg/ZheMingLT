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
                // 需要拦截的路径
                .addPathPatterns("/users/info", "/users/info/**")
                .addPathPatterns("/users/logout")
                .addPathPatterns("/posts/create", "/posts/update", "/posts/delete")
                .addPathPatterns("/comments", "/comments/**")
                .addPathPatterns("/likes", "/likes/**")
                .addPathPatterns("/collections", "/collections/**")
                // 不需要拦截的路径（公开接口）
                .excludePathPatterns("/users/login", "/users/register")
                .excludePathPatterns("/posts", "/posts/**", "/categories", "/categories/**");
    }
}
