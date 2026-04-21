package com.zheminglt.controller;

import com.zheminglt.service.AIService;
import com.zheminglt.vo.ResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@Tag(name = "AI服务", description = "AI内容生成和审核接口")
public class AIController {

    @Autowired
    private AIService aiService;

    @Operation(summary = "生成内容摘要", description = "使用AI为帖子内容生成摘要，需要登录")
    @SecurityRequirement(name = "Authorization")
    @PostMapping("/summary")
    public ResponseVO<String> generateSummary(@RequestBody String content) {
        return aiService.generateSummary(content);
    }

    @Operation(summary = "内容审核", description = "使用AI审核内容是否合规，需要登录")
    @SecurityRequirement(name = "Authorization")
    @PostMapping("/review")
    public ResponseVO<Boolean> reviewContent(@RequestBody String content) {
        return aiService.reviewContent(content);
    }
}
