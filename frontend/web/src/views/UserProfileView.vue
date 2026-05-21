<template>
  <div class="user-profile-page" :class="{ 'dark-mode': isDarkMode }">
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
            <i class="bi bi-person-fill"></i>
          </div>
          <span class="brand-text">用户主页</span>
        </div>
      </div>
      <div class="nav-actions">
        <button class="btn-icon" @click="toggleTheme">
          <i :class="isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-fill'"></i>
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

      <template v-else-if="user">
        <!-- 用户信息卡片 -->
        <div class="user-card">
          <div class="card-bg">
            <div class="bg-pattern"></div>
            <div class="bg-glow"></div>
          </div>

          <div class="card-content">
            <div class="user-header">
              <div class="avatar-wrapper">
                <img :src="user.avatar || defaultAvatar" class="user-avatar" />
                <div class="avatar-ring"></div>
              </div>

              <div class="user-info">
                <h3 class="username">{{ user.username || '未知用户' }}</h3>
                <p class="user-id">ID: {{ user.id }}</p>
                <p class="user-bio">{{ user.bio || '这个人很懒，什么都没写~' }}</p>
              </div>

              <!-- 关注按钮（如果不是自己） -->
              <button 
                v-if="!isCurrentUser" 
                class="follow-btn"
                :class="{ following: isFollowing }"
                @click="toggleFollow"
              >
                <i class="bi" :class="isFollowing ? 'bi-person-check-fill' : 'bi-person-plus-fill'"></i>
                {{ isFollowing ? '已关注' : '关注' }}
              </button>
            </div>

            <!-- 统计数据 -->
            <div class="user-stats">
              <div class="stat-item">
                <span class="stat-value">{{ formatNumber(stats.posts || 0) }}</span>
                <span class="stat-label">帖子</span>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item">
                <span class="stat-value">{{ formatNumber(stats.likes || 0) }}</span>
                <span class="stat-label">获赞</span>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item" @click="showFollowing">
                <span class="stat-value">{{ formatNumber(stats.following || 0) }}</span>
                <span class="stat-label">关注</span>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item" @click="showFollowers">
                <span class="stat-value">{{ formatNumber(stats.followers || 0) }}</span>
                <span class="stat-label">粉丝</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 用户帖子列表 -->
        <div class="posts-section">
          <div class="section-header">
            <h3>
              <i class="bi bi-file-text-fill"></i>
              TA的帖子
            </h3>
            <span class="post-count">共 {{ posts.length }} 篇</span>
          </div>

          <div v-if="posts.length > 0" class="posts-list">
            <div 
              v-for="post in posts" 
              :key="post.id" 
              class="post-card"
              @click="goToPost(post.id)"
            >
              <div class="post-header">
                <span class="category-tag">{{ post.categoryName || '未分类' }}</span>
                <span class="post-time">{{ formatTime(post.createdAt) }}</span>
              </div>
              <h4 class="post-title">{{ post.title }}</h4>
              <p class="post-summary">{{ post.summary || post.content?.substring(0, 100) + '...' }}</p>
              <div class="post-stats">
                <span><i class="bi bi-eye"></i> {{ post.viewCount || 0 }}</span>
                <span><i class="bi bi-heart"></i> {{ post.likeCount || 0 }}</span>
                <span><i class="bi bi-chat"></i> {{ post.commentCount || 0 }}</span>
              </div>
            </div>
          </div>

          <div v-else class="empty-state">
            <i class="bi bi-inbox"></i>
            <p>该用户还没有发布帖子</p>
          </div>
        </div>
      </template>

      <!-- 用户不存在 -->
      <div v-else class="not-found">
        <i class="bi bi-exclamation-circle"></i>
        <h3>用户不存在</h3>
        <p>该用户可能已被删除或不存在</p>
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
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get, post, del, getAccessToken } from '@/utils/request'
import BottomNav from '@/components/BottomNav.vue'

const route = useRoute()
const router = useRouter()

// 状态
const loading = ref(true)
const isDarkMode = ref(false)
const isLoggedIn = ref(false)
const currentUserId = ref(null)

const user = ref(null)
const posts = ref([])
const stats = ref({})
const isFollowing = ref(false)

const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

// 计算属性
const isCurrentUser = computed(() => {
  return currentUserId.value && user.value && currentUserId.value === user.value.id
})

// 方法
function toggleTheme() {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
}

function checkLogin() {
  const token = getAccessToken()
  isLoggedIn.value = !!token
  currentUserId.value = parseInt(localStorage.getItem('userId')) || null
}

async function loadUserData() {
  const userId = route.params.id
  if (!userId) {
    loading.value = false
    return
  }

  try {
    // 获取用户信息
    const userRes = await get(`/users/${userId}`)
    if (userRes.code === 200) {
      user.value = userRes.data
    } else {
      user.value = null
      loading.value = false
      return
    }

    // 获取用户统计
    const statsRes = await get(`/users/${userId}/stats`)
    if (statsRes.code === 200) {
      stats.value = statsRes.data
    }

    // 获取用户帖子
    const postsRes = await get(`/users/${userId}/posts`)
    if (postsRes.code === 200) {
      posts.value = postsRes.data?.list || postsRes.data?.content || postsRes.data || []
    } else {
      posts.value = []
    }

    // 检查是否已关注（如果已登录且不是查看自己）
    if (isLoggedIn.value && !isCurrentUser.value) {
      const followRes = await get(`/follows/check/${userId}`)
      if (followRes.code === 200) {
        isFollowing.value = followRes.data
      }
    }
  } catch (error) {
    console.error('加载用户数据失败:', error)
    posts.value = []
  } finally {
    loading.value = false
  }
}

async function toggleFollow() {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }

  const userId = user.value.id
  if (!userId) return

  if (isFollowing.value) {
    // 取消关注
    const res = await del(`/follows/${userId}`)
    if (res.code === 200) {
      isFollowing.value = false
      stats.value.followers = Math.max(0, (stats.value.followers || 0) - 1)
    }
  } else {
    // 关注
    const res = await post(`/follows/${userId}`)
    if (res.code === 200) {
      isFollowing.value = true
      stats.value.followers = (stats.value.followers || 0) + 1
    }
  }
}

function goBack() {
  router.back()
}

function goHome() {
  router.push('/')
}

function goToPost(postId) {
  router.push(`/post/${postId}`)
}

function showFollowing() {
  // TODO: 显示关注列表
  alert('关注列表功能开发中...')
}

function showFollowers() {
  // TODO: 显示粉丝列表
  alert('粉丝列表功能开发中...')
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

// 监听路由变化
watch(() => route.params.id, () => {
  loadUserData()
})

// 生命周期
onMounted(() => {
  isDarkMode.value = localStorage.getItem('darkMode') === 'true'
  checkLogin()
  loadUserData()
})
</script>

<style scoped>
.user-profile-page {
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

/* 用户卡片 */
.user-card {
  position: relative;
  background: white;
  border-radius: 24px;
  overflow: hidden;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.dark-mode .user-card {
  background: rgba(45, 45, 68, 0.8);
}

.card-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 120px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
}

.bg-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 20% 50%, rgba(255,255,255,0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 50%, rgba(255,255,255,0.1) 0%, transparent 50%);
}

.bg-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(255,255,255,0.2) 0%, transparent 70%);
}

.card-content {
  position: relative;
  padding: 80px 24px 24px;
}

.user-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 24px;
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.dark-mode .user-avatar {
  border-color: #2d2d44;
}

.avatar-ring {
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  border-radius: 50%;
  border: 2px solid transparent;
  border-top-color: #667eea;
  border-right-color: #764ba2;
  animation: rotate 3s linear infinite;
}

@keyframes rotate {
  to { transform: rotate(360deg); }
}

.user-info {
  flex: 1;
  min-width: 0;
}

.username {
  font-size: 1.4rem;
  font-weight: 700;
  color: #1a202c;
  margin-bottom: 4px;
}

.dark-mode .username {
  color: #f7fafc;
}

.user-id {
  font-size: 0.8rem;
  color: #a0aec0;
  margin-bottom: 8px;
}

.user-bio {
  font-size: 0.9rem;
  color: #718096;
  line-height: 1.5;
}

.dark-mode .user-bio {
  color: #a0aec0;
}

.follow-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border: none;
  border-radius: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.follow-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.follow-btn.following {
  background: #e2e8f0;
  color: #4a5568;
}

.dark-mode .follow-btn.following {
  background: #4a5568;
  color: #e2e8f0;
}

.follow-btn.following:hover {
  background: #fc8181;
  color: white;
}

/* 统计数据 */
.user-stats {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 16px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 16px;
}

.dark-mode .user-stats {
  background: rgba(102, 126, 234, 0.1);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 8px 16px;
  border-radius: 12px;
}

.stat-item:hover {
  background: rgba(102, 126, 234, 0.1);
}

.stat-value {
  font-size: 1.2rem;
  font-weight: 700;
  color: #667eea;
}

.stat-label {
  font-size: 0.8rem;
  color: #718096;
}

.dark-mode .stat-label {
  color: #a0aec0;
}

.stat-divider {
  width: 1px;
  height: 30px;
  background: rgba(0, 0, 0, 0.1);
}

.dark-mode .stat-divider {
  background: rgba(255, 255, 255, 0.1);
}

/* 帖子区域 */
.posts-section {
  background: white;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.dark-mode .posts-section {
  background: rgba(45, 45, 68, 0.8);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.1rem;
  font-weight: 700;
  color: #1a202c;
}

.dark-mode .section-header h3 {
  color: #f7fafc;
}

.section-header h3 i {
  color: #667eea;
}

.post-count {
  font-size: 0.85rem;
  color: #718096;
}

.dark-mode .post-count {
  color: #a0aec0;
}

/* 帖子列表 */
.posts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  padding: 20px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.post-card:hover {
  background: rgba(102, 126, 234, 0.1);
  border-color: rgba(102, 126, 234, 0.2);
  transform: translateY(-2px);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.category-tag {
  padding: 4px 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 0.75rem;
  font-weight: 600;
  border-radius: 20px;
}

.post-time {
  font-size: 0.8rem;
  color: #a0aec0;
}

.post-title {
  font-size: 1rem;
  font-weight: 600;
  color: #1a202c;
  margin-bottom: 8px;
  line-height: 1.4;
}

.dark-mode .post-title {
  color: #f7fafc;
}

.post-summary {
  font-size: 0.9rem;
  color: #718096;
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.dark-mode .post-summary {
  color: #a0aec0;
}

.post-stats {
  display: flex;
  gap: 16px;
}

.post-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.85rem;
  color: #a0aec0;
}

.post-stats i {
  font-size: 0.9rem;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 20px;
  color: #a0aec0;
}

.empty-state i {
  font-size: 3rem;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  font-size: 0.95rem;
}

/* 未找到 */
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 20px;
  text-align: center;
}

.not-found i {
  font-size: 4rem;
  color: #a0aec0;
  margin-bottom: 20px;
}

.not-found h3 {
  font-size: 1.3rem;
  color: #1a202c;
  margin-bottom: 8px;
}

.dark-mode .not-found h3 {
  color: #f7fafc;
}

.not-found p {
  font-size: 0.9rem;
  color: #718096;
  margin-bottom: 24px;
}

.back-home-btn {
  display: flex;
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
