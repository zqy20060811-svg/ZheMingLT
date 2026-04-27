package com.zheminglt.service;

import com.zheminglt.model.Post;
import com.zheminglt.vo.PageVO;
import com.zheminglt.vo.PostVO;
import com.zheminglt.vo.ResponseVO;

import java.util.List;

public interface PostService {
    ResponseVO<List<PostVO>> findAll();
    ResponseVO<List<PostVO>> findHot();
    ResponseVO<PostVO> findById(Long id);
    ResponseVO<PostVO> create(Post post);
    ResponseVO<PostVO> update(Long id, Post post, Long userId);
    ResponseVO<Void> delete(Long id);

    // 搜索功能
    ResponseVO<PageVO<PostVO>> searchPosts(String keyword, Long categoryId, String sortBy, int page, int size);
}
