package com.zheminglt.dto;

import lombok.Data;

@Data
public class CommentDTO {
    private Long postId;
    private Long parentId;
    private String content;
}