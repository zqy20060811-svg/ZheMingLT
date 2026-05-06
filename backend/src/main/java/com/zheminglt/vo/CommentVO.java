package com.zheminglt.vo;

import lombok.Data;
import java.util.Date;

@Data
public class CommentVO {
    private Long id;
    private Long postId;
    private Long parentId;
    private String content;
    private Integer status;
    private Date createdAt;
    
    // 作者信息
    private Long authorId;
    private String authorName;
    private String authorAvatar;
}
