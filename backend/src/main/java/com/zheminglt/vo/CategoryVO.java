package com.zheminglt.vo;

import lombok.Data;

@Data
public class CategoryVO {
    private Long id;
    private String name;
    private String description;
    private Integer sort;
}