package com.zheminglt.impl;

import com.zheminglt.mapper.CollectionMapper;
import com.zheminglt.model.Collection;
import com.zheminglt.service.CollectionService;
import com.zheminglt.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CollectionServiceImpl implements CollectionService {

    @Autowired
    private CollectionMapper collectionMapper;

    @Override
    public ResponseVO<Collection> create(Collection collection) {
        Collection savedCollection = collectionMapper.save(collection);
        return ResponseVO.success(savedCollection);
    }

    @Override
    public ResponseVO<Void> delete(Long id) {
        Collection collection = collectionMapper.findById(id).orElse(null);
        if (collection == null) {
            return ResponseVO.error(404, "收藏不存在");
        }
        collectionMapper.delete(collection);
        return ResponseVO.success(null);
    }

    @Override
    public ResponseVO<List<Collection>> findByUserId(Long userId) {
        Page<Collection> collections = collectionMapper.findByUserId(userId, PageRequest.of(0, 100));
        return ResponseVO.success(collections.getContent());
    }

    @Override
    public ResponseVO<Collection> findByUserAndPost(Long userId, Long postId) {
        Collection collection = collectionMapper.findByUserIdAndPostId(userId, postId).orElse(null);
        if (collection == null) {
            return ResponseVO.error(404, "收藏不存在");
        }
        return ResponseVO.success(collection);
    }
}