<template>
  <nav class="bottom-nav" :class="{ 'dark-mode': isDarkMode }">
    <router-link
      v-for="item in navItems"
      :key="item.name"
      :to="item.path"
      :class="['nav-item', { active: active === item.name }]"
    >
      <div class="nav-icon-wrapper">
        <i :class="item.icon"></i>
        <span v-if="item.name === 'notifications' && unreadCount > 0" class="badge">
          {{ unreadCount > 99 ? '99+' : unreadCount }}
        </span>
      </div>
      <span>{{ item.label }}</span>
    </router-link>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { get } from '@/utils/request'

defineProps({
  active: {
    type: String,
    default: 'home'
  }
})

const isDarkMode = ref(false)
const unreadCount = ref(0)
let refreshInterval = null

const navItems = [
  { name: 'home', path: '/', label: '首页', icon: 'bi bi-house-door' },
  { name: 'hot', path: '/hot', label: '热门', icon: 'bi bi-fire' },
  { name: 'category', path: '/category', label: '分类', icon: 'bi bi-grid' },
  { name: 'notifications', path: '/notifications', label: '消息', icon: 'bi bi-bell' },
  { name: 'profile', path: '/profile', label: '我的', icon: 'bi bi-person' }
]

// 加载未读消息数
async function loadUnreadCount() {
  const token = localStorage.getItem('accessToken')
  if (!token) return

  try {
    const res = await get('/notifications/unread-count')
    if (res.code === 200) {
      unreadCount.value = res.data || 0
    }
  } catch (error) {
    console.error('加载未读消息数失败', error)
  }
}

onMounted(() => {
  isDarkMode.value = localStorage.getItem('darkMode') === 'true'
  loadUnreadCount()

  // 每30秒刷新一次未读消息数
  refreshInterval = setInterval(loadUnreadCount, 30000)
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
</script>

<style scoped>
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  align-items: center;
  height: 60px;
  background: white;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
  z-index: 1000;
}

.dark-mode {
  background: #1e1e2e;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.2);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  flex: 1;
  height: 100%;
  color: #999;
  text-decoration: none;
  transition: all 0.3s;
}

.nav-item i {
  font-size: 1.3rem;
}

.nav-item span {
  font-size: 0.7rem;
}

.nav-item.active {
  color: #4a6fa5;
}

.nav-item:active {
  transform: scale(0.9);
}

.nav-icon-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.badge {
  position: absolute;
  top: -5px;
  right: -8px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  color: white;
  font-size: 10px;
  font-weight: 600;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
