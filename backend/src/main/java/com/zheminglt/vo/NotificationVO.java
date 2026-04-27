package com.zheminglt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "通知对象")
public class NotificationVO {

    @Schema(description = "通知ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "通知类型：COMMENT-评论回复，LIKE-点赞，SYSTEM-系统通知")
    private String type;

    @Schema(description = "通知标题")
    private String title;

    @Schema(description = "通知内容")
    private String content;

    @Schema(description = "关联的帖子ID")
    private Long postId;

    @Schema(description = "关联的评论ID")
    private Long commentId;

    @Schema(description = "触发用户ID")
    private Long fromUserId;

    @Schema(description = "触发用户名称")
    private String fromUserName;

    @Schema(description = "是否已读")
    private Boolean isRead;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
