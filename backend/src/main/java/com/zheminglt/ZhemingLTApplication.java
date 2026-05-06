package com.zheminglt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ZhemingLTApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZhemingLTApplication.class, args);
    }
}