package com.zheminglt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "分页数据对象")
public class PageVO<T> {

    @Schema(description = "数据列表")
    private List<T> list;

    @Schema(description = "总记录数")
    private long total;

    @Schema(description = "当前页码")
    private int page;

    @Schema(description = "每页大小")
    private int size;

    @Schema(description = "总页数")
    private int pages;
}
