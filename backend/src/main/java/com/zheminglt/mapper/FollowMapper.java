package com.zheminglt.mapper;

import com.zheminglt.model.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowMapper extends JpaRepository<Follow, Long> {

    // 检查是否已关注
    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    // 获取用户的关注列表
    Page<Follow> findByFollowerIdOrderByCreatedAtDesc(Long followerId, Pageable pageable);

    // 获取用户的粉丝列表
    Page<Follow> findByFollowingIdOrderByCreatedAtDesc(Long followingId, Pageable pageable);

    // 获取关注数量
    long countByFollowerId(Long followerId);

    // 获取粉丝数量
    long countByFollowingId(Long followingId);

    // 检查是否互相关注
    @Query("SELECT COUNT(f) > 0 FROM Follow f WHERE f.follower.id = :userId1 AND f.following.id = :userId2")
    boolean existsByFollowerIdAndFollowingId(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // 删除关注关系
    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);
}
