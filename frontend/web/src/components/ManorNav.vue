<template>
  <nav class="manor-nav" :class="{ 'night-mode': isDarkMode }">
    <div class="nav-content">
      <div class="nav-brand" @click="goHome">
        <div class="brand-icon">
          <i class="bi bi-house-heart-fill"></i>
        </div>
        <span class="brand-text">择明庄园</span>
      </div>
      
      <div class="nav-links">
        <router-link
          v-for="item in navItems"
          :key="item.name"
          :to="item.path"
          :class="['nav-link', { active: isActive(item.name) }]"
        >
          <i :class="item.icon"></i>
          <span>{{ item.label }}</span>
        </router-link>
      </div>
      
      <div class="nav-actions">
        <button class="theme-btn text-btn" @click="toggleTheme">
          <span class="btn-text">{{ isDarkMode ? '明' : '暗' }}</span>
        </button>
        <div class="user-menu" v-if="isLoggedIn">
          <router-link to="/notifications" class="notification-btn text-btn">
            <span class="btn-text">消</span>
            <span v-if="unreadCount > 0" class="badge">{{ unreadCount }}</span>
          </router-link>
          <router-link to="/profile" class="avatar-btn text-btn">
            <span class="btn-text">我</span>
          </router-link>
        </div>
        <router-link v-else to="/login" class="login-btn">
          <span>登录</span>
        </router-link>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get } from '@/utils/request'

const route = useRoute()
const router = useRouter()

const props = defineProps({
  isDarkMode: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toggle-theme'])

const isLoggedIn = ref(false)
const userAvatar = ref('')
const unreadCount = ref(0)
let refreshInterval = null

const navItems = [
  { name: 'home', path: '/', label: '首页', icon: 'bi bi-house-door-fill' },
  { name: 'category', path: '/category', label: '分类', icon: 'bi bi-grid-fill' },
  { name: 'search', path: '/search', label: '搜索', icon: 'bi bi-search-heart' }
]

function isActive(name) {
  return route.name === name
}

function goHome() {
  router.push('/')
}

function toggleTheme() {
  emit('toggle-theme')
}

async function loadUserInfo() {
  const token = localStorage.getItem('accessToken')
  if (!token) {
    isLoggedIn.value = false
    return
  }
  
  try {
    const res = await get('/users/profile')
    if (res.code === 200) {
      isLoggedIn.value = true
      userAvatar.value = res.data.avatar
    }
  } catch (error) {
    isLoggedIn.value = false
  }
}

async function loadUnreadCount() {
  if (!isLoggedIn.value) return
  try {
    const res = await get('/notifications/unread-count')
    if (res.code === 200) {
      unreadCount.value = res.data || 0
    }
  } catch (error) {
    console.error('加载未读消息失败', error)
  }
}

onMounted(() => {
  loadUserInfo()
  loadUnreadCount()
  refreshInterval = setInterval(loadUnreadCount, 30000)
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
</script>

<style scoped>
.manor-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 70px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(139, 115, 85, 0.2);
  z-index: 1000;
  transition: all 0.3s ease;
}

.night-mode {
  background: rgba(30, 35, 45, 0.9);
  border-bottom-color: rgba(100, 110, 130, 0.3);
}

.nav-content {
  max-width: 1400px;
  height: 100%;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.nav-brand:hover {
  transform: scale(1.02);
}

.brand-icon {
  width: 45px;
  height: 45px;
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: white;
  box-shadow: 0 4px 15px rgba(124, 184, 124, 0.3);
}

.night-mode .brand-icon {
  background: linear-gradient(135deg, #4a6a5a, #3a5a4a);
  box-shadow: 0 4px 15px rgba(60, 90, 80, 0.4);
}

.brand-text {
  font-size: 22px;
  font-weight: 700;
  color: #5a7c5a;
  font-family: 'Georgia', serif;
  letter-spacing: 1px;
}

.night-mode .brand-text {
  color: #8ab88a;
}

.nav-links {
  display: flex;
  gap: 8px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 25px;
  text-decoration: none;
  color: #4a6a4a;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.3s ease;
  background: rgba(139, 188, 143, 0.1);
}

.night-mode .nav-link {
  color: #9ab89a;
  background: rgba(100, 140, 120, 0.15);
}

.nav-link:hover {
  background: rgba(139, 188, 143, 0.15);
  color: #5a8c5a;
}

.night-mode .nav-link:hover {
  background: rgba(100, 140, 120, 0.2);
  color: #7ac87a;
}

.nav-link.active {
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  color: white;
  box-shadow: 0 4px 15px rgba(124, 184, 124, 0.3);
}

.night-mode .nav-link.active {
  background: linear-gradient(135deg, #4a7c5a, #3a6c4a);
  box-shadow: 0 4px 15px rgba(60, 120, 80, 0.4);
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.theme-btn {
  width: 42px;
  height: 42px;
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

.theme-btn.text-btn {
  background: rgba(139, 188, 143, 0.2);
  color: #5a8c5a;
  font-size: 14px;
  font-weight: 600;
}

.night-mode .theme-btn.text-btn {
  background: rgba(100, 140, 120, 0.2);
  color: #7ac87a;
}

.theme-btn.text-btn:hover {
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  color: white;
  transform: scale(1.05);
}

.btn-text {
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notification-btn {
  position: relative;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: rgba(139, 115, 85, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8B7355;
  font-size: 18px;
  text-decoration: none;
  transition: all 0.3s ease;
}

.night-mode .notification-btn {
  background: rgba(200, 180, 150, 0.1);
  color: #D4C596;
}

.notification-btn:hover {
  background: rgba(139, 115, 85, 0.2);
}

.notification-btn.text-btn {
  background: rgba(139, 188, 143, 0.2);
  color: #5a8c5a;
  font-size: 14px;
  font-weight: 600;
}

.night-mode .notification-btn.text-btn {
  background: rgba(100, 140, 120, 0.2);
  color: #7ac87a;
}

.notification-btn.text-btn:hover {
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  color: white;
  transform: scale(1.05);
}

.badge {
  position: absolute;
  top: -2px;
  right: -2px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: linear-gradient(135deg, #FF6B6B, #EE5A5A);
  border-radius: 9px;
  font-size: 11px;
  font-weight: 600;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-btn {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #8FBC8F;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(143, 188, 143, 0.2);
  color: #5a7c5a;
  font-size: 20px;
  text-decoration: none;
  transition: all 0.3s ease;
}

.night-mode .avatar-btn {
  border-color: #4a7c5a;
  background: rgba(74, 124, 90, 0.2);
  color: #8ab88a;
}

.avatar-btn img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 15px rgba(124, 184, 124, 0.3);
}

.avatar-btn.text-btn {
  background: rgba(139, 188, 143, 0.2);
  color: #5a8c5a;
  font-size: 14px;
  font-weight: 600;
  border: none;
}

.night-mode .avatar-btn.text-btn {
  background: rgba(100, 140, 120, 0.2);
  color: #7ac87a;
}

.avatar-btn.text-btn:hover {
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  color: white;
  transform: scale(1.05);
}

.login-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  border-radius: 25px;
  color: white;
  font-size: 15px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(124, 184, 124, 0.3);
}

.night-mode .login-btn {
  background: linear-gradient(135deg, #4a7c5a, #3a6c4a);
  box-shadow: 0 4px 15px rgba(60, 120, 80, 0.4);
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(124, 184, 124, 0.4);
}
</style>
