package com.zheminglt.mapper;

import com.zheminglt.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationMapper extends JpaRepository<Notification, Long> {

    // 获取用户的所有通知（分页）
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 获取用户的未读通知
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    // 获取用户的未读通知数量
    long countByUserIdAndIsReadFalse(Long userId);

    // 标记单条通知为已读
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id AND n.user.id = :userId")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    // 标记用户所有通知为已读
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") Long userId);

    // 删除用户的所有通知
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
