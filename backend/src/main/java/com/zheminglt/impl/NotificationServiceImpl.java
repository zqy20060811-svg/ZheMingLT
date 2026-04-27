package com.zheminglt.impl;

import com.zheminglt.constant.ErrorCodeConstant;
import com.zheminglt.constant.MessageConstant;
import com.zheminglt.mapper.NotificationMapper;
import com.zheminglt.mapper.PostMapper;
import com.zheminglt.mapper.UserMapper;
import com.zheminglt.model.Notification;
import com.zheminglt.model.Post;
import com.zheminglt.model.User;
import com.zheminglt.service.NotificationService;
import com.zheminglt.vo.NotificationVO;
import com.zheminglt.vo.PageVO;
import com.zheminglt.vo.ResponseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @Override
    public ResponseVO<PageVO<NotificationVO>> getUserNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Notification> notificationPage = notificationMapper.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<NotificationVO> voList = notificationPage.getContent().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        PageVO<NotificationVO> pageVO = new PageVO<>();
        pageVO.setList(voList);
        pageVO.setTotal(notificationPage.getTotalElements());
        pageVO.setPage(page);
        pageVO.setSize(size);
        pageVO.setPages(notificationPage.getTotalPages());

        return ResponseVO.success(pageVO);
    }

    @Override
    public ResponseVO<Long> getUnreadCount(Long userId) {
        long count = notificationMapper.countByUserIdAndIsReadFalse(userId);
        return ResponseVO.success(count);
    }

    @Override
    @Transactional
    public ResponseVO<Void> markAsRead(Long notificationId, Long userId) {
        int updated = notificationMapper.markAsRead(notificationId, userId);
        if (updated == 0) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, "通知不存在或无权限");
        }
        return ResponseVO.success(null);
    }

    @Override
    @Transactional
    public ResponseVO<Void> markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
        return ResponseVO.success(null);
    }

    @Override
    @Transactional
    public ResponseVO<Void> deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationMapper.findById(notificationId).orElse(null);
        if (notification == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.NOTIFICATION_NOT_FOUND);
        }

        // 检查是否是通知接收者
        if (!notification.getUser().getId().equals(userId)) {
            return ResponseVO.error(ErrorCodeConstant.CODE_FORBIDDEN, MessageConstant.FORBIDDEN);
        }

        notificationMapper.delete(notification);
        return ResponseVO.success(null);
    }

    @Override
    @Transactional
    public void createCommentNotification(Long postId, Long commentId, Long fromUserId, Long toUserId) {
        // 不给自己发通知
        if (fromUserId.equals(toUserId)) {
            return;
        }

        User fromUser = userMapper.findById(fromUserId).orElse(null);
        Post post = postMapper.findById(postId).orElse(null);
        User toUser = userMapper.findById(toUserId).orElse(null);

        if (fromUser == null || post == null || toUser == null) {
            return;
        }

        Notification notification = new Notification();
        notification.setUser(toUser);
        notification.setType("COMMENT");
        notification.setTitle("收到新评论");
        notification.setContent(fromUser.getUsername() + " 评论了你的帖子《" + post.getTitle() + "》");
        notification.setPostId(postId);
        notification.setCommentId(commentId);
        notification.setFromUserId(fromUserId);
        notification.setIsRead(false);

        notificationMapper.save(notification);
    }

    @Override
    @Transactional
    public void createLikeNotification(Long postId, Long fromUserId, Long toUserId) {
        // 不给自己发通知
        if (fromUserId.equals(toUserId)) {
            return;
        }

        User fromUser = userMapper.findById(fromUserId).orElse(null);
        Post post = postMapper.findById(postId).orElse(null);
        User toUser = userMapper.findById(toUserId).orElse(null);

        if (fromUser == null || post == null || toUser == null) {
            return;
        }

        Notification notification = new Notification();
        notification.setUser(toUser);
        notification.setType("LIKE");
        notification.setTitle("收到新点赞");
        notification.setContent(fromUser.getUsername() + " 赞了你的帖子《" + post.getTitle() + "》");
        notification.setPostId(postId);
        notification.setFromUserId(fromUserId);
        notification.setIsRead(false);

        notificationMapper.save(notification);
    }

    @Override
    @Transactional
    public void createSystemNotification(Long userId, String title, String content) {
        User user = userMapper.findById(userId).orElse(null);
        if (user == null) {
            return;
        }

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType("SYSTEM");
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(false);

        notificationMapper.save(notification);
    }

    private NotificationVO convertToVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        BeanUtils.copyProperties(notification, vo);
        vo.setUserId(notification.getUser().getId());

        // 获取触发用户名称
        if (notification.getFromUserId() != null) {
            User fromUser = userMapper.findById(notification.getFromUserId()).orElse(null);
            if (fromUser != null) {
                vo.setFromUserName(fromUser.getUsername());
            }
        }

        return vo;
    }
}
