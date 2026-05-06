package com.zheminglt.impl;

import com.zheminglt.mapper.CollectionMapper;
import com.zheminglt.mapper.PostMapper;
import com.zheminglt.model.Collection;
import com.zheminglt.model.Post;
import com.zheminglt.service.CollectionService;
import com.zheminglt.utils.RedisCacheUtil;
import com.zheminglt.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class CollectionServiceImpl implements CollectionService {

    @Autowired
    private CollectionMapper collectionMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Override
    public ResponseVO<Collection> create(Collection collection) {
        // 先保存到数据库
        Collection savedCollection = collectionMapper.save(collection);

        // 更新 Redis 缓存
        if (savedCollection.getPost() != null && savedCollection.getUser() != null) {
            redisCacheUtil.addCollect(savedCollection.getPost().getId(), savedCollection.getUser().getId());
        }

        return ResponseVO.success(savedCollection);
    }

    @Override
    public ResponseVO<Void> delete(Long id) {
        Optional<Collection> optionalCollection = collectionMapper.findById(id);
        if (optionalCollection.isEmpty()) {
            return ResponseVO.error(404, "收藏不存在");
        }

        Collection collection = optionalCollection.get();

        // 删除数据库记录
        collectionMapper.delete(collection);

        // 更新 Redis 缓存
        if (collection.getPost() != null && collection.getUser() != null) {
            redisCacheUtil.removeCollect(collection.getPost().getId(), collection.getUser().getId());
        }

        return ResponseVO.success(null);
    }

    @Override
    public ResponseVO<List<Collection>> findByUserId(Long userId) {
        Page<Collection> collections = collectionMapper.findByUserId(userId, PageRequest.of(0, 100));
        return ResponseVO.success(collections.getContent());
    }

    @Override
    public ResponseVO<Collection> findByUserAndPost(Long userId, Long postId) {
        // 先从 Redis 查询
        boolean hasCollected = redisCacheUtil.hasCollected(postId, userId);

        if (hasCollected) {
            // 从数据库获取完整对象
            Optional<Collection> optionalCollection = collectionMapper.findByUserIdAndPostId(userId, postId);
            if (optionalCollection.isPresent()) {
                return ResponseVO.success(optionalCollection.get());
            }
        }

        // Redis 中没有，查询数据库
        Optional<Collection> optionalCollection = collectionMapper.findByUserIdAndPostId(userId, postId);
        if (optionalCollection.isPresent()) {
            Collection collection = optionalCollection.get();
            // 同步到 Redis
            redisCacheUtil.addCollect(postId, userId);
            return ResponseVO.success(collection);
        }

        return ResponseVO.error(404, "收藏不存在");
    }

    /**
     * 获取帖子收藏数量（优先从 Redis 获取）
     */
    public Long getCollectCount(Long postId) {
        // 先从 Redis 获取
        Long count = redisCacheUtil.getCollectCount(postId);
        if (count != null) {
            return count;
        }

        // Redis 中没有，从数据库获取
        Optional<Post> optionalPost = postMapper.findById(postId);
        if (optionalPost.isPresent()) {
            return (long) optionalPost.get().getCollectCount();
        }
        return 0L;
    }

    /**
     * 检查用户是否收藏（优先从 Redis 获取）
     */
    public boolean checkUserCollected(Long userId, Long postId) {
        return redisCacheUtil.hasCollected(postId, userId);
    }

    /**
     * 同步 Redis 收藏数据到数据库
     */
    public void syncCollectsToDatabase(Long postId) {
        Set<String> userIds = redisCacheUtil.getCollectUsers(postId);
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        // 更新数据库中的收藏数
        Optional<Post> optionalPost = postMapper.findById(postId);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            post.setCollectCount(userIds.size());
            postMapper.save(post);
        }
    }
}
