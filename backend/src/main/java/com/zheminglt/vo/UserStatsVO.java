package com.zheminglt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户统计信息VO")
public class UserStatsVO {

    @Schema(description = "发帖数")
    private Long posts;

    @Schema(description = "获赞数")
    private Long likes;

    @Schema(description = "关注数")
    private Long following;

    @Schema(description = "粉丝数")
    private Long followers;
}
