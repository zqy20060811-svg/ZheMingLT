package com.zheminglt.mapper;

import com.zheminglt.model.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeMapper extends JpaRepository<Like, Long> {
    Optional<Like> findByUserIdAndPostId(Long userId, Long postId);
    Optional<Like> findByUserIdAndCommentId(Long userId, Long commentId);
    void deleteByUserIdAndPostId(Long userId, Long postId);
    void deleteByUserIdAndCommentId(Long userId, Long commentId);
    long countByPostId(Long postId);
    long countByCommentId(Long commentId);

    // 查询用户的点赞列表（按时间倒序）
    Page<Like> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}