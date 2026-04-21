package com.zheminglt.service;

import com.zheminglt.model.Collection;
import com.zheminglt.vo.ResponseVO;

import java.util.List;

public interface CollectionService {
    ResponseVO<Collection> create(Collection collection);
    ResponseVO<Void> delete(Long id);
    ResponseVO<List<Collection>> findByUserId(Long userId);
    ResponseVO<Collection> findByUserAndPost(Long userId, Long postId);
}