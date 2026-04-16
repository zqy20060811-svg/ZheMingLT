package com.zheminglt.model;

import lombok.Data;
import jakarta.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "summary", length = 500)
    private String summary;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "view_count", nullable = false, columnDefinition = "int default 0")
    private Integer viewCount;

    @Column(name = "like_count", nullable = false, columnDefinition = "int default 0")
    private Integer likeCount;

    @Column(name = "comment_count", nullable = false, columnDefinition = "int default 0")
    private Integer commentCount;

    @Column(name = "status", nullable = false, columnDefinition = "int default 0")
    private Integer status;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    private Date createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}