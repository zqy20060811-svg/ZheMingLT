package com.zheminglt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "关注对象")
public class FollowVO {

    @Schema(description = "关注ID")
    private Long id;

    @Schema(description = "关注者ID")
    private Long followerId;

    @Schema(description = "关注者用户名")
    private String followerName;

    @Schema(description = "关注者头像")
    private String followerAvatar;

    @Schema(description = "被关注者ID")
    private Long followingId;

    @Schema(description = "被关注者用户名")
    private String followingName;

    @Schema(description = "被关注者头像")
    private String followingAvatar;

    @Schema(description = "是否互相关注")
    private Boolean mutualFollow;

    @Schema(description = "关注时间")
    private LocalDateTime createdAt;
}
