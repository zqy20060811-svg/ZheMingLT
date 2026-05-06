package com.zheminglt.utils;

import com.zheminglt.model.User;
import com.zheminglt.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PasswordMigrationUtil {

    @Autowired
    private UserMapper userMapper;

    /**
     * 检查密码是否是旧格式（明文或未加盐的哈希）
     * 新格式: salt:hash
     */
    public static boolean isOldPasswordFormat(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        // 新格式包含冒号分隔符
        return !password.contains(":");
    }

    /**
     * 迁移单个用户的密码
     */
    public void migrateUserPassword(User user) {
        if (user == null || user.getPassword() == null) {
            return;
        }

        String oldPassword = user.getPassword();

        // 检查是否已经是新格式
        if (!isOldPasswordFormat(oldPassword)) {
            System.out.println("用户 " + user.getUsername() + " 的密码已经是新格式，跳过");
            return;
        }

        // 使用旧密码作为明文重新加密
        String newPassword = PasswordUtil.encryptPassword(oldPassword);
        user.setPassword(newPassword);
        userMapper.save(user);

        System.out.println("用户 " + user.getUsername() + " 的密码已迁移完成");
    }

    /**
     * 迁移所有用户的密码
     */
    public void migrateAllPasswords() {
        List<User> users = userMapper.findAll();
        int migratedCount = 0;
        int skippedCount = 0;

        for (User user : users) {
            if (isOldPasswordFormat(user.getPassword())) {
                migrateUserPassword(user);
                migratedCount++;
            } else {
                skippedCount++;
            }
        }

        System.out.println("密码迁移完成: 迁移 " + migratedCount + " 个用户, 跳过 " + skippedCount + " 个用户");
    }
}
