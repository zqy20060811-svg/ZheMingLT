package com.zheminglt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
public class OSSConfig {
    private String endpoint = "oss-cn-beijing.aliyuncs.com";
    private String bucketName = "zhemingluntan";
    private String accessKeyId;
    private String accessKeySecret;
}
