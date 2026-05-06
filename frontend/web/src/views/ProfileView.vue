<template>
  <div class="manor-profile" :class="{ 'night-mode': isDarkMode }">
    <!-- 庄园背景 -->
    <ManorBackground :isDarkMode="isDarkMode" />

    <!-- 顶部导航 -->
    <nav class="profile-top-nav">
      <div class="nav-brand">
        <i class="bi bi-person-heart"></i>
        <span>个人中心</span>
      </div>
      <button class="theme-btn" @click="toggleTheme">
        <i :class="isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-stars-fill'"></i>
      </button>
    </nav>

    <!-- 主内容 -->
    <main class="profile-content">
      <!-- 用户信息卡片 -->
      <div class="user-profile-card">
        <div class="card-header-bg"></div>
        <div class="card-body">
          <div class="user-header">
            <div class="avatar-wrapper" @click="triggerAvatarUpload" v-if="isLoggedIn">
              <img :src="user.avatar || defaultAvatar" class="user-avatar" />
              <div class="avatar-upload-overlay">
                <i class="bi bi-camera-fill"></i>
              </div>
              <div class="avatar-badge" v-if="isLoggedIn">
                <i class="bi bi-check-circle-fill"></i>
              </div>
              <input
                ref="avatarInput"
                type="file"
                accept="image/*"
                style="display: none"
                @change="handleAvatarChange"
              />
            </div>
            <div class="avatar-wrapper" v-else>
              <img :src="user.avatar || defaultAvatar" class="user-avatar" />
            </div>
            <div class="user-info">
              <div class="username-row" v-if="isLoggedIn">
                <h3 v-if="!editingUsername" class="username">{{ user.username || '游客' }}</h3>
                <input
                  v-else
                  v-model="newUsername"
                  class="username-input"
                  maxlength="20"
                  @keyup.enter="saveUsername"
                  @keyup.esc="cancelEditUsername"
                  ref="usernameInput"
                />
                <button v-if="!editingUsername" class="edit-name-btn" @click="startEditUsername">
                  <i class="bi bi-pencil-fill"></i>
                </button>
                <div v-else class="edit-actions">
                  <button class="save-btn" @click="saveUsername">
                    <i class="bi bi-check-lg"></i>
                  </button>
                  <button class="cancel-btn" @click="cancelEditUsername">
                    <i class="bi bi-x-lg"></i>
                  </button>
                </div>
              </div>
              <h3 v-else class="username">{{ user.username || '游客' }}</h3>
              <p class="user-id" v-if="isLoggedIn">ID: {{ user.id || '000000' }}</p>
              <p class="user-bio">{{ user.bio || '这个人很懒，什么都没写~' }}</p>
            </div>
            <button v-if="isLoggedIn" class="edit-btn" @click="editProfile">
              <i class="bi bi-pencil-square"></i>
            </button>
          </div>

          <!-- 等级 -->
          <div class="level-bar" v-if="isLoggedIn">
            <div class="level-info">
              <i class="bi bi-stars"></i>
              <span>等级 {{ Math.floor((stats.posts || 0) / 10) + 1 }}</span>
            </div>
            <div class="progress-track">
              <div class="progress-fill" :style="{ width: `${((stats.posts || 0) % 10) * 10}%` }"></div>
            </div>
          </div>

          <!-- 统计数据 -->
          <div class="stats-grid">
            <div class="stat-box" @click="goToMyPosts">
              <span class="stat-num">{{ formatNumber(stats.posts || 0) }}</span>
              <span class="stat-label">帖子</span>
            </div>
            <div class="stat-box" @click="goToMyLikes">
              <span class="stat-num">{{ formatNumber(stats.likes || 0) }}</span>
              <span class="stat-label">获赞</span>
            </div>
            <div class="stat-box">
              <span class="stat-num">{{ formatNumber(stats.following || 0) }}</span>
              <span class="stat-label">关注</span>
            </div>
            <div class="stat-box">
              <span class="stat-num">{{ formatNumber(stats.followers || 0) }}</span>
              <span class="stat-label">粉丝</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 功能菜单 -->
      <div class="menu-section">
        <!-- 内容管理 -->
        <div class="menu-group">
          <div class="group-title">
            <i class="bi bi-grid-fill"></i>
            <span>内容管理</span>
          </div>
          <div class="menu-list">
            <div class="menu-item" @click="goToMyPosts">
              <div class="item-icon" style="background: linear-gradient(135deg, #8FBC8F, #7CB87C);">
                <i class="bi bi-file-text"></i>
              </div>
              <span class="item-title">我的帖子</span>
              <span class="item-badge">{{ stats.posts || 0 }}</span>
              <i class="bi bi-chevron-right item-arrow"></i>
            </div>
            <div class="menu-item" @click="goToMyComments">
              <div class="item-icon" style="background: linear-gradient(135deg, #87CEEB, #5F9EA0);">
                <i class="bi bi-chat-dots"></i>
              </div>
              <span class="item-title">我的评论</span>
              <i class="bi bi-chevron-right item-arrow"></i>
            </div>
            <div class="menu-item" @click="goToMyCollections">
              <div class="item-icon" style="background: linear-gradient(135deg, #DDA0DD, #DA70D6);">
                <i class="bi bi-bookmark-star"></i>
              </div>
              <span class="item-title">我的收藏</span>
              <i class="bi bi-chevron-right item-arrow"></i>
            </div>
            <div class="menu-item" @click="goToHistory">
              <div class="item-icon" style="background: linear-gradient(135deg, #F0E68C, #BDB76B);">
                <i class="bi bi-clock-history"></i>
              </div>
              <span class="item-title">浏览历史</span>
              <i class="bi bi-chevron-right item-arrow"></i>
            </div>
          </div>
        </div>

        <!-- 设置 -->
        <div class="menu-group">
          <div class="group-title">
            <i class="bi bi-gear-fill"></i>
            <span>设置</span>
          </div>
          <div class="menu-list">
            <div class="menu-item" @click="toggleTheme">
              <div class="item-icon" :style="{ background: isDarkMode ? 'linear-gradient(135deg, #FFD700, #FFA500)' : 'linear-gradient(135deg, #8FBC8F, #7CB87C)' }">
                <i :class="isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-stars-fill'"></i>
              </div>
              <span class="item-title">{{ isDarkMode ? '切换日间模式' : '切换夜间模式' }}</span>
              <div class="toggle-switch" :class="{ active: isDarkMode }">
                <div class="toggle-thumb"></div>
              </div>
            </div>
            <div class="menu-item" @click="goToSettings">
              <div class="item-icon" style="background: linear-gradient(135deg, #FFB6C1, #FF69B4);">
                <i class="bi bi-sliders"></i>
              </div>
              <span class="item-title">账号设置</span>
              <i class="bi bi-chevron-right item-arrow"></i>
            </div>
            <div class="menu-item" @click="goToAbout">
              <div class="item-icon" style="background: linear-gradient(135deg, #98D8C8, #7FCDCD);">
                <i class="bi bi-info-circle-fill"></i>
              </div>
              <span class="item-title">关于我们</span>
              <span class="version">v1.0.0</span>
              <i class="bi bi-chevron-right item-arrow"></i>
            </div>
          </div>
        </div>
      </div>

      <!-- 登录/退出按钮 -->
      <div class="action-section">
        <button v-if="!isLoggedIn" class="btn-login" @click="goToLogin">
          <i class="bi bi-box-arrow-in-right"></i>
          <span>登录 / 注册</span>
        </button>
        <button v-else class="btn-logout" @click="logout">
          <i class="bi bi-box-arrow-right"></i>
          <span>退出登录</span>
        </button>
      </div>
    </main>

    <!-- 底部导航 -->
    <ManorBottomNav :isDarkMode="isDarkMode" />
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { get, post, upload, clearTokens, getAccessToken } from '@/utils/request'
import ManorBackground from '@/components/ManorBackground.vue'
import ManorBottomNav from '@/components/ManorBottomNav.vue'

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
const editingUsername = ref(false)
const newUsername = ref('')
const usernameInput = ref(null)
const avatarInput = ref(null)

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

// 用户名编辑功能
function startEditUsername() {
  newUsername.value = user.value.username || ''
  editingUsername.value = true
  nextTick(() => {
    usernameInput.value?.focus()
  })
}

function cancelEditUsername() {
  editingUsername.value = false
  newUsername.value = ''
}

async function saveUsername() {
  const username = newUsername.value.trim()
  if (!username) {
    alert('用户名不能为空')
    return
  }
  if (username.length < 2 || username.length > 20) {
    alert('用户名长度必须在2-20个字符之间')
    return
  }
  if (username === user.value.username) {
    editingUsername.value = false
    return
  }

  try {
    const res = await post('/users/username', { username })
    if (res.code === 200) {
      user.value.username = res.data.username
      editingUsername.value = false
      // 更新本地存储的用户信息
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      userInfo.username = res.data.username
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    } else {
      alert(res.message || '修改用户名失败')
    }
  } catch (error) {
    console.error('修改用户名失败', error)
    alert('修改用户名失败，请稍后重试')
  }
}

// 头像上传功能
function triggerAvatarUpload() {
  avatarInput.value?.click()
}

async function handleAvatarChange(event) {
  const file = event.target.files[0]
  if (!file) return

  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    alert('请上传图片文件')
    return
  }

  // 检查文件大小（最大2MB）
  if (file.size > 2 * 1024 * 1024) {
    alert('图片大小不能超过2MB')
    return
  }

  try {
    const formData = new FormData()
    formData.append('file', file)

    const res = await upload('/users/avatar', formData)
    if (res.code === 200) {
      user.value.avatar = res.data.avatar
      // 更新本地存储的用户信息
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      userInfo.avatar = res.data.avatar
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    } else {
      alert(res.message || '上传头像失败')
    }
  } catch (error) {
    console.error('上传头像失败', error)
    alert('上传头像失败，请稍后重试')
  }

  // 清空input，允许重复选择同一文件
  event.target.value = ''
}

onMounted(() => {
  isDarkMode.value = localStorage.getItem('darkMode') === 'true'
  loadUserInfo()
})
</script>

<style scoped>
.manor-profile {
  min-height: 100vh;
  position: relative;
  padding-bottom: 100px;
}

/* 顶部导航 */
.profile-top-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 70px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(139, 115, 85, 0.15);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  z-index: 100;
}

.night-mode .profile-top-nav {
  background: rgba(30, 35, 45, 0.9);
  border-bottom-color: rgba(100, 110, 130, 0.2);
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
  color: #5a7c5a;
  font-family: 'Georgia', serif;
}

.night-mode .nav-brand {
  color: #8ab88a;
}

.nav-brand i {
  font-size: 24px;
  color: #8FBC8F;
}

.night-mode .nav-brand i {
  color: #7ac87a;
}

.theme-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: rgba(139, 115, 85, 0.1);
  color: #8B7355;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.night-mode .theme-btn {
  background: rgba(200, 180, 150, 0.1);
  color: #D4C596;
}

.theme-btn:hover {
  background: rgba(139, 115, 85, 0.2);
  transform: rotate(15deg);
}

/* 主内容 */
.profile-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 90px 20px 40px;
  position: relative;
  z-index: 1;
}

/* 用户卡片 */
.user-profile-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(139, 188, 143, 0.2);
  margin-bottom: 24px;
}

.night-mode .user-profile-card {
  background: rgba(40, 45, 55, 0.95);
  border-color: rgba(74, 124, 90, 0.2);
}

.card-header-bg {
  height: 100px;
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
}

.card-body {
  padding: 0 24px 24px;
}

.user-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-top: -40px;
  margin-bottom: 20px;
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
  cursor: pointer;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 4px solid white;
  object-fit: cover;
  background: #f0f2f5;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.night-mode .user-avatar {
  border-color: #2d3748;
}

.avatar-upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  border: 4px solid transparent;
}

.avatar-wrapper:hover .avatar-upload-overlay {
  opacity: 1;
}

.avatar-upload-overlay i {
  color: white;
  font-size: 24px;
}

.avatar-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 24px;
  height: 24px;
  background: #8FBC8F;
  border: 2px solid white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
}

.night-mode .avatar-badge {
  border-color: #2d3748;
}

.user-info {
  flex: 1;
  padding-top: 45px;
  min-width: 0;
}

.username-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.username {
  font-size: 20px;
  font-weight: 700;
  color: #2d3748;
  font-family: 'Georgia', serif;
}

.night-mode .username {
  color: #e2e8f0;
}

.username-input {
  font-size: 18px;
  font-weight: 700;
  color: #2d3748;
  font-family: 'Georgia', serif;
  border: 2px solid #8FBC8F;
  border-radius: 8px;
  padding: 4px 10px;
  background: white;
  outline: none;
  width: 150px;
}

.night-mode .username-input {
  background: #2d3748;
  color: #e2e8f0;
  border-color: #4a7c4a;
}

.edit-name-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: rgba(139, 188, 143, 0.2);
  color: #5a7c5a;
  border-radius: 50%;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.night-mode .edit-name-btn {
  background: rgba(74, 124, 90, 0.3);
  color: #8ab88a;
}

.edit-name-btn:hover {
  background: #8FBC8F;
  color: white;
}

.edit-actions {
  display: flex;
  gap: 6px;
}

.save-btn,
.cancel-btn {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 50%;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.save-btn {
  background: #8FBC8F;
  color: white;
}

.save-btn:hover {
  background: #7CB87C;
}

.cancel-btn {
  background: rgba(255, 107, 107, 0.2);
  color: #ff6b6b;
}

.cancel-btn:hover {
  background: rgba(255, 107, 107, 0.3);
}

.user-id {
  font-size: 12px;
  color: #8a9a8a;
  margin-bottom: 6px;
}

.night-mode .user-id {
  color: #7a9a8a;
}

.user-bio {
  font-size: 14px;
  color: #6a7c6a;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.night-mode .user-bio {
  color: #9ab89a;
}

.edit-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  margin-top: 45px;
  border: none;
  background: rgba(139, 188, 143, 0.15);
  color: #5a7c5a;
  border-radius: 50%;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.night-mode .edit-btn {
  background: rgba(74, 124, 90, 0.2);
  color: #8ab88a;
}

.edit-btn:hover {
  background: #8FBC8F;
  color: white;
  transform: scale(1.05);
}

/* 等级条 */
.level-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(139, 188, 143, 0.1);
  border-radius: 16px;
  margin-bottom: 20px;
}

.night-mode .level-bar {
  background: rgba(74, 124, 90, 0.15);
}

.level-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  color: #5a7c5a;
  font-size: 14px;
  white-space: nowrap;
}

.night-mode .level-info {
  color: #8ab88a;
}

.level-info i {
  color: #FFD700;
}

.progress-track {
  flex: 1;
  height: 6px;
  background: rgba(139, 188, 143, 0.2);
  border-radius: 3px;
  overflow: hidden;
}

.night-mode .progress-track {
  background: rgba(74, 124, 90, 0.2);
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #8FBC8F, #7CB87C);
  border-radius: 3px;
  transition: width 0.5s ease;
}

/* 统计数据 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 16px 8px;
  background: rgba(139, 188, 143, 0.08);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.night-mode .stat-box {
  background: rgba(74, 124, 90, 0.1);
}

.stat-box:hover {
  transform: translateY(-2px);
  background: rgba(139, 188, 143, 0.15);
}

.stat-num {
  font-size: 20px;
  font-weight: 700;
  color: #5a7c5a;
}

.night-mode .stat-num {
  color: #8ab88a;
}

.stat-label {
  font-size: 12px;
  color: #8a9a8a;
}

.night-mode .stat-label {
  color: #7a9a8a;
}

/* 菜单区域 */
.menu-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-bottom: 24px;
}

.menu-group {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(139, 188, 143, 0.2);
}

.night-mode .menu-group {
  background: rgba(40, 45, 55, 0.95);
  border-color: rgba(74, 124, 90, 0.2);
}

.group-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 700;
  color: #2d3748;
  font-family: 'Georgia', serif;
}

.night-mode .group-title {
  color: #e2e8f0;
}

.group-title i {
  color: #8FBC8F;
}

.night-mode .group-title i {
  color: #7ac87a;
}

.menu-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: rgba(139, 188, 143, 0.06);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.night-mode .menu-item {
  background: rgba(74, 124, 90, 0.08);
}

.menu-item:hover {
  background: rgba(139, 188, 143, 0.12);
  transform: translateX(4px);
}

.night-mode .menu-item:hover {
  background: rgba(74, 124, 90, 0.15);
}

.item-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  flex-shrink: 0;
}

.item-title {
  flex: 1;
  font-size: 15px;
  font-weight: 500;
  color: #2d3748;
}

.night-mode .item-title {
  color: #e2e8f0;
}

.item-badge {
  padding: 4px 10px;
  background: rgba(139, 188, 143, 0.2);
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  color: #5a7c5a;
}

.night-mode .item-badge {
  background: rgba(74, 124, 90, 0.25);
  color: #8ab88a;
}

.item-arrow {
  color: #8a9a8a;
  font-size: 14px;
}

.night-mode .item-arrow {
  color: #7a9a8a;
}

.version {
  font-size: 12px;
  color: #8a9a8a;
}

.night-mode .version {
  color: #7a9a8a;
}

/* 切换开关 */
.toggle-switch {
  width: 44px;
  height: 24px;
  background: rgba(139, 188, 143, 0.3);
  border-radius: 12px;
  position: relative;
  cursor: pointer;
  transition: background 0.3s ease;
}

.night-mode .toggle-switch {
  background: rgba(74, 124, 90, 0.3);
}

.toggle-switch.active {
  background: #8FBC8F;
}

.toggle-thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 20px;
  height: 20px;
  background: white;
  border-radius: 50%;
  transition: transform 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.toggle-switch.active .toggle-thumb {
  transform: translateX(20px);
}

/* 操作按钮 */
.action-section {
  display: flex;
  justify-content: center;
}

.btn-login,
.btn-logout {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 16px 48px;
  border: none;
  border-radius: 16px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-login {
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  color: white;
  box-shadow: 0 4px 15px rgba(124, 184, 124, 0.3);
}

.btn-login:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(124, 184, 124, 0.4);
}

.btn-logout {
  background: rgba(255, 107, 107, 0.1);
  color: #ff6b6b;
  border: 2px solid rgba(255, 107, 107, 0.3);
}

.btn-logout:hover {
  background: rgba(255, 107, 107, 0.2);
  transform: translateY(-2px);
}

/* 响应式 */
@media (max-width: 640px) {
  .profile-content {
    padding: 80px 16px 40px;
  }

  .user-header {
    flex-wrap: wrap;
  }

  .user-avatar {
    width: 64px;
    height: 64px;
  }

  .user-info {
    padding-top: 35px;
  }

  .edit-btn {
    margin-top: 35px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .menu-group {
    padding: 16px;
  }
}
</style>