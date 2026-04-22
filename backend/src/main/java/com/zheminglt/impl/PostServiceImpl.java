package com.zheminglt.impl;

import com.zheminglt.constant.BusinessConstant;
import com.zheminglt.constant.ErrorCodeConstant;
import com.zheminglt.constant.MessageConstant;
import com.zheminglt.mapper.PostMapper;
import com.zheminglt.model.Post;
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
        // 手动设置作者名称和分类名称
        if (post.getUser() != null) {
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
    public ResponseVO<PostVO> update(Long id, Post post) {
        Post existingPost = postMapper.findById(id).orElse(null);
        if (existingPost == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.POST_NOT_FOUND);
        }
        BeanUtils.copyProperties(post, existingPost);
        Post savedPost = postMapper.save(existingPost);
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
        postMapper.delete(post);
        return ResponseVO.success(null);
    }
}
