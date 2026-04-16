# 择明论坛数据库设计

## 1. 数据库概览

**数据库名称**：zheminglt

**设计目标**：
- 支持用户注册、登录和管理
- 支持帖子发布、审核和管理
- 支持评论功能
- 支持分类管理
- 支持点赞和收藏功能
- 支持AI审核和摘要功能

## 2. 表结构设计

### 2.1 用户表（users）

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `PRIMARY KEY AUTO_INCREMENT` | 用户ID |
| `username` | `VARCHAR(50)` | `NOT NULL UNIQUE` | 用户名 |
| `password` | `VARCHAR(100)` | `NOT NULL` | 密码（加密存储） |
| `email` | `VARCHAR(100)` | `UNIQUE` | 邮箱 |
| `phone` | `VARCHAR(20)` | `UNIQUE` | 手机号 |
| `avatar` | `VARCHAR(255)` | | 头像URL |
| `role` | `INT` | `DEFAULT 0` | 角色（0：普通用户，1：管理员） |
| `status` | `INT` | `DEFAULT 1` | 状态（0：禁用，1：启用） |
| `created_at` | `DATETIME` | `DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `updated_at` | `DATETIME` | `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |

### 2.2 分类表（categories）

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `PRIMARY KEY AUTO_INCREMENT` | 分类ID |
| `name` | `VARCHAR(50)` | `NOT NULL UNIQUE` | 分类名称 |
| `description` | `VARCHAR(255)` | | 分类描述 |
| `sort` | `INT` | `DEFAULT 0` | 排序权重 |
| `created_at` | `DATETIME` | `DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `updated_at` | `DATETIME` | `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |

### 2.3 帖子表（posts）

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `PRIMARY KEY AUTO_INCREMENT` | 帖子ID |
| `title` | `VARCHAR(100)` | `NOT NULL` | 帖子标题 |
| `content` | `TEXT` | `NOT NULL` | 帖子内容 |
| `summary` | `VARCHAR(255)` | | AI生成的摘要 |
| `user_id` | `BIGINT` | `NOT NULL REFERENCES users(id)` | 作者ID |
| `category_id` | `BIGINT` | `NOT NULL REFERENCES categories(id)` | 分类ID |
| `status` | `INT` | `DEFAULT 0` | 审核状态（0：待审核，1：审核通过，2：审核拒绝） |
| `reason` | `VARCHAR(255)` | | 审核拒绝原因 |
| `view_count` | `INT` | `DEFAULT 0` | 浏览量 |
| `like_count` | `INT` | `DEFAULT 0` | 点赞数 |
| `comment_count` | `INT` | `DEFAULT 0` | 评论数 |
| `created_at` | `DATETIME` | `DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `updated_at` | `DATETIME` | `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |

### 2.4 评论表（comments）

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `PRIMARY KEY AUTO_INCREMENT` | 评论ID |
| `post_id` | `BIGINT` | `NOT NULL REFERENCES posts(id)` | 帖子ID |
| `user_id` | `BIGINT` | `NOT NULL REFERENCES users(id)` | 评论用户ID |
| `parent_id` | `BIGINT` | `REFERENCES comments(id)` | 父评论ID（用于回复） |
| `content` | `TEXT` | `NOT NULL` | 评论内容 |
| `status` | `INT` | `DEFAULT 1` | 状态（0：禁用，1：启用） |
| `created_at` | `DATETIME` | `DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `updated_at` | `DATETIME` | `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |

### 2.5 点赞表（likes）

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `PRIMARY KEY AUTO_INCREMENT` | 点赞ID |
| `user_id` | `BIGINT` | `NOT NULL REFERENCES users(id)` | 用户ID |
| `post_id` | `BIGINT` | `REFERENCES posts(id)` | 帖子ID（与comment_id二选一） |
| `comment_id` | `BIGINT` | `REFERENCES comments(id)` | 评论ID（与post_id二选一） |
| `created_at` | `DATETIME` | `DEFAULT CURRENT_TIMESTAMP` | 创建时间 |

### 2.6 收藏表（collections）

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `PRIMARY KEY AUTO_INCREMENT` | 收藏ID |
| `user_id` | `BIGINT` | `NOT NULL REFERENCES users(id)` | 用户ID |
| `post_id` | `BIGINT` | `NOT NULL REFERENCES posts(id)` | 帖子ID |
| `created_at` | `DATETIME` | `DEFAULT CURRENT_TIMESTAMP` | 创建时间 |

## 3. 关系图

```
users (1:N) posts
users (1:N) comments
users (1:N) likes
users (1:N) collections
categories (1:N) posts
posts (1:N) comments
posts (1:N) likes
posts (1:N) collections
comments (1:N) comments (回复关系)
comments (1:N) likes
```

## 4. 索引设计

### 4.1 主键索引
- 所有表的`id`字段

### 4.2 唯一索引
- `users.username`
- `users.email`
- `users.phone`
- `categories.name`

### 4.3 普通索引
- `posts.user_id`
- `posts.category_id`
- `posts.status`
- `comments.post_id`
- `comments.user_id`
- `comments.parent_id`
- `likes.user_id`
- `likes.post_id`
- `likes.comment_id`
- `collections.user_id`
- `collections.post_id`

## 5. 数据安全

1. **密码加密**：用户密码使用BCrypt算法加密存储
2. **SQL注入防护**：使用参数化查询和ORM框架
3. **XSS防护**：对用户输入进行过滤和转义
4. **CSRF防护**：实现CSRF令牌验证
5. **权限控制**：基于角色的权限管理
6. **敏感数据保护**：敏感信息加密存储

## 6. 性能优化

1. **缓存策略**：使用Redis缓存热门帖子和用户信息
2. **分页查询**：实现高效的分页查询
3. **索引优化**：合理创建和使用索引
4. **数据库连接池**：使用连接池管理数据库连接
5. **异步处理**：使用消息队列处理异步任务
6. **读写分离**：考虑主从复制实现读写分离

## 7. 扩展性考虑

1. **分表分库**：预留分表分库方案
2. **微服务架构**：考虑未来服务拆分
3. **多租户支持**：预留多租户架构
4. **国际化支持**：预留国际化字段

## 8. 数据迁移策略

1. **版本控制**：使用数据库版本控制工具
2. **备份策略**：定期备份数据库
3. **恢复方案**：制定数据恢复方案

## 9. 总结

本数据库设计方案支持择明论坛的核心功能，包括用户管理、帖子发布与审核、评论互动、分类管理等。设计考虑了数据安全性、性能优化和扩展性，为论坛的稳定运行和未来发展提供了坚实的基础。