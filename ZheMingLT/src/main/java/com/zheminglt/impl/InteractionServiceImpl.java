package com.zheminglt.impl;

import com.zheminglt.service.InteractionService;
import com.zheminglt.vo.ResponseVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class InteractionServiceImpl implements InteractionService {

    @Override
    public ResponseVO<Void> likePost(Long userId, Long postId) {
        // 暂时返回成功，实际实现需要调用LikeService
        return ResponseVO.success(null);
    }

    @Override
    public ResponseVO<Void> unlikePost(Long userId, Long postId) {
        // 暂时返回成功，实际实现需要调用LikeService
        return ResponseVO.success(null);
    }

    @Override
    public ResponseVO<Boolean> isPostLiked(Long userId, Long postId) {
        // 暂时返回false，实际实现需要调用LikeService
        return ResponseVO.success(false);
    }

    @Override
    public ResponseVO<Void> likeComment(Long userId, Long commentId) {
        // 暂时返回成功，实际实现需要调用LikeService
        return ResponseVO.success(null);
    }

    @Override
    public ResponseVO<Void> unlikeComment(Long userId, Long commentId) {
        // 暂时返回成功，实际实现需要调用LikeService
        return ResponseVO.success(null);
    }

    @Override
    public ResponseVO<Boolean> isCommentLiked(Long userId, Long commentId) {
        // 暂时返回false，实际实现需要调用LikeService
        return ResponseVO.success(false);
    }

    @Override
    public ResponseVO<Void> collectPost(Long userId, Long postId) {
        // 暂时返回成功，实际实现需要调用CollectionService
        return ResponseVO.success(null);
    }

    @Override
    public ResponseVO<Void> uncollectPost(Long userId, Long postId) {
        // 暂时返回成功，实际实现需要调用CollectionService
        return ResponseVO.success(null);
    }

    @Override
    public ResponseVO<Boolean> isPostCollected(Long userId, Long postId) {
        // 暂时返回false，实际实现需要调用CollectionService
        return ResponseVO.success(false);
    }

    @Override
    public ResponseVO<Page<?>> getUserCollections(Long userId, int page, int size) {
        // 暂时返回空页，实际实现需要调用CollectionService
        Page<?> emptyPage = new PageImpl<>(Collections.emptyList());
        return ResponseVO.success(emptyPage);
    }
}