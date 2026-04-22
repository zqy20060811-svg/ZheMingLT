<template>
  <div class="profile-page" :class="{ 'dark-mode': isDarkMode }">
    <!-- 动态背景 -->
    <div class="animated-bg">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
    </div>

    <!-- 粒子效果 -->
    <div class="particles" ref="particlesRef"></div>

    <!-- 顶部导航 -->
    <nav class="top-nav">
      <div class="nav-brand">
        <div class="brand-icon">
          <i class="bi bi-person-fill"></i>
        </div>
        <span class="brand-text">个人中心</span>
      </div>
      <button class="btn-icon theme-btn" @click="toggleTheme">
        <i :class="isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-fill'"></i>
      </button>
    </nav>

    <!-- 用户信息卡片 -->
    <div class="user-card-section">
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
              <div class="online-status" v-if="isLoggedIn"></div>
            </div>

            <div class="user-info">
              <h3 class="username">{{ user.username || '游客' }}</h3>
              <p class="user-id" v-if="isLoggedIn">ID: {{ user.id || '000000' }}</p>
              <p class="user-bio">{{ user.bio || '这个人很懒，什么都没写~' }}</p>
            </div>

            <button v-if="isLoggedIn" class="btn-edit" @click="editProfile">
              <i class="bi bi-pencil-square"></i>
              <span>编辑</span>
            </button>
          </div>

          <!-- 等级徽章 -->
          <div class="level-badge" v-if="isLoggedIn">
            <div class="level-icon">
              <i class="bi bi-stars"></i>
            </div>
            <span class="level-text">Lv.{{ Math.floor((stats.posts || 0) / 10) + 1 }}</span>
            <div class="level-progress">
              <div class="progress-bar" :style="{ width: `${((stats.posts || 0) % 10) * 10}%` }"></div>
            </div>
          </div>

          <!-- 统计数据 -->
          <div class="user-stats">
            <div class="stat-item" @click="goToMyPosts">
              <div class="stat-icon posts">
                <i class="bi bi-file-text-fill"></i>
              </div>
              <div class="stat-info">
                <span class="stat-value">{{ formatNumber(stats.posts || 0) }}</span>
                <span class="stat-label">帖子</span>
              </div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item" @click="goToMyLikes">
              <div class="stat-icon likes">
                <i class="bi bi-heart-fill"></i>
              </div>
              <div class="stat-info">
                <span class="stat-value">{{ formatNumber(stats.likes || 0) }}</span>
                <span class="stat-label">获赞</span>
              </div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-icon following">
                <i class="bi bi-people-fill"></i>
              </div>
              <div class="stat-info">
                <span class="stat-value">{{ formatNumber(stats.following || 0) }}</span>
                <span class="stat-label">关注</span>
              </div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-icon followers">
                <i class="bi bi-person-heart"></i>
              </div>
              <div class="stat-info">
                <span class="stat-value">{{ formatNumber(stats.followers || 0) }}</span>
                <span class="stat-label">粉丝</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 功能菜单 -->
    <div class="menu-section">
      <!-- 内容管理 -->
      <div class="menu-group">
        <div class="group-header">
          <div class="group-icon">
            <i class="bi bi-grid-fill"></i>
          </div>
          <span class="group-title">内容管理</span>
        </div>
        <div class="menu-grid">
          <div class="menu-card" @click="goToMyPosts">
            <div class="card-glow"></div>
            <div class="menu-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
              <i class="bi bi-file-text"></i>
            </div>
            <span class="menu-title">我的帖子</span>
            <span class="menu-desc">{{ stats.posts || 0 }} 篇</span>
          </div>
          <div class="menu-card" @click="goToMyComments">
            <div class="card-glow"></div>
            <div class="menu-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
              <i class="bi bi-chat-dots"></i>
            </div>
            <span class="menu-title">我的评论</span>
            <span class="menu-desc">查看全部</span>
          </div>
          <div class="menu-card" @click="goToMyCollections">
            <div class="card-glow"></div>
            <div class="menu-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
              <i class="bi bi-bookmark-star"></i>
            </div>
            <span class="menu-title">我的收藏</span>
            <span class="menu-desc">精彩内容</span>
          </div>
          <div class="menu-card" @click="goToHistory">
            <div class="card-glow"></div>
            <div class="menu-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
              <i class="bi bi-clock-history"></i>
            </div>
            <span class="menu-title">浏览历史</span>
            <span class="menu-desc">最近浏览</span>
          </div>
        </div>
      </div>

      <!-- 设置 -->
      <div class="menu-group">
        <div class="group-header">
          <div class="group-icon">
            <i class="bi bi-gear-fill"></i>
          </div>
          <span class="group-title">设置</span>
        </div>
        <div class="settings-list">
          <div class="settings-item" @click="toggleTheme">
            <div class="item-left">
              <div class="item-icon" :style="{ background: isDarkMode ? 'linear-gradient(135deg, #feca57 0%, #ff9ff3 100%)' : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' }">
                <i :class="isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-fill'"></i>
              </div>
              <span class="item-title">{{ isDarkMode ? '切换浅色模式' : '切换深色模式' }}</span>
            </div>
            <div class="item-right">
              <div class="toggle-switch" :class="{ active: isDarkMode }">
                <div class="toggle-thumb"></div>
              </div>
            </div>
          </div>

          <div class="settings-item" @click="goToSettings">
            <div class="item-left">
              <div class="item-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
                <i class="bi bi-sliders"></i>
              </div>
              <span class="item-title">账号设置</span>
            </div>
            <div class="item-right">
              <i class="bi bi-chevron-right"></i>
            </div>
          </div>

          <div class="settings-item" @click="goToAbout">
            <div class="item-left">
              <div class="item-icon" style="background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);">
                <i class="bi bi-info-circle-fill"></i>
              </div>
              <span class="item-title">关于我们</span>
            </div>
            <div class="item-right">
              <span class="version">v1.0.0</span>
              <i class="bi bi-chevron-right"></i>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 登录/退出按钮 -->
    <div class="action-section">
      <button v-if="!isLoggedIn" class="btn-primary" @click="goToLogin">
        <div class="btn-glow"></div>
        <i class="bi bi-box-arrow-in-right"></i>
        <span>登录 / 注册</span>
      </button>
      <button v-else class="btn-logout" @click="logout">
        <div class="btn-glow"></div>
        <i class="bi bi-box-arrow-right"></i>
        <span>退出登录</span>
      </button>
    </div>

    <!-- 页脚 -->
    <footer class="page-footer">
      <p>&copy; 2024 择明论坛. All rights reserved.</p>
    </footer>

    <BottomNav active="profile" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get, post, clearTokens, getAccessToken } from '@/utils/request'
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
const particlesRef = ref(null)

const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

function toggleTheme() {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
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

async function loadUserInfo() {
  const token = getAccessToken()
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
  const token = getAccessToken()
  if (token) {
    await post('/users/logout')
  }
  clearTokens()
  isLoggedIn.value = false
  user.value = {}
  router.push('/')
}

// 生成粒子
function generateParticles() {
  if (!particlesRef.value) return
  particlesRef.value.innerHTML = ''
  for (let i = 0; i < 30; i++) {
    const particle = document.createElement('div')
    particle.className = 'particle'
    particle.style.left = Math.random() * 100 + '%'
    particle.style.top = Math.random() * 100 + '%'
    particle.style.width = Math.random() * 4 + 2 + 'px'
    particle.style.height = particle.style.width
    particle.style.animationDelay = Math.random() * 5 + 's'
    particle.style.animationDuration = Math.random() * 5 + 5 + 's'
    particlesRef.value.appendChild(particle)
  }
}

onMounted(() => {
  isDarkMode.value = localStorage.getItem('darkMode') === 'true'
  generateParticles()
  loadUserInfo()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 50%, #f8f9fa 100%);
  position: relative;
  overflow-x: hidden;
  padding-bottom: 100px;
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
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  top: -150px;
  right: -150px;
  animation-delay: 0s;
}

.orb-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  bottom: -100px;
  left: -100px;
  animation-delay: -5s;
}

.orb-3 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  top: 30%;
  left: 20%;
  animation-delay: -10s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(30px, -30px) scale(1.1);
  }
  50% {
    transform: translate(-20px, 20px) scale(0.95);
  }
  75% {
    transform: translate(20px, 10px) scale(1.05);
  }
}

/* 粒子效果 */
.particles {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 1;
}

.particle {
  position: absolute;
  background: rgba(102, 126, 234, 0.3);
  border-radius: 50%;
  animation: particle-float 10s infinite ease-in-out;
}

@keyframes particle-float {
  0%, 100% {
    transform: translateY(0) translateX(0);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100vh) translateX(50px);
    opacity: 0;
  }
}

/* 顶部导航 */
.top-nav {
  position: sticky;
  top: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.5);
  z-index: 100;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-icon {
  width: 42px;
  height: 42px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.4rem;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.brand-text {
  font-size: 1.2rem;
  font-weight: 700;
  color: #333;
}

.btn-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  border: none;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-icon:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-2px);
}

/* 用户卡片区域 */
.user-card-section {
  position: relative;
  z-index: 10;
  padding: 20px;
}

.user-card {
  position: relative;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.card-bg {
  position: relative;
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
    radial-gradient(circle at 80% 80%, rgba(255,255,255,0.1) 0%, transparent 50%);
}

.bg-glow {
  position: absolute;
  top: -50%;
  right: -20%;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(255,255,255,0.3) 0%, transparent 70%);
  border-radius: 50%;
}

.card-content {
  padding: 0 24px 24px;
}

/* 用户头部信息 */
.user-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-top: -50px;
  margin-bottom: 20px;
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.user-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 4px solid white;
  object-fit: cover;
  background: #f0f2f5;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.avatar-ring {
  position: absolute;
  top: -6px;
  left: -6px;
  right: -6px;
  bottom: -6px;
  border: 2px solid transparent;
  border-top-color: #667eea;
  border-right-color: #764ba2;
  border-radius: 50%;
  animation: rotate 3s linear infinite;
}

@keyframes rotate {
  to { transform: rotate(360deg); }
}

.online-status {
  position: absolute;
  bottom: 8px;
  right: 8px;
  width: 16px;
  height: 16px;
  background: #4ade80;
  border: 3px solid white;
  border-radius: 50%;
}

.user-info {
  flex: 1;
  padding-top: 55px;
  min-width: 0;
}

.username {
  font-size: 1.4rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
}

.user-id {
  font-size: 0.8rem;
  color: #999;
  margin-bottom: 6px;
}

.user-bio {
  font-size: 0.9rem;
  color: #666;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.btn-edit {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  margin-top: 55px;
  border: none;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-edit:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

/* 等级徽章 */
.level-badge {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-radius: 25px;
  margin-bottom: 20px;
}

.level-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.9rem;
}

.level-text {
  font-weight: 600;
  color: #667eea;
}

.level-progress {
  flex: 1;
  height: 6px;
  background: rgba(102, 126, 234, 0.2);
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border-radius: 3px;
  transition: width 0.5s ease;
}

/* 统计数据 */
.user-stats {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 16px;
  background: rgba(248, 249, 250, 0.8);
  border-radius: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-2px);
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
}

.stat-icon.posts {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.stat-icon.likes {
  background: rgba(255, 107, 107, 0.1);
  color: #ff6b6b;
}

.stat-icon.following {
  background: rgba(79, 172, 254, 0.1);
  color: #4facfe;
}

.stat-icon.followers {
  background: rgba(67, 233, 123, 0.1);
  color: #43e97b;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 1.2rem;
  font-weight: 700;
  color: #333;
}

.stat-label {
  font-size: 0.8rem;
  color: #999;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(0, 0, 0, 0.1);
}

/* 菜单区域 */
.menu-section {
  position: relative;
  z-index: 10;
  padding: 0 20px 20px;
}

.menu-group {
  margin-bottom: 24px;
}

.group-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.group-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1rem;
}

.group-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #333;
}

/* 菜单网格 */
.menu-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.menu-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 16px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.menu-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.card-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, transparent 50%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.menu-card:hover .card-glow {
  opacity: 1;
}

.menu-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.4rem;
}

.menu-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: #333;
}

.menu-desc {
  font-size: 0.8rem;
  color: #999;
}

/* 设置列表 */
.settings-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.settings-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.settings-item:hover {
  transform: translateX(4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
}

.item-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.item-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
}

.item-title {
  font-size: 1rem;
  font-weight: 500;
  color: #333;
}

.item-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.version {
  font-size: 0.85rem;
  color: #999;
}

.item-right > i {
  color: #ccc;
  transition: all 0.3s ease;
}

.settings-item:hover .item-right > i {
  color: #667eea;
  transform: translateX(4px);
}

/* 切换开关 */
.toggle-switch {
  width: 50px;
  height: 28px;
  background: #e0e0e0;
  border-radius: 14px;
  position: relative;
  cursor: pointer;
  transition: background 0.3s ease;
}

.toggle-switch.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.toggle-thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 24px;
  height: 24px;
  background: white;
  border-radius: 50%;
  transition: transform 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.toggle-switch.active .toggle-thumb {
  transform: translateX(22px);
}

/* 操作按钮区域 */
.action-section {
  position: relative;
  z-index: 10;
  padding: 0 20px 20px;
}

.btn-primary,
.btn-logout {
  position: relative;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 16px;
  border: none;
  border-radius: 16px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s ease;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.btn-logout {
  background: rgba(255, 107, 107, 0.1);
  color: #ff6b6b;
  border: 2px solid rgba(255, 107, 107, 0.3);
}

.btn-primary:hover,
.btn-logout:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
}

.btn-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, transparent 60%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.btn-primary:hover .btn-glow,
.btn-logout:hover .btn-glow {
  opacity: 1;
}

/* 页脚 */
.page-footer {
  position: relative;
  z-index: 10;
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 0.85rem;
}

/* 深色模式 */
.dark-mode {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #1a1a2e 100%);
}

.dark-mode .top-nav {
  background: rgba(30, 30, 46, 0.9);
  border-color: rgba(255, 255, 255, 0.1);
}

.dark-mode .brand-text {
  color: #fff;
}

.dark-mode .btn-icon {
  background: rgba(167, 139, 250, 0.2);
  color: #a78bfa;
}

.dark-mode .user-card {
  background: rgba(30, 30, 46, 0.95);
}

.dark-mode .user-avatar {
  border-color: #1e1e2e;
}

.dark-mode .username {
  color: #fff;
}

.dark-mode .user-bio {
  color: #aaa;
}

.dark-mode .btn-edit {
  background: rgba(167, 139, 250, 0.2);
  color: #a78bfa;
}

.dark-mode .btn-edit:hover {
  background: linear-gradient(135deg, #a78bfa 0%, #c084fc 100%);
  color: white;
}

.dark-mode .level-badge {
  background: rgba(167, 139, 250, 0.1);
}

.dark-mode .level-text {
  color: #a78bfa;
}

.dark-mode .user-stats {
  background: rgba(45, 45, 68, 0.8);
}

.dark-mode .stat-value {
  color: #fff;
}

.dark-mode .stat-label {
  color: #666;
}

.dark-mode .stat-divider {
  background: rgba(255, 255, 255, 0.1);
}

.dark-mode .group-title {
  color: #fff;
}

.dark-mode .menu-card,
.dark-mode .settings-item {
  background: rgba(30, 30, 46, 0.95);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
}

.dark-mode .menu-title,
.dark-mode .item-title {
  color: #fff;
}

.dark-mode .menu-desc {
  color: #666;
}

.dark-mode .item-right > i {
  color: #666;
}

.dark-mode .settings-item:hover .item-right > i {
  color: #a78bfa;
}

.dark-mode .version {
  color: #666;
}

.dark-mode .toggle-switch {
  background: #2d2d44;
}

.dark-mode .btn-logout {
  background: rgba(255, 107, 107, 0.2);
  border-color: rgba(255, 107, 107, 0.5);
}

.dark-mode .page-footer {
  color: #666;
}

/* 响应式 */
@media (max-width: 768px) {
  .top-nav {
    padding: 12px 16px;
  }

  .user-card-section,
  .menu-section,
  .action-section {
    padding-left: 16px;
    padding-right: 16px;
  }

  .user-header {
    flex-wrap: wrap;
  }

  .user-avatar {
    width: 80px;
    height: 80px;
  }

  .user-info {
    padding-top: 40px;
  }

  .btn-edit {
    margin-top: 10px;
    margin-left: auto;
  }

  .user-stats {
    flex-wrap: wrap;
    gap: 16px;
  }

  .stat-divider {
    display: none;
  }

  .menu-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .menu-card {
    padding: 16px 12px;
  }

  .menu-icon {
    width: 40px;
    height: 40px;
    font-size: 1.2rem;
  }
}
</style>
