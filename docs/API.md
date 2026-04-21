# 择明论坛 API 接口文档

## 基础信息

- **Base URL**: `http://localhost:8080/api`
- **Swagger UI**: `http://localhost:8080/api/swagger-ui/index.html`
- **认证方式**: Bearer Token (JWT)

## 认证说明

所有需要登录的接口需要在请求头中添加：

```
Authorization: Bearer {token}
```

## 接口列表

### 1. 用户管理

#### 1.1 用户注册
- **URL**: `POST /users/register`
- **描述**: 新用户注册
- **无需认证**
- **请求参数**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名，唯一 |
| password | string | 是 | 密码 |
| email | string | 否 | 邮箱，唯一 |
| phone | string | 否 | 手机号，唯一 |
| avatar | string | 否 | 头像URL |

- **响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "phone": "13800138000",
    "avatar": null,
    "role": 0,
    "status": 1,
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

#### 1.2 用户登录
- **URL**: `POST /users/login`
- **描述**: 用户登录获取 token
- **无需认证**
- **请求参数**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |

- **响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "user": {
      "id": 1,
      "username": "testuser",
      "email": "test@example.com",
      "role": 0
    }
  }
}
```

#### 1.3 用户退出
- **URL**: `POST /users/logout`
- **描述**: 退出登录，token加入黑名单
- **需要认证**
- **响应示例**:
```json
{
  "code": 200,
  "message": "退出登录成功",
  "data": null
}
```

#### 1.4 获取当前用户信息
- **URL**: `GET /users/profile`
- **描述**: 获取当前登录用户信息
- **需要认证**
- **响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "phone": "13800138000",
    "avatar": "https://example.com/avatar.png",
    "bio": "个人简介",
    "role": 0,
    "status": 1,
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

#### 1.5 获取用户统计
- **URL**: `GET /users/stats`
- **描述**: 获取当前用户统计数据
- **需要认证**
- **响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "posts": 10,
    "likes": 50,
    "following": 20,
    "followers": 30
  }
}
```

#### 1.6 更新用户信息
- **URL**: `PUT /users/{id}`
- **描述**: 更新用户信息
- **需要认证**
- **请求参数**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 否 | 用户名 |
| email | string | 否 | 邮箱 |
| phone | string | 否 | 手机号 |
| avatar | string | 否 | 头像URL |
| bio | string | 否 | 个人简介 |

---

### 2. 帖子管理

#### 2.1 获取帖子列表
- **URL**: `GET /posts`
- **描述**: 分页获取帖子列表
- **无需认证**
- **请求参数**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认0 |
| size | int | 否 | 每页数量，默认10 |
| categoryId | int | 否 | 分类ID筛选 |
| sort | string | 否 | 排序方式：newest/hot/best |

- **响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "帖子标题",
        "summary": "帖子摘要",
        "content": "帖子内容",
        "authorId": 1,
        "authorName": "用户名",
        "authorAvatar": "头像URL",
        "categoryId": 1,
        "categoryName": "分类名",
        "viewCount": 100,
        "likeCount": 50,
        "commentCount": 20,
        "isLiked": false,
        "isCollected": false,
        "createdAt": "2024-01-01T00:00:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "number": 0,
    "size": 10,
    "first": true,
    "last": false
  }
}
```

#### 2.2 获取热门帖子
- **URL**: `GET /posts/hot`
- **描述**: 获取热门帖子排行
- **无需认证**
- **请求参数**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| period | string | 否 | 时间范围：day/week/month/all |
| size | int | 否 | 数量，默认50 |

- **响应示例**: 同帖子列表

#### 2.3 获取帖子详情
- **URL**: `GET /posts/{id}`
- **描述**: 获取单个帖子详情
- **无需认证**
- **响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "title": "帖子标题",
    "content": "帖子内容",
    "authorId": 1,
    "authorName": "用户名",
    "authorAvatar": "头像URL",
    "categoryId": 1,
    "categoryName": "分类名",
    "viewCount": 100,
    "likeCount": 50,
    "commentCount": 20,
    "isLiked": false,
    "isCollected": false,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
}
```

#### 2.4 创建帖子
- **URL**: `POST /posts`
- **描述**: 创建新帖子
- **需要认证**
- **请求参数**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string | 是 | 标题 |
| content | string | 是 | 内容 |
| categoryId | int | 是 | 分类ID |
| summary | string | 否 | 摘要 |

- **响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "title": "帖子标题",
    "content": "帖子内容",
    "authorId": 1,
    "categoryId": 1,
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

#### 2.5 更新帖子
- **URL**: `PUT /posts/{id}`
- **描述**: 更新帖子
- **需要认证**
- **请求参数**: 同创建帖子

#### 2.6 删除帖子
- **URL**: `DELETE /posts/{id}`
- **描述**: 删除帖子
- **需要认证**

---

### 3. 分类管理

#### 3.1 获取分类列表
- **URL**: `GET /categories`
- **描述**: 获取所有分类
- **无需认证**
- **响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "技术交流",
      "description": "技术讨论区",
      "postCount": 100,
      "sortOrder": 1,
      "createdAt": "2024-01-01T00:00:00"
    }
  ]
}
```

#### 3.2 获取分类详情
- **URL**: `GET /categories/{id}`
- **描述**: 获取单个分类详情
- **无需认证**

---

### 4. 评论管理

#### 4.1 获取评论列表
- **URL**: `GET /comments/post/{postId}`
- **描述**: 获取帖子评论列表
- **无需认证**
- **响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "content": "评论内容",
      "authorId": 1,
      "authorName": "用户名",
      "authorAvatar": "头像URL",
      "postId": 1,
      "parentId": null,
      "likeCount": 10,
      "createdAt": "2024-01-01T00:00:00",
      "replies": []
    }
  ]
}
```

#### 4.2 发表评论
- **URL**: `POST /comments`
- **描述**: 发表评论
- **需要认证**
- **请求参数**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| postId | int | 是 | 帖子ID |
| content | string | 是 | 评论内容 |
| parentId | int | 否 | 父评论ID（回复） |

---

### 5. 互动管理

#### 5.1 点赞/取消点赞
- **URL**: `POST /interactions/like/{postId}`
- **描述**: 点赞或取消点赞帖子
- **需要认证**
- **响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "isLiked": true,
    "likeCount": 51
  }
}
```

#### 5.2 收藏/取消收藏
- **URL**: `POST /interactions/collect/{postId}`
- **描述**: 收藏或取消收藏帖子
- **需要认证**
- **响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "isCollected": true,
    "collectCount": 20
  }
}
```

---

### 6. AI 服务

#### 6.1 生成摘要
- **URL**: `POST /ai/summary`
- **描述**: AI生成帖子摘要
- **需要认证**
- **请求参数**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| text | string | 是 | 帖子内容 |
| maxLength | int | 否 | 最大长度，默认200 |

- **响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "summary": "生成的摘要内容"
  }
}
```

---

## 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权/Token无效 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 前端页面结构

### Web端 (Vue3)
- `/` - 首页（帖子流）
- `/hot` - 热门排行
- `/category` - 分类浏览
- `/category/:id` - 分类详情
- `/profile` - 个人中心
- `/login` - 登录
- `/register` - 注册
- `/post/:id` - 帖子详情
- `/create` - 发布帖子
- `/edit/:id` - 编辑帖子

### 微信小程序端
- `pages/index/index` - 首页
- `pages/hot/hot` - 热门
- `pages/category/category` - 分类
- `pages/profile/profile` - 我的
- `pages/login/login` - 登录
- `pages/register/register` - 注册
- `pages/post-detail/post-detail` - 帖子详情
- `pages/create-post/create-post` - 发布帖子
- `pages/category-detail/category-detail` - 分类详情
