package com.zheminglt.vo;

import lombok.Data;
import java.util.Date;

@Data
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
    private Date createdAt;
    private Date updatedAt;
    private Boolean isLiked;
    private Boolean isCollected;
}