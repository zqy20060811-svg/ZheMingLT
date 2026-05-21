package com.zheminglt.controller;

import com.zheminglt.constant.BusinessConstant;
import com.zheminglt.mapper.CategoryMapper;
import com.zheminglt.mapper.PostMapper;
import com.zheminglt.mapper.UserMapper;
import com.zheminglt.model.Category;
import com.zheminglt.model.Post;
import com.zheminglt.model.User;
import com.zheminglt.service.PostService;
import com.zheminglt.service.HotPostService;
import com.zheminglt.utils.JWTUtil;
import com.zheminglt.vo.PostVO;
import com.zheminglt.vo.ResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/posts")
@Tag(name = "帖子管理", description = "帖子的增删改查、热门帖子等接口")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private HotPostService hotPostService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private com.zheminglt.service.FollowService followService;

    @Autowired
    private com.zheminglt.service.TagService tagService;

    @Autowired
    private PostMapper postMapper;

    @Operation(summary = "获取帖子列表", description = "分页获取所有帖子，可按分类筛选，按创建时间倒序（最新的在前面）")
    @GetMapping
    public ResponseVO<Map<String, Object>> getAllPosts(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小，默认10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "分类ID，可选") @RequestParam(required = false) Long categoryId) {
        // 页码从1开始，转换为从0开始（Spring Data JPA 使用从0开始的页码）
        int pageIndex = Math.max(0, page - 1);
        
        // 创建分页对象，按创建时间倒序排序（最新的在前面）
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<Post> postPage;
        if (categoryId != null) {
            // 按分类查询
            postPage = postMapper.findByCategoryId(categoryId, pageable);
        } else {
            // 查询所有帖子
            postPage = postMapper.findAll(pageable);
        }
        
        // 转换为 PostVO
        List<PostVO> postVOs = postPage.getContent().stream()
                .map(this::convertToPostVO)
                .collect(Collectors.toList());
        
        Map<String, Object> data = new HashMap<>();
        data.put("content", postVOs);
        data.put("totalElements", postPage.getTotalElements());
        data.put("totalPages", postPage.getTotalPages());
        data.put("last", postPage.isLast());
        data.put("number", page);
        data.put("size", size);
        
        return ResponseVO.success(data);
    }
    
    /**
     * 将 Post 实体转换为 PostVO
     */
    private PostVO convertToPostVO(Post post) {
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

    @Autowired
    private com.zheminglt.service.InteractionService interactionService;

    // 从请求中获取userId的辅助方法
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader(BusinessConstant.TOKEN_HEADER);
        if (authorization == null || !authorization.startsWith(BusinessConstant.TOKEN_PREFIX)) {
            return null;
        }
        String accessToken = authorization.substring(BusinessConstant.TOKEN_PREFIX.length());
        if (!JWTUtil.validateAccessToken(accessToken)) {
            return null;
        }
        return JWTUtil.getUserIdFromAccessToken(accessToken);
    }

    @Operation(summary = "获取帖子详情", description = "根据ID获取单个帖子的详细信息")
    @GetMapping("/{id}")
    public ResponseVO<PostVO> getPostById(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        ResponseVO<PostVO> response = postService.findById(id);
        
        if (response.getData() != null) {
            PostVO postVO = response.getData();
            
            // 设置默认值
            postVO.setIsLiked(false);
            postVO.setIsCollected(false);
            postVO.setIsFollowing(false);
            
            // 如果用户已登录，检查是否已点赞和收藏
            if (userId != null) {
                // 检查是否已点赞
                try {
                    ResponseVO<Boolean> likeResponse = interactionService.isPostLiked(userId, id);
                    if (likeResponse.getData() != null) {
                        postVO.setIsLiked(likeResponse.getData());
                    }
                } catch (Exception e) {
                    System.err.println("检查点赞状态失败: " + e.getMessage());
                }
                
                // 检查是否已收藏
                try {
                    ResponseVO<Boolean> collectResponse = interactionService.isPostCollected(userId, id);
                    if (collectResponse.getData() != null) {
                        postVO.setIsCollected(collectResponse.getData());
                    }
                } catch (Exception e) {
                    System.err.println("检查收藏状态失败: " + e.getMessage());
                }
                
                // 检查是否已关注作者
                if (postVO.getUserId() != null) {
                    try {
                        ResponseVO<Boolean> followResponse = followService.isFollowing(userId, postVO.getUserId());
                        if (followResponse.getData() != null) {
                            postVO.setIsFollowing(followResponse.getData());
                        }
                    } catch (Exception e) {
                        System.err.println("检查关注状态失败: " + e.getMessage());
                    }
                }
                
            }
        }
        
        return response;
    }

    @Operation(summary = "发布帖子", description = "创建新帖子，需要登录")
    @SecurityRequirement(name = "Authorization")
    @PostMapping
    public ResponseVO<PostVO> createPost(HttpServletRequest request,
                                         @RequestBody com.zheminglt.dto.PostDTO postDTO) {
        Long userId = (Long) request.getAttribute("userId");
        
        // 检查userId是否为null
        if (userId == null) {
            return ResponseVO.error(401, "请先登录");
        }
        
        // 创建Post实体
        com.zheminglt.model.Post post = new com.zheminglt.model.Post();
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setSummary(postDTO.getSummary());
        
        // 设置计数器默认值
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCollectCount(0);
        post.setStatus(0); // 0-正常
        
        // 从数据库查询用户和分类实体
        User user = userMapper.findById(userId).orElse(null);
        if (user == null) {
            return ResponseVO.error(404, "用户不存在");
        }
        post.setUser(user);
        
        Category category = categoryMapper.findById(postDTO.getCategoryId()).orElse(null);
        if (category == null) {
            return ResponseVO.error(404, "分类不存在");
        }
        post.setCategory(category);
        
        // 处理标签
        if (postDTO.getTags() != null && !postDTO.getTags().isEmpty()) {
            post.setTags(tagService.processTags(postDTO.getTags()));
        }
        
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

    @Operation(summary = "搜索帖子", description = "根据关键词搜索帖子标题和内容，支持按分类筛选和排序")
    @GetMapping("/search")
    public ResponseVO<com.zheminglt.vo.PageVO<PostVO>> searchPosts(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "分类ID，可选") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "排序方式：time-时间，hot-热度，默认time") @RequestParam(defaultValue = "time") String sortBy,
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小，默认10") @RequestParam(defaultValue = "10") int size) {
        return postService.searchPosts(keyword, categoryId, sortBy, page, size);
    }
}
