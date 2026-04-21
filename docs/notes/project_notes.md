# 择明论坛项目注意事项

## 后端开发

### 技术栈
- Java 21
- Spring Boot 2.7+
- MySQL 8.0+
- Redis 5.0+
- DeepSeek API

### 环境配置
1. **Java环境**：确保安装JDK 21
2. **Maven**：使用 `D:\develop\maven\apache-maven-3.9.14\bin\mvn.cmd` 命令
3. **MySQL**：启动服务并创建 `zheminglt` 数据库
4. **Redis**：启动服务，使用默认端口 6379，数据库 1

### 配置文件
- 配置文件路径：`ZheMingLT/src/main/resources/application.properties`
- 主要配置项：
  - 数据库连接信息
  - Redis连接信息
  - DeepSeek API密钥
  - 线程池配置

### 启动命令
```bash
D:\develop\maven\apache-maven-3.9.14\bin\mvn.cmd spring-boot:run
```

## 前端开发

### 浏览器端
- 访问地址：`http://localhost:8080/login`
- 技术栈：HTML5 + CSS3 + JavaScript + Bootstrap
- 主要页面：
  - `login.html`：登录页面
  - `register.html`：注册页面
  - `index.html`：首页（帖子列表）
  - `post-detail.html`：帖子详情页
  - `create-post.html`：发布帖子页面

### 微信小程序端
- 技术栈：微信小程序原生框架
- 主要页面：
  - `pages/login/login.wxml`：登录页面
  - `pages/register/register.wxml`：注册页面
  - `pages/index/index.wxml`：首页（帖子列表）
  - `pages/post-detail/post-detail.wxml`：帖子详情页
  - `pages/create-post/create-post.wxml`：发布帖子页面

### 图标配置
- 参考 `Notes/icon_guide.md` 文件

## 数据库

### 数据库结构
- 数据库名称：`zheminglt`
- 主要表：
  - `users`：用户表
  - `categories`：分类表
  - `posts`：帖子表
  - `comments`：评论表
  - `likes`：点赞表
  - `collections`：收藏表

### 建表语句
- 建表脚本：`Mysql/create_tables.sql`
- 执行方法：在MySQL客户端中执行该脚本

## API接口

### 基础URL
- `http://localhost:8080/api`

### 认证方式
- 使用JWT令牌
- 请求头：`Authorization: Bearer {token}`

### 主要接口
- 用户模块：`/users/*`
- 分类模块：`/categories/*`
- 帖子模块：`/posts/*`
- 评论模块：`/comments/*`
- 互动模块：`/likes/*`, `/collections/*`
- AI模块：`/ai/*`

### 接口文档
- 文档路径：`Api-Documentation/api_documentation.md`

## DeepSeek API集成

### 功能
- 帖子内容审核
- 帖子摘要生成

### 配置
- API密钥：在 `application.properties` 中配置
- API地址：`https://api.deepseek.com/v1/chat/completions`

## 跨域问题

### 解决方案
- 在后端添加CORS配置
- 使用 `@CrossOrigin` 注解或配置CORS过滤器

## 性能优化

### 后端优化
- 使用Redis缓存热门帖子
- 实现线程池处理高并发
- 优化数据库查询

### 前端优化
- 图片懒加载
- 页面缓存
- 减少API调用次数

## 安全考虑

### 后端安全
- 密码加密存储
- SQL注入防护
- XSS防护
- CSRF防护
- 权限控制

### 前端安全
- 输入验证
- 避免敏感信息泄露
- 使用HTTPS

## 测试

### 测试方法
- 单元测试
- 集成测试
- 端到端测试

### 测试工具
- JUnit（后端）
- Postman（API测试）
- 微信开发者工具（小程序测试）

## 部署

### 开发环境
- 本地部署：直接运行Spring Boot应用
- 前端：直接打开HTML文件或在微信开发者工具中运行

### 生产环境
- 后端：部署到Tomcat或使用Docker
- 前端：部署到Nginx
- 数据库：使用生产环境MySQL
- Redis：使用生产环境Redis

## 常见问题

### 后端启动失败
- 检查数据库连接
- 检查Redis连接
- 检查端口占用

### 前端API调用失败
- 检查后端服务是否启动
- 检查API地址是否正确
- 检查认证令牌是否有效

### 微信小程序错误
- 检查网络连接
- 检查AppID配置
- 检查API调用权限

## 后续开发计划

1. **功能完善**：
   - 完善用户中心功能
   - 添加消息通知系统
   - 实现搜索功能

2. **性能优化**：
   - 数据库索引优化
   - 缓存策略优化
   - 代码重构

3. **安全加固**：
   - 完善权限系统
   - 添加日志记录
   - 实现监控系统

4. **扩展功能**：
   - 添加文件上传功能
   - 实现消息推送
   - 集成第三方登录