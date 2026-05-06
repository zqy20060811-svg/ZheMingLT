package com.zheminglt.impl;

import com.zheminglt.mapper.PostMapper;
import com.zheminglt.mapper.TagMapper;
import com.zheminglt.model.Post;
import com.zheminglt.model.Tag;
import com.zheminglt.service.TagService;
import com.zheminglt.vo.PageVO;
import com.zheminglt.vo.PostVO;
import com.zheminglt.vo.ResponseVO;
import com.zheminglt.vo.TagVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private PostMapper postMapper;

    @Override
    public ResponseVO<List<TagVO>> getAllTags() {
        List<Tag> tags = tagMapper.findAllOrderByUsageCountDesc();
        List<TagVO> tagVOs = tags.stream().map(this::convertToVO).collect(Collectors.toList());
        return ResponseVO.success(tagVOs);
    }

    @Override
    public ResponseVO<List<TagVO>> getHotTags(int limit) {
        List<Tag> tags = tagMapper.findAllOrderByUsageCountDesc();
        List<TagVO> tagVOs = tags.stream()
                .limit(limit)
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return ResponseVO.success(tagVOs);
    }

    @Override
    public ResponseVO<List<TagVO>> searchTags(String keyword) {
        List<Tag> tags = tagMapper.findByNameContaining(keyword);
        List<TagVO> tagVOs = tags.stream().map(this::convertToVO).collect(Collectors.toList());
        return ResponseVO.success(tagVOs);
    }

    @Override
    public ResponseVO<PageVO<PostVO>> getPostsByTag(Long tagId, int page, int size) {
        Optional<Tag> tagOpt = tagMapper.findById(tagId);
        if (tagOpt.isEmpty()) {
            return ResponseVO.success(new PageVO<>());
        }

        Tag tag = tagOpt.get();
        Set<Post> posts = tag.getPosts();
        
        // 转换为列表并排序（按创建时间倒序）
        List<Post> postList = posts.stream()
                .filter(p -> p.getStatus() == 0) // 只显示正常状态的帖子
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .collect(Collectors.toList());

        // 手动分页
        int total = postList.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        
        List<PostVO> postVOs = new ArrayList<>();
        if (start < total) {
            postVOs = postList.subList(start, end).stream()
                    .map(this::convertPostToVO)
                    .collect(Collectors.toList());
        }

        PageVO<PostVO> pageVO = new PageVO<>();
        pageVO.setList(postVOs);
        pageVO.setTotal((long) total);
        pageVO.setPages((int) Math.ceil((double) total / size));
        pageVO.setPage(page);
        pageVO.setSize(size);

        return ResponseVO.success(pageVO);
    }

    @Override
    public Set<Tag> processTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<Tag> tags = new HashSet<>();
        for (String name : tagNames) {
            String trimmedName = name.trim();
            if (trimmedName.isEmpty()) continue;

            Optional<Tag> existingTag = tagMapper.findByName(trimmedName);
            if (existingTag.isPresent()) {
                Tag tag = existingTag.get();
                tag.setUsageCount(tag.getUsageCount() + 1);
                tagMapper.save(tag);
                tags.add(tag);
            } else {
                Tag newTag = new Tag();
                newTag.setName(trimmedName);
                newTag.setColor(generateRandomColor());
                newTag.setUsageCount(1);
                tagMapper.save(newTag);
                tags.add(newTag);
            }
        }
        return tags;
    }

    private TagVO convertToVO(Tag tag) {
        TagVO vo = new TagVO();
        vo.setId(tag.getId());
        vo.setName(tag.getName());
        vo.setColor(tag.getColor());
        vo.setUsageCount(tag.getUsageCount());
        return vo;
    }

    private PostVO convertPostToVO(Post post) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setSummary(post.getSummary());
        vo.setUserId(post.getUser().getId());
        vo.setAuthorName(post.getUser().getUsername());
        vo.setAuthorAvatar(post.getUser().getAvatar());
        vo.setCategoryName(post.getCategory().getName());
        vo.setViewCount(post.getViewCount());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setCollectCount(post.getCollectCount());
        vo.setCreatedAt(post.getCreatedAt());
        vo.setUpdatedAt(post.getUpdatedAt());
        
        // 转换标签
        if (post.getTags() != null) {
            List<TagVO> tagVOs = post.getTags().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            vo.setTags(tagVOs);
        }
        
        return vo;
    }

    private String generateRandomColor() {
        String[] colors = {
            "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FECA57",
            "#FF9FF3", "#54A0FF", "#48DBFB", "#1DD1A1", "#FFC048",
            "#FF6348", "#30336B", "#22A6B3", "#BE2EDD", "#EB4D4B"
        };
        return colors[new Random().nextInt(colors.length)];
    }
}
