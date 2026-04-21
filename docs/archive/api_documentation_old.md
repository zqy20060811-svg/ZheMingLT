# 择明论坛API接口文档

## 1. 接口概述

**基础URL**：`http://localhost:8080/api`

**请求头**：
- `Content-Type: application/json`
- `Authorization: Bearer {token}`（需要认证的接口）

**响应格式**：
```json
{
  "code": 200,
  "message": "成功",
  "data": {}
}
```

**错误响应**：
```json
{
  "code": 400,
  "message": "错误信息",
  "data": null
}
```

## 2. 用户模块

### 2.1 注册

**请求**：
- 方法：`POST`
- 路径：`/users/register`
- 参数：
  ```json
  {
    "username": "用户名",
    "password": "密码",
    "email": "邮箱",
    "phone": "手机号"
  }
  ```

**响应**：
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "用户名",
    "email": "邮箱",
    "phone": "手机号",
    "token": "JWT令牌"
  }
}
```

### 2.2 登录

**请求**：
- 方法：`POST`
- 路径：`/users/login`
- 参数：
  ```json
  {
    "username": "用户名",
    "password": "密码"
  }
  ```

**响应**：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "id": 1,
    "username": "用户名",
    "email": "邮箱",
    "phone": "手机号",
    "token": "JWT令牌"
  }
}
```

### 2.3 获取用户信息

**请求**：
- 方法：`GET`
- 路径：`/users/info`
- 认证：需要

**响应**：
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "username": "用户名",
    "email": "邮箱",
    "phone": "手机号",
    "avatar": "头像URL",
    "role": 0,
    "status": 1
  }
}
```

### 2.4 更新用户信息

**请求**：
- 方法：`PUT`
- 路径：`/users/info`
- 认证：需要
- 参数：
  ```json
  {
    "username": "用户名",
    "email": "邮箱",
    "phone": "手机号",
    "avatar": "头像URL"
  }
  ```

**响应**：
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "username": "用户名",
    "email": "邮箱",
    "phone": "手机号",
    "avatar": "头像URL"
  }
}
```

## 3. 分类模块

### 3.1 获取分类列表

**请求**：
- 方法：`GET`
- 路径：`/categories`

**响应**：
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 1,
      "name": "技术讨论",
      "description": "技术相关的讨论和分享",
      "sort": 1
    },
    {
      "id": 2,
      "name": "生活闲聊",
      "description": "日常生活的分享和交流",
      "sort": 2
    }
  ]
}
```

### 3.2 创建分类（管理员）

**请求**：
- 方法：`POST`
- 路径：`/categories`
- 认证：需要（管理员）
- 参数：
  ```json
  {
    "name": "分类名称",
    "description": "分类描述",
    "sort": 1
  }
  ```

**响应**：
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 3,
    "name": "分类名称",
    "description": "分类描述",
    "sort": 1
  }
}
```

## 4. 帖子模块

### 4.1 发布帖子

**请求**：
- 方法：`POST`
- 路径：`/posts`
- 认证：需要
- 参数：
  ```json
  {
    "title": "帖子标题",
    "content": "帖子内容",
    "categoryId": 1
  }
  ```

**响应**：
```json
{
  "code": 200,
  "message": "发布成功，等待审核",
  "data": {
    "id": 1,
    "title": "帖子标题",
    "content": "帖子内容",
    "status": 0,
    "createdAt": "2026-04-12 12:00:00"
  }
}
```

### 4.2 获取帖子列表

**请求**：
- 方法：`GET`
- 路径：`/posts`
- 参数：
  - `page`：页码（默认1）
  - `size`：每页数量（默认10）
  - `categoryId`：分类ID（可选）
  - `status`：状态（可选，1：审核通过）

**响应**：
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "total": 100,
    "pages": 10,
    "list": [
      {
        "id": 1,
        "title": "帖子标题",
        "summary": "帖子摘要",
        "username": "作者",
        "categoryName": "分类名称",
        "viewCount": 100,
        "likeCount": 10,
        "commentCount": 5,
        "createdAt": "2026-04-12 12:00:00"
      }
    ]
  }
}
```

### 4.3 获取帖子详情

**请求**：
- 方法：`GET`
- 路径：`/posts/{id}`

**响应**：
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "title": "帖子标题",
    "content": "帖子内容",
    "summary": "帖子摘要",
    "userId": 1,
    "username": "作者",
    "categoryId": 1,
    "categoryName": "分类名称",
    "viewCount": 100,
    "likeCount": 10,
    "commentCount": 5,
    "status": 1,
    "createdAt": "2026-04-12 12:00:00",
    "updatedAt": "2026-04-12 13:00:00"
  }
}
```

### 4.4 更新帖子

**请求**：
- 方法：`PUT`
- 路径：`/posts/{id}`
- 认证：需要（帖子作者）
- 参数：
  ```json
  {
    "title": "帖子标题",
    "content": "帖子内容",
    "categoryId": 1
  }
  ```

**响应**：
```json
{
  "code": 200,
  "message": "更新成功，等待审核",
  "data": {
    "id": 1,
    "title": "帖子标题",
    "content": "帖子内容",
    "status": 0,
    "updatedAt": "2026-04-12 13:00:00"
  }
}
```

### 4.5 删除帖子

**请求**：
- 方法：`DELETE`
- 路径：`/posts/{id}`
- 认证：需要（帖子作者或管理员）

**响应**：
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 4.6 审核帖子（管理员）

**请求**：
- 方法：`POST`
- 路径：`/posts/{id}/audit`
- 认证：需要（管理员）
- 参数：
  ```json
  {
    "status": 1, // 1：通过，2：拒绝
    "reason": "拒绝原因" // 拒绝时必填
  }
  ```

**响应**：
```json
{
  "code": 200,
  "message": "审核成功",
  "data": {
    "id": 1,
    "status": 1
  }
}
```

## 5. 评论模块

### 5.1 发布评论

**请求**：
- 方法：`POST`
- 路径：`/comments`
- 认证：需要
- 参数：
  ```json
  {
    "postId": 1,
    "content": "评论内容",
    "parentId": 0 // 回复评论时填写父评论ID
  }
  ```

**响应**：
```json
{
  "code": 200,
  "message": "评论成功",
  "data": {
    "id": 1,
    "content": "评论内容",
    "username": "用户名",
    "createdAt": "2026-04-12 12:00:00"
  }
}
```

### 5.2 获取帖子评论

**请求**：
- 方法：`GET`
- 路径：`/comments`
- 参数：
  - `postId`：帖子ID（必填）
  - `page`：页码（默认1）
  - `size`：每页数量（默认20）

**响应**：
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "total": 50,
    "pages": 3,
    "list": [
      {
        "id": 1,
        "content": "评论内容",
        "username": "用户名",
        "createdAt": "2026-04-12 12:00:00",
        "replies": [
          {
            "id": 2,
            "content": "回复内容",
            "username": "回复用户",
            "createdAt": "2026-04-12 12:30:00"
          }
        ]
      }
    ]
  }
}
```

### 5.3 删除评论

**请求**：
- 方法：`DELETE`
- 路径：`/comments/{id}`
- 认证：需要（评论作者或管理员）

**响应**：
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

## 6. 互动模块

### 6.1 点赞

**请求**：
- 方法：`POST`
- 路径：`/likes`
- 认证：需要
- 参数：
  ```json
  {
    "postId": 1, // 帖子ID或评论ID二选一
    "commentId": null
  }
  ```

**响应**：
```json
{
  "code": 200,
  "message": "点赞成功",
  "data": {
    "id": 1,
    "postId": 1,
    "commentId": null
  }
}
```

### 6.2 取消点赞

**请求**：
- 方法：`DELETE`
- 路径：`/likes`
- 认证：需要
- 参数：
  ```json
  {
    "postId": 1, // 帖子ID或评论ID二选一
    "commentId": null
  }
  ```

**响应**：
```json
{
  "code": 200,
  "message": "取消点赞成功",
  "data": null
}
```

### 6.3 收藏帖子

**请求**：
- 方法：`POST`
- 路径：`/collections`
- 认证：需要
- 参数：
  ```json
  {
    "postId": 1
  }
  ```

**响应**：
```json
{
  "code": 200,
  "message": "收藏成功",
  "data": {
    "id": 1,
    "postId": 1
  }
}
```

### 6.4 取消收藏

**请求**：
- 方法：`DELETE`
- 路径：`/collections/{postId}`
- 认证：需要

**响应**：
```json
{
  "code": 200,
  "message": "取消收藏成功",
  "data": null
}
```

### 6.5 获取用户收藏列表

**请求**：
- 方法：`GET`
- 路径：`/collections`
- 认证：需要
- 参数：
  - `page`：页码（默认1）
  - `size`：每页数量（默认10）

**响应**：
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "total": 20,
    "pages": 2,
    "list": [
      {
        "id": 1,
        "title": "帖子标题",
        "summary": "帖子摘要",
        "username": "作者",
        "categoryName": "分类名称",
        "createdAt": "2026-04-12 12:00:00"
      }
    ]
  }
}
```

## 7. AI模块

### 7.1 生成帖子摘要

**请求**：
- 方法：`POST`
- 路径：`/ai/summary`
- 认证：需要
- 参数：
  ```json
  {
    "content": "帖子内容"
  }
  ```

**响应**：
```json
{
  "code": 200,
  "message": "生成成功",
  "data": {
    "summary": "帖子摘要"
  }
}
```

### 7.2 内容审核

**请求**：
- 方法：`POST`
- 路径：`/ai/audit`
- 认证：需要
- 参数：
  ```json
  {
    "content": "需要审核的内容"
  }
  ```

**响应**：
```json
{
  "code": 200,
  "message": "审核完成",
  "data": {
    "status": 1, // 1：通过，2：拒绝
    "reason": "拒绝原因" // 拒绝时返回
  }
}
```

## 8. 错误码定义

| 错误码 | 描述 |
| :--- | :--- |
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 501 | AI服务调用失败 |

## 9. 注意事项

1. 所有需要认证的接口都需要在请求头中携带 `Authorization: Bearer {token}`
2. 上传文件的接口需要使用 `multipart/form-data` 格式
3. 分页接口默认返回第1页，每页10条数据
4. 敏感操作需要二次验证
5. API调用频率限制：每分钟最多60次

## 10. 版本控制

- **v1.0**：初始版本，包含核心功能
- **v1.1**：增加AI辅助创作功能
- **v1.2**：增加个性化推荐功能