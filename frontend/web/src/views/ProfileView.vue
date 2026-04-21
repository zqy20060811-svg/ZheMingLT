<template>
  <div class="profile-page" :class="{ 'dark-mode': isDarkMode }">
    <!-- 用户信息卡片 -->
    <div class="user-card">
      <div class="card-bg"></div>
      <div class="user-info">
        <img :src="user.avatar || defaultAvatar" class="user-avatar" />
        <div class="user-details">
          <h3 class="username">{{ user.username || '未登录' }}</h3>
          <p class="user-bio">{{ user.bio || '这个人很懒，什么都没写~' }}</p>
        </div>
        <button v-if="isLoggedIn" class="btn-edit" @click="editProfile">
          <i class="bi bi-pencil"></i>
        </button>
      </div>
      
      <!-- 统计数据 -->
      <div class="user-stats">
        <div class="stat-item" @click="goToMyPosts">
          <span class="stat-value">{{ stats.posts || 0 }}</span>
          <span class="stat-label">帖子</span>
        </div>
        <div class="stat-item" @click="goToMyLikes">
          <span class="stat-value">{{ stats.likes || 0 }}</span>
          <span class="stat-label">获赞</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ stats.following || 0 }}</span>
          <span class="stat-label">关注</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ stats.followers || 0 }}</span>
          <span class="stat-label">粉丝</span>
        </div>
      </div>
    </div>

    <!-- 功能菜单 -->
    <div class="menu-section">
      <!-- 内容管理 -->
      <div class="menu-group">
        <h4 class="group-title">内容管理</h4>
        <div class="menu-list">
          <div class="menu-item" @click="goToMyPosts">
            <div class="item-icon" style="background: #e3f2fd; color: #1976d2;">
              <i class="bi bi-file-text"></i>
            </div>
            <span class="item-title">我的帖子</span>
            <i class="bi bi-chevron-right"></i>
          </div>
          <div class="menu-item" @click="goToMyComments">
            <div class="item-icon" style="background: #f3e5f5; color: #7b1fa2;">
              <i class="bi bi-chat-dots"></i>
            </div>
            <span class="item-title">我的评论</span>
            <i class="bi bi-chevron-right"></i>
          </div>
          <div class="menu-item" @click="goToMyCollections">
            <div class="item-icon" style="background: #fff3e0; color: #f57c00;">
              <i class="bi bi-bookmark"></i>
            </div>
            <span class="item-title">我的收藏</span>
            <i class="bi bi-chevron-right"></i>
          </div>
          <div class="menu-item" @click="goToHistory">
            <div class="item-icon" style="background: #e8f5e9; color: #388e3c;">
              <i class="bi bi-clock-history"></i>
            </div>
            <span class="item-title">浏览历史</span>
            <i class="bi bi-chevron-right"></i>
          </div>
        </div>
      </div>

      <!-- 设置 -->
      <div class="menu-group">
        <h4 class="group-title">设置</h4>
        <div class="menu-list">
          <div class="menu-item" @click="toggleTheme">
            <div class="item-icon" style="background: #fce4ec; color: #c2185b;">
              <i :class="isDarkMode ? 'bi bi-sun' : 'bi bi-moon'"></i>
            </div>
            <span class="item-title">{{ isDarkMode ? '浅色模式' : '深色模式' }}</span>
            <i class="bi bi-chevron-right"></i>
          </div>
          <div class="menu-item" @click="goToSettings">
            <div class="item-icon" style="background: #e0f2f1; color: #00796b;">
              <i class="bi bi-gear"></i>
            </div>
            <span class="item-title">账号设置</span>
            <i class="bi bi-chevron-right"></i>
          </div>
          <div class="menu-item" @click="goToAbout">
            <div class="item-icon" style="background: #f5f5f5; color: #616161;">
              <i class="bi bi-info-circle"></i>
            </div>
            <span class="item-title">关于我们</span>
            <i class="bi bi-chevron-right"></i>
          </div>
        </div>
      </div>
    </div>

    <!-- 登录/退出按钮 -->
    <div class="action-section">
      <button v-if="!isLoggedIn" class="btn-primary" @click="goToLogin">
        登录 / 注册
      </button>
      <button v-else class="btn-danger" @click="logout">
        退出登录
      </button>
    </div>

    <BottomNav active="profile" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get, post } from '@/utils/request'
import BottomNav from '@/components/BottomNav.vue'

const router = useRouter()

const isDarkMode = ref(false)
const isLoggedIn = ref(false)
const user = ref({})
const stats = ref({
  posts: 0,
  likes: 0,
  following: 0,
  followers: 0
})

const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

function toggleTheme() {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
}

async function loadUserInfo() {
  const token = localStorage.getItem('token')
  if (!token) {
    isLoggedIn.value = false
    return
  }

  try {
    const res = await get('/users/profile')
    if (res.code === 200) {
      user.value = res.data
      isLoggedIn.value = true
      loadStats()
    } else {
      isLoggedIn.value = false
    }
  } catch (error) {
    isLoggedIn.value = false
  }
}

async function loadStats() {
  try {
    const res = await get('/users/stats')
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('加载统计失败', error)
  }
}

function editProfile() {
  router.push('/settings')
}

function goToMyPosts() {
  if (!checkLogin()) return
  router.push('/my-posts')
}

function goToMyComments() {
  if (!checkLogin()) return
  router.push('/my-comments')
}

function goToMyCollections() {
  if (!checkLogin()) return
  router.push('/my-collections')
}

function goToHistory() {
  if (!checkLogin()) return
  router.push('/history')
}

function goToSettings() {
  if (!checkLogin()) return
  router.push('/settings')
}

function goToAbout() {
  router.push('/about')
}

function goToLogin() {
  router.push('/login')
}

function checkLogin() {
  if (!isLoggedIn.value) {
    router.push('/login')
    return false
  }
  return true
}

async function logout() {
  const token = localStorage.getItem('token')
  if (token) {
    await post('/users/logout')
  }
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('username')
  isLoggedIn.value = false
  user.value = {}
  router.push('/')
}

onMounted(() => {
  isDarkMode.value = localStorage.getItem('darkMode') === 'true'
  loadUserInfo()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8f9fa 0%, #e9ecef 100%);
  padding-bottom: 100px;
}

.dark-mode {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
}

/* 用户卡片 */
.user-card {
  position: relative;
  background: white;
  margin: 16px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}

.dark-mode .user-card {
  background: #1e1e2e;
  box-shadow: 0 4px 20px rgba(0,0,0,0.2);
}

.card-bg {
  height: 100px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.user-info {
  display: flex;
  align-items: flex-start;
  padding: 0 20px 20px;
  margin-top: -40px;
  gap: 16px;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 4px solid white;
  object-fit: cover;
  background: #f0f2f5;
}

.dark-mode .user-avatar {
  border-color: #1e1e2e;
}

.user-details {
  flex: 1;
  padding-top: 40px;
}

.username {
  font-size: 1.2rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.dark-mode .username {
  color: #fff;
}

.user-bio {
  font-size: 0.85rem;
  color: #999;
  line-height: 1.4;
}

.btn-edit {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: #f0f2f5;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  margin-top: 40px;
}

.dark-mode .btn-edit {
  background: #2d2d44;
  color: #aaa;
}

/* 统计数据 */
.user-stats {
  display: flex;
  justify-content: space-around;
  padding: 16px 0;
  border-top: 1px solid #f0f2f5;
}

.dark-mode .user-stats {
  border-top-color: #2d2d44;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}

.stat-value {
  font-size: 1.3rem;
  font-weight: 700;
  color: #333;
}

.dark-mode .stat-value {
  color: #fff;
}

.stat-label {
  font-size: 0.8rem;
  color: #999;
}

/* 菜单区域 */
.menu-section {
  padding: 0 16px;
}

.menu-group {
  margin-bottom: 20px;
}

.group-title {
  font-size: 0.9rem;
  color: #999;
  margin-bottom: 12px;
  padding-left: 8px;
}

.menu-list {
  background: white;
  border-radius: 16px;
  overflow: hidden;
}

.dark-mode .menu-list {
  background: #1e1e2e;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.menu-item:active {
  background: #f5f5f5;
}

.dark-mode .menu-item:active {
  background: #2d2d44;
}

.menu-item:not(:last-child) {
  border-bottom: 1px solid #f5f5f5;
}

.dark-mode .menu-item:not(:last-child) {
  border-bottom-color: #2d2d44;
}

.item-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
}

.item-title {
  flex: 1;
  font-size: 0.95rem;
  color: #333;
}

.dark-mode .item-title {
  color: #fff;
}

.menu-item > i:last-child {
  color: #ccc;
  font-size: 0.9rem;
}

/* 操作按钮 */
.action-section {
  padding: 0 16px;
}

.btn-primary, .btn-danger {
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-danger {
  background: #ffebee;
  color: #e53935;
}

.btn-primary:active, .btn-danger:active {
  transform: scale(0.98);
}
</style>
