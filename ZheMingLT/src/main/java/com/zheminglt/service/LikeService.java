package com.zheminglt.service;

import com.zheminglt.model.Like;
import com.zheminglt.vo.ResponseVO;

import java.util.List;

public interface LikeService {
    ResponseVO<Like> create(Like like);
    ResponseVO<Void> delete(Long id);
    ResponseVO<Like> findByUserAndPost(Long userId, Long postId);
    ResponseVO<Like> findByUserAndComment(Long userId, Long commentId);
}