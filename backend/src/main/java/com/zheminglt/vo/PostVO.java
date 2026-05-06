package com.zheminglt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "帖子信息VO")
public class PostVO {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private UserVO user;
    private CategoryVO category;
    private Integer status;
    private String reason;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer collectCount;
    private Date createdAt;
    private Date updatedAt;
    private Boolean isLiked;
    private Boolean isCollected;

    @Schema(description = "热度分数")
    private Integer heat;

    @Schema(description = "作者ID")
    private Long userId;

    @Schema(description = "作者名称")
    private String authorName;

    @Schema(description = "作者头像")
    private String authorAvatar;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "是否已关注作者")
    private Boolean isFollowing;

    @Schema(description = "标签列表")
    private List<TagVO> tags;
}