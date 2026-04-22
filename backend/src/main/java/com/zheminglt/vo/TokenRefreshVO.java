package com.zheminglt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Token刷新返回对象")
public class TokenRefreshVO {

    @Schema(description = "新的访问令牌（AccessToken），有效期15分钟")
    private String accessToken;

    @Schema(description = "新的刷新令牌（RefreshToken），有效期7天")
    private String refreshToken;

    @Schema(description = "访问令牌过期时间（秒）")
    private Long accessTokenExpiresIn;

    @Schema(description = "刷新令牌过期时间（秒）")
    private Long refreshTokenExpiresIn;

    @Schema(description = "令牌类型，固定为Bearer")
    private String tokenType;
}
