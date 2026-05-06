package com.zheminglt.controller;

import com.zheminglt.service.FollowService;
import com.zheminglt.vo.ResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/follows")
@Tag(name = "关注管理", description = "用户关注相关接口")
public class FollowController {

    @Autowired
    private FollowService followService;

    @Operation(summary = "关注用户", description = "关注指定用户，需要登录")
    @SecurityRequirement(name = "Authorization")
    @PostMapping("/{userId}")
    public ResponseVO<Void> follow(
            @Parameter(description = "要关注的用户ID") @PathVariable Long userId,
            @Parameter(hidden = true) @RequestAttribute("userId") Long currentUserId) {
        return followService.follow(currentUserId, userId);
    }

    @Operation(summary = "取消关注", description = "取消关注指定用户，需要登录")
    @SecurityRequirement(name = "Authorization")
    @DeleteMapping("/{userId}")
    public ResponseVO<Void> unfollow(
            @Parameter(description = "要取消关注的用户ID") @PathVariable Long userId,
            @Parameter(hidden = true) @RequestAttribute("userId") Long currentUserId) {
        return followService.unfollow(currentUserId, userId);
    }

    @Operation(summary = "检查是否已关注", description = "检查当前用户是否已关注指定用户，需要登录")
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/check/{userId}")
    public ResponseVO<Boolean> isFollowing(
            @Parameter(description = "要检查的用户ID") @PathVariable Long userId,
            @Parameter(hidden = true) @RequestAttribute("userId") Long currentUserId) {
        return followService.isFollowing(currentUserId, userId);
    }

    @Operation(summary = "获取关注列表", description = "获取当前用户关注的用户列表，需要登录")
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/following")
    public ResponseVO<?> getFollowingList(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小，默认10") @RequestParam(defaultValue = "10") int size) {
        return followService.getFollowingList(userId, page, size);
    }

    @Operation(summary = "获取粉丝列表", description = "获取关注当前用户的粉丝列表，需要登录")
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/followers")
    public ResponseVO<?> getFollowerList(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小，默认10") @RequestParam(defaultValue = "10") int size) {
        return followService.getFollowerList(userId, page, size);
    }

    @Operation(summary = "获取关注数", description = "获取指定用户的关注数量")
    @GetMapping("/{userId}/following/count")
    public ResponseVO<Long> getFollowingCount(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        return followService.getFollowingCount(userId);
    }

    @Operation(summary = "获取粉丝数", description = "获取指定用户的粉丝数量")
    @GetMapping("/{userId}/followers/count")
    public ResponseVO<Long> getFollowerCount(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        return followService.getFollowerCount(userId);
    }
}
