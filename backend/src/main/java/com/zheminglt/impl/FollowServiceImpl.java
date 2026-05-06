package com.zheminglt.impl;

import com.zheminglt.constant.ErrorCodeConstant;
import com.zheminglt.constant.MessageConstant;
import com.zheminglt.mapper.FollowMapper;
import com.zheminglt.mapper.UserMapper;
import com.zheminglt.model.Follow;
import com.zheminglt.model.User;
import com.zheminglt.service.FollowService;
import com.zheminglt.service.NotificationService;
import com.zheminglt.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class FollowServiceImpl implements FollowService {

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public ResponseVO<Void> follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            return ResponseVO.error(ErrorCodeConstant.HTTP_BAD_REQUEST, "不能关注自己");
        }

        User follower = userMapper.findById(followerId).orElse(null);
        User following = userMapper.findById(followingId).orElse(null);

        if (follower == null || following == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.USER_NOT_FOUND);
        }

        if (followMapper.existsByFollowerAndFollowing(follower, following)) {
            return ResponseVO.error(ErrorCodeConstant.HTTP_BAD_REQUEST, "已经关注过该用户");
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        followMapper.save(follow);

        // 发送关注通知
        notificationService.createFollowNotification(followerId, followingId);

        return ResponseVO.success(null);
    }

    @Override
    @Transactional
    public ResponseVO<Void> unfollow(Long followerId, Long followingId) {
        User follower = userMapper.findById(followerId).orElse(null);
        User following = userMapper.findById(followingId).orElse(null);

        if (follower == null || following == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.USER_NOT_FOUND);
        }

        Optional<Follow> follow = followMapper.findByFollowerAndFollowing(follower, following);
        if (follow.isPresent()) {
            followMapper.delete(follow.get());
        }

        return ResponseVO.success(null);
    }

    @Override
    public ResponseVO<Boolean> isFollowing(Long followerId, Long followingId) {
        User follower = userMapper.findById(followerId).orElse(null);
        User following = userMapper.findById(followingId).orElse(null);

        if (follower == null || following == null) {
            return ResponseVO.success(false);
        }

        return ResponseVO.success(followMapper.existsByFollowerAndFollowing(follower, following));
    }

    @Override
    public ResponseVO<Page<?>> getFollowingList(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<User> followingPage = followMapper.findFollowingByFollowerId(userId, pageable);
        return ResponseVO.success(followingPage);
    }

    @Override
    public ResponseVO<Page<?>> getFollowerList(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<User> followerPage = followMapper.findFollowersByFollowingId(userId, pageable);
        return ResponseVO.success(followerPage);
    }

    @Override
    public ResponseVO<Long> getFollowingCount(Long userId) {
        return ResponseVO.success(followMapper.countByFollowerId(userId));
    }

    @Override
    public ResponseVO<Long> getFollowerCount(Long userId) {
        return ResponseVO.success(followMapper.countByFollowingId(userId));
    }
}
