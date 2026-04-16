package com.zheminglt.service;

import com.zheminglt.vo.ResponseVO;

public interface AIService {
    ResponseVO<String> generateSummary(String content);
    ResponseVO<Boolean> reviewContent(String content);
}