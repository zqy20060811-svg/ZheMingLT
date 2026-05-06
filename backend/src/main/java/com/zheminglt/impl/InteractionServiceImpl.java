package com.zheminglt.impl;

import com.zheminglt.constant.ErrorCodeConstant;
import com.zheminglt.constant.MessageConstant;
import com.zheminglt.mapper.*;
import com.zheminglt.model.*;
import com.zheminglt.service.InteractionService;
import com.zheminglt.service.NotificationService;
import com.zheminglt.utils.RedisCacheUtil;
import com.zheminglt.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InteractionServiceImpl implements InteractionService {

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private CollectionMapper collectionMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Override
    @Transactional
    public ResponseVO<Void> likePost(Long userId, Long postId) {
        // 检查帖子是否存在
        Post post = postMapper.findById(postId).orElse(null);
        if (post == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.POST_NOT_FOUND);
        }

        User user = userMapper.findById(userId).orElse(null);
        if (user == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.USER_NOT_FOUND);
        }

        // 先检查 Redis 缓存
        if (redisCacheUtil.hasLiked(postId, userId)) {
            return ResponseVO.error(ErrorCodeConstant.HTTP_BAD_REQUEST, MessageConstant.ALREADY_LIKED);
        }

        // 检查数据库
        Optional<Like> existingLike = likeMapper.findByUserIdAndPostId(userId, postId);
        if (existingLike.isPresent()) {
            // 同步到 Redis
            redisCacheUtil.addLike(postId, userId);
            return ResponseVO.error(ErrorCodeConstant.HTTP_BAD_REQUEST, MessageConstant.ALREADY_LIKED);
        }

        // 创建点赞记录
        Like like = new Like();
        like.setUser(user);
        like.setPost(post);
        likeMapper.save(like);

        // 更新 Redis 缓存
        redisCacheUtil.addLike(postId, userId);

        // 更新帖子点赞数
        post.setLikeCount(post.getLikeCount() + 1);
        postMapper.save(post);

        // 发送通知给帖子作者
        if (post.getUser() != null && !post.getUser().getId().equals(userId)) {
            notificationService.createLikeNotification(postId, userId, post.getUser().getId());
        }

        return ResponseVO.success(null);
    }

    @Override
    @Transactional
    public ResponseVO<Void> unlikePost(Long userId, Long postId) {
        // 检查帖子是否存在
        Post post = postMapper.findById(postId).orElse(null);
        if (post == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.POST_NOT_FOUND);
        }

        // 先检查 Redis 缓存
        boolean hasLiked = redisCacheUtil.hasLiked(postId, userId);

        // 查找点赞记录
        Optional<Like> existingLike = likeMapper.findByUserIdAndPostId(userId, postId);
        if (!existingLike.isPresent() && !hasLiked) {
            return ResponseVO.error(ErrorCodeConstant.HTTP_BAD_REQUEST, MessageConstant.NOT_LIKED);
        }

        // 删除数据库记录
        if (existingLike.isPresent()) {
            likeMapper.delete(existingLike.get());
        }

        // 更新 Redis 缓存
        redisCacheUtil.removeLike(postId, userId);

        // 更新帖子点赞数
        if (post.getLikeCount() > 0) {
            post.setLikeCount(post.getLikeCount() - 1);
            postMapper.save(post);
        }

        return ResponseVO.success(null);
    }

    @Override
    public ResponseVO<Boolean> isPostLiked(Long userId, Long postId) {
        // 先检查 Redis
        if (redisCacheUtil.hasLiked(postId, userId)) {
            return ResponseVO.success(true);
        }

        // 再检查数据库
        Optional<Like> like = likeMapper.findByUserIdAndPostId(userId, postId);
        if (like.isPresent()) {
            // 同步到 Redis
            redisCacheUtil.addLike(postId, userId);
        }
        return ResponseVO.success(like.isPresent());
    }

    @Override
    @Transactional
    public ResponseVO<Void> likeComment(Long userId, Long commentId) {
        // 检查评论是否存在
        Comment comment = commentMapper.findById(commentId).orElse(null);
        if (comment == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.COMMENT_NOT_FOUND);
        }

        User user = userMapper.findById(userId).orElse(null);
        if (user == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.USER_NOT_FOUND);
        }

        // 检查是否已经点赞
        Optional<Like> existingLike = likeMapper.findByUserIdAndCommentId(userId, commentId);
        if (existingLike.isPresent()) {
            return ResponseVO.error(ErrorCodeConstant.HTTP_BAD_REQUEST, MessageConstant.ALREADY_LIKED);
        }

        // 创建点赞记录
        Like like = new Like();
        like.setUser(user);
        like.setComment(comment);
        likeMapper.save(like);

        return ResponseVO.success(null);
    }

    @Override
    @Transactional
    public ResponseVO<Void> unlikeComment(Long userId, Long commentId) {
        // 查找点赞记录
        Optional<Like> existingLike = likeMapper.findByUserIdAndCommentId(userId, commentId);
        if (!existingLike.isPresent()) {
            return ResponseVO.error(ErrorCodeConstant.HTTP_BAD_REQUEST, MessageConstant.NOT_LIKED);
        }

        // 删除点赞记录
        likeMapper.delete(existingLike.get());

        return ResponseVO.success(null);
    }

    @Override
    public ResponseVO<Boolean> isCommentLiked(Long userId, Long commentId) {
        Optional<Like> like = likeMapper.findByUserIdAndCommentId(userId, commentId);
        return ResponseVO.success(like.isPresent());
    }

    @Override
    @Transactional
    public ResponseVO<Void> collectPost(Long userId, Long postId) {
        // 检查帖子是否存在
        Post post = postMapper.findById(postId).orElse(null);
        if (post == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.POST_NOT_FOUND);
        }

        User user = userMapper.findById(userId).orElse(null);
        if (user == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.USER_NOT_FOUND);
        }

        // 先检查 Redis 缓存
        if (redisCacheUtil.hasCollected(postId, userId)) {
            return ResponseVO.error(ErrorCodeConstant.HTTP_BAD_REQUEST, MessageConstant.ALREADY_COLLECTED);
        }

        // 检查数据库
        Optional<Collection> existingCollection = collectionMapper.findByUserIdAndPostId(userId, postId);
        if (existingCollection.isPresent()) {
            // 同步到 Redis
            redisCacheUtil.addCollect(postId, userId);
            return ResponseVO.error(ErrorCodeConstant.HTTP_BAD_REQUEST, MessageConstant.ALREADY_COLLECTED);
        }

        // 创建收藏记录
        Collection collection = new Collection();
        collection.setUser(user);
        collection.setPost(post);
        collectionMapper.save(collection);

        // 更新 Redis 缓存
        redisCacheUtil.addCollect(postId, userId);

        // 更新帖子收藏数量
        post.setCollectCount((post.getCollectCount() != null ? post.getCollectCount() : 0) + 1);
        postMapper.save(post);

        return ResponseVO.success(null);
    }

    @Override
    @Transactional
    public ResponseVO<Void> uncollectPost(Long userId, Long postId) {
        // 先检查 Redis 缓存
        boolean hasCollected = redisCacheUtil.hasCollected(postId, userId);

        // 查找收藏记录
        Optional<Collection> existingCollection = collectionMapper.findByUserIdAndPostId(userId, postId);
        if (!existingCollection.isPresent() && !hasCollected) {
            return ResponseVO.error(ErrorCodeConstant.HTTP_BAD_REQUEST, MessageConstant.NOT_COLLECTED);
        }

        // 删除数据库记录
        if (existingCollection.isPresent()) {
            collectionMapper.delete(existingCollection.get());
        }

        // 更新 Redis 缓存
        redisCacheUtil.removeCollect(postId, userId);

        // 更新帖子收藏数量
        Post post = postMapper.findById(postId).orElse(null);
        if (post != null && post.getCollectCount() != null && post.getCollectCount() > 0) {
            post.setCollectCount(post.getCollectCount() - 1);
            postMapper.save(post);
        }

        return ResponseVO.success(null);
    }

    @Override
    public ResponseVO<Boolean> isPostCollected(Long userId, Long postId) {
        // 先检查 Redis
        if (redisCacheUtil.hasCollected(postId, userId)) {
            return ResponseVO.success(true);
        }

        // 再检查数据库
        Optional<Collection> collection = collectionMapper.findByUserIdAndPostId(userId, postId);
        if (collection.isPresent()) {
            // 同步到 Redis
            redisCacheUtil.addCollect(postId, userId);
        }
        return ResponseVO.success(collection.isPresent());
    }

    @Override
    public ResponseVO<Page<?>> getUserCollections(Long userId, int page, int size) {
        // 暂时返回空页，实际实现需要调用CollectionService
        Page<?> emptyPage = new PageImpl<>(Collections.emptyList());
        return ResponseVO.success(emptyPage);
    }
}
