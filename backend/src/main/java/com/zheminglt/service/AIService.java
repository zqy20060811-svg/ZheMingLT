package com.zheminglt.service;

import java.util.List;

public interface AIService {
    /**
     * 生成智能回复建议
     * @param postContent 帖子内容
     * @param existingComments 已有评论
     * @return 回复建议列表
     */
    List<String> generateReplySuggestions(String postContent, List<String> existingComments);

    /**
     * 智能总结帖子内容
     * @param content 帖子内容
     * @return 总结内容
     */
    String summarizePost(String content);

    /**
     * 自动推荐标签
     * @param title 帖子标题
     * @param content 帖子内容
     * @return 推荐标签列表
     */
    List<String> recommendTags(String title, String content);

    /**
     * 检测敏感内容
     * @param content 内容
     * @return 是否包含敏感内容
     */
    boolean containsSensitiveContent(String content);

    /**
     * 生成帖子标题建议
     * @param content 帖子内容
     * @return 标题建议
     */
    String suggestTitle(String content);
}
