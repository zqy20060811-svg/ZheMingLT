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
        <button class="btn-icon back-btn" @click="goBack">
          <i class="bi bi-arrow-left"></i>
        </button>
        <span class="nav-title">用户主页</span>
      </div>
      <div class="nav-actions">
        <button class="btn-icon theme-btn" @click="toggleTheme">
          <i :class="isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-fill'"></i>
        </button>
      </div>
    </nav>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 用户资料 -->
    <div v-else class="profile-content">
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
              <h3 class="username">{{ user.username }}</h3>
              <p class="user-id">ID: {{ user.id }}</p>
              <p class="user-bio">{{ user.bio || '这个人很懒，什么都没写~' }}</p>
            </div>

            <!-- 关注按钮 -->
            <div class="follow-actions" v-if="!isCurrentUser">
              <button
                v-if="!isFollowing"
                class="follow-btn primary"
                @click="followUser"
                :disabled="followLoading"
              >
                <i v-if="followLoading" class="bi bi-arrow-repeat spinning"></i>
                <i v-else class="bi bi-plus-lg"></i>
                <span>关注</span>
              </button>
              <button
                v-else
                class="follow-btn following"
                @click="unfollowUser"
                :disabled="followLoading"
              >
                <i v-if="followLoading" class="bi bi-arrow-repeat spinning"></i>
                <i v-else class="bi bi-check-lg"></i>
                <span>已关注</span>
              </button>
            </div>
          </div>

          <!-- 统计数据 -->
          <div class="user-stats">
            <div class="stat-item" @click="goToUserPosts">
              <span class="stat-value">{{ formatNumber(stats.posts || 0) }}</span>
              <span class="stat-label">帖子</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item" @click="goToFollowing">
              <span class="stat-value">{{ formatNumber(stats.following || 0) }}</span>
              <span class="stat-label">关注</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item" @click="goToFollowers">
              <span class="stat-value">{{ formatNumber(stats.followers || 0) }}</span>
              <span class="stat-label">粉丝</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-value">{{ formatNumber(stats.likes || 0) }}</span>
              <span class="stat-label">获赞</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 用户的帖子 -->
      <div class="posts-section">
        <div class="section-header">
          <div class="header-icon">
            <i class="bi bi-file-text-fill"></i>
          </div>
          <h3>TA的帖子</h3>
        </div>

        <div v-if="userPosts.length === 0" class="empty-state">
          <div class="empty-icon">
            <i class="bi bi-inbox"></i>
          </div>
          <p>该用户还没有发布帖子</p>
        </div>

        <div v-else class="posts-list">
          <div
            v-for="post in userPosts"
            :key="post.id"
            class="post-card"
            @click="goToPost(post.id)"
          >
            <div class="post-header">
              <span class="category-tag">{{ post.categoryName }}</span>
              <span class="post-time">{{ formatTime(post.createdAt) }}</span>
            </div>
            <h4 class="post-title">{{ post.title }}</h4>
            <p class="post-summary">{{ post.summary || post.content.substring(0, 100) }}...</p>
            <div class="post-stats">
              <span class="stat-item">
                <i class="bi bi-eye"></i> {{ post.viewCount || 0 }}
              </span>
              <span class="stat-item">
                <i class="bi bi-heart"></i> {{ post.likeCount || 0 }}
              </span>
              <span class="stat-item">
                <i class="bi bi-chat"></i> {{ post.commentCount || 0 }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部导航 -->
    <BottomNav />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { get, post, del } from '@/utils/request'
import BottomNav from '@/components/BottomNav.vue'

const router = useRouter()
const route = useRoute()

// 状态
const isDarkMode = ref(false)
const loading = ref(true)
const followLoading = ref(false)
const user = ref({})
const stats = ref({})
const userPosts = ref([])
const isFollowing = ref(false)
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

// 计算属性
const userId = computed(() => route.params.id)
const currentUserId = computed(() => localStorage.getItem('userId'))
const isCurrentUser = computed(() => currentUserId.value === userId.value)

// 初始化
onMounted(() => {
  isDarkMode.value = localStorage.getItem('darkMode') === 'true'
  loadUserData()
})

// 加载用户数据
async function loadUserData() {
  loading.value = true
  try {
    // 加载用户信息
    const userRes = await get(`/users/${userId.value}`)
    if (userRes.code === 200 && userRes.data) {
      user.value = userRes.data
    }

    // 加载关注统计
    const statsRes = await get(`/follows/${userId.value}/stats`)
    if (statsRes.code === 200 && statsRes.data) {
      stats.value = {
        posts: userRes.data?.postCount || 0,
        following: statsRes.data.followingCount || 0,
        followers: statsRes.data.followerCount || 0,
        likes: userRes.data?.likeCount || 0
      }
      isFollowing.value = statsRes.data.isFollowing || false
    }

    // 加载用户帖子
    const postsRes = await get(`/users/${userId.value}/posts`, { page: 1, size: 10 })
    if (postsRes.code === 200 && postsRes.data) {
      userPosts.value = postsRes.data.list || []
    }
  } catch (error) {
    console.error('加载用户数据失败', error)
    alert('加载用户数据失败')
  } finally {
    loading.value = false
  }
}

// 关注用户
async function followUser() {
  if (!currentUserId.value) {
    alert('请先登录')
    router.push('/login')
    return
  }

  followLoading.value = true
  try {
    const res = await post(`/follows/${userId.value}`)
    if (res.code === 200) {
      isFollowing.value = true
      stats.value.followers++
    } else {
      alert(res.message || '关注失败')
    }
  } catch (error) {
    console.error('关注失败', error)
    alert('关注失败，请重试')
  } finally {
    followLoading.value = false
  }
}

// 取消关注
async function unfollowUser() {
  followLoading.value = true
  try {
    const res = await del(`/follows/${userId.value}`)
    if (res.code === 200) {
      isFollowing.value = false
      stats.value.followers--
    } else {
      alert(res.message || '取消关注失败')
    }
  } catch (error) {
    console.error('取消关注失败', error)
    alert('取消关注失败，请重试')
  } finally {
    followLoading.value = false
  }
}

// 跳转到用户的帖子列表
function goToUserPosts() {
  router.push(`/users/${userId.value}/posts`)
}

// 跳转到关注列表
function goToFollowing() {
  router.push(`/follows/${userId.value}/following`)
}

// 跳转到粉丝列表
function goToFollowers() {
  router.push(`/follows/${userId.value}/followers`)
}

// 跳转到帖子详情
function goToPost(postId) {
  router.push(`/post/${postId}`)
}

// 返回上一页
function goBack() {
  router.back()
}

// 切换主题
function toggleTheme() {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
}

// 格式化数字
function formatNumber(num) {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num
}

// 格式化时间
function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return date.toLocaleDateString()
}
</script>

<style scoped>
.user-profile-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf5 100%);
  position: relative;
  padding-bottom: 80px;
}

.dark-mode {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
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
  opacity: 0.4;
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
  left: -50px;
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
  33% { transform: translate(30px, -30px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
}

/* 顶部导航 */
.top-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .top-nav {
  background: rgba(30, 30, 50, 0.8);
  border-bottom-color: rgba(255, 255, 255, 0.05);
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.nav-title {
  font-size: 20px;
  font-weight: 700;
  color: #333;
}

.dark-mode .nav-title {
  color: #e0e0e0;
}

.nav-actions {
  display: flex;
  gap: 10px;
}

.btn-icon {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 10px;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-icon:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-2px);
}

.dark-mode .btn-icon {
  background: rgba(255, 255, 255, 0.1);
  color: #a0a0c0;
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 100px 20px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(102, 126, 234, 0.2);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.spinning {
  animation: spin 1s linear infinite;
}

/* 用户卡片 */
.profile-content {
  position: relative;
  z-index: 1;
  padding: 20px;
}

.user-card {
  position: relative;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 24px;
  overflow: hidden;
  margin-bottom: 20px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .user-card {
  background: rgba(40, 40, 60, 0.9);
  border-color: rgba(255, 255, 255, 0.05);
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
  background-image: radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
                    radial-gradient(circle at 80% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
}

.card-content {
  position: relative;
  padding: 80px 24px 24px;
}

.user-header {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 24px;
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.user-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid white;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.dark-mode .user-avatar {
  border-color: #1a1a2e;
}

.avatar-ring {
  position: absolute;
  top: -6px;
  left: -6px;
  right: -6px;
  bottom: -6px;
  border: 2px solid transparent;
  border-top-color: #667eea;
  border-radius: 50%;
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
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
}

.dark-mode .username {
  color: #e0e0e0;
}

.user-id {
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
}

.user-bio {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.dark-mode .user-bio {
  color: #a0a0c0;
}

/* 关注按钮 */
.follow-actions {
  flex-shrink: 0;
}

.follow-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border: none;
  border-radius: 25px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.follow-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.follow-btn.primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.follow-btn.following {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.follow-btn.following:hover:not(:disabled) {
  background: rgba(255, 107, 107, 0.1);
  color: #ff6b6b;
}

.follow-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* 统计数据 */
.user-stats {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 20px;
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
}

.stat-item:hover {
  transform: translateY(-2px);
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #333;
}

.dark-mode .stat-value {
  color: #e0e0e0;
}

.stat-label {
  font-size: 13px;
  color: #999;
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
  background: rgba(255, 255, 255, 0.9);
  border-radius: 24px;
  padding: 24px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .posts-section {
  background: rgba(40, 40, 60, 0.9);
  border-color: rgba(255, 255, 255, 0.05);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.header-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.section-header h3 {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

.dark-mode .section-header h3 {
  color: #e0e0e0;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  color: #ccc;
  margin-bottom: 16px;
}

.empty-state p {
  color: #999;
}

/* 帖子列表 */
.posts-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.post-card {
  padding: 20px;
  background: rgba(102, 126, 234, 0.03);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.dark-mode .post-card {
  background: rgba(102, 126, 234, 0.05);
}

.post-card:hover {
  background: rgba(102, 126, 234, 0.08);
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
  font-size: 12px;
  border-radius: 20px;
}

.post-time {
  font-size: 13px;
  color: #999;
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.4;
}

.dark-mode .post-title {
  color: #e0e0e0;
}

.post-summary {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 12px;
}

.dark-mode .post-summary {
  color: #a0a0c0;
}

.post-stats {
  display: flex;
  gap: 16px;
}

.post-stats .stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #999;
  flex-direction: row;
}

/* 响应式 */
@media (max-width: 768px) {
  .user-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .follow-actions {
    width: 100%;
    margin-top: 16px;
  }

  .follow-btn {
    width: 100%;
    justify-content: center;
  }

  .user-stats {
    padding: 15px;
  }

  .stat-value {
    font-size: 18px;
  }
}
</style>
