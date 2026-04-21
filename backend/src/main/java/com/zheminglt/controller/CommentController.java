package com.zheminglt.controller;

import com.zheminglt.dto.CommentDTO;
import com.zheminglt.service.CommentService;
import com.zheminglt.vo.CommentVO;
import com.zheminglt.vo.ResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/comments")
@Tag(name = "评论管理", description = "评论的增删改查接口")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Operation(summary = "发表评论", description = "为帖子发表评论，需要登录")
    @SecurityRequirement(name = "Authorization")
    @PostMapping
    public ResponseVO<CommentVO> createComment(@Parameter(hidden = true) @RequestAttribute("userId") Long userId, 
                                               @RequestBody CommentDTO commentDTO) {
        return commentService.createComment(userId, commentDTO);
    }

    @Operation(summary = "获取帖子评论", description = "分页获取指定帖子的所有评论")
    @GetMapping("/post/{postId}")
    public ResponseVO<Page<CommentVO>> getPostComments(
            @Parameter(description = "帖子ID") @PathVariable Long postId,
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小，默认10") @RequestParam(defaultValue = "10") int size) {
        return commentService.getPostComments(postId, page, size);
    }

    @Operation(summary = "获取评论详情", description = "根据ID获取单个评论的详细信息")
    @GetMapping("/{id}")
    public ResponseVO<CommentVO> getCommentById(@Parameter(description = "评论ID") @PathVariable Long id) {
        return commentService.getCommentById(id);
    }

    @Operation(summary = "更新评论", description = "更新评论内容，需要登录且是评论作者")
    @SecurityRequirement(name = "Authorization")
    @PutMapping("/{id}")
    public ResponseVO<CommentVO> updateComment(
            @Parameter(description = "评论ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @RequestBody String content) {
        return commentService.updateComment(id, userId, content);
    }

    @Operation(summary = "删除评论", description = "删除评论，需要登录且是评论作者或管理员")
    @SecurityRequirement(name = "Authorization")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> deleteComment(@Parameter(description = "评论ID") @PathVariable Long id, 
                                          @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        return commentService.deleteComment(id, userId);
    }
}
