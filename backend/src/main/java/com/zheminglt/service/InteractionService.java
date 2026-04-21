package com.zheminglt.service;

import com.zheminglt.vo.ResponseVO;
import org.springframework.data.domain.Page;

public interface InteractionService {
    ResponseVO<Void> likePost(Long userId, Long postId);
    ResponseVO<Void> unlikePost(Long userId, Long postId);
    ResponseVO<Boolean> isPostLiked(Long userId, Long postId);
    ResponseVO<Void> likeComment(Long userId, Long commentId);
    ResponseVO<Void> unlikeComment(Long userId, Long commentId);
    ResponseVO<Boolean> isCommentLiked(Long userId, Long commentId);
    ResponseVO<Void> collectPost(Long userId, Long postId);
    ResponseVO<Void> uncollectPost(Long userId, Long postId);
    ResponseVO<Boolean> isPostCollected(Long userId, Long postId);
    ResponseVO<Page<?>> getUserCollections(Long userId, int page, int size);
}