package com.zheminglt.vo;

import lombok.Data;
import java.util.Date;

@Data
public class CommentVO {
    private Long id;
    private Long postId;
    private UserVO user;
    private Long parentId;
    private String content;
    private Integer status;
    private Date createdAt;
    private String authorName;  // 作者名称
}
