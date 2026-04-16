package com.zheminglt.service;

import com.zheminglt.dto.CommentDTO;
import com.zheminglt.vo.CommentVO;
import com.zheminglt.vo.ResponseVO;

import org.springframework.data.domain.Page;

public interface CommentService {
    ResponseVO<CommentVO> createComment(Long userId, CommentDTO commentDTO);
    ResponseVO<Page<CommentVO>> getPostComments(Long postId, int page, int size);
    ResponseVO<CommentVO> getCommentById(Long id);
    ResponseVO<CommentVO> updateComment(Long id, Long userId, String content);
    ResponseVO<Void> deleteComment(Long id, Long userId);
}