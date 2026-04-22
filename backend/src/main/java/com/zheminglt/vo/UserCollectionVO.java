package com.zheminglt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户收藏对象")
public class UserCollectionVO {

    @Schema(description = "收藏ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "帖子ID")
    private Long postId;

    @Schema(description = "收藏时间")
    private LocalDateTime createdAt;

    @Schema(description = "关联的帖子信息")
    private PostVO post;
}
