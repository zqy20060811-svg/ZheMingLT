package com.zheminglt.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zheminglt.constant.BusinessConstant;
import com.zheminglt.constant.ErrorCodeConstant;
import com.zheminglt.constant.MessageConstant;
import com.zheminglt.constant.RedisKeyConstant;
import com.zheminglt.dto.LoginDTO;
import com.zheminglt.dto.UserDTO;
import com.zheminglt.mapper.UserMapper;
import com.zheminglt.mapper.PostMapper;
import com.zheminglt.mapper.LikeMapper;
import com.zheminglt.mapper.CollectionMapper;
import com.zheminglt.mapper.CommentMapper;
import com.zheminglt.mapper.CategoryMapper;
import com.zheminglt.model.User;
import com.zheminglt.model.Post;
import com.zheminglt.model.Like;
import com.zheminglt.model.Collection;
import com.zheminglt.model.Comment;
import com.zheminglt.model.Category;
import com.zheminglt.service.TokenService;
import com.zheminglt.service.UserService;
import com.zheminglt.utils.PasswordMigrationUtil;
import com.zheminglt.utils.PasswordUtil;
import com.zheminglt.vo.LoginVO;
import com.zheminglt.vo.UserVO;
import com.zheminglt.vo.UserStatsVO;
import com.zheminglt.vo.ResponseVO;
import com.zheminglt.vo.PageVO;
import com.zheminglt.vo.UserLikeVO;
import com.zheminglt.vo.UserCollectionVO;
import com.zheminglt.vo.PostVO;
import com.zheminglt.vo.CommentVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private CollectionMapper collectionMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ResponseVO<UserVO> register(UserDTO userDTO) {
        // 检查用户名是否已存在
        if (userMapper.findByUsername(userDTO.getUsername()) != null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_CONFLICT, MessageConstant.USERNAME_ALREADY_EXISTS);
        }

        // 检查邮箱是否已存在
        if (userDTO.getEmail() != null && !userDTO.getEmail().isEmpty()) {
            if (userMapper.findByEmail(userDTO.getEmail()) != null) {
                return ResponseVO.error(ErrorCodeConstant.CODE_CONFLICT, MessageConstant.EMAIL_ALREADY_EXISTS);
            }
        }

        // 检查手机号是否已存在
        if (userDTO.getPhone() != null && !userDTO.getPhone().isEmpty()) {
            if (userMapper.findByPhone(userDTO.getPhone()) != null) {
                return ResponseVO.error(ErrorCodeConstant.CODE_CONFLICT, MessageConstant.PHONE_ALREADY_EXISTS);
            }
        }

        // 检查密码长度
        String password = userDTO.getPassword();
        if (password == null || password.length() < 6) {
            return ResponseVO.error(ErrorCodeConstant.HTTP_BAD_REQUEST, "密码长度不能少于6位");
        }
        if (password.length() > 32) {
            return ResponseVO.error(ErrorCodeConstant.HTTP_BAD_REQUEST, "密码长度不能超过32位");
        }

        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        // 使用SHA-256加盐加密密码
        user.setPassword(PasswordUtil.encryptPassword(userDTO.getPassword()));
        user.setRole(BusinessConstant.ROLE_USER);
        user.setStatus(BusinessConstant.STATUS_ENABLED);
        User savedUser = userMapper.save(user);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(savedUser, userVO);
        return ResponseVO.success(userVO);
    }

    @Override
    public ResponseVO<LoginVO> login(LoginDTO loginDTO) {
        if (loginDTO.getUsername() == null || loginDTO.getPassword() == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.USERNAME_OR_PASSWORD_ERROR);
        }

        User user = userMapper.findByUsername(loginDTO.getUsername());
        if (user == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.USERNAME_OR_PASSWORD_ERROR);
        }

        // 验证密码
        boolean passwordValid = false;
        String storedPassword = user.getPassword();

        boolean isOldFormat = PasswordMigrationUtil.isOldPasswordFormat(storedPassword);

        if (isOldFormat) {
            // 旧格式：明文直接比较
            passwordValid = loginDTO.getPassword().equals(storedPassword);
            if (passwordValid) {
                // 登录成功，自动迁移密码为新格式
                String newPassword = PasswordUtil.encryptPassword(loginDTO.getPassword());
                user.setPassword(newPassword);
                userMapper.save(user);
            }
        } else {
            // 新格式：SHA-256加盐验证
            passwordValid = PasswordUtil.verifyPassword(loginDTO.getPassword(), storedPassword);
        }

        if (!passwordValid) {
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.USERNAME_OR_PASSWORD_ERROR);
        }

        try {
            // 生成双Token
            String accessToken = com.zheminglt.utils.JWTUtil.generateAccessToken(user.getId());
            String refreshToken = com.zheminglt.utils.JWTUtil.generateRefreshToken(user.getId());

            // 将双Token存入存储
            tokenService.storeAccessToken(accessToken, user.getId());
            tokenService.storeRefreshToken(refreshToken, user.getId());

            // 创建登录VO
            LoginVO loginVO = new LoginVO();
            loginVO.setAccessToken(accessToken);
            loginVO.setRefreshToken(refreshToken);
            loginVO.setAccessTokenExpiresIn(BusinessConstant.ACCESS_TOKEN_EXPIRATION_SECONDS);
            loginVO.setRefreshTokenExpiresIn(BusinessConstant.REFRESH_TOKEN_EXPIRATION_SECONDS);
            loginVO.setTokenType("Bearer");

            // 设置用户信息
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            loginVO.setUser(userVO);

            return ResponseVO.success(loginVO);
        } catch (Exception e) {
            return ResponseVO.error(ErrorCodeConstant.CODE_ERROR, "登录处理失败");
        }
    }

    @Override
    public ResponseVO<String> logout(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            return ResponseVO.error(ErrorCodeConstant.HTTP_BAD_REQUEST, MessageConstant.TOKEN_EMPTY);
        }

        // 检查AccessToken是否有效
        if (!tokenService.isAccessTokenValid(accessToken)) {
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_INVALID);
        }

        // 从AccessToken中获取用户ID
        Long userId = com.zheminglt.utils.JWTUtil.getUserIdFromAccessToken(accessToken);

        // 使该用户的所有Token失效（包括AccessToken和RefreshToken）
        tokenService.invalidateAllUserTokens(userId);

        return ResponseVO.success(MessageConstant.USER_LOGOUT_SUCCESS);
    }

    @Override
    public ResponseVO<UserVO> getUserInfo(Long userId) {
        User user = userMapper.findById(userId).orElse(null);
        if (user == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.USER_NOT_FOUND);
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ResponseVO.success(userVO);
    }

    @Override
    public ResponseVO<UserVO> getUserById(Long userId) {
        User user = userMapper.findById(userId).orElse(null);
        if (user == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.USER_NOT_FOUND);
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ResponseVO.success(userVO);
    }

    @Override
    public ResponseVO<UserVO> updateUserInfo(Long userId, UserDTO userDTO) {
        User user = userMapper.findById(userId).orElse(null);
        if (user == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.USER_NOT_FOUND);
        }
        // 保存原密码
        String originalPassword = user.getPassword();
        BeanUtils.copyProperties(userDTO, user);
        // 恢复密码，避免被覆盖
        user.setPassword(originalPassword);
        User updatedUser = userMapper.save(user);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(updatedUser, userVO);
        return ResponseVO.success(userVO);
    }

    @Override
    public ResponseVO<UserStatsVO> getUserStats(Long userId) {
        User user = userMapper.findById(userId).orElse(null);
        if (user == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.USER_NOT_FOUND);
        }

        UserStatsVO statsVO = new UserStatsVO();

        // 发帖数
        long postCount = postMapper.countByUserId(userId);
        statsVO.setPosts(postCount);

        // 获赞数（统计该用户所有帖子的点赞数）
        long likeCount = postMapper.findByUserId(userId, Pageable.unpaged()).getContent().stream()
                .mapToLong(post -> likeMapper.countByPostId(post.getId()))
                .sum();
        statsVO.setLikes(likeCount);

        // 关注数和粉丝数（暂时返回0，后续实现关注功能）
        statsVO.setFollowing(0L);
        statsVO.setFollowers(0L);

        return ResponseVO.success(statsVO);
    }

    // ==================== 用户互动相关实现 ====================

    @Override
    public ResponseVO<PageVO<UserLikeVO>> getUserLikes(Long userId, int page, int size) {
        // 检查用户是否存在
        User user = userMapper.findById(userId).orElse(null);
        if (user == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.USER_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Like> likePage = likeMapper.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<UserLikeVO> likeVOList = likePage.getContent().stream().map(like -> {
            UserLikeVO vo = new UserLikeVO();
            vo.setId(like.getId());
            vo.setUserId(like.getUser() != null ? like.getUser().getId() : null);
            vo.setCreatedAt(like.getCreatedAt() != null ? like.getCreatedAt().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);

            // 判断点赞类型
            if (like.getPost() != null) {
                vo.setType("POST");
                vo.setPostId(like.getPost().getId());
                // 获取帖子信息
                Post post = postMapper.findById(like.getPost().getId()).orElse(null);
                if (post != null) {
                    PostVO postVO = convertToPostVO(post);
                    vo.setPost(postVO);
                }
            } else if (like.getComment() != null) {
                vo.setType("COMMENT");
                vo.setCommentId(like.getComment().getId());
                // 获取评论信息
                Comment comment = commentMapper.findById(like.getComment().getId()).orElse(null);
                if (comment != null) {
                    CommentVO commentVO = new CommentVO();
                    BeanUtils.copyProperties(comment, commentVO);
                    // 获取评论作者信息
                    User commentUser = userMapper.findById(comment.getUser().getId()).orElse(null);
                    if (commentUser != null) {
                        commentVO.setAuthorName(commentUser.getUsername());
                    }
                    vo.setComment(commentVO);
                }
            }

            return vo;
        }).collect(Collectors.toList());

        PageVO<UserLikeVO> pageVO = new PageVO<>();
        pageVO.setList(likeVOList);
        pageVO.setTotal(likePage.getTotalElements());
        pageVO.setPage(page);
        pageVO.setSize(size);
        pageVO.setPages(likePage.getTotalPages());

        return ResponseVO.success(pageVO);
    }

    @Override
    public ResponseVO<PageVO<UserCollectionVO>> getUserFavorites(Long userId, int page, int size) {
        // 检查用户是否存在
        User user = userMapper.findById(userId).orElse(null);
        if (user == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.USER_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Collection> collectionPage = collectionMapper.findByUserId(userId, pageable);

        List<UserCollectionVO> collectionVOList = collectionPage.getContent().stream().map(collection -> {
            UserCollectionVO vo = new UserCollectionVO();
            vo.setId(collection.getId());
            vo.setUserId(collection.getUser() != null ? collection.getUser().getId() : null);
            vo.setPostId(collection.getPost() != null ? collection.getPost().getId() : null);
            vo.setCreatedAt(collection.getCreatedAt() != null ? collection.getCreatedAt().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);

            // 获取帖子信息
            if (collection.getPost() != null) {
                Post post = postMapper.findById(collection.getPost().getId()).orElse(null);
                if (post != null) {
                    PostVO postVO = convertToPostVO(post);
                    vo.setPost(postVO);
                }
            }

            return vo;
        }).collect(Collectors.toList());

        PageVO<UserCollectionVO> pageVO = new PageVO<>();
        pageVO.setList(collectionVOList);
        pageVO.setTotal(collectionPage.getTotalElements());
        pageVO.setPage(page);
        pageVO.setSize(size);
        pageVO.setPages(collectionPage.getTotalPages());

        return ResponseVO.success(pageVO);
    }

    @Override
    public ResponseVO<PageVO<PostVO>> getUserPosts(Long userId, int page, int size) {
        // 检查用户是否存在
        User user = userMapper.findById(userId).orElse(null);
        if (user == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.USER_NOT_FOUND);
        }

        // 构建缓存key
        String cacheKey = RedisKeyConstant.USER_POSTS + userId + ":" + page + ":" + size;

        try {
            // 尝试从缓存获取
            String cachedData = redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                PageVO<PostVO> cachedPageVO = objectMapper.readValue(cachedData, new TypeReference<PageVO<PostVO>>() {});
                return ResponseVO.success(cachedPageVO);
            }
        } catch (Exception e) {
            // 缓存读取失败，继续查询数据库
            System.err.println("Redis缓存读取失败: " + e.getMessage());
        }

        // 查询数据库
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Post> postPage = postMapper.findByUserId(userId, pageable);

        List<PostVO> postVOList = postPage.getContent().stream()
                .map(this::convertToPostVO)
                .collect(Collectors.toList());

        PageVO<PostVO> pageVO = new PageVO<>();
        pageVO.setList(postVOList);
        pageVO.setTotal(postPage.getTotalElements());
        pageVO.setPage(page);
        pageVO.setSize(size);
        pageVO.setPages(postPage.getTotalPages());

        // 写入缓存
        try {
            String jsonData = objectMapper.writeValueAsString(pageVO);
            redisTemplate.opsForValue().set(cacheKey, jsonData, RedisKeyConstant.USER_POSTS_EXPIRE, TimeUnit.MINUTES);
        } catch (Exception e) {
            // 缓存写入失败，不影响返回结果
            System.err.println("Redis缓存写入失败: " + e.getMessage());
        }

        return ResponseVO.success(pageVO);
    }

    @Override
    public ResponseVO<UserVO> updateAvatar(Long userId, String avatarUrl) {
        User user = userMapper.findById(userId).orElse(null);
        if (user == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.USER_NOT_FOUND);
        }
        user.setAvatar(avatarUrl);
        User updatedUser = userMapper.save(user);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(updatedUser, userVO);
        return ResponseVO.success(userVO);
    }

    @Override
    public ResponseVO<UserVO> updateUsername(Long userId, String newUsername) {
        if (newUsername == null || newUsername.trim().isEmpty()) {
            return ResponseVO.error(ErrorCodeConstant.HTTP_BAD_REQUEST, "用户名不能为空");
        }
        if (newUsername.length() < 2 || newUsername.length() > 20) {
            return ResponseVO.error(ErrorCodeConstant.HTTP_BAD_REQUEST, "用户名长度必须在2-20个字符之间");
        }

        // 检查新用户名是否已被使用
        User existingUser = userMapper.findByUsername(newUsername);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            return ResponseVO.error(ErrorCodeConstant.CODE_CONFLICT, "用户名已被使用");
        }

        User user = userMapper.findById(userId).orElse(null);
        if (user == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.USER_NOT_FOUND);
        }

        user.setUsername(newUsername);
        User updatedUser = userMapper.save(user);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(updatedUser, userVO);
        return ResponseVO.success(userVO);
    }

    // 辅助方法：将Post转换为PostVO
    private PostVO convertToPostVO(Post post) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setSummary(post.getSummary());
        vo.setViewCount(post.getViewCount());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setStatus(post.getStatus());
        vo.setCreatedAt(post.getCreatedAt());
        vo.setUpdatedAt(post.getUpdatedAt());

        // 设置作者信息
        if (post.getUser() != null) {
            vo.setUserId(post.getUser().getId());
            vo.setAuthorName(post.getUser().getUsername());
            vo.setAuthorAvatar(post.getUser().getAvatar());
        }

        // 设置分类信息
        if (post.getCategory() != null) {
            vo.setCategoryName(post.getCategory().getName());
        }

        return vo;
    }
}
