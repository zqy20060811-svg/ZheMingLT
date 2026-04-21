package com.zheminglt.impl;

import com.zheminglt.constant.BusinessConstant;
import com.zheminglt.constant.ErrorCodeConstant;
import com.zheminglt.constant.MessageConstant;
import com.zheminglt.dto.LoginDTO;
import com.zheminglt.dto.UserDTO;
import com.zheminglt.mapper.UserMapper;
import com.zheminglt.mapper.PostMapper;
import com.zheminglt.mapper.LikeMapper;
import com.zheminglt.model.User;
import com.zheminglt.service.TokenService;
import com.zheminglt.service.UserService;
import com.zheminglt.vo.LoginVO;
import com.zheminglt.vo.UserVO;
import com.zheminglt.vo.UserStatsVO;
import com.zheminglt.vo.ResponseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setRole(BusinessConstant.ROLE_USER);
        user.setStatus(BusinessConstant.STATUS_ENABLED);
        User savedUser = userMapper.save(user);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(savedUser, userVO);
        return ResponseVO.success(userVO);
    }

    @Override
    public ResponseVO<LoginVO> login(LoginDTO loginDTO) {
        System.out.println("登录请求: username=" + loginDTO.getUsername());

        if (loginDTO.getUsername() == null || loginDTO.getPassword() == null) {
            System.out.println("用户名或密码为空");
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.USERNAME_OR_PASSWORD_ERROR);
        }

        User user = userMapper.findByUsername(loginDTO.getUsername());
        if (user == null) {
            System.out.println("用户不存在: " + loginDTO.getUsername());
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.USERNAME_OR_PASSWORD_ERROR);
        }

        System.out.println("找到用户: " + user.getUsername());

        // 验证密码
        if (!loginDTO.getPassword().equals(user.getPassword())) {
            System.out.println("密码不匹配");
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.USERNAME_OR_PASSWORD_ERROR);
        }

        try {
            String token = com.zheminglt.utils.JWTUtil.generateToken(user.getId());
            System.out.println("生成token成功");

            // 将token存入Redis
            tokenService.storeToken(token, user.getId());
            System.out.println("存储token成功");

            // 创建登录VO
            LoginVO loginVO = new LoginVO();
            loginVO.setToken(token);

            // 设置用户信息
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            loginVO.setUser(userVO);
            System.out.println("登录成功");

            return ResponseVO.success(loginVO);
        } catch (Exception e) {
            System.out.println("登录异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseVO.error(ErrorCodeConstant.CODE_ERROR, "登录处理失败");
        }
    }

    @Override
    public ResponseVO<String> logout(String token) {
        if (token == null || token.isEmpty()) {
            return ResponseVO.error(ErrorCodeConstant.HTTP_BAD_REQUEST, MessageConstant.TOKEN_EMPTY);
        }

        // 检查token是否在Redis中存在且有效
        if (!tokenService.isTokenValid(token)) {
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_INVALID);
        }

        // 将token加入黑名单
        tokenService.blacklistToken(token);

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
    public ResponseVO<UserVO> updateUserInfo(Long userId, UserDTO userDTO) {
        User user = userMapper.findById(userId).orElse(null);
        if (user == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.USER_NOT_FOUND);
        }
        BeanUtils.copyProperties(userDTO, user);
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
        long likeCount = postMapper.findByUserId(userId, org.springframework.data.domain.Pageable.unpaged()).getContent().stream()
                .mapToLong(post -> likeMapper.countByPostId(post.getId()))
                .sum();
        statsVO.setLikes(likeCount);

        // 关注数和粉丝数（暂时返回0，后续实现关注功能）
        statsVO.setFollowing(0L);
        statsVO.setFollowers(0L);

        return ResponseVO.success(statsVO);
    }
}
