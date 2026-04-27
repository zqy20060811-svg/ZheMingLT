package com.zheminglt.controller;

import com.zheminglt.service.FollowService;
import com.zheminglt.vo.FollowVO;
import com.zheminglt.vo.PageVO;
import com.zheminglt.vo.ResponseVO;
import com.zheminglt.vo.UserFollowStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/follows")
@Tag(name = "用户关注", description = "用户关注、粉丝相关接口")
public class FollowController {

    @Autowired
    private FollowService followService;

    @Operation(summary = "关注用户", description = "关注指定用户，需要登录")
    @SecurityRequirement(name = "Authorization")
    @PostMapping("/{userId}")
    public ResponseVO<Void> followUser(
            @Parameter(hidden = true) @RequestAttribute("userId") Long currentUserId,
            @Parameter(description = "要关注的用户ID") @PathVariable Long userId) {
        return followService.followUser(currentUserId, userId);
    }

    @Operation(summary = "取消关注", description = "取消关注指定用户，需要登录")
    @SecurityRequirement(name = "Authorization")
    @DeleteMapping("/{userId}")
    public ResponseVO<Void> unfollowUser(
            @Parameter(hidden = true) @RequestAttribute("userId") Long currentUserId,
            @Parameter(description = "要取消关注的用户ID") @PathVariable Long userId) {
        return followService.unfollowUser(currentUserId, userId);
    }

    @Operation(summary = "检查是否已关注", description = "检查当前用户是否已关注指定用户，需要登录")
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/{userId}/check")
    public ResponseVO<Boolean> isFollowing(
            @Parameter(hidden = true) @RequestAttribute("userId") Long currentUserId,
            @Parameter(description = "要检查的用户ID") @PathVariable Long userId) {
        return followService.isFollowing(currentUserId, userId);
    }

    @Operation(summary = "获取关注列表", description = "获取指定用户的关注列表")
    @GetMapping("/{userId}/following")
    public ResponseVO<PageVO<FollowVO>> getFollowingList(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        return followService.getFollowingList(userId, page, size);
    }

    @Operation(summary = "获取粉丝列表", description = "获取指定用户的粉丝列表")
    @GetMapping("/{userId}/followers")
    public ResponseVO<PageVO<FollowVO>> getFollowerList(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        return followService.getFollowerList(userId, page, size);
    }

    @Operation(summary = "获取关注统计", description = "获取用户的关注数和粉丝数")
    @GetMapping("/{userId}/stats")
    public ResponseVO<UserFollowStatsVO> getFollowStats(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long currentUserId) {
        return followService.getFollowStats(userId, currentUserId);
    }
}
