# 择明论坛数据库设计文档

## 数据库概述

- **数据库类型**: MySQL 8.0
- **数据库名称**: `zheminglt`
- **字符集**: `utf8mb4`
- **排序规则**: `utf8mb4_unicode_ci`

## 表结构

### 1. 用户表 (users)

存储用户基本信息

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 用户名 |
| password | VARCHAR(100) | NOT NULL | 密码（明文存储，生产环境应加密） |
| email | VARCHAR(100) | UNIQUE | 邮箱 |
| phone | VARCHAR(20) | UNIQUE | 手机号 |
| avatar | VARCHAR(500) | NULL | 头像URL |
| role | TINYINT | DEFAULT 0 | 角色：0-用户，1-管理员，2-超级管理员 |
| status | TINYINT | DEFAULT 1 | 状态：0-禁用，1-启用，2-锁定 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引**:
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_username` (`username`)
- UNIQUE KEY `uk_email` (`email`)
- UNIQUE KEY `uk_phone` (`phone`)

---

### 2. 帖子表 (posts)

存储帖子内容

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| title | VARCHAR(200) | NOT NULL | 标题 |
| content | TEXT | NOT NULL | 内容 |
| summary | VARCHAR(500) | NULL | 摘要 |
| user_id | BIGINT | NOT NULL, FK | 作者ID |
| category_id | BIGINT | NOT NULL, FK | 分类ID |
| view_count | INT | DEFAULT 0 | 浏览量 |
| like_count | INT | DEFAULT 0 | 点赞数 |
| comment_count | INT | DEFAULT 0 | 评论数 |
| status | TINYINT | DEFAULT 1 | 状态：0-草稿，1-已发布，2-已删除 |
| reason | VARCHAR(500) | NULL | 审核原因 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引**:
- PRIMARY KEY (`id`)
- KEY `idx_user_id` (`user_id`)
- KEY `idx_category_id` (`category_id`)
- KEY `idx_status` (`status`)
- KEY `idx_created_at` (`created_at`)

---

### 3. 评论表 (comments)

存储帖子评论

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| post_id | BIGINT | NOT NULL, FK | 帖子ID |
| user_id | BIGINT | NOT NULL, FK | 评论者ID |
| content | TEXT | NOT NULL | 评论内容 |
| parent_id | BIGINT | NULL, FK | 父评论ID（回复） |
| like_count | INT | DEFAULT 0 | 点赞数 |
| status | TINYINT | DEFAULT 0 | 状态：0-正常，1-已删除，2-隐藏 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引**:
- PRIMARY KEY (`id`)
- KEY `idx_post_id` (`post_id`)
- KEY `idx_user_id` (`user_id`)
- KEY `idx_parent_id` (`parent_id`)

---

### 4. 分类表 (categories)

存储帖子分类

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| name | VARCHAR(50) | NOT NULL, UNIQUE | 分类名称 |
| description | VARCHAR(200) | NULL | 分类描述 |
| sort_order | INT | DEFAULT 0 | 排序顺序 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**索引**:
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_name` (`name`)

---

### 5. 点赞表 (likes)

存储用户点赞记录

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| user_id | BIGINT | NOT NULL, FK | 用户ID |
| target_id | BIGINT | NOT NULL | 目标ID（帖子或评论） |
| target_type | TINYINT | NOT NULL | 类型：1-帖子，2-评论 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引**:
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_user_target` (`user_id`, `target_id`, `target_type`)
- KEY `idx_target` (`target_id`, `target_type`)

---

### 6. 收藏表 (collections)

存储用户收藏记录

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| user_id | BIGINT | NOT NULL, FK | 用户ID |
| post_id | BIGINT | NOT NULL, FK | 帖子ID |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引**:
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_user_post` (`user_id`, `post_id`)
- KEY `idx_post_id` (`post_id`)

---

## 表关系图

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│    users    │       │    posts    │       │  categories │
├─────────────┤       ├─────────────┤       ├─────────────┤
│     id      │◄──────┤   user_id   │       │     id      │◄────┐
│  username   │       │   title     │       │    name     │     │
│   password  │       │   content   │       │ description │     │
│    email    │       │  category_id├───────┤  sort_order │     │
│    phone    │       │  view_count │       │  created_at │     │
│    role     │       │  like_count │       │  updated_at │     │
│   status    │       │comment_count│       └─────────────┘     │
│  created_at │       │   status    │                           │
│  updated_at │       │  created_at │                           │
└─────────────┘       │  updated_at │                           │
       ▲              └─────────────┘                           │
       │                      ▲                                 │
       │                      │                                 │
       │               ┌──────┴──────┐                          │
       │               │   comments  │                          │
       │               ├─────────────┤                          │
       │               │     id      │                          │
       │               │   post_id   │                          │
       └───────────────┤   user_id   │                          │
                       │   content   │                          │
                       │  parent_id  │                          │
                       │  like_count │                          │
                       │   status    │                          │
                       │  created_at │                          │
                       │  updated_at │                          │
                       └─────────────┘                          │
                                                                │
┌─────────────┐       ┌─────────────┐                          │
│    likes    │       │ collections │                          │
├─────────────┤       ├─────────────┤                          │
│     id      │       │     id      │                          │
│   user_id   │◄──────┤   user_id   │                          │
│  target_id  │       │   post_id   │                          │
│ target_type │       │  created_at │                          │
│  created_at │       └─────────────┘                          │
└─────────────┘              ▲                                 │
                             └─────────────────────────────────┘
```

## 关系说明

1. **users : posts** = 1 : N (一个用户可以发布多个帖子)
2. **users : comments** = 1 : N (一个用户可以发表多条评论)
3. **posts : comments** = 1 : N (一个帖子可以有多条评论)
4. **categories : posts** = 1 : N (一个分类可以有多个帖子)
5. **users : likes** = 1 : N (一个用户可以点赞多次)
6. **users : collections** = 1 : N (一个用户可以收藏多个帖子)
7. **posts : collections** = 1 : N (一个帖子可以被多个用户收藏)
8. **comments : comments** = 1 : N (一条评论可以有多个回复，自关联)

## SQL 建表语句

见 `Mysql/create_tables.sql`
