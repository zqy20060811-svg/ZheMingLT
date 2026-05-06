package com.zheminglt.mapper;

import com.zheminglt.model.Follow;
import com.zheminglt.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowMapper extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    boolean existsByFollowerAndFollowing(User follower, User following);

    @Query("SELECT f.following FROM Follow f WHERE f.follower.id = :followerId")
    Page<User> findFollowingByFollowerId(@Param("followerId") Long followerId, Pageable pageable);

    @Query("SELECT f.follower FROM Follow f WHERE f.following.id = :followingId")
    Page<User> findFollowersByFollowingId(@Param("followingId") Long followingId, Pageable pageable);

    long countByFollowerId(Long followerId);

    long countByFollowingId(Long followingId);

    void deleteByFollowerAndFollowing(User follower, User following);
}
