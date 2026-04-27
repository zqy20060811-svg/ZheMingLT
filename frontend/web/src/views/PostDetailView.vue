<template>
  <div class="post-detail-page" :class="{ 'dark-mode': isDarkMode }">
    <!-- 动态背景 -->
    <div class="animated-bg">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
    </div>

    <!-- 顶部导航 -->
    <nav class="top-nav">
      <div class="nav-left">
        <button class="back-btn" @click="goBack">
          <i class="bi bi-arrow-left"></i>
        </button>
        <div class="nav-brand">
          <div class="logo-wrapper">
            <i class="bi bi-lightbulb-fill"></i>
          </div>
          <span class="brand-text">择明论坛</span>
        </div>
      </div>
      <div class="nav-actions">
        <button class="btn-icon" @click="toggleTheme">
          <i :class="isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-fill'"></i>
        </button>
        <button class="btn-icon" @click="sharePost">
          <i class="bi bi-share-fill"></i>
        </button>
      </div>
    </nav>

    <!-- 主内容 -->
    <div class="main-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner-wrapper">
          <div class="spinner"></div>
          <div class="spinner-ring"></div>
        </div>
        <span class="loading-text">加载中...</span>
      </div>

      <template v-else-if="postDetail">
        <!-- 帖子头部信息 -->
        <div class="post-header-card">
          <div class="category-badge" :style="getCategoryStyle(postDetail.categoryName)">
            <i class="bi bi-folder-fill"></i>
            {{ postDetail.categoryName || '未分类' }}
          </div>
          <h1 class="post-title">{{ postDetail.title }}</h1>
          <div class="post-meta">
            <div class="author-info">
              <img :src="postDetail.authorAvatar || defaultAvatar" class="author-avatar" @click="goToUserProfile(postDetail.userId)" />
              <div class="author-details">
                <span class="author-name" @click="goToUserProfile(postDetail.userId)">{{ postDetail.authorName || '匿名用户' }}</span>
                <span class="post-time">
                  <i class="bi bi-clock"></i>
                  {{ formatTime(postDetail.createdAt) }}
                </span>
                <!-- 关注作者按钮 -->
                <button
                  v-if="!isAuthor && postDetail.userId"
                  :class="['follow-author-btn', { following: isFollowingAuthor }]"
                  @click.stop="toggleFollowAuthor"
                  :disabled="followLoading"
                >
                  <i v-if="followLoading" class="bi bi-arrow-repeat spinning"></i>
                  <i v-else :class="isFollowingAuthor ? 'bi bi-check-lg' : 'bi bi-plus-lg'"></i>
                  <span>{{ isFollowingAuthor ? '已关注' : '关注' }}</span>
                </button>
              </div>
            </div>
            <div class="post-stats">
              <span class="stat-item">
                <i class="bi bi-eye-fill"></i>
                {{ formatNumber(postDetail.viewCount || 0) }}
              </span>
            </div>
          </div>
        </div>

        <!-- 帖子内容 -->
        <div class="post-content-card">
          <div class="content-body">{{ postDetail.content }}</div>
          
          <!-- 帖子操作 -->
          <div class="post-actions">
            <button
              class="action-btn like-btn"
              :class="{ active: postDetail.isLiked }"
              @click="toggleLike"
            >
              <div class="btn-icon-wrapper">
                <i class="bi" :class="postDetail.isLiked ? 'bi-heart-fill' : 'bi-heart'"></i>
              </div>
              <span class="btn-text">{{ postDetail.isLiked ? '已点赞' : '点赞' }}</span>
              <span class="btn-count">{{ formatNumber(postDetail.likeCount || 0) }}</span>
            </button>
            
            <button
              class="action-btn collect-btn"
              :class="{ active: postDetail.isCollected }"
              @click="toggleCollect"
            >
              <div class="btn-icon-wrapper">
                <i class="bi" :class="postDetail.isCollected ? 'bi-bookmark-fill' : 'bi-bookmark'"></i>
              </div>
              <span class="btn-text">{{ postDetail.isCollected ? '已收藏' : '收藏' }}</span>
              <span class="btn-count">{{ formatNumber(postDetail.collectCount || 0) }}</span>
            </button>
            
            <button class="action-btn comment-btn" @click="scrollToComments">
              <div class="btn-icon-wrapper">
                <i class="bi bi-chat-fill"></i>
              </div>
              <span class="btn-text">评论</span>
              <span class="btn-count">{{ formatNumber(comments.length || 0) }}</span>
            </button>
          </div>

          <!-- 编辑删除按钮（仅作者可见） -->
          <div v-if="isAuthor" class="author-actions">
            <button class="edit-btn" @click="editPost">
              <i class="bi bi-pencil-square"></i>
              编辑帖子
            </button>
            <button class="delete-btn" @click="deletePost">
              <i class="bi bi-trash-fill"></i>
              删除帖子
            </button>
          </div>
        </div>

        <!-- 评论区 -->
        <div id="comments-section" class="comments-card">
          <div class="comments-header">
            <div class="header-title">
              <i class="bi bi-chat-dots-fill"></i>
              <h3>评论交流</h3>
              <span class="comment-count">{{ comments.length }}</span>
            </div>
          </div>

          <!-- 评论输入框 -->
          <div v-if="isLoggedIn" class="comment-input-area">
            <div class="input-wrapper">
              <img :src="currentUserAvatar" class="user-avatar" />
              <div class="input-box">
                <textarea
                  v-model="newComment"
                  placeholder="写下你的想法，与大家交流..."
                  rows="3"
                  @keydown.enter.ctrl="submitComment"
                ></textarea>
                <div class="input-actions">
                  <span class="hint">Ctrl + Enter 发送</span>
                  <button 
                    class="submit-btn" 
                    @click="submitComment"
                    :disabled="!newComment.trim()"
                  >
                    <i class="bi bi-send-fill"></i>
                    发表评论
                  </button>
                </div>
              </div>
            </div>
          </div>
          
          <div v-else class="login-tip">
            <div class="tip-content">
              <i class="bi bi-person-circle"></i>
              <p>登录后即可参与讨论</p>
              <router-link to="/login" class="login-btn">立即登录</router-link>
            </div>
          </div>

          <!-- 评论列表 -->
          <div v-if="comments.length > 0" class="comments-list">
            <div
              v-for="(comment, index) in comments"
              :key="comment.id"
              class="comment-item"
              :style="{ animationDelay: `${index * 0.1}s` }"
            >
              <img :src="comment.authorAvatar || defaultAvatar" class="comment-avatar" />
              <div class="comment-body">
                <div class="comment-header">
                  <span class="comment-author">{{ comment.authorName }}</span>
                  <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
                <div class="comment-actions">
                  <button class="action-link" @click="replyTo(comment)">
                    <i class="bi bi-reply-fill"></i>
                    回复
                  </button>
                  <button 
                    v-if="comment.authorId === currentUserId" 
                    class="action-link delete"
                    @click="deleteComment(comment.id)"
                  >
                    <i class="bi bi-trash"></i>
                    删除
                  </button>
                </div>

                <!-- 回复列表 -->
                <div v-if="comment.replies && comment.replies.length > 0" class="replies-list">
                  <div
                    v-for="reply in comment.replies"
                    :key="reply.id"
                    class="reply-item"
                  >
                    <img :src="reply.authorAvatar || defaultAvatar" class="reply-avatar" />
                    <div class="reply-body">
                      <div class="reply-header">
                        <span class="reply-author">{{ reply.authorName }}</span>
                        <span class="reply-time">{{ formatTime(reply.createdAt) }}</span>
                      </div>
                      <div class="reply-content">{{ reply.content }}</div>
                    </div>
                  </div>
                </div>

                <!-- 回复输入框 -->
                <div v-if="replyingTo === comment.id" class="reply-input-area">
                  <textarea
                    v-model="replyContent"
                    :placeholder="`回复 @${comment.authorName}...`"
                    rows="2"
                    @keydown.enter.ctrl="submitReply(comment.id)"
                  ></textarea>
                  <div class="reply-actions">
                    <button class="cancel-btn" @click="cancelReply">取消</button>
                    <button 
                      class="submit-btn" 
                      @click="submitReply(comment.id)"
                      :disabled="!replyContent.trim()"
                    >
                      发送回复
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div v-else class="empty-comments">
            <div class="empty-icon">
              <i class="bi bi-chat-square-text"></i>
            </div>
            <h4>暂无评论</h4>
            <p>成为第一个评论的人吧！</p>
          </div>
        </div>
      </template>

      <!-- 帖子不存在 -->
      <div v-else class="not-found">
        <div class="empty-icon">
          <i class="bi bi-exclamation-circle-fill"></i>
        </div>
        <h3>帖子不存在或已被删除</h3>
        <p>该帖子可能已被作者删除或不存在</p>
        <button class="back-home-btn" @click="goHome">
          <i class="bi bi-house-fill"></i>
          返回首页
        </button>
      </div>
    </div>

    <!-- 底部导航 -->
    <BottomNav />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get, post, del, getAccessToken } from '@/utils/request'
import BottomNav from '@/components/BottomNav.vue'

const route = useRoute()
const router = useRouter()

// 状态
const loading = ref(true)
const isDarkMode = ref(false)
const isLoggedIn = ref(false)
const username = ref('')
const currentUserId = ref(null)
const currentUserAvatar = ref('https://api.dicebear.com/7.x/avataaars/svg?seed=user')

const postDetail = ref(null)
const comments = ref([])
const newComment = ref('')
const replyingTo = ref(null)
const replyContent = ref('')
const isFollowingAuthor = ref(false)
const followLoading = ref(false)

const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

// 计算属性
const isAuthor = computed(() => {
  if (!postDetail.value || !postDetail.value.userId || !currentUserId.value) return false
  return String(postDetail.value.userId) === String(currentUserId.value)
})

// 方法
function toggleTheme() {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
}

function checkLogin() {
  const token = getAccessToken()
  isLoggedIn.value = !!token
  username.value = localStorage.getItem('username') || ''
  currentUserId.value = parseInt(localStorage.getItem('userId')) || null
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
      postDetail.value = res.data
      await loadComments(id)
      // 检查是否已关注作者
      if (currentUserId.value && postDetail.value.userId && !isAuthor.value) {
        await checkFollowStatus()
      }
    } else {
      postDetail.value = null
    }
  } finally {
    loading.value = false
  }
}

// 检查是否已关注作者
async function checkFollowStatus() {
  try {
    const res = await get(`/follows/${postDetail.value.userId}/check`)
    if (res.code === 200) {
      isFollowingAuthor.value = res.data
    }
  } catch (error) {
    console.error('检查关注状态失败', error)
  }
}

// 关注/取消关注作者
async function toggleFollowAuthor() {
  if (!isLoggedIn.value) {
    alert('请先登录')
    router.push('/login')
    return
  }

  followLoading.value = true
  try {
    if (isFollowingAuthor.value) {
      // 取消关注
      const res = await del(`/follows/${postDetail.value.userId}`)
      if (res.code === 200) {
        isFollowingAuthor.value = false
      }
    } else {
      // 关注
      const res = await post(`/follows/${postDetail.value.userId}`)
      if (res.code === 200) {
        isFollowingAuthor.value = true
      }
    }
  } catch (error) {
    console.error('关注操作失败', error)
    alert('操作失败，请重试')
  } finally {
    followLoading.value = false
  }
}

// 跳转到用户主页
function goToUserProfile(userId) {
  if (userId) {
    router.push(`/user/${userId}`)
  }
}

async function loadComments(postId) {
  const res = await get(`/comments/post/${postId}`)
  if (res.code === 200) {
    comments.value = res.data || []
  }
}

async function toggleLike() {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }

  const res = await post(`/interactions/like/${postDetail.value.id}`)
  if (res.code === 200) {
    postDetail.value.isLiked = !postDetail.value.isLiked
    postDetail.value.likeCount = (postDetail.value.likeCount || 0) + (postDetail.value.isLiked ? 1 : -1)
  }
}

async function toggleCollect() {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }

  const res = await post(`/interactions/collect/${postDetail.value.id}`)
  if (res.code === 200) {
    postDetail.value.isCollected = !postDetail.value.isCollected
    postDetail.value.collectCount = (postDetail.value.collectCount || 0) + (postDetail.value.isCollected ? 1 : -1)
  }
}

function sharePost() {
  if (navigator.share) {
    navigator.share({
      title: postDetail.value.title,
      text: postDetail.value.summary || postDetail.value.content.substring(0, 100),
      url: window.location.href
    })
  } else {
    navigator.clipboard.writeText(window.location.href)
    alert('链接已复制到剪贴板')
  }
}

function scrollToComments() {
  document.getElementById('comments-section')?.scrollIntoView({ behavior: 'smooth' })
}

function editPost() {
  router.push(`/post/${postDetail.value.id}/edit`)
}

async function deletePost() {
  if (!confirm('确定要删除这篇帖子吗？此操作不可恢复。')) return

  const res = await del(`/posts/${postDetail.value.id}`)
  if (res.code === 200) {
    alert('删除成功')
    router.push('/')
  } else {
    alert(res.message || '删除失败')
  }
}

async function submitComment() {
  if (!newComment.value.trim()) return

  const res = await post('/comments', {
    postId: postDetail.value.id,
    content: newComment.value.trim()
  })

  if (res.code === 200) {
    newComment.value = ''
    await loadComments(postDetail.value.id)
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

  const res = await post('/comments', {
    postId: postDetail.value.id,
    content: replyContent.value.trim(),
    parentId: parentId
  })

  if (res.code === 200) {
    cancelReply()
    await loadComments(postDetail.value.id)
  } else {
    alert(res.message || '回复失败')
  }
}

async function deleteComment(commentId) {
  if (!confirm('确定要删除这条评论吗？')) return

  const res = await del(`/comments/${commentId}`)
  if (res.code === 200) {
    await loadComments(postDetail.value.id)
  } else {
    alert(res.message || '删除失败')
  }
}

function goBack() {
  router.back()
}

function goHome() {
  router.push('/')
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  return date.toLocaleDateString('zh-CN')
}

function formatNumber(num) {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

function getCategoryStyle(categoryName) {
  const colors = {
    '技术': { bg: '#e0e7ff', color: '#4f46e5' },
    '生活': { bg: '#fce7f3', color: '#db2777' },
    '娱乐': { bg: '#dbeafe', color: '#2563eb' },
    '学习': { bg: '#d1fae5', color: '#059669' }
  }
  const style = colors[categoryName] || { bg: '#f3f4f6', color: '#6b7280' }
  return {
    background: style.bg,
    color: style.color
  }
}

// 生命周期
onMounted(() => {
  isDarkMode.value = localStorage.getItem('darkMode') === 'true'
  checkLogin()
  loadPost()
})
</script>

<style scoped>
.post-detail-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  padding-bottom: 80px;
  position: relative;
  overflow-x: hidden;
}

.dark-mode {
  background: linear-gradient(135deg, #0f0f1e 0%, #1a1a2e 100%);
}

/* 动态背景 */
.animated-bg {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 0;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: float 20s infinite ease-in-out;
}

.orb-1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  top: -100px;
  right: -100px;
  animation-delay: 0s;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  bottom: 20%;
  left: -100px;
  animation-delay: -7s;
}

.orb-3 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  top: 40%;
  right: 10%;
  animation-delay: -14s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(30px, -30px) scale(1.1); }
  50% { transform: translate(-20px, 20px) scale(0.9); }
  75% { transform: translate(20px, 30px) scale(1.05); }
}

.dark-mode .gradient-orb {
  opacity: 0.2;
}

/* 顶部导航 */
.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .top-nav {
  background: rgba(30, 30, 46, 0.8);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.05);
  color: #4a5568;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 1.2rem;
}

.dark-mode .back-btn {
  background: rgba(255, 255, 255, 0.1);
  color: #e2e8f0;
}

.back-btn:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-wrapper {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-wrapper i {
  font-size: 1.2rem;
  color: white;
}

.brand-text {
  font-size: 1.1rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.nav-actions {
  display: flex;
  gap: 10px;
}

.btn-icon {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.05);
  color: #4a5568;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 1.1rem;
}

.dark-mode .btn-icon {
  background: rgba(255, 255, 255, 0.1);
  color: #e2e8f0;
}

.btn-icon:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-2px);
}

/* 主内容 */
.main-content {
  position: relative;
  z-index: 1;
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 20px;
}

.spinner-wrapper {
  position: relative;
  width: 60px;
  height: 60px;
  margin-bottom: 16px;
}

.spinner {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 3px solid transparent;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.spinner-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 3px solid rgba(102, 126, 234, 0.2);
  border-radius: 50%;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 0.95rem;
  color: #718096;
}

/* 帖子头部卡片 */
.post-header-card {
  background: white;
  border-radius: 24px;
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .post-header-card {
  background: rgba(45, 45, 68, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.category-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  margin-bottom: 16px;
}

.post-title {
  font-size: 1.5rem;
  font-weight: 800;
  color: #1a202c;
  line-height: 1.4;
  margin-bottom: 20px;
}

.dark-mode .post-title {
  color: #f7fafc;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(102, 126, 234, 0.2);
  cursor: pointer;
  transition: all 0.3s ease;
}

.author-avatar:hover {
  transform: scale(1.1);
  border-color: #667eea;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.author-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-size: 0.95rem;
  font-weight: 700;
  color: #2d3748;
  cursor: pointer;
  transition: all 0.3s ease;
}

.author-name:hover {
  color: #667eea;
}

.dark-mode .author-name {
  color: #e2e8f0;
}

.dark-mode .author-name:hover {
  color: #a0b4f0;
}

/* 关注作者按钮 */
.follow-author-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  margin-top: 8px;
  border: 1px solid #667eea;
  border-radius: 20px;
  background: transparent;
  color: #667eea;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.follow-author-btn:hover:not(:disabled) {
  background: rgba(102, 126, 234, 0.1);
}

.follow-author-btn.following {
  background: rgba(102, 126, 234, 0.1);
  border-color: transparent;
  color: #666;
}

.dark-mode .follow-author-btn.following {
  color: #a0a0c0;
}

.follow-author-btn.following:hover:not(:disabled) {
  background: rgba(255, 107, 107, 0.1);
  color: #ff6b6b;
}

.follow-author-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.post-time {
  font-size: 0.8rem;
  color: #a0aec0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.9rem;
  color: #718096;
}

.stat-item i {
  color: #667eea;
}

/* 帖子内容卡片 */
.post-content-card {
  background: white;
  border-radius: 24px;
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .post-content-card {
  background: rgba(45, 45, 68, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.content-body {
  font-size: 1rem;
  line-height: 1.8;
  color: #4a5568;
  white-space: pre-wrap;
  margin-bottom: 24px;
}

.dark-mode .content-body {
  color: #cbd5e0;
}

/* 帖子操作 */
.post-actions {
  display: flex;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .post-actions {
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  border: none;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.03);
  color: #718096;
  cursor: pointer;
  transition: all 0.3s ease;
}

.dark-mode .action-btn {
  background: rgba(255, 255, 255, 0.05);
  color: #a0aec0;
}

.action-btn:hover {
  transform: translateY(-2px);
}

.btn-icon-wrapper {
  font-size: 1.2rem;
}

.btn-text {
  font-size: 0.9rem;
  font-weight: 600;
}

.btn-count {
  font-size: 0.85rem;
  opacity: 0.8;
}

.like-btn:hover, .like-btn.active {
  background: #fce7f3;
  color: #db2777;
}

.dark-mode .like-btn:hover, .dark-mode .like-btn.active {
  background: rgba(219, 39, 119, 0.2);
}

.collect-btn:hover, .collect-btn.active {
  background: #dbeafe;
  color: #2563eb;
}

.dark-mode .collect-btn:hover, .dark-mode .collect-btn.active {
  background: rgba(37, 99, 235, 0.2);
}

.comment-btn:hover {
  background: #d1fae5;
  color: #059669;
}

.dark-mode .comment-btn:hover {
  background: rgba(5, 150, 105, 0.2);
}

/* 作者操作 */
.author-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .author-actions {
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.edit-btn, .delete-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 16px;
  border: none;
  border-radius: 10px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.edit-btn {
  background: #e0e7ff;
  color: #4f46e5;
}

.edit-btn:hover {
  background: #4f46e5;
  color: white;
}

.delete-btn {
  background: #fee2e2;
  color: #dc2626;
}

.delete-btn:hover {
  background: #dc2626;
  color: white;
}

/* 评论区卡片 */
.comments-card {
  background: white;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .comments-card {
  background: rgba(45, 45, 68, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.comments-header {
  margin-bottom: 20px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-title i {
  font-size: 1.3rem;
  color: #667eea;
}

.header-title h3 {
  font-size: 1.2rem;
  font-weight: 700;
  color: #2d3748;
}

.dark-mode .header-title h3 {
  color: #e2e8f0;
}

.comment-count {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
}

/* 评论输入区 */
.comment-input-area {
  margin-bottom: 24px;
}

.input-wrapper {
  display: flex;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(102, 126, 234, 0.2);
  flex-shrink: 0;
}

.input-box {
  flex: 1;
  background: rgba(0, 0, 0, 0.03);
  border-radius: 16px;
  padding: 16px;
}

.dark-mode .input-box {
  background: rgba(255, 255, 255, 0.05);
}

.input-box textarea {
  width: 100%;
  border: none;
  background: transparent;
  resize: none;
  font-size: 0.95rem;
  color: #2d3748;
  outline: none;
}

.dark-mode .input-box textarea {
  color: #e2e8f0;
}

.input-box textarea::placeholder {
  color: #a0aec0;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.hint {
  font-size: 0.8rem;
  color: #a0aec0;
}

.submit-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border: none;
  border-radius: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 登录提示 */
.login-tip {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-radius: 16px;
  padding: 30px;
  margin-bottom: 24px;
  text-align: center;
}

.tip-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.tip-content i {
  font-size: 3rem;
  color: #667eea;
}

.tip-content p {
  color: #718096;
  font-size: 0.95rem;
}

.login-btn {
  padding: 10px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 20px;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 评论列表 */
.comments-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 12px;
  animation: slide-up 0.5s ease forwards;
  opacity: 0;
  transform: translateY(20px);
}

@keyframes slide-up {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(102, 126, 234, 0.2);
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 16px;
  padding: 16px;
}

.dark-mode .comment-body {
  background: rgba(255, 255, 255, 0.03);
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 700;
  color: #667eea;
  font-size: 0.95rem;
}

.comment-time {
  font-size: 0.8rem;
  color: #a0aec0;
}

.comment-content {
  color: #4a5568;
  line-height: 1.6;
  margin-bottom: 12px;
}

.dark-mode .comment-content {
  color: #cbd5e0;
}

.comment-actions {
  display: flex;
  gap: 16px;
}

.action-link {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: #718096;
  font-size: 0.85rem;
  cursor: pointer;
  transition: color 0.3s ease;
}

.action-link:hover {
  color: #667eea;
}

.action-link.delete:hover {
  color: #dc2626;
}

/* 回复列表 */
.replies-list {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .replies-list {
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.reply-item {
  display: flex;
  gap: 10px;
  padding: 12px 0;
}

.reply-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.reply-body {
  flex: 1;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.reply-author {
  font-weight: 600;
  color: #667eea;
  font-size: 0.9rem;
}

.reply-time {
  font-size: 0.75rem;
  color: #a0aec0;
}

.reply-content {
  color: #4a5568;
  font-size: 0.9rem;
  line-height: 1.5;
}

.dark-mode .reply-content {
  color: #cbd5e0;
}

/* 回复输入 */
.reply-input-area {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .reply-input-area {
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.reply-input-area textarea {
  width: 100%;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  padding: 12px;
  background: white;
  resize: none;
  font-size: 0.9rem;
  color: #2d3748;
  outline: none;
  margin-bottom: 10px;
}

.dark-mode .reply-input-area textarea {
  background: rgba(45, 45, 68, 0.8);
  border-color: rgba(255, 255, 255, 0.1);
  color: #e2e8f0;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.cancel-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.05);
  color: #718096;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background: rgba(0, 0, 0, 0.1);
}

/* 空评论 */
.empty-comments {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.empty-icon i {
  font-size: 2.5rem;
  color: #667eea;
}

.empty-comments h4 {
  font-size: 1.1rem;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 8px;
}

.dark-mode .empty-comments h4 {
  color: #e2e8f0;
}

.empty-comments p {
  color: #a0aec0;
  font-size: 0.9rem;
}

/* 404 */
.not-found {
  text-align: center;
  padding: 80px 20px;
}

.not-found .empty-icon {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, rgba(220, 38, 38, 0.1) 0%, rgba(239, 68, 68, 0.1) 100%);
  margin-bottom: 24px;
}

.not-found .empty-icon i {
  font-size: 3rem;
  color: #dc2626;
}

.not-found h3 {
  font-size: 1.3rem;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 8px;
}

.dark-mode .not-found h3 {
  color: #e2e8f0;
}

.not-found p {
  color: #a0aec0;
  margin-bottom: 24px;
}

.back-home-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.back-home-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}
</style>