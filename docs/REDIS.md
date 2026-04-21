# 择明论坛 Redis 设计文档

## Redis 用途

- Token 存储与验证
- Token 黑名单
- 热点数据缓存
- 用户会话管理

## Key 命名规范

格式：`zheminglt:{module}:{identifier}`

## 数据结构

### 1. Token 存储

**用途**: 存储用户登录 token，用于验证和单点登录控制

- **Key**: `zheminglt:token:{token}`
- **Value**: 用户ID (String)
- **TTL**: 7天 (604800秒)
- **示例**:
  ```
  SET zheminglt:token:eyJhbGciOiJIUzI1NiIs... 1 EX 604800
  ```

### 2. Token 黑名单

**用途**: 存储已注销的 token，防止重复使用

- **Key**: `zheminglt:blacklist:{token}`
- **Value**: 注销时间戳 (String)
- **TTL**: 7天 (与 token 有效期一致)
- **示例**:
  ```
  SET zheminglt:blacklist:eyJhbGciOiJIUzI1NiIs... 1704067200 EX 604800
  ```

### 3. 用户会话

**用途**: 记录用户当前有效 token，支持单点登录

- **Key**: `zheminglt:user:session:{userId}`
- **Value**: token (String)
- **TTL**: 7天
- **示例**:
  ```
  SET zheminglt:user:session:1 eyJhbGciOiJIUzI1NiIs... EX 604800
  ```

### 4. 热门帖子缓存

**用途**: 缓存热门帖子列表，减少数据库查询

- **Key**: `zheminglt:hot:posts`
- **Value**: 帖子ID列表 (Sorted Set，按热度排序)
- **TTL**: 1小时 (3600秒)
- **示例**:
  ```
  ZADD zheminglt:hot:posts 100 1 95 2 88 3
  EXPIRE zheminglt:hot:posts 3600
  ```

### 5. 帖子详情缓存

**用途**: 缓存帖子详情，减少数据库查询

- **Key**: `zheminglt:post:{postId}`
- **Value**: 帖子信息 (Hash)
- **TTL**: 30分钟 (1800秒)
- **示例**:
  ```
  HSET zheminglt:post:1 title "帖子标题" content "帖子内容" view_count 100
  EXPIRE zheminglt:post:1 1800
  ```

### 6. 用户基本信息缓存

**用途**: 缓存用户基本信息

- **Key**: `zheminglt:user:{userId}`
- **Value**: 用户信息 (Hash)
- **TTL**: 1小时 (3600秒)
- **示例**:
  ```
  HSET zheminglt:user:1 username "testuser" email "test@example.com"
  EXPIRE zheminglt:user:1 3600
  ```

### 7. 帖子浏览量计数

**用途**: 记录帖子浏览量，定期同步到数据库

- **Key**: `zheminglt:post:view:{postId}`
- **Value**: 浏览量 (String/Integer)
- **TTL**: 永久（或定期清理）
- **示例**:
  ```
  INCR zheminglt:post:view:1
  ```

### 8. 点赞/收藏计数

**用途**: 记录帖子点赞数和收藏数

- **Key**: `zheminglt:post:like:{postId}`
- **Key**: `zheminglt:post:collect:{postId}`
- **Value**: 数量 (String/Integer)
- **示例**:
  ```
  INCR zheminglt:post:like:1
  INCR zheminglt:post:collect:1
  ```

## 当前实现状态

### 已实现

- [x] Token 存储 (内存实现，需迁移到 Redis)
- [x] Token 黑名单 (内存实现，需迁移到 Redis)

### 待实现

- [ ] 用户会话管理
- [ ] 热门帖子缓存
- [ ] 帖子详情缓存
- [ ] 用户基本信息缓存
- [ ] 浏览量计数
- [ ] 点赞/收藏计数

## 迁移计划

### 阶段 1: Token 管理 (已完成)
- 使用 ConcurrentHashMap 模拟 Redis
- 后续替换为 RedisTemplate

### 阶段 2: 热点数据缓存
- 帖子列表缓存
- 用户基本信息缓存

### 阶段 3: 计数功能
- 浏览量计数
- 点赞/收藏计数
- 定期同步到数据库

## Redis 配置

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0
    timeout: 3000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
```

## 代码实现参考

### TokenServiceImpl (当前内存实现)

```java
@Service
public class TokenServiceImpl implements TokenService {
    
    // 使用内存存储（生产环境应使用 Redis）
    private static final Map<String, Long> tokenStore = new ConcurrentHashMap<>();
    private static final Map<String, Long> blacklistStore = new ConcurrentHashMap<>();
    
    @Override
    public void storeToken(String token, Long userId) {
        tokenStore.put(token, userId);
    }
    
    @Override
    public boolean isTokenValid(String token) {
        if (isTokenBlacklisted(token)) {
            return false;
        }
        return tokenStore.containsKey(token);
    }
    
    @Override
    public void blacklistToken(String token) {
        blacklistStore.put(token, System.currentTimeMillis());
        removeToken(token);
    }
    
    @Override
    public boolean isTokenBlacklisted(String token) {
        return blacklistStore.containsKey(token);
    }
    
    @Override
    public void removeToken(String token) {
        tokenStore.remove(token);
    }
}
```

### Redis 实现示例

```java
@Service
public class RedisTokenServiceImpl implements TokenService {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    private static final String TOKEN_PREFIX = "zheminglt:token:";
    private static final String BLACKLIST_PREFIX = "zheminglt:blacklist:";
    private static final long TOKEN_EXPIRE = 7 * 24 * 60 * 60; // 7天
    
    @Override
    public void storeToken(String token, Long userId) {
        String key = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(key, userId.toString(), TOKEN_EXPIRE, TimeUnit.SECONDS);
    }
    
    @Override
    public boolean isTokenValid(String token) {
        if (isTokenBlacklisted(token)) {
            return false;
        }
        String key = TOKEN_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    @Override
    public void blacklistToken(String token) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()), 
            TOKEN_EXPIRE, TimeUnit.SECONDS);
        removeToken(token);
    }
    
    @Override
    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    @Override
    public void removeToken(String token) {
        String key = TOKEN_PREFIX + token;
        redisTemplate.delete(key);
    }
}
```
