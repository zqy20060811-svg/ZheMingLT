package com.zheminglt.service;

import com.zheminglt.vo.PageVO;
import com.zheminglt.vo.PostVO;
import com.zheminglt.vo.ResponseVO;
import com.zheminglt.vo.TagVO;

import java.util.List;
import java.util.Set;

public interface TagService {
    ResponseVO<List<TagVO>> getAllTags();
    ResponseVO<List<TagVO>> getHotTags(int limit);
    ResponseVO<List<TagVO>> searchTags(String keyword);
    ResponseVO<PageVO<PostVO>> getPostsByTag(Long tagId, int page, int size);
    Set<com.zheminglt.model.Tag> processTags(List<String> tagNames);
}
