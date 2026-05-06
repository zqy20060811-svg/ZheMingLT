package com.zheminglt.impl;

import com.zheminglt.mapper.LikeMapper;
import com.zheminglt.mapper.PostMapper;
import com.zheminglt.model.Like;
import com.zheminglt.model.Post;
import com.zheminglt.model.User;
import com.zheminglt.service.LikeService;
import com.zheminglt.utils.RedisCacheUtil;
import com.zheminglt.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Override
    public ResponseVO<Like> create(Like like) {
        // 先保存到数据库
        Like savedLike = likeMapper.save(like);

        // 更新 Redis 缓存
        if (savedLike.getPost() != null && savedLike.getUser() != null) {
            redisCacheUtil.addLike(savedLike.getPost().getId(), savedLike.getUser().getId());
        }

        return ResponseVO.success(savedLike);
    }

    @Override
    public ResponseVO<Void> delete(Long id) {
        Optional<Like> optionalLike = likeMapper.findById(id);
        if (optionalLike.isEmpty()) {
            return ResponseVO.error(404, "点赞不存在");
        }

        Like like = optionalLike.get();

        // 删除数据库记录
        likeMapper.delete(like);

        // 更新 Redis 缓存
        if (like.getPost() != null && like.getUser() != null) {
            redisCacheUtil.removeLike(like.getPost().getId(), like.getUser().getId());
        }

        return ResponseVO.success(null);
    }

    @Override
    public ResponseVO<Like> findByUserAndPost(Long userId, Long postId) {
        // 先从 Redis 查询
        boolean hasLiked = redisCacheUtil.hasLiked(postId, userId);

        if (hasLiked) {
            // 从数据库获取完整对象
            Optional<Like> optionalLike = likeMapper.findByUserIdAndPostId(userId, postId);
            if (optionalLike.isPresent()) {
                return ResponseVO.success(optionalLike.get());
            }
        }

        // Redis 中没有，查询数据库
        Optional<Like> optionalLike = likeMapper.findByUserIdAndPostId(userId, postId);
        if (optionalLike.isPresent()) {
            Like like = optionalLike.get();
            // 同步到 Redis
            redisCacheUtil.addLike(postId, userId);
            return ResponseVO.success(like);
        }

        return ResponseVO.error(404, "点赞不存在");
    }

    @Override
    public ResponseVO<Like> findByUserAndComment(Long userId, Long commentId) {
        // 评论点赞暂时不缓存，直接查询数据库
        Optional<Like> optionalLike = likeMapper.findByUserIdAndCommentId(userId, commentId);
        if (optionalLike.isPresent()) {
            return ResponseVO.success(optionalLike.get());
        }
        return ResponseVO.error(404, "点赞不存在");
    }

    /**
     * 获取帖子点赞数量（优先从 Redis 获取）
     */
    public Long getLikeCount(Long postId) {
        // 先从 Redis 获取
        Long count = redisCacheUtil.getLikeCount(postId);
        if (count != null) {
            return count;
        }

        // Redis 中没有，从数据库获取
        Optional<Post> optionalPost = postMapper.findById(postId);
        if (optionalPost.isPresent()) {
            return (long) optionalPost.get().getLikeCount();
        }
        return 0L;
    }

    /**
     * 检查用户是否点赞（优先从 Redis 获取）
     */
    public boolean checkUserLiked(Long userId, Long postId) {
        return redisCacheUtil.hasLiked(postId, userId);
    }

    /**
     * 同步 Redis 点赞数据到数据库
     */
    public void syncLikesToDatabase(Long postId) {
        Set<String> userIds = redisCacheUtil.getLikeUsers(postId);
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        // 更新数据库中的点赞数
        Optional<Post> optionalPost = postMapper.findById(postId);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            post.setLikeCount(userIds.size());
            postMapper.save(post);
        }
    }

    /**
     * 从数据库加载点赞数据到 Redis
     */
    public void loadLikesFromDatabase(Long postId) {
        // 查询数据库中该帖子的所有点赞
        Long count = likeMapper.countByPostId(postId);
        if (count != null && count > 0) {
            // 将数量写入 Redis
            redisCacheUtil.addLike(postId, 0L);
            redisCacheUtil.removeLike(postId, 0L);
        }
    }
}
