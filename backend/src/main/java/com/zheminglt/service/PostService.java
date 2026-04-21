package com.zheminglt.service;

import com.zheminglt.model.Post;
import com.zheminglt.vo.PostVO;
import com.zheminglt.vo.ResponseVO;

import java.util.List;

public interface PostService {
    ResponseVO<List<PostVO>> findAll();
    ResponseVO<List<PostVO>> findHot();
    ResponseVO<PostVO> findById(Long id);
    ResponseVO<PostVO> create(Post post);
    ResponseVO<PostVO> update(Long id, Post post);
    ResponseVO<Void> delete(Long id);
}