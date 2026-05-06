package com.zheminglt.service;

import org.springframework.web.multipart.MultipartFile;

public interface OSSService {
    String uploadAvatar(MultipartFile file, Long userId);
}
