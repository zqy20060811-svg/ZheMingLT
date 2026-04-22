package com.zheminglt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "发布帖子请求对象")
public class PostDTO {

    @Schema(description = "帖子标题", required = true)
    private String title;

    @Schema(description = "帖子内容", required = true)
    private String content;

    @Schema(description = "帖子摘要")
    private String summary;

    @Schema(description = "分类ID", required = true)
    private Long categoryId;
}
