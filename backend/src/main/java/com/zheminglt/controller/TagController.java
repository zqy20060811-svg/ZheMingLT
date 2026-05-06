package com.zheminglt.controller;

import com.zheminglt.service.TagService;
import com.zheminglt.vo.ResponseVO;
import com.zheminglt.vo.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
@Tag(name = "标签管理", description = "标签相关接口")
public class TagController {

    @Autowired
    private TagService tagService;

    @Operation(summary = "获取所有标签", description = "获取所有标签列表，按使用次数排序")
    @GetMapping
    public ResponseVO<List<TagVO>> getAllTags() {
        return tagService.getAllTags();
    }

    @Operation(summary = "获取热门标签", description = "获取使用次数最多的标签")
    @GetMapping("/hot")
    public ResponseVO<List<TagVO>> getHotTags(
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") int limit) {
        return tagService.getHotTags(limit);
    }

    @Operation(summary = "搜索标签", description = "根据关键词搜索标签")
    @GetMapping("/search")
    public ResponseVO<List<TagVO>> searchTags(
            @Parameter(description = "关键词") @RequestParam String keyword) {
        return tagService.searchTags(keyword);
    }

    @Operation(summary = "根据标签获取帖子", description = "获取包含指定标签的所有帖子")
    @GetMapping("/{tagId}/posts")
    public ResponseVO<?> getPostsByTag(
            @Parameter(description = "标签ID") @PathVariable Long tagId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        return tagService.getPostsByTag(tagId, page, size);
    }
}
