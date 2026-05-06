package com.zheminglt.controller;

import com.zheminglt.service.AIService;
import com.zheminglt.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    /**
     * 生成回复建议
     */
    @PostMapping("/reply-suggestions")
    public ResponseVO<List<String>> generateReplySuggestions(@RequestBody Map<String, Object> request) {
        String postContent = (String) request.get("postContent");
        List<String> existingComments = (List<String>) request.get("existingComments");

        if (postContent == null || postContent.trim().isEmpty()) {
            return ResponseVO.error(400, "帖子内容不能为空");
        }

        List<String> suggestions = aiService.generateReplySuggestions(postContent, existingComments);
        return ResponseVO.success(suggestions);
    }

    /**
     * 总结帖子内容
     */
    @PostMapping("/summarize")
    public ResponseVO<String> summarizePost(@RequestBody Map<String, String> request) {
        String content = request.get("content");

        if (content == null || content.trim().isEmpty()) {
            return ResponseVO.error(400, "内容不能为空");
        }

        String summary = aiService.summarizePost(content);
        return ResponseVO.success(summary);
    }

    /**
     * 生成摘要（与summarize相同，为了兼容前端调用）
     */
    @PostMapping("/summary")
    public ResponseVO<String> generateSummary(@RequestBody Map<String, String> request) {
        String content = request.get("content");

        if (content == null || content.trim().isEmpty()) {
            return ResponseVO.error(400, "内容不能为空");
        }

        String summary = aiService.summarizePost(content);
        return ResponseVO.success(summary);
    }

    /**
     * 推荐标签
     */
    @PostMapping("/recommend-tags")
    public ResponseVO<List<String>> recommendTags(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        String content = request.get("content");

        if (content == null || content.trim().isEmpty()) {
            return ResponseVO.error(400, "内容不能为空");
        }

        List<String> tags = aiService.recommendTags(title != null ? title : "", content);
        return ResponseVO.success(tags);
    }

    /**
     * 检测敏感内容
     */
    @PostMapping("/check-sensitive")
    public ResponseVO<Map<String, Object>> checkSensitiveContent(@RequestBody Map<String, String> request) {
        String content = request.get("content");

        if (content == null || content.trim().isEmpty()) {
            return ResponseVO.error(400, "内容不能为空");
        }

        boolean isSensitive = aiService.containsSensitiveContent(content);
        return ResponseVO.success(Map.of(
                "isSensitive", isSensitive,
                "message", isSensitive ? "内容包含敏感信息" : "内容正常"
        ));
    }

    /**
     * 生成标题建议
     */
    @PostMapping("/suggest-title")
    public ResponseVO<String> suggestTitle(@RequestBody Map<String, String> request) {
        String content = request.get("content");

        if (content == null || content.trim().isEmpty()) {
            return ResponseVO.error(400, "内容不能为空");
        }

        String title = aiService.suggestTitle(content);
        return ResponseVO.success(title);
    }
}
