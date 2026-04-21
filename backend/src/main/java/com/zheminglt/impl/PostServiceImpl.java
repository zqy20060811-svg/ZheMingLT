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
                .map(post -> {
                    PostVO postVO = new PostVO();
                    BeanUtils.copyProperties(post, postVO);
                    return postVO;
                })
                .collect(Collectors.toList());
        return ResponseVO.success(postVOs);
    }

    @Override
    public ResponseVO<List<PostVO>> findHot() {
        List<Post> posts = postMapper.findAll();
        List<PostVO> postVOs = posts.stream()
                .map(post -> {
                    PostVO postVO = new PostVO();
                    BeanUtils.copyProperties(post, postVO);
                    return postVO;
                })
                .collect(Collectors.toList());
        return ResponseVO.success(postVOs);
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
        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(post, postVO);
        return ResponseVO.success(postVO);
    }

    @Override
    public ResponseVO<PostVO> create(Post post) {
        post.setStatus(BusinessConstant.POST_STATUS_PUBLISHED);
        Post savedPost = postMapper.save(post);
        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(savedPost, postVO);
        return ResponseVO.success(postVO);
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
