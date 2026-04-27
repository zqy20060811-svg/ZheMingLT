package com.zheminglt.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 接收消息的用户
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 消息类型：COMMENT-评论回复，LIKE-点赞，SYSTEM-系统通知
    @Column(name = "type", nullable = false, length = 20)
    private String type;

    // 消息标题
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    // 消息内容
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // 关联的帖子ID（可选）
    @Column(name = "post_id")
    private Long postId;

    // 关联的评论ID（可选）
    @Column(name = "comment_id")
    private Long commentId;

    // 触发消息的用户ID（可选，如点赞用户、评论用户）
    @Column(name = "from_user_id")
    private Long fromUserId;

    // 是否已读
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    // 创建时间
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
