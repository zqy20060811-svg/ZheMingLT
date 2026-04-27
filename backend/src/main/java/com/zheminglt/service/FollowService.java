package com.zheminglt.service;

import com.zheminglt.vo.FollowVO;
import com.zheminglt.vo.PageVO;
import com.zheminglt.vo.ResponseVO;
import com.zheminglt.vo.UserFollowStatsVO;

public interface FollowService {

    // 关注用户
    ResponseVO<Void> followUser(Long followerId, Long followingId);

    // 取消关注
    ResponseVO<Void> unfollowUser(Long followerId, Long followingId);

    // 检查是否已关注
    ResponseVO<Boolean> isFollowing(Long followerId, Long followingId);

    // 获取用户的关注列表
    ResponseVO<PageVO<FollowVO>> getFollowingList(Long userId, int page, int size);

    // 获取用户的粉丝列表
    ResponseVO<PageVO<FollowVO>> getFollowerList(Long userId, int page, int size);

    // 获取用户关注统计
    ResponseVO<UserFollowStatsVO> getFollowStats(Long userId, Long currentUserId);
}
