<template>
  <div class="post-detail-page" :class="{ 'dark-mode': isDarkMode }">
    <!-- 星光背景 -->
    <div class="starry-background" ref="starryBg"></div>

    <!-- 导航栏 -->
    <nav class="navbar navbar-expand-lg">
      <div class="container">
        <router-link class="navbar-brand" to="/">
          <i class="bi bi-lightbulb"></i> 择明论坛
        </router-link>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
          <ul class="navbar-nav me-auto">
            <li class="nav-item">
              <router-link class="nav-link" to="/">首页</router-link>
            </li>
          </ul>
          <ul class="navbar-nav">
            <li class="nav-item">
              <button class="btn btn-link nav-link" @click="toggleTheme">
                <i :class="isDarkMode ? 'bi bi-sun' : 'bi bi-moon'"></i>
              </button>
            </li>
            <template v-if="isLoggedIn">
              <li class="nav-item">
                <span class="nav-link">欢迎, {{ username }}</span>
              </li>
              <li class="nav-item">
                <router-link class="nav-link" to="/create">发布帖子</router-link>
              </li>
              <li class="nav-item">
                <a class="nav-link" href="#" @click.prevent="logout">退出</a>
              </li>
            </template>
            <template v-else>
              <li class="nav-item">
                <router-link class="nav-link" to="/login">登录</router-link>
              </li>
              <li class="nav-item">
                <router-link class="nav-link" to="/register">注册</router-link>
              </li>
            </template>
          </ul>
        </div>
      </div>
    </nav>

    <!-- 主内容 -->
    <div class="container main-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary"></div>
        <p class="mt-2">加载中...</p>
      </div>

      <template v-else-if="post">
        <div class="row">
          <div class="col-md-8">
            <!-- 帖子内容 -->
            <div class="post-container">
              <h1 class="post-title">{{ post.title }}</h1>
              <div class="post-meta">
                <span class="post-author">
                  <i class="bi bi-person"></i> {{ post.authorName }}
                </span>
                <span class="post-time">
                  <i class="bi bi-clock"></i> {{ formatTime(post.createdAt) }}
                </span>
                <span class="post-category">
                  <i class="bi bi-folder"></i> {{ post.categoryName }}
                </span>
              </div>
              <div class="post-content">{{ post.content }}</div>

              <!-- 帖子操作 -->
              <div class="post-actions">
                <button
                  class="action-btn"
                  :class="{ active: post.isLiked }"
                  @click="toggleLike"
                >
                  <i class="bi" :class="post.isLiked ? 'bi-heart-fill' : 'bi-heart'"></i>
                  <span>{{ post.likeCount || 0 }}</span>
                </button>
                <button
                  class="action-btn"
                  :class="{ active: post.isCollected }"
                  @click="toggleCollect"
                >
                  <i class="bi" :class="post.isCollected ? 'bi-bookmark-fill' : 'bi-bookmark'"></i>
                  <span>{{ post.collectCount || 0 }}</span>
                </button>
                <button class="action-btn" @click="sharePost">
                  <i class="bi bi-share"></i>
                  <span>分享</span>
                </button>
              </div>

              <!-- 编辑删除按钮（仅作者可见） -->
              <div v-if="isAuthor" class="post-edit-actions">
                <button class="btn btn-sm btn-outline-primary" @click="editPost">
                  <i class="bi bi-pencil"></i> 编辑
                </button>
                <button class="btn btn-sm btn-outline-danger" @click="deletePost">
                  <i class="bi bi-trash"></i> 删除
                </button>
              </div>
            </div>

            <!-- 评论区 -->
            <div class="comments-section">
              <h5 class="comments-title">
                <i class="bi bi-chat-dots"></i> 评论 ({{ comments.length }})
              </h5>

              <!-- 评论表单 -->
              <div v-if="isLoggedIn" class="comment-form">
                <div class="mb-3">
                  <textarea
                    class="form-control"
                    rows="3"
                    v-model="newComment"
                    placeholder="发表你的评论..."
                  ></textarea>
                </div>
                <button class="btn btn-primary" @click="submitComment" :disabled="!newComment.trim()">
                  发表评论
                </button>
              </div>
              <div v-else class="comment-login-tip">
                <router-link to="/login">登录</router-link> 后发表评论
              </div>

              <!-- 评论列表 -->
              <div v-if="comments.length > 0" class="comments-list">
                <div
                  v-for="comment in comments"
                  :key="comment.id"
                  class="comment-item"
                >
                  <div class="comment-author">{{ comment.authorName }}</div>
                  <div class="comment-time">{{ formatTime(comment.createdAt) }}</div>
                  <div class="comment-content">{{ comment.content }}</div>
                  <div class="comment-actions">
                    <button class="reply-btn" @click="replyTo(comment)">
                      <i class="bi bi-reply"></i> 回复
                    </button>
                    <button
                      v-if="comment.authorId === currentUserId"
                      class="delete-btn"
                      @click="deleteComment(comment.id)"
                    >
                      <i class="bi bi-trash"></i> 删除
                    </button>
                  </div>

                  <!-- 回复列表 -->
                  <div v-if="comment.replies && comment.replies.length > 0" class="replies-list">
                    <div
                      v-for="reply in comment.replies"
                      :key="reply.id"
                      class="reply-item"
                    >
                      <div class="reply-author">{{ reply.authorName }}</div>
                      <div class="reply-time">{{ formatTime(reply.createdAt) }}</div>
                      <div class="reply-content">{{ reply.content }}</div>
                    </div>
                  </div>

                  <!-- 回复表单 -->
                  <div v-if="replyingTo === comment.id" class="reply-form">
                    <textarea
                      class="form-control"
                      rows="2"
                      v-model="replyContent"
                      placeholder="回复 @{{ comment.authorName }}..."
                    ></textarea>
                    <div class="reply-form-actions">
                      <button class="btn btn-sm btn-primary" @click="submitReply(comment.id)">
                        提交回复
                      </button>
                      <button class="btn btn-sm btn-secondary" @click="cancelReply">
                        取消
                      </button>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="empty-comments">
                <i class="bi bi-chat"></i>
                <p>暂无评论，快来抢沙发吧！</p>
              </div>
            </div>
          </div>

          <!-- 侧边栏 -->
          <div class="col-md-4">
            <!-- 作者信息 -->
            <div class="author-card">
              <h5 class="card-title">作者信息</h5>
              <div class="author-info">
                <div class="author-avatar">
                  <i class="bi bi-person-circle"></i>
                </div>
                <div class="author-name">{{ post.authorName }}</div>
              </div>
            </div>

            <!-- 相关帖子 -->
            <div class="related-posts">
              <h5 class="card-title">相关帖子</h5>
              <div v-if="relatedPosts.length > 0" class="related-list">
                <div
                  v-for="related in relatedPosts"
                  :key="related.id"
                  class="related-item"
                  @click="goToPost(related.id)"
                >
                  {{ related.title }}
                </div>
              </div>
              <div v-else class="empty-related">
                暂无相关帖子
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- 帖子不存在 -->
      <div v-else class="not-found">
        <i class="bi bi-exclamation-circle"></i>
        <h3>帖子不存在或已被删除</h3>
        <router-link to="/" class="btn btn-primary">返回首页</router-link>
      </div>
    </div>

    <!-- 页脚 -->
    <footer class="main-footer">
      <div class="container">
        <p>&copy; 2024 择明论坛. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get, post as postRequest, put, del } from '@/utils/request'

const route = useRoute()
const router = useRouter()

// 状态
const loading = ref(true)
const isDarkMode = ref(false)
const isLoggedIn = ref(false)
const username = ref('')
const currentUserId = ref(null)
const starryBg = ref(null)

const post = ref(null)
const comments = ref([])
const relatedPosts = ref([])
const newComment = ref('')
const replyingTo = ref(null)
const replyContent = ref('')

// 计算属性
const isAuthor = computed(() => {
  return post.value && post.value.authorId === currentUserId.value
})

// 方法
function generateStars() {
  if (!starryBg.value) return
  starryBg.value.innerHTML = ''
  for (let i = 0; i < 100; i++) {
    const star = document.createElement('div')
    star.className = 'star'
    star.style.left = Math.random() * 100 + '%'
    star.style.top = Math.random() * 100 + '%'
    star.style.width = Math.random() * 3 + 1 + 'px'
    star.style.height = star.style.width
    star.style.animationDelay = Math.random() * 3 + 's'
    star.style.animationDuration = Math.random() * 3 + 2 + 's'
    starryBg.value.appendChild(star)
  }
}

function toggleTheme() {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
}

function checkLogin() {
  const token = localStorage.getItem('token')
  isLoggedIn.value = !!token
  username.value = localStorage.getItem('username') || ''
  currentUserId.value = parseInt(localStorage.getItem('userId')) || null
}

async function logout() {
  const token = localStorage.getItem('token')
  if (token) {
    await postRequest('/users/logout')
  }
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('username')
  isLoggedIn.value = false
  username.value = ''
  currentUserId.value = null
}

async function loadPost() {
  const id = route.params.id
  if (!id) {
    loading.value = false
    return
  }

  try {
    const res = await get(`/posts/${id}`)
    if (res.code === 200) {
      post.value = res.data
      await loadComments(id)
      await loadRelatedPosts(id)
    } else {
      post.value = null
    }
  } finally {
    loading.value = false
  }
}

async function loadComments(postId) {
  const res = await get(`/comments/post/${postId}`)
  if (res.code === 200) {
    comments.value = res.data || []
  }
}

async function loadRelatedPosts(postId) {
  const res = await get('/posts', { size: 5 })
  if (res.code === 200) {
    relatedPosts.value = (res.data.content || [])
      .filter(p => p.id !== parseInt(postId))
      .slice(0, 5)
  }
}

async function toggleLike() {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }

  const res = await postRequest(`/interactions/like/${post.value.id}`)
  if (res.code === 200) {
    post.value.isLiked = !post.value.isLiked
    post.value.likeCount = (post.value.likeCount || 0) + (post.value.isLiked ? 1 : -1)
  }
}

async function toggleCollect() {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }

  const res = await postRequest(`/interactions/collect/${post.value.id}`)
  if (res.code === 200) {
    post.value.isCollected = !post.value.isCollected
    post.value.collectCount = (post.value.collectCount || 0) + (post.value.isCollected ? 1 : -1)
  }
}

function sharePost() {
  if (navigator.share) {
    navigator.share({
      title: post.value.title,
      text: post.value.summary || post.value.content.substring(0, 100),
      url: window.location.href
    })
  } else {
    // 复制链接到剪贴板
    navigator.clipboard.writeText(window.location.href)
    alert('链接已复制到剪贴板')
  }
}

function editPost() {
  router.push(`/edit/${post.value.id}`)
}

async function deletePost() {
  if (!confirm('确定要删除这篇帖子吗？此操作不可恢复。')) return

  const res = await del(`/posts/${post.value.id}`)
  if (res.code === 200) {
    alert('删除成功')
    router.push('/')
  } else {
    alert(res.message || '删除失败')
  }
}

async function submitComment() {
  if (!newComment.value.trim()) return

  const res = await postRequest('/comments', {
    postId: post.value.id,
    content: newComment.value.trim()
  })

  if (res.code === 200) {
    newComment.value = ''
    await loadComments(post.value.id)
  } else {
    alert(res.message || '评论失败')
  }
}

function replyTo(comment) {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }
  replyingTo.value = comment.id
  replyContent.value = ''
}

function cancelReply() {
  replyingTo.value = null
  replyContent.value = ''
}

async function submitReply(parentId) {
  if (!replyContent.value.trim()) return

  const res = await postRequest('/comments', {
    postId: post.value.id,
    content: replyContent.value.trim(),
    parentId: parentId
  })

  if (res.code === 200) {
    cancelReply()
    await loadComments(post.value.id)
  } else {
    alert(res.message || '回复失败')
  }
}

async function deleteComment(commentId) {
  if (!confirm('确定要删除这条评论吗？')) return

  const res = await del(`/comments/${commentId}`)
  if (res.code === 200) {
    await loadComments(post.value.id)
  } else {
    alert(res.message || '删除失败')
  }
}

function goToPost(postId) {
  router.push(`/post/${postId}`)
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  isDarkMode.value = localStorage.getItem('darkMode') === 'true'
  generateStars()
  checkLogin()
  loadPost()
})
</script>

<style scoped>
.post-detail-page {
  min-height: 100vh;
  background-color: var(--background-color);
  position: relative;
}

.main-content {
  padding-top: 30px;
  padding-bottom: 30px;
}

/* 帖子容器 */
.post-container {
  background-color: var(--card-background);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow);
  padding: 30px;
  margin-bottom: 20px;
}

.post-title {
  font-size: 1.8rem;
  font-weight: bold;
  color: var(--text-color);
  margin-bottom: 15px;
}

.post-meta {
  display: flex;
  gap: 20px;
  color: var(--text-light);
  font-size: 0.9rem;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid var(--border-color);
}

.post-meta span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.post-content {
  font-size: 1rem;
  line-height: 1.8;
  color: var(--text-color);
  margin-bottom: 30px;
  white-space: pre-wrap;
}

/* 帖子操作 */
.post-actions {
  display: flex;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 20px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  border: none;
  background: none;
  color: var(--text-light);
  cursor: pointer;
  border-radius: 20px;
  transition: var(--transition);
}

.action-btn:hover {
  background-color: var(--hover-color);
}

.action-btn.active {
  color: #ff6b6b;
}

.action-btn i {
  font-size: 1.2rem;
}

.post-edit-actions {
  display: flex;
  gap: 10px;
}

/* 评论区 */
.comments-section {
  background-color: var(--card-background);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow);
  padding: 25px;
}

.comments-title {
  color: var(--text-color);
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid var(--primary-color);
}

.comments-title i {
  color: var(--primary-color);
  margin-right: 8px;
}

.comment-form {
  margin-bottom: 25px;
}

.comment-login-tip {
  text-align: center;
  padding: 20px;
  background-color: var(--hover-color);
  border-radius: var(--border-radius);
  margin-bottom: 25px;
  color: var(--text-light);
}

.comment-login-tip a {
  color: var(--primary-color);
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.comment-item {
  padding: 15px;
  background-color: var(--hover-color);
  border-radius: var(--border-radius);
}

.comment-author {
  font-weight: bold;
  color: var(--primary-color);
  margin-bottom: 5px;
}

.comment-time {
  font-size: 0.8rem;
  color: var(--text-light);
  margin-bottom: 10px;
}

.comment-content {
  color: var(--text-color);
  line-height: 1.6;
  margin-bottom: 10px;
}

.comment-actions {
  display: flex;
  gap: 15px;
}

.reply-btn,
.delete-btn {
  background: none;
  border: none;
  color: var(--primary-color);
  cursor: pointer;
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  gap: 3px;
}

.delete-btn {
  color: #dc3545;
}

/* 回复 */
.replies-list {
  margin-top: 15px;
  margin-left: 20px;
}

.reply-item {
  padding: 10px;
  background-color: var(--card-background);
  border-radius: 8px;
  margin-bottom: 10px;
}

.reply-author {
  font-weight: bold;
  color: var(--primary-color);
  font-size: 0.9rem;
}

.reply-time {
  font-size: 0.75rem;
  color: var(--text-light);
  margin-bottom: 5px;
}

.reply-content {
  color: var(--text-color);
  font-size: 0.9rem;
}

.reply-form {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid var(--border-color);
}

.reply-form-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.empty-comments {
  text-align: center;
  padding: 40px;
  color: var(--text-light);
}

.empty-comments i {
  font-size: 3rem;
  margin-bottom: 10px;
}

/* 侧边栏 */
.author-card,
.related-posts {
  background-color: var(--card-background);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow);
  padding: 20px;
  margin-bottom: 20px;
}

.card-title {
  color: var(--text-color);
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid var(--primary-color);
}

.author-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.author-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: var(--primary-color);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 2rem;
}

.author-name {
  font-weight: bold;
  color: var(--text-color);
}

.related-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.related-item {
  padding: 10px;
  background-color: var(--hover-color);
  border-radius: 8px;
  cursor: pointer;
  color: var(--text-color);
  transition: var(--transition);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.related-item:hover {
  background-color: var(--primary-color);
  color: white;
}

.empty-related {
  text-align: center;
  color: var(--text-light);
  padding: 20px;
}

/* 404 */
.not-found {
  text-align: center;
  padding: 80px 20px;
  color: var(--text-light);
}

.not-found i {
  font-size: 5rem;
  margin-bottom: 20px;
}

.not-found h3 {
  margin-bottom: 20px;
  color: var(--text-color);
}

/* 页脚 */
.main-footer {
  background-color: var(--card-background);
  border-top: 1px solid var(--border-color);
  padding: 20px 0;
  text-align: center;
  color: var(--text-light);
}
</style>
