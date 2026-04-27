package com.zheminglt.impl;

import com.zheminglt.constant.ErrorCodeConstant;
import com.zheminglt.constant.MessageConstant;
import com.zheminglt.mapper.FollowMapper;
import com.zheminglt.mapper.UserMapper;
import com.zheminglt.model.Follow;
import com.zheminglt.model.User;
import com.zheminglt.service.FollowService;
import com.zheminglt.service.NotificationService;
import com.zheminglt.vo.FollowVO;
import com.zheminglt.vo.PageVO;
import com.zheminglt.vo.ResponseVO;
import com.zheminglt.vo.UserFollowStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseVO<Void> followUser(Long followerId, Long followingId) {
        // 不能关注自己
        if (followerId.equals(followingId)) {
            return ResponseVO.error(ErrorCodeConstant.HTTP_BAD_REQUEST, "不能关注自己");
        }

        // 检查被关注用户是否存在
        User following = userMapper.findById(followingId).orElse(null);
        if (following == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.USER_NOT_FOUND);
        }

        // 检查是否已经关注
        if (followMapper.findByFollowerIdAndFollowingId(followerId, followingId).isPresent()) {
            return ResponseVO.error(ErrorCodeConstant.HTTP_BAD_REQUEST, "已经关注该用户");
        }

        // 创建关注关系
        Follow follow = new Follow();
        User follower = userMapper.findById(followerId).orElse(null);
        follow.setFollower(follower);
        follow.setFollowing(following);
        followMapper.save(follow);

        // 发送关注通知
        notificationService.createSystemNotification(
                followingId,
                "新增粉丝",
                follower.getUsername() + " 关注了你"
        );

        return ResponseVO.success(null);
    }

    @Override
    @Transactional
    public ResponseVO<Void> unfollowUser(Long followerId, Long followingId) {
        // 检查关注关系是否存在
        Follow follow = followMapper.findByFollowerIdAndFollowingId(followerId, followingId).orElse(null);
        if (follow == null) {
            return ResponseVO.error(ErrorCodeConstant.HTTP_BAD_REQUEST, "未关注该用户");
        }

        followMapper.delete(follow);
        return ResponseVO.success(null);
    }

    @Override
    public ResponseVO<Boolean> isFollowing(Long followerId, Long followingId) {
        boolean isFollowing = followMapper.findByFollowerIdAndFollowingId(followerId, followingId).isPresent();
        return ResponseVO.success(isFollowing);
    }

    @Override
    public ResponseVO<PageVO<FollowVO>> getFollowingList(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Follow> followPage = followMapper.findByFollowerIdOrderByCreatedAtDesc(userId, pageable);

        List<FollowVO> voList = followPage.getContent().stream()
                .map(follow -> {
                    FollowVO vo = new FollowVO();
                    vo.setId(follow.getId());
                    vo.setFollowerId(follow.getFollower().getId());
                    vo.setFollowerName(follow.getFollower().getUsername());
                    vo.setFollowingId(follow.getFollowing().getId());
                    vo.setFollowingName(follow.getFollowing().getUsername());
                    vo.setCreatedAt(follow.getCreatedAt());
                    // 检查是否互相关注
                    vo.setMutualFollow(followMapper.findByFollowerIdAndFollowingId(
                            follow.getFollowing().getId(), follow.getFollower().getId()).isPresent());
                    return vo;
                })
                .collect(Collectors.toList());

        PageVO<FollowVO> pageVO = new PageVO<>();
        pageVO.setList(voList);
        pageVO.setTotal(followPage.getTotalElements());
        pageVO.setPage(page);
        pageVO.setSize(size);
        pageVO.setPages(followPage.getTotalPages());

        return ResponseVO.success(pageVO);
    }

    @Override
    public ResponseVO<PageVO<FollowVO>> getFollowerList(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Follow> followPage = followMapper.findByFollowingIdOrderByCreatedAtDesc(userId, pageable);

        List<FollowVO> voList = followPage.getContent().stream()
                .map(follow -> {
                    FollowVO vo = new FollowVO();
                    vo.setId(follow.getId());
                    vo.setFollowerId(follow.getFollower().getId());
                    vo.setFollowerName(follow.getFollower().getUsername());
                    vo.setFollowingId(follow.getFollowing().getId());
                    vo.setFollowingName(follow.getFollowing().getUsername());
                    vo.setCreatedAt(follow.getCreatedAt());
                    // 检查是否互相关注
                    vo.setMutualFollow(followMapper.findByFollowerIdAndFollowingId(
                            follow.getFollowing().getId(), follow.getFollower().getId()).isPresent());
                    return vo;
                })
                .collect(Collectors.toList());

        PageVO<FollowVO> pageVO = new PageVO<>();
        pageVO.setList(voList);
        pageVO.setTotal(followPage.getTotalElements());
        pageVO.setPage(page);
        pageVO.setSize(size);
        pageVO.setPages(followPage.getTotalPages());

        return ResponseVO.success(pageVO);
    }

    @Override
    public ResponseVO<UserFollowStatsVO> getFollowStats(Long userId, Long currentUserId) {
        UserFollowStatsVO stats = new UserFollowStatsVO();
        stats.setUserId(userId);
        stats.setFollowingCount(followMapper.countByFollowerId(userId));
        stats.setFollowerCount(followMapper.countByFollowingId(userId));

        // 检查当前用户是否关注了该用户
        if (currentUserId != null && !currentUserId.equals(userId)) {
            stats.setIsFollowing(followMapper.findByFollowerIdAndFollowingId(currentUserId, userId).isPresent());
        } else {
            stats.setIsFollowing(false);
        }

        return ResponseVO.success(stats);
    }
}
