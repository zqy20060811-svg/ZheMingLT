package com.zheminglt.mapper;

import com.zheminglt.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PostMapper extends JpaRepository<Post, Long> {
    Page<Post> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Post> findByUserId(Long userId, Pageable pageable);
    long countByUserId(Long userId);

    // 基础热度排序（总榜）
    @Query("SELECT p FROM Post p ORDER BY (p.viewCount * 1 + p.likeCount * 3 + p.commentCount * 5) DESC")
    Page<Post> findHotPosts(Pageable pageable);

    // 今日热门
    @Query("SELECT p FROM Post p WHERE p.createdAt >= :startTime ORDER BY (p.viewCount * 1 + p.likeCount * 3 + p.commentCount * 5) DESC")
    Page<Post> findHotPostsToday(@Param("startTime") LocalDateTime startTime, Pageable pageable);

    // 本周热门
    @Query("SELECT p FROM Post p WHERE p.createdAt >= :startTime ORDER BY (p.viewCount * 1 + p.likeCount * 3 + p.commentCount * 5) DESC")
    Page<Post> findHotPostsWeek(@Param("startTime") LocalDateTime startTime, Pageable pageable);

    // 本月热门
    @Query("SELECT p FROM Post p WHERE p.createdAt >= :startTime ORDER BY (p.viewCount * 1 + p.likeCount * 3 + p.commentCount * 5) DESC")
    Page<Post> findHotPostsMonth(@Param("startTime") LocalDateTime startTime, Pageable pageable);
}