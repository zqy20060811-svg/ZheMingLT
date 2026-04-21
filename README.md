# 择明论坛 (ZheMingLT)

一个基于 Spring Boot + Vue3 + 微信小程序的现代化论坛系统。

## 项目简介

择明论坛是一个功能完善的社区论坛平台，支持 Web 端和微信小程序端，提供帖子发布、评论互动、点赞收藏等核心功能。

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.0
- **数据库**: MySQL 8.0 + JPA/Hibernate
- **缓存**: Redis（计划中）
- **安全**: Spring Security + JWT
- **文档**: SpringDoc OpenAPI (Swagger)
- **构建**: Maven

### 前端
- **Web**: HTML5 + CSS3 + JavaScript
- **小程序**: 微信小程序原生开发

## 快速开始

### 环境要求
- JDK 21+
- MySQL 8.0+
- Maven 3.8+
- Redis 7.0+（可选，当前为内存实现）

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/zqy20060811-svg/ZheMingLT.git
cd ZheMingLT
```

2. **创建数据库**
```sql
CREATE DATABASE zheminglt CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. **配置应用**
```bash
cd ZheMingLT/src/main/resources
cp application.properties.template application.properties
# 编辑 application.properties，修改数据库连接信息
```

4. **启动服务**
```bash
cd ZheMingLT
mvn spring-boot:run
```

5. **访问服务**
- API 文档: http://localhost:8080/api/swagger-ui/index.html
- 首页: http://localhost:8080/api/index.html

## 项目文档

| 文档 | 说明 |
|------|------|
| [API 文档](docs/API.md) | 完整的接口文档，包含请求参数和响应示例 |
| [数据库设计](docs/DATABASE.md) | 数据库表结构、字段说明和关系图 |
| [Redis 设计](docs/REDIS.md) | Redis 数据结构和缓存策略 |
| [项目结构](docs/PROJECT_STRUCTURE.md) | 项目目录结构和代码规范 |

## 功能特性

### 已实现
- [x] 用户注册/登录/退出
- [x] JWT Token 认证
- [x] 帖子发布/编辑/删除
- [x] 评论发表/回复
- [x] 分类管理
- [x] 点赞/收藏功能
- [x] Swagger API 文档
- [x] 常量管理

### 进行中
- [ ] Redis 缓存集成
- [ ] AI 内容审核
- [ ] 热门帖子算法
- [ ] 消息通知

### 计划中
- [ ] 图片上传
- [ ] 搜索功能
- [ ] 用户关注
- [ ] 私信功能

## 项目结构

```
zhemingluntan/
├── docs/                   # 项目文档
│   ├── API.md             # 接口文档
│   ├── DATABASE.md        # 数据库设计
│   ├── REDIS.md           # Redis设计
│   ├── PROJECT_STRUCTURE.md # 项目结构
│   ├── sql/               # SQL脚本
│   ├── notes/             # 开发笔记
│   └── archive/           # 归档文档
│
├── ZheMingLT/             # 后端服务
│   ├── src/main/java/     # Java源码
│   ├── src/main/resources/# 配置文件
│   └── pom.xml            # Maven配置
│
├── Wen-WeiXing/           # 微信小程序
├── Web/                   # Web前端页面
└── README.md              # 本文件
```

## API 概览

### 用户模块
- `POST /users/register` - 用户注册
- `POST /users/login` - 用户登录
- `POST /users/logout` - 用户退出
- `GET /users/{id}` - 获取用户信息

### 帖子模块
- `GET /posts` - 获取帖子列表
- `GET /posts/hot` - 获取热门帖子
- `GET /posts/{id}` - 获取帖子详情
- `POST /posts` - 发布帖子
- `PUT /posts/{id}` - 更新帖子
- `DELETE /posts/{id}` - 删除帖子

### 评论模块
- `GET /comments/post/{postId}` - 获取帖子评论
- `POST /comments` - 发表评论
- `PUT /comments/{id}` - 更新评论
- `DELETE /comments/{id}` - 删除评论

更多接口详见 [API 文档](docs/API.md)

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

[MIT](LICENSE)

## 联系方式

- 项目地址: https://github.com/zqy20060811-svg/ZheMingLT
- 问题反馈: https://github.com/zqy20060811-svg/ZheMingLT/issues

---

**择明论坛** - 让交流更有价值
