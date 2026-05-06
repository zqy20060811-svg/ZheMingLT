package com.zheminglt.controller;

import com.zheminglt.constant.BusinessConstant;
import com.zheminglt.constant.ErrorCodeConstant;
import com.zheminglt.constant.MessageConstant;
import com.zheminglt.service.InteractionService;
import com.zheminglt.utils.JWTUtil;
import com.zheminglt.vo.ResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/interactions")
@Tag(name = "互动管理", description = "点赞、收藏等互动功能接口")
public class InteractionController {

    @Autowired
    private InteractionService interactionService;

    // 从请求中获取userId的辅助方法
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader(BusinessConstant.TOKEN_HEADER);
        if (authorization == null || !authorization.startsWith(BusinessConstant.TOKEN_PREFIX)) {
            return null;
        }
        String accessToken = authorization.substring(BusinessConstant.TOKEN_PREFIX.length());
        if (!JWTUtil.validateAccessToken(accessToken)) {
            return null;
        }
        return JWTUtil.getUserIdFromAccessToken(accessToken);
    }

    @Operation(summary = "点赞帖子", description = "为指定帖子点赞，需要登录")
    @SecurityRequirement(name = "Authorization")
    @PostMapping("/like/post/{postId}")
    public ResponseVO<Void> likePost(@Parameter(description = "帖子ID") @PathVariable Long postId, 
                                     HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_EMPTY);
        }
        return interactionService.likePost(userId, postId);
    }

    @Operation(summary = "取消点赞帖子", description = "取消对指定帖子的点赞，需要登录")
    @SecurityRequirement(name = "Authorization")
    @DeleteMapping("/like/post/{postId}")
    public ResponseVO<Void> unlikePost(@Parameter(description = "帖子ID") @PathVariable Long postId, 
                                       HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_EMPTY);
        }
        return interactionService.unlikePost(userId, postId);
    }

    @Operation(summary = "检查是否已点赞帖子", description = "检查当前用户是否已点赞指定帖子，需要登录")
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/like/post/{postId}")
    public ResponseVO<Boolean> isPostLiked(@Parameter(description = "帖子ID") @PathVariable Long postId, 
                                           HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_EMPTY);
        }
        return interactionService.isPostLiked(userId, postId);
    }

    @Operation(summary = "点赞评论", description = "为指定评论点赞，需要登录")
    @SecurityRequirement(name = "Authorization")
    @PostMapping("/like/comment/{commentId}")
    public ResponseVO<Void> likeComment(@Parameter(description = "评论ID") @PathVariable Long commentId, 
                                        HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_EMPTY);
        }
        return interactionService.likeComment(userId, commentId);
    }

    @Operation(summary = "取消点赞评论", description = "取消对指定评论的点赞，需要登录")
    @SecurityRequirement(name = "Authorization")
    @DeleteMapping("/like/comment/{commentId}")
    public ResponseVO<Void> unlikeComment(@Parameter(description = "评论ID") @PathVariable Long commentId, 
                                          HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_EMPTY);
        }
        return interactionService.unlikeComment(userId, commentId);
    }

    @Operation(summary = "检查是否已点赞评论", description = "检查当前用户是否已点赞指定评论，需要登录")
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/like/comment/{commentId}")
    public ResponseVO<Boolean> isCommentLiked(@Parameter(description = "评论ID") @PathVariable Long commentId, 
                                              HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_EMPTY);
        }
        return interactionService.isCommentLiked(userId, commentId);
    }

    @Operation(summary = "收藏帖子", description = "收藏指定帖子，需要登录")
    @SecurityRequirement(name = "Authorization")
    @PostMapping("/collect/{postId}")
    public ResponseVO<Void> collectPost(@Parameter(description = "帖子ID") @PathVariable Long postId, 
                                        HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_EMPTY);
        }
        return interactionService.collectPost(userId, postId);
    }

    @Operation(summary = "取消收藏帖子", description = "取消对指定帖子的收藏，需要登录")
    @SecurityRequirement(name = "Authorization")
    @DeleteMapping("/collect/{postId}")
    public ResponseVO<Void> uncollectPost(@Parameter(description = "帖子ID") @PathVariable Long postId, 
                                          HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_EMPTY);
        }
        return interactionService.uncollectPost(userId, postId);
    }

    @Operation(summary = "检查是否已收藏帖子", description = "检查当前用户是否已收藏指定帖子，需要登录")
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/collect/{postId}")
    public ResponseVO<Boolean> isPostCollected(@Parameter(description = "帖子ID") @PathVariable Long postId, 
                                               HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_EMPTY);
        }
        return interactionService.isPostCollected(userId, postId);
    }

    @Operation(summary = "获取用户收藏列表", description = "分页获取当前用户的所有收藏帖子，需要登录")
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/collects")
    public ResponseVO<?> getUserCollections(
            HttpServletRequest request,
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小，默认10") @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_EMPTY);
        }
        return interactionService.getUserCollections(userId, page, size);
    }
}
