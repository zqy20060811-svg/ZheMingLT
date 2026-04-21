package com.zheminglt.service;

import com.zheminglt.dto.UserDTO;
import com.zheminglt.dto.LoginDTO;
import com.zheminglt.vo.LoginVO;
import com.zheminglt.vo.UserVO;
import com.zheminglt.vo.UserStatsVO;
import com.zheminglt.vo.ResponseVO;

public interface UserService {
    ResponseVO<UserVO> register(UserDTO userDTO);
    ResponseVO<LoginVO> login(LoginDTO loginDTO);
    ResponseVO<String> logout(String token);
    ResponseVO<UserVO> getUserInfo(Long userId);
    ResponseVO<UserVO> updateUserInfo(Long userId, UserDTO userDTO);
    ResponseVO<UserStatsVO> getUserStats(Long userId);
}