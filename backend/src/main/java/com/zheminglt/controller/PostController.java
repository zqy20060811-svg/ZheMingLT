package com.zheminglt.controller;

import com.zheminglt.service.PostService;
import com.zheminglt.service.HotPostService;
import com.zheminglt.vo.PostVO;
import com.zheminglt.vo.ResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
@Tag(name = "帖子管理", description = "帖子的增删改查、热门帖子等接口")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private HotPostService hotPostService;

    @Operation(summary = "获取帖子列表", description = "分页获取所有帖子，可按分类筛选")
    @GetMapping
    public ResponseVO<Map<String, Object>> getAllPosts(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小，默认10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "分类ID，可选") @RequestParam(required = false) Long categoryId) {
        List<PostVO> posts = postService.findAll().getData();
        
        // 模拟分页逻辑
        int total = posts.size();
        int pages = (total + size - 1) / size;
        List<PostVO> paginatedPosts = posts;
        
        if (total > 0) {
            int start = (page - 1) * size;
            int end = Math.min(start + size, total);
            paginatedPosts = posts.subList(start, end);
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", paginatedPosts);
        data.put("total", total);
        data.put("pages", pages);
        
        return ResponseVO.success(data);
    }

    @Operation(summary = "获取热门帖子", description = "获取热度最高的帖子列表，支持时间筛选（day今日/week本周/month本月/all总榜）")
    @GetMapping("/hot")
    public ResponseVO<List<PostVO>> getHotPosts(
            @Parameter(description = "时间筛选：day今日/week本周/month本月/all总榜，默认all") @RequestParam(defaultValue = "all") String filter,
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小，默认20") @RequestParam(defaultValue = "20") int size) {
        switch (filter.toLowerCase()) {
            case "day":
                return hotPostService.getHotPostsToday(page, size);
            case "week":
                return hotPostService.getHotPostsWeek(page, size);
            case "month":
                return hotPostService.getHotPostsMonth(page, size);
            case "all":
            default:
                return hotPostService.getHotPostsAll(page, size);
        }
    }

    @Operation(summary = "获取帖子详情", description = "根据ID获取单个帖子的详细信息")
    @GetMapping("/{id}")
    public ResponseVO<PostVO> getPostById(@Parameter(description = "帖子ID") @PathVariable Long id) {
        return postService.findById(id);
    }

    @Operation(summary = "发布帖子", description = "创建新帖子，需要登录")
    @SecurityRequirement(name = "Authorization")
    @PostMapping
    public ResponseVO<PostVO> createPost(@Parameter(hidden = true) @RequestAttribute("userId") Long userId, 
                                         @RequestBody com.zheminglt.model.Post post) {
        // 设置发帖用户
        com.zheminglt.model.User user = new com.zheminglt.model.User();
        user.setId(userId);
        post.setUser(user);
        return postService.create(post);
    }

    @Operation(summary = "更新帖子", description = "更新帖子内容，需要登录且是帖子作者")
    @SecurityRequirement(name = "Authorization")
    @PutMapping("/{id}")
    public ResponseVO<PostVO> updatePost(@Parameter(description = "帖子ID") @PathVariable Long id, 
                                         @RequestBody com.zheminglt.model.Post post) {
        return postService.update(id, post);
    }

    @Operation(summary = "删除帖子", description = "删除帖子，需要登录且是帖子作者或管理员")
    @SecurityRequirement(name = "Authorization")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> deletePost(@Parameter(description = "帖子ID") @PathVariable Long id) {
        return postService.delete(id);
    }
}
