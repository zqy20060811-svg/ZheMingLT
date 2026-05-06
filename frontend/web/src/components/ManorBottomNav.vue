<template>
  <nav class="manor-bottom-nav" :class="{ 'night-mode': isDarkMode }">
    <router-link
      v-for="item in navItems"
      :key="item.name"
      :to="item.path"
      :class="['nav-item', { active: isActive(item.name) }]"
    >
      <div class="nav-icon-wrapper" :class="{ 'text-icon': !item.icon }">
        <i v-if="item.icon" :class="item.icon"></i>
        <span v-else class="icon-text">{{ item.text }}</span>
        <span v-if="item.name === 'notifications' && unreadCount > 0" class="badge">
          {{ unreadCount > 99 ? '99+' : unreadCount }}
        </span>
      </div>
      <span class="nav-label">{{ item.label }}</span>
    </router-link>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { get } from '@/utils/request'

const route = useRoute()

const props = defineProps({
  isDarkMode: {
    type: Boolean,
    default: false
  }
})

const unreadCount = ref(0)
let refreshInterval = null

const navItems = [
  { name: 'home', path: '/', label: '首页', icon: 'bi bi-house-heart-fill', text: '首' },
  { name: 'hot', path: '/hot', label: '热门', icon: 'bi bi-fire', text: '热' },
  { name: 'create', path: '/create', label: '发布', icon: 'bi bi-plus-circle-fill', text: '发' },
  { name: 'tags', path: '/tags', label: '标签', icon: 'bi bi-tags-fill', text: '标' },
  { name: 'profile', path: '/profile', label: '我的', icon: 'bi bi-person-heart', text: '我' }
]

function isActive(name) {
  return route.name === name
}

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
.manor-bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 70px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(139, 115, 85, 0.15);
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 0 16px;
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 1000;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.05);
}

.night-mode {
  background: rgba(30, 35, 45, 0.95);
  border-top-color: rgba(100, 110, 130, 0.2);
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.2);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  text-decoration: none;
  color: #8a9a8a;
  transition: all 0.3s ease;
  position: relative;
}

.night-mode .nav-item {
  color: #7a9a8a;
}

.nav-item.active {
  color: #5a8c5a;
}

.night-mode .nav-item.active {
  color: #7ac87a;
}

.nav-icon-wrapper {
  position: relative;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  transition: all 0.3s ease;
}

.nav-icon-wrapper.text-icon {
  width: 28px;
  height: 28px;
  background: rgba(139, 188, 143, 0.2);
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
  color: #5a8c5a;
}

.night-mode .nav-icon-wrapper.text-icon {
  background: rgba(100, 140, 120, 0.2);
  color: #7ac87a;
}

.nav-item.active .nav-icon-wrapper.text-icon {
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  color: white;
}

.night-mode .nav-item.active .nav-icon-wrapper.text-icon {
  background: linear-gradient(135deg, #4a7c5a, #3a6c4a);
}

.icon-text {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.nav-item.active .nav-icon-wrapper {
  transform: translateY(-4px);
}

.nav-item[href="/create"] .nav-icon-wrapper,
.nav-item[to="/create"] .nav-icon-wrapper {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  border-radius: 50%;
  color: white;
  font-size: 26px;
  margin-top: -20px;
  box-shadow: 0 4px 15px rgba(124, 184, 124, 0.4);
}

.night-mode .nav-item[href="/create"] .nav-icon-wrapper,
.night-mode .nav-item[to="/create"] .nav-icon-wrapper {
  background: linear-gradient(135deg, #4a7c5a, #3a6c4a);
  box-shadow: 0 4px 15px rgba(60, 120, 80, 0.5);
}

.nav-label {
  font-size: 11px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.nav-item.active .nav-label {
  font-weight: 600;
}

.badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: linear-gradient(135deg, #FF6B6B, #EE5A5A);
  border-radius: 8px;
  font-size: 10px;
  font-weight: 600;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
