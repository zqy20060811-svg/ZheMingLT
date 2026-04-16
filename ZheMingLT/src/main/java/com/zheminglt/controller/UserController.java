package com.zheminglt.controller;

import com.zheminglt.dto.UserDTO;
import com.zheminglt.dto.LoginDTO;
import com.zheminglt.service.UserService;
import com.zheminglt.vo.ResponseVO;
import com.zheminglt.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "用户管理", description = "用户登录、注册、退出等相关接口")
public class UserController {

    @Autowired
    private UserService userService;

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

    @Operation(summary = "用户登录", description = "用户登录接口，返回JWT token")
    @PostMapping("/login")
    public ResponseVO<com.zheminglt.vo.LoginVO> login(@RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    @Operation(summary = "退出登录", description = "用户退出登录，将token加入黑名单")
    @SecurityRequirement(name = "Authorization")
    @PostMapping("/logout")
    public ResponseVO<String> logout(@Parameter(hidden = true) @RequestHeader("Authorization") String authorization) {
        String token = authorization.substring(7); // 去掉 "Bearer " 前缀
        return userService.logout(token);
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
}
