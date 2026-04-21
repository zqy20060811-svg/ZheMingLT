package com.zheminglt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录返回对象")
public class LoginVO {

    @Schema(description = "JWT token")
    private String token;

    @Schema(description = "用户信息")
    private UserVO user;
}
