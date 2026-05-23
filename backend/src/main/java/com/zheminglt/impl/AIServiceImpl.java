package com.zheminglt.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zheminglt.config.DeepSeekConfig;
import com.zheminglt.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class AIServiceImpl implements AIService {

    @Autowired
    private DeepSeekConfig deepSeekConfig;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 调用 DeepSeek API
     */
    private String callDeepSeekAPI(String systemPrompt, String userPrompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + deepSeekConfig.getApiKey());

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", deepSeekConfig.getModel());
            requestBody.put("max_tokens", deepSeekConfig.getMaxTokens());
            requestBody.put("temperature", deepSeekConfig.getTemperature());

            JSONArray messages = new JSONArray();

            // 系统消息
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);

            // 用户消息
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt);
            messages.add(userMessage);

            requestBody.put("messages", messages);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toJSONString(), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    deepSeekConfig.getApiUrl(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject jsonResponse = JSON.parseObject(response.getBody());
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject message = choice.getJSONObject("message");
                    return message.getString("content");
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<String> generateReplySuggestions(String postContent, List<String> existingComments) {
        String systemPrompt = "你是一个专业的论坛回复助手。请根据帖子内容生成3条简短、友好、有建设性的回复建议。每条建议不超过50字，用中文回复。";

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("帖子内容：\n").append(postContent).append("\n\n");

        if (existingComments != null && !existingComments.isEmpty()) {
            userPrompt.append("已有评论：\n");
            for (int i = 0; i < Math.min(existingComments.size(), 3); i++) {
                userPrompt.append("- ").append(existingComments.get(i)).append("\n");
            }
            userPrompt.append("\n");
        }

        userPrompt.append("请生成3条回复建议，每条一行，前面加上数字序号：");

        String response = callDeepSeekAPI(systemPrompt, userPrompt.toString());

        if (response != null) {
            List<String> suggestions = new ArrayList<>();
            String[] lines = response.split("\n");
            for (String line : lines) {
                line = line.trim();
                // 移除序号前缀
                if (line.matches("^\\d+[.、]\\s*.*")) {
                    line = line.replaceFirst("^\\d+[.、]\\s*", "");
                }
                if (!line.isEmpty() && suggestions.size() < 3) {
                    suggestions.add(line);
                }
            }
            return suggestions;
        }

        // 返回默认建议
        return Arrays.asList(
                "感谢分享，很有启发！",
                "这个问题我也遇到过，确实值得讨论。",
                "期待更多相关内容~"
        );
    }

    @Override
    public String summarizePost(String content) {
        String systemPrompt = "你是一个内容总结专家。请将以下内容总结为一句话，不超过100字，用中文回复。";

        String response = callDeepSeekAPI(systemPrompt, content);

        if (response != null) {
            return response.trim();
        }

        return content.length() > 100 ? content.substring(0, 100) + "..." : content;
    }

    @Override
    public List<String> recommendTags(String title, String content) {
        String systemPrompt = "你是一个标签推荐专家。请根据帖子标题和内容推荐3-5个相关标签。标签应该简洁、相关、热门。用中文回复，每个标签一行。";

        String userPrompt = "标题：" + title + "\n\n内容：" + content + "\n\n请推荐3-5个标签：";

        String response = callDeepSeekAPI(systemPrompt, userPrompt);

        if (response != null) {
            List<String> tags = new ArrayList<>();
            String[] lines = response.split("\n");
            for (String line : lines) {
                line = line.trim();
                // 移除序号和 # 符号
                line = line.replaceFirst("^\\d+[.、]\\s*", "");
                line = line.replaceFirst("^#\\s*", "");
                if (!line.isEmpty() && line.length() <= 20 && tags.size() < 5) {
                    tags.add(line);
                }
            }
            return tags;
        }

        return Arrays.asList("讨论", "分享");
    }

    @Override
    public boolean containsSensitiveContent(String content) {
        String systemPrompt = "你是一个内容审核助手。请判断以下内容是否包含敏感、违规或不当信息（如暴力、色情、政治敏感、广告等）。只回答\"是\"或\"否\"。";

        String response = callDeepSeekAPI(systemPrompt, content);

        if (response != null) {
            String lowerResponse = response.trim().toLowerCase();
            return lowerResponse.contains("是") || lowerResponse.contains("yes");
        }

        return false;
    }

    @Override
    public String suggestTitle(String content) {
        String systemPrompt = "你是一个标题生成专家。请根据以下内容生成一个吸引人的标题，不超过30字，用中文回复。";

        String response = callDeepSeekAPI(systemPrompt, content);

        if (response != null) {
            String title = response.trim();
            if (title.length() > 30) {
                title = title.substring(0, 30);
            }
            return title;
        }

        return "新帖子";
    }
}
