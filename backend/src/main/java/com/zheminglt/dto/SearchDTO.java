package com.zheminglt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "搜索请求DTO")
public class SearchDTO {

    @Schema(description = "搜索关键词")
    private String keyword;

    @Schema(description = "分类ID（可选）")
    private Long categoryId;

    @Schema(description = "排序方式：time-时间，hot-热度", example = "time")
    private String sortBy = "time";

    @Schema(description = "页码", example = "1")
    private int page = 1;

    @Schema(description = "每页数量", example = "10")
    private int size = 10;
}
