package com.zheminglt.mapper;

import com.zheminglt.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostMapper extends JpaRepository<Post, Long> {
    Page<Post> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Post> findByUserId(Long userId, Pageable pageable);
    
    @Query("SELECT p FROM Post p ORDER BY p.viewCount DESC, p.likeCount DESC, p.commentCount DESC")
    Page<Post> findHotPosts(Pageable pageable);
}