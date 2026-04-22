package com.zheminglt.controller;

import com.zheminglt.constant.BusinessConstant;
import com.zheminglt.constant.ErrorCodeConstant;
import com.zheminglt.constant.MessageConstant;
import com.zheminglt.dto.UserDTO;
import com.zheminglt.dto.LoginDTO;
import com.zheminglt.service.TokenService;
import com.zheminglt.service.UserService;
import com.zheminglt.vo.LoginVO;
import com.zheminglt.vo.ResponseVO;
import com.zheminglt.vo.TokenRefreshVO;
import com.zheminglt.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
@Tag(name = "用户管理", description = "用户登录、注册、退出等相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    @Operation(summary = "测试接口", description = "测试服务是否正常运行")
    @GetMapping("/test")
    public ResponseVO<String> test() {
        return ResponseVO.success("服务运行正常，时间：" + new java.util.Date());
    }

    @Operation(summary = "用户注册", description = "用户注册接口，不需要手机号和邮箱验证")
    @PostMapping("/register")
    public ResponseVO<UserVO> register(@RequestBody UserDTO userDTO) {
        return userService.register(userDTO);
    }

    @Operation(summary = "用户登录", description = "用户登录接口，返回双Token（AccessToken和RefreshToken）")
    @PostMapping("/login")
    public ResponseVO<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    @Operation(summary = "退出登录", description = "用户退出登录，将token加入黑名单")
    @SecurityRequirement(name = "Authorization")
    @PostMapping("/logout")
    public ResponseVO<String> logout(@Parameter(hidden = true) @RequestHeader("Authorization") String authorization) {
        String token = authorization.substring(7); // 去掉 "Bearer " 前缀
        return userService.logout(token);
    }

    @Operation(summary = "刷新Token", description = "使用RefreshToken刷新双Token，实现无感登录")
    @PostMapping("/refresh")
    public ResponseVO<TokenRefreshVO> refreshToken(
            @RequestHeader(value = BusinessConstant.REFRESH_TOKEN_HEADER, required = false) String refreshTokenHeader) {

        // 从请求头中获取RefreshToken
        String refreshToken = refreshTokenHeader;
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_EMPTY);
        }

        // 去掉Bearer前缀（如果有）
        if (refreshToken.startsWith(BusinessConstant.TOKEN_PREFIX)) {
            refreshToken = refreshToken.substring(BusinessConstant.TOKEN_PREFIX.length());
        }

        // 调用TokenService刷新Token
        Map<String, String> newTokens = tokenService.refreshTokens(refreshToken);

        if (newTokens == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_INVALID);
        }

        // 构建返回对象
        TokenRefreshVO tokenRefreshVO = new TokenRefreshVO();
        tokenRefreshVO.setAccessToken(newTokens.get("accessToken"));
        tokenRefreshVO.setRefreshToken(newTokens.get("refreshToken"));
        tokenRefreshVO.setAccessTokenExpiresIn(BusinessConstant.ACCESS_TOKEN_EXPIRATION_SECONDS);
        tokenRefreshVO.setRefreshTokenExpiresIn(BusinessConstant.REFRESH_TOKEN_EXPIRATION_SECONDS);
        tokenRefreshVO.setTokenType("Bearer");

        return ResponseVO.success(tokenRefreshVO);
    }

    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/info")
    public ResponseVO<UserVO> getUserInfo(@Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        return userService.getUserInfo(userId);
    }

    @Operation(summary = "更新用户信息", description = "更新当前登录用户的信息")
    @SecurityRequirement(name = "Authorization")
    @PutMapping("/info")
    public ResponseVO<UserVO> updateUserInfo(@Parameter(hidden = true) @RequestAttribute("userId") Long userId, @RequestBody UserDTO userDTO) {
        return userService.updateUserInfo(userId, userDTO);
    }

    @Operation(summary = "获取用户统计", description = "获取当前登录用户的统计数据（发帖数、获赞数、关注数、粉丝数）")
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/stats")
    public ResponseVO<com.zheminglt.vo.UserStatsVO> getUserStats(@Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        return userService.getUserStats(userId);
    }

    @Operation(summary = "获取用户资料", description = "获取当前登录用户的完整资料信息")
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/profile")
    public ResponseVO<UserVO> getUserProfile(@Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        return userService.getUserInfo(userId);
    }

    // ==================== 用户互动相关接口 ====================

    @Operation(summary = "获取用户的点赞列表", description = "获取指定用户点赞的帖子和评论列表")
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/{userId}/likes")
    public ResponseVO<com.zheminglt.vo.PageVO<com.zheminglt.vo.UserLikeVO>> getUserLikes(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        return userService.getUserLikes(userId, page, size);
    }

    @Operation(summary = "获取用户的收藏列表", description = "获取指定用户收藏的帖子列表")
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/{userId}/favorites")
    public ResponseVO<com.zheminglt.vo.PageVO<com.zheminglt.vo.UserCollectionVO>> getUserFavorites(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        return userService.getUserFavorites(userId, page, size);
    }

    @Operation(summary = "获取用户的帖子列表", description = "获取指定用户发布的帖子列表")
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/{userId}/posts")
    public ResponseVO<com.zheminglt.vo.PageVO<com.zheminglt.vo.PostVO>> getUserPosts(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        return userService.getUserPosts(userId, page, size);
    }
}
