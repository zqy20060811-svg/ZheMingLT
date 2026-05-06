# 择明论坛 (ZheMing Forum)

一个基于 Spring Boot + Vue3 + 微信小程序的现代化论坛系统，集成 DeepSeek AI 智能助手。

## ✨ 核心特性

- 🤖 **AI 智能助手** - 集成 DeepSeek API，提供智能回复建议、内容总结、标签推荐、敏感内容检测
- 🎨 **庄园风格主题** - 优雅的庄园风格 UI 设计，支持日间/夜间模式
- 📱 **多端支持** - Web 端 + 微信小程序，随时随地参与讨论
- 🔐 **安全可靠** - JWT 认证 + SHA-256 加盐密码加密
- 🚀 **高性能** - Redis 缓存 + 数据库优化
- 📊 **实时通知** - 消息推送系统，不错过任何互动

## 🛠️ 技术栈

### 后端
| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.0 | 核心框架 |
| Spring Security | 6.x | 安全认证 |
| JPA/Hibernate | 6.3.x | ORM 框架 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 7.0+ | 缓存存储 |
| JWT | 0.12.x | 身份认证 |
| DeepSeek API | - | AI 智能服务 |
| 阿里云 OSS | - | 文件存储 |

### 前端
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.4.x | 渐进式框架 |
| Vite | 5.x | 构建工具 |
| Vue Router | 4.x | 路由管理 |
| Bootstrap Icons | 1.11.x | 图标库 |
| 微信小程序 | - | 移动端 |

## 🚀 快速开始

### 环境要求
- JDK 21+
- MySQL 8.0+
- Redis 7.0+
- Maven 3.8+
- Node.js 18+

### 1. 克隆项目
```bash
git clone https://github.com/zqy20060811-svg/ZheMingLT.git
cd ZheMingLT
```

### 2. 数据库配置
```sql
CREATE DATABASE zheminglt CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 后端启动
```bash
cd backend

# 配置数据库（编辑 src/main/resources/application.properties）
spring.datasource.url=jdbc:mysql://localhost:3306/zheminglt
spring.datasource.username=root
spring.datasource.password=your_password

# 配置 DeepSeek API（可选）
deepseek.api.key=your_deepseek_api_key

# 启动服务
mvn spring-boot:run
```

### 4. 前端启动
```bash
cd frontend/web
npm install
npm run dev
```

访问 http://localhost:5173

## 📁 项目结构

```
zhemingluntan/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com/zheminglt/
│   │       ├── config/         # 配置类
│   │       ├── controller/     # API 控制器
│   │       ├── service/        # 服务接口
│   │       ├── impl/           # 服务实现
│   │       ├── model/          # 实体类
│   │       ├── dto/            # 数据传输对象
│   │       ├── vo/             # 视图对象
│   │       ├── mapper/         # 数据访问层
│   │       ├── utils/          # 工具类
│   │       └── constant/       # 常量定义
│   └── src/main/resources/
│       └── application.properties
├── frontend/
│   ├── web/                    # Vue3 Web 前端
│   │   ├── src/
│   │   │   ├── views/          # 页面组件
│   │   │   ├── components/     # 公共组件
│   │   │   ├── services/       # API 服务
│   │   │   └── router/         # 路由配置
│   │   └── package.json
│   └── wechat/                 # 微信小程序
├── docs/                       # 项目文档
│   ├── API.md                  # API 接口文档
│   ├── DATABASE.md             # 数据库设计
│   └── sql/                    # SQL 脚本
└── README.md
```

## 🔌 API 接口

### 用户模块
- `POST /api/users/register` - 用户注册
- `POST /api/users/login` - 用户登录
- `GET /api/users/profile` - 获取用户信息
- `PUT /api/users/profile` - 更新用户信息

### 帖子模块
- `GET /api/posts` - 帖子列表
- `POST /api/posts` - 发布帖子
- `GET /api/posts/{id}` - 帖子详情
- `PUT /api/posts/{id}` - 编辑帖子
- `DELETE /api/posts/{id}` - 删除帖子

### AI 模块
- `POST /api/ai/reply-suggestions` - 智能回复建议
- `POST /api/ai/summarize` - 内容总结
- `POST /api/ai/recommend-tags` - 标签推荐
- `POST /api/ai/check-sensitive` - 敏感内容检测

更多接口详见 [docs/API.md](docs/API.md)

## 🤖 AI 功能

### 智能回复建议
根据帖子内容自动生成 3 条回复建议，帮助用户快速参与讨论。

### 内容总结
自动总结长帖子内容，方便用户快速了解核心要点。

### 标签推荐
根据帖子标题和内容智能推荐相关标签。

### 敏感内容检测
使用 AI 检测违规内容，维护社区健康环境。

## 📝 文档

- [API 接口文档](docs/API.md)
- [数据库设计](docs/DATABASE.md)
- [项目结构说明](docs/PROJECT_STRUCTURE.md)

## 🔒 安全配置

- 密码使用 SHA-256 + 随机盐加密存储
- JWT Token 认证机制
- API 接口权限控制
- 敏感信息使用环境变量配置

## 🌟 特色功能

- **庄园风格主题** - 优雅的视觉设计，支持日间/夜间切换
- **实时通知** - 点赞、评论、关注等消息实时推送
- **热门帖子** - 基于浏览量和互动数的热门算法
- **用户等级** - 积分系统激励用户参与
- **内容搜索** - 支持关键词搜索帖子

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📧 联系

如有问题，请通过 GitHub Issues 联系。

---

Made with ❤️ by 择明团队
