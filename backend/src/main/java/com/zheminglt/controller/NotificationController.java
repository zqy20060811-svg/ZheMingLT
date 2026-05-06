package com.zheminglt.controller;

import com.zheminglt.constant.BusinessConstant;
import com.zheminglt.constant.ErrorCodeConstant;
import com.zheminglt.constant.MessageConstant;
import com.zheminglt.service.NotificationService;
import com.zheminglt.utils.JWTUtil;
import com.zheminglt.vo.NotificationVO;
import com.zheminglt.vo.PageVO;
import com.zheminglt.vo.ResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@Tag(name = "消息通知", description = "消息通知相关接口")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

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

    @Operation(summary = "获取用户通知列表", description = "获取当前用户的所有通知消息")
    @SecurityRequirement(name = "Authorization")
    @GetMapping
    public ResponseVO<PageVO<NotificationVO>> getNotifications(
            HttpServletRequest request,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_EMPTY);
        }
        return notificationService.getUserNotifications(userId, page, size);
    }

    @Operation(summary = "获取未读通知数量", description = "获取当前用户未读通知的数量")
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/unread-count")
    public ResponseVO<Long> getUnreadCount(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_EMPTY);
        }
        return notificationService.getUnreadCount(userId);
    }

    @Operation(summary = "标记通知为已读", description = "将指定通知标记为已读状态")
    @SecurityRequirement(name = "Authorization")
    @PutMapping("/{id}/read")
    public ResponseVO<Void> markAsRead(
            HttpServletRequest request,
            @Parameter(description = "通知ID") @PathVariable Long id) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_EMPTY);
        }
        return notificationService.markAsRead(id, userId);
    }

    @Operation(summary = "标记所有通知为已读", description = "将当前用户的所有未读通知标记为已读")
    @SecurityRequirement(name = "Authorization")
    @PutMapping("/read-all")
    public ResponseVO<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_EMPTY);
        }
        return notificationService.markAllAsRead(userId);
    }

    @Operation(summary = "删除通知", description = "删除指定的通知消息")
    @SecurityRequirement(name = "Authorization")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> deleteNotification(
            HttpServletRequest request,
            @Parameter(description = "通知ID") @PathVariable Long id) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_UNAUTHORIZED, MessageConstant.TOKEN_EMPTY);
        }
        return notificationService.deleteNotification(id, userId);
    }
}
