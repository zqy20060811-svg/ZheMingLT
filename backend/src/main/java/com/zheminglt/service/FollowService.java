package com.zheminglt.service;

import com.zheminglt.vo.ResponseVO;
import org.springframework.data.domain.Page;

public interface FollowService {
    ResponseVO<Void> follow(Long followerId, Long followingId);
    ResponseVO<Void> unfollow(Long followerId, Long followingId);
    ResponseVO<Boolean> isFollowing(Long followerId, Long followingId);
    ResponseVO<Page<?>> getFollowingList(Long userId, int page, int size);
    ResponseVO<Page<?>> getFollowerList(Long userId, int page, int size);
    ResponseVO<Long> getFollowingCount(Long userId);
    ResponseVO<Long> getFollowerCount(Long userId);
}
