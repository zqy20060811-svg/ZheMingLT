# 择明论坛 (ZheMing Forum)

基于 Spring Boot + Vue3 构建的社区论坛系统，核心亮点在于**双 Token 认证体系**与**Redis 缓存驱动的高性能交互**。

---

## 核心架构设计

本项目采用经典的分层架构，整体结构清晰，职责边界明确：

```
Controller → Service → Mapper → MySQL
                ↓
            Redis (缓存 / Token / 排行榜)
```

- **Controller 层**：负责接收 HTTP 请求、参数校验、组装响应，不做业务逻辑。
- **Service 层**：承载核心业务逻辑，如用户认证、帖子热度计算、点赞缓存同步等。
- **Mapper 层**：基于 Spring Data JPA 实现数据持久化，简化 CRUD 操作。
- **Redis 层**：作为高性能缓存中间件，承担 Token 存储、热点数据加速、分布式计数等职责。

---

## 登录与 Token 管理（面试重点）

### 双 Token 机制

系统采用 **AccessToken + RefreshToken** 的双 Token 策略，兼顾安全性与用户体验：

| Token 类型 | 有效期 | 用途 | 存储位置 |
|---|---|---|---|
| AccessToken | 15 分钟 | 日常接口鉴权 | Redis + 前端内存 |
| RefreshToken | 7 天 | 换取新的双 Token | Redis + 前端 localStorage |

**为什么用双 Token？**

- AccessToken 有效期短，即便泄露，攻击者可用时间窗口极小。
- RefreshToken 有效期长，但仅用于 `/users/refresh` 接口，且**只能使用一次**（使用后旧 RefreshToken 立即加入黑名单），防止重放攻击。
- 用户在使用过程中，AccessToken 过期时前端自动调用刷新接口，用户**无感知续登**，体验流畅。

### Token 刷新流程

```
前端请求接口 → AccessToken 过期（返回 401001）
    → 前端携带 RefreshToken 请求 /users/refresh
        → 后端验证 RefreshToken 有效性
            → 生成新的 AccessToken + RefreshToken
                → 旧 RefreshToken 加入黑名单（Redis）
                    → 返回新 Token 给前端
                        → 前端重试原请求
```

### Token 黑名单

登出或刷新 Token 时，旧 Token 会被写入 Redis 黑名单，键值格式为 `token:blacklist:{token}`，有效期与 Token 剩余过期时间一致。JWT 拦截器每次校验时，先查黑名单再验签，确保失效 Token 无法继续使用。

---

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|---|---|---|
| Spring Boot | 3.2.0 | 核心框架，自动配置，快速开发 |
| Spring Security | 6.x | 安全框架，配置化权限控制 |
| Spring Data JPA | 6.3.x | ORM 框架，简化数据库操作 |
| MySQL | 8.0+ | 关系型数据库，持久化存储 |
| Redis | 7.0+ | 缓存、Token 存储、排行榜、计数器 |
| JWT | 0.12.x | Token 生成与解析 |
| 阿里云 OSS | - | 头像等静态资源存储 |

### 前端

| 技术 | 版本 | 说明 |
|---|---|---|
| Vue 3 | 3.4.x | 渐进式框架，组合式 API |
| Vue Router | 4.x | 前端路由管理 |
| Bootstrap 5 | 5.3.x | UI 组件库 |

---

## 快速启动

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 7.0+
- Maven 3.8+

### 1. 克隆项目

```bash
git clone https://github.com/zqy20060811-svg/ZheMingLT.git
cd ZheMingLT
```

### 2. 数据库配置

```sql
CREATE DATABASE zheminglt CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

然后修改 `backend/src/main/resources/application.properties` 中的数据库连接信息。

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端服务默认运行在 `http://localhost:8080`。

### 4. 启动前端

前端为纯静态页面，直接用浏览器打开 `frontend/web/index.html` 即可，或部署到任意 Web 服务器。

---

## 项目结构

```
zhemingluntan/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/zheminglt/
│   │   ├── config/             # 配置类（Security、Web、Redis 等）
│   │   ├── constant/           # 常量定义（业务、错误码、Redis Key）
│   │   ├── controller/         # API 控制器
│   │   ├── service/            # 服务接口
│   │   ├── impl/               # 服务实现
│   │   ├── mapper/             # 数据访问层（JPA）
│   │   ├── model/              # 实体类
│   │   ├── dto/                # 数据传输对象
│   │   ├── vo/                 # 视图对象
│   │   ├── utils/              # 工具类（JWT、密码加密）
│   │   └── interceptor/        # JWT 拦截器
│   └── src/main/resources/
│       └── application.properties
├── frontend/
│   └── web/                    # Vue3 前端（单页应用）
│       ├── index.html
│       └── app.js
└── README.md
```

---

## 核心功能模块

### 用户模块

- 用户注册 / 登录 / 登出
- 双 Token 认证与自动刷新
- Token 黑名单机制
- 用户信息管理、头像上传

### 帖子模块

- 帖子发布、编辑、删除
- 浏览量统计（Redis 缓存 + 定时同步 MySQL）
- 热门帖子排行榜（基于浏览量、点赞数、评论数，加入时间衰减算法）

### 互动模块

- 点赞 / 收藏（Redis 缓存计数，定时落库）
- 评论发布、删除
- 关注 / 粉丝

### 通知模块

- 评论、点赞、关注等消息通知
- 未读消息计数轮询

---

## 缓存策略

系统大量使用 Redis 提升性能，主要应用场景包括：

| 场景 | Redis 数据结构 | 说明 |
|---|---|---|
| Token 存储 | String | 用户登录态管理 |
| Token 黑名单 | String | 失效 Token 拦截 |
| 帖子浏览量 | String | 高频计数，定时同步 MySQL |
| 点赞 / 收藏 | Set / String | 用户去重 + 计数 |
| 热门帖子 | ZSet | 按热度排序，支持日/周/月/总榜 |
| 用户帖子列表 | String | 缓存用户发帖列表 |

---

## 安全设计

- **密码加密**：采用 SHA-256 + 随机盐，防止彩虹表攻击。
- **JWT 签名**：AccessToken 与 RefreshToken 使用不同密钥签名，隔离风险。
- **API 鉴权**：Spring Security 配置化权限控制，配合 JWT 拦截器实现无状态认证。
- **CORS 配置**：精细化跨域控制，防止 CSRF 攻击。

---

## 数据库设计要点

- 用户表 `user`：包含用户名、密码、邮箱、手机号、角色、状态等字段，邮箱和手机号支持唯一索引但允许为空。
- 帖子表 `post`：关联用户和分类，支持浏览量、点赞数、评论数统计。
- 评论表 `comment`：支持楼中楼回复，通过 `parent_id` 自关联。
- 点赞表 `like`、收藏表 `collection`、关注表 `follow`：记录用户互动关系。

---

## 后续优化建议

1. **引入 Spring Cache 注解**：进一步简化缓存代码，减少模板化操作。
2. **消息队列**：点赞、评论等高频写操作可接入 RabbitMQ / Kafka，削峰填谷。
3. **分库分表**：用户量增长后，帖子表、评论表可按用户 ID 或时间维度拆分。
4. **全文搜索**：集成 Elasticsearch，替代 MySQL LIKE 模糊查询，提升搜索性能。
5. **容器化部署**：编写 Dockerfile 与 docker-compose.yml，实现一键部署。

---

## 许可证

MIT License

---

Made with ❤️ by 择明团队