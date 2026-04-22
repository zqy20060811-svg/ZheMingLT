package com.zheminglt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户点赞对象")
public class UserLikeVO {

    @Schema(description = "点赞ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "帖子ID（如果是帖子点赞）")
    private Long postId;

    @Schema(description = "评论ID（如果是评论点赞）")
    private Long commentId;

    @Schema(description = "点赞类型：POST-帖子点赞，COMMENT-评论点赞")
    private String type;

    @Schema(description = "点赞时间")
    private LocalDateTime createdAt;

    @Schema(description = "关联的帖子信息（如果是帖子点赞）")
    private PostVO post;

    @Schema(description = "关联的评论信息（如果是评论点赞）")
    private CommentVO comment;
}
