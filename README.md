# 择明论坛

基于 Spring Boot + Vue3 的社区论坛系统，核心在于双 Token 认证与 Redis 缓存架构的实践。

---

## 核心架构

```
Controller → Service → Mapper → MySQL
                ↓
            Redis (Token / 缓存 / 排行榜)
```

- **Controller**：接收请求、参数校验、返回响应
- **Service**：业务逻辑，如认证、热度计算、缓存同步
- **Mapper**：Spring Data JPA 持久化
- **Redis**：Token 存储、热点数据加速、分布式计数

---

## 双 Token 认证体系

| Token 类型 | 有效期 | 用途 |
|---|---|---|
| AccessToken | 15 分钟 | 日常接口鉴权 |
| RefreshToken | 7 天 | 换取新的双 Token |

**设计思路**：AccessToken 短效降低泄露风险；RefreshToken 专用于刷新接口，且一次性使用（旧 Token 立即拉黑），防止重放攻击。用户无感知续登，体验流畅。

**刷新流程**：
```
请求接口 → AccessToken 过期(401001)
    → 携带 RefreshToken 请求 /users/refresh
        → 验证有效 → 生成新双 Token
            → 旧 RefreshToken 加入 Redis 黑名单
                → 返回新 Token → 前端重试原请求
```

**Token 黑名单**：登出或刷新时旧 Token 写入 Redis，键为 `token:blacklist:{token}`，有效期与 Token 剩余时间一致。JWT 拦截器先查黑名单再验签。

---

## 技术栈

| 后端 | 版本 | 说明 |
|---|---|---|
| Spring Boot | 3.2.0 | 核心框架 |
| Spring Security | 6.x | 安全认证 |
| Spring Data JPA | 6.3.x | ORM |
| MySQL | 8.0+ | 数据持久化 |
| Redis | 7.0+ | 缓存 / Token / 排行榜 |
| JWT | 0.12.x | Token 生成与解析 |

| 前端 | 版本 |
|---|---|
| Vue 3 | 3.4.x |
| Vue Router | 4.x |
| Bootstrap 5 | 5.3.x |

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
CREATE DATABASE zhemingluntan CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

修改 `backend/src/main/resources/application.properties`：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/zhemingluntan?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=你的密码

spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.database=2
spring.data.redis.password=你的密码
```

### 3. 启动后端
```bash
cd backend
mvn spring-boot:run
```

### 4. 启动前端
前端为纯静态页面，直接用浏览器打开 `frontend/web/index.html`，或部署到 Web 服务器。

---

## 项目结构

```
zhemingluntan/
├── backend/
│   └── src/main/java/com/zheminglt/
│       ├── config/         # 配置类
│       ├── constant/       # 常量定义
│       ├── controller/     # API 控制器
│       ├── service/        # 服务接口
│       ├── impl/           # 服务实现
│       ├── mapper/         # 数据访问层
│       ├── model/          # 实体类
│       ├── dto/            # 数据传输对象
│       ├── vo/             # 视图对象
│       ├── utils/          # 工具类
│       └── interceptor/    # JWT 拦截器
├── frontend/
│   └── web/
│       ├── index.html
│       └── app.js
└── README.md
```

---

## 核心功能

### 用户模块
- 注册 / 登录 / 登出
- 双 Token 认证与自动刷新
- Token 黑名单机制
- 头像上传（阿里云 OSS）

### 帖子模块
- 发布、编辑、删除
- 浏览量统计（Redis 缓存 + 定时同步 MySQL）
- 热门帖子排行榜（浏览量、点赞数、评论数加权，加入时间衰减算法）

### 互动模块
- 点赞 / 收藏（Redis Set 去重 + String 计数，定时落库）
- 评论发布、删除（支持楼中楼）
- 关注 / 粉丝

### 通知模块
- 评论、点赞、关注等消息通知
- 未读消息计数

---

## Redis 应用场景

| 场景 | 数据结构 | 说明 |
|---|---|---|
| Token 存储 | String | 用户登录态 |
| Token 黑名单 | String | 失效 Token 拦截 |
| 帖子浏览量 | String | 高频计数，定时同步 MySQL |
| 点赞 / 收藏 | Set / String | 用户去重 + 计数 |
| 热门帖子 | ZSet | 按热度排序，支持日/周/月/总榜 |
| 用户帖子列表 | String | 缓存用户发帖列表 |

---

## 安全设计

- **密码加密**：SHA-256 + 随机盐，防彩虹表攻击
- **JWT 签名**：AccessToken 与 RefreshToken 使用不同密钥，隔离风险
- **API 鉴权**：Spring Security 配置化权限控制 + JWT 拦截器实现无状态认证
- **CORS**：精细化跨域配置

---

## 数据库设计要点

- **user**：用户名、密码、邮箱、手机号、角色、状态；邮箱和手机号支持唯一索引但允许为空
- **post**：关联用户和分类，支持浏览量、点赞数、评论数统计
- **comment**：`parent_id` 自关联实现楼中楼
- **like / collection / follow**：记录用户互动关系

---

## 后续优化方向

1. **Spring Cache 注解**：简化缓存代码
2. **消息队列**：RabbitMQ / Kafka 削峰填谷，处理高频写操作
3. **分库分表**：按用户 ID 或时间维度拆分帖子表、评论表
4. **Elasticsearch**：替代 MySQL LIKE 模糊查询，提升搜索性能
5. **容器化**：Docker + docker-compose 一键部署