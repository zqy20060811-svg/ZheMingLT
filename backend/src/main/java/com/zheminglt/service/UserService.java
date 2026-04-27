package com.zheminglt.service;

import com.zheminglt.dto.UserDTO;
import com.zheminglt.dto.LoginDTO;
import com.zheminglt.vo.LoginVO;
import com.zheminglt.vo.UserVO;
import com.zheminglt.vo.UserStatsVO;
import com.zheminglt.vo.ResponseVO;
import com.zheminglt.vo.PageVO;
import com.zheminglt.vo.UserLikeVO;
import com.zheminglt.vo.UserCollectionVO;
import com.zheminglt.vo.PostVO;

public interface UserService {
    ResponseVO<UserVO> register(UserDTO userDTO);
    ResponseVO<LoginVO> login(LoginDTO loginDTO);
    ResponseVO<String> logout(String token);
    ResponseVO<UserVO> getUserInfo(Long userId);
    ResponseVO<UserVO> updateUserInfo(Long userId, UserDTO userDTO);
    ResponseVO<UserStatsVO> getUserStats(Long userId);

    // 根据ID获取用户信息
    ResponseVO<UserVO> getUserById(Long userId);

    // 用户互动相关
    ResponseVO<PageVO<UserLikeVO>> getUserLikes(Long userId, int page, int size);
    ResponseVO<PageVO<UserCollectionVO>> getUserFavorites(Long userId, int page, int size);
    ResponseVO<PageVO<PostVO>> getUserPosts(Long userId, int page, int size);
}
