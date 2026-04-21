# 择明论坛项目结构说明

## 项目概述

择明论坛是一个基于 Spring Boot 的论坛系统，包含 Web 端和微信小程序端。

## 目录结构

```
zhemingluntan/
├── docs/                           # 项目文档
│   ├── API.md                      # 接口文档
│   ├── DATABASE.md                 # 数据库设计文档
│   ├── REDIS.md                    # Redis设计文档
│   └── PROJECT_STRUCTURE.md        # 项目结构说明（本文件）
│
├── ZheMingLT/                      # 后端服务（Spring Boot）
│   ├── src/main/java/com/zheminglt/
│   │   ├── config/                 # 配置类
│   │   │   ├── OpenApiConfig.java      # Swagger/OpenAPI 配置
│   │   │   ├── SecurityConfig.java     # Spring Security 配置
│   │   │   ├── WebConfig.java          # Web 配置（拦截器）
│   │   │   └── redisconfig.java        # Redis 配置
│   │   │
│   │   ├── constant/               # 常量类
│   │   │   ├── BusinessConstant.java   # 业务常量
│   │   │   ├── ErrorCodeConstant.java  # 错误码常量
│   │   │   └── MessageConstant.java    # 消息提示常量
│   │   │
│   │   ├── controller/             # 控制器层（API 入口）
│   │   │   ├── AIController.java       # AI 服务接口
│   │   │   ├── CategoryController.java # 分类管理接口
│   │   │   ├── CommentController.java  # 评论管理接口
│   │   │   ├── InteractionController.java # 互动接口（点赞/收藏）
│   │   │   ├── PostController.java     # 帖子管理接口
│   │   │   └── UserController.java     # 用户管理接口
│   │   │
│   │   ├── dto/                    # 数据传输对象
│   │   │   ├── CommentDTO.java         # 评论传输对象
│   │   │   ├── LoginDTO.java           # 登录传输对象
│   │   │   ├── postdto.java            # 帖子传输对象
│   │   │   └── UserDTO.java            # 用户传输对象
│   │   │
│   │   ├── impl/                   # 服务实现类
│   │   │   ├── AIServiceImpl.java      # AI 服务实现
│   │   │   ├── CategoryServiceImpl.java # 分类服务实现
│   │   │   ├── CollectionServiceImpl.java # 收藏服务实现
│   │   │   ├── CommentServiceImpl.java  # 评论服务实现
│   │   │   ├── InteractionServiceImpl.java # 互动服务实现
│   │   │   ├── LikeServiceImpl.java     # 点赞服务实现
│   │   │   ├── PostServiceImpl.java     # 帖子服务实现
│   │   │   ├── TokenServiceImpl.java    # Token 服务实现
│   │   │   └── UserServiceImpl.java     # 用户服务实现
│   │   │
│   │   ├── interceptor/            # 拦截器
│   │   │   └── JWTInterceptor.java     # JWT 认证拦截器
│   │   │
│   │   ├── mapper/                 # 数据访问层（JPA Repository）
│   │   │   ├── CategoryMapper.java     # 分类数据访问
│   │   │   ├── CollectionMapper.java   # 收藏数据访问
│   │   │   ├── CommentMapper.java      # 评论数据访问
│   │   │   ├── LikeMapper.java         # 点赞数据访问
│   │   │   ├── PostMapper.java         # 帖子数据访问
│   │   │   └── UserMapper.java         # 用户数据访问
│   │   │
│   │   ├── model/                  # 实体类（数据库映射）
│   │   │   ├── Category.java           # 分类实体
│   │   │   ├── Collection.java         # 收藏实体
│   │   │   ├── Comment.java            # 评论实体
│   │   │   ├── Like.java               # 点赞实体
│   │   │   ├── Post.java               # 帖子实体
│   │   │   └── User.java               # 用户实体
│   │   │
│   │   ├── service/                # 服务接口
│   │   │   ├── AIService.java          # AI 服务接口
│   │   │   ├── CategoryService.java    # 分类服务接口
│   │   │   ├── CollectionService.java  # 收藏服务接口
│   │   │   ├── CommentService.java     # 评论服务接口
│   │   │   ├── InteractionService.java # 互动服务接口
│   │   │   ├── LikeService.java        # 点赞服务接口
│   │   │   ├── PostService.java        # 帖子服务接口
│   │   │   ├── TokenService.java       # Token 服务接口
│   │   │   └── UserService.java        # 用户服务接口
│   │   │
│   │   ├── utils/                  # 工具类
│   │   │   └── JWTUtil.java            # JWT 工具类
│   │   │
│   │   ├── vo/                     # 视图对象（响应数据）
│   │   │   ├── CategoryVO.java         # 分类视图对象
│   │   │   ├── CommentVO.java          # 评论视图对象
│   │   │   ├── LoginVO.java            # 登录响应对象
│   │   │   ├── PostVO.java             # 帖子视图对象
│   │   │   ├── ResponseVO.java         # 通用响应对象
│   │   │   └── UserVO.java             # 用户视图对象
│   │   │
│   │   └── ZhemingLTApplication.java   # 启动类
│   │
│   ├── src/main/resources/
│   │   ├── static/                 # 静态资源
│   │   │   ├── create-post.html        # 创建帖子页面
│   │   │   ├── index.html              # 首页
│   │   │   ├── login.html              # 登录页面
│   │   │   ├── post-detail.html        # 帖子详情页
│   │   │   └── register.html           # 注册页面
│   │   │
│   │   └── application.properties.template  # 配置文件模板
│   │
│   └── pom.xml                     # Maven 依赖配置
│
├── Wen-WeiXing/                    # 微信小程序端
│   ├── pages/                      # 页面目录
│   │   ├── create-post/            # 创建帖子页
│   │   ├── index/                  # 首页
│   │   ├── login/                  # 登录页
│   │   ├── post-detail/            # 帖子详情页
│   │   └── register/               # 注册页
│   │
│   ├── app.js                      # 小程序入口
│   ├── app.json                    # 小程序配置
│   ├── app.wxss                    # 全局样式
│   ├── project.config.json         # 项目配置
│   └── sitemap.json                # 站点地图
│
├── Web/                            # Web 前端页面（备用）
│   ├── create-post.html
│   ├── index.html
│   ├── login.html
│   ├── post-detail.html
│   └── register.html
│
├── Mysql/                          # 数据库脚本
│   └── create_tables.sql           # 建表语句
│
├── Notes/                          # 开发笔记
│   ├── icon_guide.md               # 图标指南
│   └── project_notes.md            # 项目笔记
│
├── .trae/rules/                    # Trae AI 规则
│   └── karpathy-guidelines-zh.md   # 编码规范
│
├── README.md                       # 项目主文档
└── .gitignore                      # Git 忽略配置
```

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.0
- **数据库**: MySQL 8.0 + JPA/Hibernate
- **缓存**: Redis（待完善）
- **安全**: Spring Security + JWT
- **文档**: SpringDoc OpenAPI (Swagger)
- **构建**: Maven

### 前端
- **小程序**: 微信小程序原生开发
- **Web**: HTML5 + CSS3 + JavaScript

## 核心功能模块

### 1. 用户模块
- 用户注册/登录/退出
- JWT Token 认证
- 用户信息管理

### 2. 帖子模块
- 帖子发布/编辑/删除
- 帖子列表/详情
- 热门帖子

### 3. 评论模块
- 评论发表/回复
- 评论列表
- 评论管理

### 4. 分类模块
- 分类管理
- 按分类筛选帖子

### 5. 互动模块
- 点赞/取消点赞
- 收藏/取消收藏
- 浏览量统计

### 6. AI 模块
- 内容摘要生成
- 内容审核

## 开发规范

1. **包命名**: `com.zheminglt.{layer}`
2. **类命名**: 大驼峰，如 `UserServiceImpl`
3. **方法命名**: 小驼峰，如 `getUserById`
4. **常量**: 全大写下划线，如 `TOKEN_EXPIRE_TIME`
5. **数据库**: 表名小写下划线，如 `user_likes`

## 配置文件

### application.properties 模板

```properties
# 服务器配置
server.port=8080
server.servlet.context-path=/api

# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/zheminglt?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA配置
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Redis配置（待完善）
# spring.redis.host=localhost
# spring.redis.port=6379

# JWT配置
jwt.secret=your_secret_key
jwt.expiration=604800000
```

## 启动步骤

1. 创建 MySQL 数据库 `zheminglt`
2. 复制 `application.properties.template` 为 `application.properties`
3. 修改数据库连接配置
4. 运行 `ZhemingLTApplication`
5. 访问 Swagger UI: `http://localhost:8080/api/swagger-ui/index.html`
