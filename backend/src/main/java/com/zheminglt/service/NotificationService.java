package com.zheminglt.service;

import com.zheminglt.vo.NotificationVO;
import com.zheminglt.vo.PageVO;
import com.zheminglt.vo.ResponseVO;

public interface NotificationService {

    // 获取用户通知列表
    ResponseVO<PageVO<NotificationVO>> getUserNotifications(Long userId, int page, int size);

    // 获取未读通知数量
    ResponseVO<Long> getUnreadCount(Long userId);

    // 标记通知为已读
    ResponseVO<Void> markAsRead(Long notificationId, Long userId);

    // 标记所有通知为已读
    ResponseVO<Void> markAllAsRead(Long userId);

    // 删除通知
    ResponseVO<Void> deleteNotification(Long notificationId, Long userId);

    // 创建评论通知
    void createCommentNotification(Long postId, Long commentId, Long fromUserId, Long toUserId);

    // 创建点赞通知
    void createLikeNotification(Long postId, Long fromUserId, Long toUserId);

    // 创建系统通知
    void createSystemNotification(Long userId, String title, String content);
}
