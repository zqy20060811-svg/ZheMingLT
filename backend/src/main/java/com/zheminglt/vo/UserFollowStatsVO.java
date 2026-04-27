package com.zheminglt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户关注统计")
public class UserFollowStatsVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "关注数量")
    private Long followingCount;

    @Schema(description = "粉丝数量")
    private Long followerCount;

    @Schema(description = "是否已关注该用户")
    private Boolean isFollowing;
}
