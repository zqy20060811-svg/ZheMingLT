<template>
  <nav class="bottom-nav" :class="{ 'dark-mode': isDarkMode }">
    <router-link
      v-for="item in navItems"
      :key="item.name"
      :to="item.path"
      :class="['nav-item', { active: active === item.name }]"
    >
      <i :class="item.icon"></i>
      <span>{{ item.label }}</span>
    </router-link>
  </nav>
</template>

<script setup>
import { ref, onMounted } from 'vue'

defineProps({
  active: {
    type: String,
    default: 'home'
  }
})

const isDarkMode = ref(false)

const navItems = [
  { name: 'home', path: '/', label: '首页', icon: 'bi bi-house-door' },
  { name: 'hot', path: '/hot', label: '热门', icon: 'bi bi-fire' },
  { name: 'category', path: '/category', label: '分类', icon: 'bi bi-grid' },
  { name: 'profile', path: '/profile', label: '我的', icon: 'bi bi-person' }
]

onMounted(() => {
  isDarkMode.value = localStorage.getItem('darkMode') === 'true'
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
</style>
