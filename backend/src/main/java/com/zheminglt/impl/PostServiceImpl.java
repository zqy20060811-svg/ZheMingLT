package com.zheminglt.impl;

import com.zheminglt.constant.BusinessConstant;
import com.zheminglt.constant.ErrorCodeConstant;
import com.zheminglt.constant.MessageConstant;
import com.zheminglt.mapper.CategoryMapper;
import com.zheminglt.mapper.PostMapper;
import com.zheminglt.mapper.UserMapper;
import com.zheminglt.model.Category;
import com.zheminglt.model.Post;
import com.zheminglt.model.User;
import com.zheminglt.service.PostService;
import com.zheminglt.vo.PostVO;
import com.zheminglt.vo.ResponseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CategoryMapper categoryMapper;

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
        // 暂时注释掉，避免getViewCount方法不存在的问题
        // post.setViewCount(post.getViewCount() + 1);
        // postMapper.save(post);
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
    public ResponseVO<PostVO> update(Long id, Post post, Long userId) {
        Post existingPost = postMapper.findById(id).orElse(null);
        if (existingPost == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.POST_NOT_FOUND);
        }

        // 检查是否是帖子作者或管理员
        User user = userMapper.findById(userId).orElse(null);
        if (user == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.USER_NOT_FOUND);
        }

        boolean isAuthor = existingPost.getUser() != null && existingPost.getUser().getId().equals(userId);
        boolean isAdmin = user.getRole() != null && user.getRole() == BusinessConstant.ROLE_ADMIN;

        if (!isAuthor && !isAdmin) {
            return ResponseVO.error(ErrorCodeConstant.CODE_FORBIDDEN, MessageConstant.FORBIDDEN);
        }

        // 只更新允许更新的字段
        if (post.getTitle() != null) {
            existingPost.setTitle(post.getTitle());
        }
        if (post.getContent() != null) {
            existingPost.setContent(post.getContent());
        }
        if (post.getSummary() != null) {
            existingPost.setSummary(post.getSummary());
        }
        if (post.getCategory() != null && post.getCategory().getId() != null) {
            Category category = categoryMapper.findById(post.getCategory().getId()).orElse(null);
            if (category != null) {
                existingPost.setCategory(category);
            }
        }

        Post savedPost = postMapper.save(existingPost);
        PostVO postVO = convertToVO(savedPost);
        return ResponseVO.success(postVO);
    }

    @Override
    public ResponseVO<Void> delete(Long id) {
        Post post = postMapper.findById(id).orElse(null);
        if (post == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.POST_NOT_FOUND);
        }
        postMapper.delete(post);
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
