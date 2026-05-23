package com.zheminglt.impl;

import com.zheminglt.constant.BusinessConstant;
import com.zheminglt.constant.ErrorCodeConstant;
import com.zheminglt.constant.MessageConstant;
import com.zheminglt.constant.RedisKeyConstant;
import com.zheminglt.mapper.PostMapper;
import com.zheminglt.model.Category;
import com.zheminglt.model.Post;
import com.zheminglt.model.User;
import com.zheminglt.service.PostService;
import com.zheminglt.vo.PostVO;
import com.zheminglt.vo.ResponseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 清除用户帖子缓存
     */
    private void clearUserPostsCache(Long userId) {
        try {
            String pattern = RedisKeyConstant.USER_POSTS + userId + ":*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            // 缓存清除失败，不影响主流程
        }
    }

    @Override
    public ResponseVO<List<PostVO>> findAll() {
        List<Post> posts = postMapper.findAll();
        List<PostVO> postVOs = posts.stream()
                .map(post -> convertToVO(post))
                .collect(Collectors.toList());
        return ResponseVO.success(postVOs);
    }

    @Override
    public ResponseVO<List<PostVO>> findHot() {
        List<Post> posts = postMapper.findAll();
        List<PostVO> postVOs = posts.stream()
                .map(post -> convertToVO(post))
                .collect(Collectors.toList());
        return ResponseVO.success(postVOs);
    }

    private PostVO convertToVO(Post post) {
        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(post, postVO);
        // 手动设置作者信息和分类名称
        if (post.getUser() != null) {
            postVO.setUserId(post.getUser().getId());
            postVO.setAuthorName(post.getUser().getUsername());
            postVO.setAuthorAvatar(post.getUser().getAvatar());
        }
        if (post.getCategory() != null) {
            postVO.setCategoryName(post.getCategory().getName());
        }
        return postVO;
    }

    @Override
    public ResponseVO<PostVO> findById(Long id) {
        Post post = postMapper.findById(id).orElse(null);
        if (post == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.POST_NOT_FOUND);
        }
        // 增加浏览量
        post.setViewCount(post.getViewCount() != null ? post.getViewCount() + 1 : 1);
        postMapper.save(post);
        PostVO postVO = convertToVO(post);
        return ResponseVO.success(postVO);
    }

    @Override
    public ResponseVO<PostVO> create(Post post) {
        try {
            post.setStatus(BusinessConstant.POST_STATUS_PUBLISHED);
            // 设置默认值
            if (post.getViewCount() == null) {
                post.setViewCount(0);
            }
            if (post.getLikeCount() == null) {
                post.setLikeCount(0);
            }
            if (post.getCommentCount() == null) {
                post.setCommentCount(0);
            }
            Post savedPost = postMapper.save(post);
            
            // 清除该用户的帖子缓存
            if (savedPost.getUser() != null) {
                clearUserPostsCache(savedPost.getUser().getId());
            }
            
            PostVO postVO = new PostVO();
            BeanUtils.copyProperties(savedPost, postVO);
            // 手动设置作者名称和分类名称
            if (savedPost.getUser() != null) {
                postVO.setAuthorName(savedPost.getUser().getUsername());
            }
            if (savedPost.getCategory() != null) {
                postVO.setCategoryName(savedPost.getCategory().getName());
            }
            return ResponseVO.success(postVO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.error(ErrorCodeConstant.CODE_ERROR, "创建帖子失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseVO<PostVO> update(Long id, Post post) {
        Post existingPost = postMapper.findById(id).orElse(null);
        if (existingPost == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.POST_NOT_FOUND);
        }
        // 保存原有的ID、用户、分类和计数器信息
        Long existingId = existingPost.getId();
        User existingUser = existingPost.getUser();
        Category existingCategory = existingPost.getCategory();
        Integer viewCount = existingPost.getViewCount();
        Integer likeCount = existingPost.getLikeCount();
        Integer commentCount = existingPost.getCommentCount();
        Integer collectCount = existingPost.getCollectCount();
        Integer status = existingPost.getStatus();
        Date createdAt = existingPost.getCreatedAt();
        
        // 复制属性，但保留原有信息
        BeanUtils.copyProperties(post, existingPost);
        
        // 恢复ID、用户、分类和计数器信息
        existingPost.setId(existingId);
        existingPost.setUser(existingUser);
        existingPost.setCategory(existingCategory);
        existingPost.setViewCount(viewCount);
        existingPost.setLikeCount(likeCount);
        existingPost.setCommentCount(commentCount);
        existingPost.setCollectCount(collectCount);
        existingPost.setStatus(status);
        existingPost.setCreatedAt(createdAt);
        
        Post savedPost = postMapper.save(existingPost);
        
        // 清除该用户的帖子缓存
        if (savedPost.getUser() != null) {
            clearUserPostsCache(savedPost.getUser().getId());
        }
        
        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(savedPost, postVO);
        return ResponseVO.success(postVO);
    }

    @Override
    public ResponseVO<Void> delete(Long id) {
        Post post = postMapper.findById(id).orElse(null);
        if (post == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.POST_NOT_FOUND);
        }
        
        // 获取用户ID用于清除缓存
        Long userId = post.getUser() != null ? post.getUser().getId() : null;
        
        postMapper.delete(post);
        
        // 清除该用户的帖子缓存
        if (userId != null) {
            clearUserPostsCache(userId);
        }
        
        return ResponseVO.success(null);
    }

    @Override
    public ResponseVO<com.zheminglt.vo.PageVO<PostVO>> searchPosts(String keyword, Long categoryId, String sortBy, int page, int size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseVO.error(ErrorCodeConstant.HTTP_BAD_REQUEST, "搜索关键词不能为空");
        }

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page - 1, size);
        org.springframework.data.domain.Page<Post> postPage;

        // 根据是否有分类ID选择查询方法
        if (categoryId != null) {
            postPage = postMapper.searchPostsByCategory(keyword.trim(), categoryId, pageable);
        } else {
            postPage = postMapper.searchPosts(keyword.trim(), pageable);
        }

        // 转换为VO列表
        List<PostVO> postVOList = postPage.getContent().stream()
                .map(this::convertToVO)
                .collect(java.util.stream.Collectors.toList());

        // 如果是按热度排序，重新排序
        if ("hot".equalsIgnoreCase(sortBy)) {
            postVOList.sort((a, b) -> {
                int heatA = a.getViewCount() * 1 + a.getLikeCount() * 3 + a.getCommentCount() * 5;
                int heatB = b.getViewCount() * 1 + b.getLikeCount() * 3 + b.getCommentCount() * 5;
                return Integer.compare(heatB, heatA);
            });
        }

        com.zheminglt.vo.PageVO<PostVO> pageVO = new com.zheminglt.vo.PageVO<>();
        pageVO.setList(postVOList);
        pageVO.setTotal(postPage.getTotalElements());
        pageVO.setPage(page);
        pageVO.setSize(size);
        pageVO.setPages(postPage.getTotalPages());

        return ResponseVO.success(pageVO);
    }
}
