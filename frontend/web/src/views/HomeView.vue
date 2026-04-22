<template>
  <div class="home-page" :class="{ 'dark-mode': isDarkMode }">
    <!-- 动态背景 -->
    <div class="animated-bg">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
    </div>

    <!-- 顶部导航 -->
    <nav class="top-nav">
      <div class="nav-brand">
        <div class="logo-wrapper">
          <i class="bi bi-lightbulb-fill"></i>
        </div>
        <span class="brand-text">择明论坛</span>
      </div>
      <div class="nav-actions">
        <button class="btn-icon search-btn" @click="goToSearch">
          <i class="bi bi-search"></i>
        </button>
        <button class="btn-icon theme-btn" @click="toggleTheme">
          <i :class="isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-fill'"></i>
        </button>
      </div>
    </nav>

    <!-- 内容区域 -->
    <div class="content-area">
      <!-- 欢迎横幅 -->
      <div class="welcome-banner">
        <div class="banner-content">
          <h1 class="welcome-title">
            <span class="gradient-text">探索·分享·成长</span>
          </h1>
          <p class="welcome-subtitle">在择明论坛，与志同道合的伙伴一起交流</p>
        </div>
        <div class="floating-icons">
          <i class="bi bi-chat-dots"></i>
          <i class="bi bi-lightbulb"></i>
          <i class="bi bi-book"></i>
        </div>
      </div>

      <!-- 快速分类 -->
      <div class="categories-section">
        <div class="section-title">
          <i class="bi bi-grid-3x3-gap-fill"></i>
          <span>热门分类</span>
        </div>
        <div class="quick-categories">
          <div
            v-for="(cat, index) in quickCategories"
            :key="cat.id"
            class="category-card"
            :class="`cat-${index}`"
            @click="goToCategory(cat.id)"
          >
            <div class="cat-icon">
              <i :class="cat.icon"></i>
            </div>
            <span class="cat-name">{{ cat.name }}</span>
            <div class="cat-glow"></div>
          </div>
        </div>
      </div>

      <!-- 帖子列表 -->
      <div class="posts-section">
        <div class="section-header">
          <div class="header-title">
            <i class="bi bi-file-text-fill"></i>
            <h3>最新帖子</h3>
          </div>
          <div class="filter-tabs">
            <button
              v-for="tab in tabs"
              :key="tab.value"
              :class="['tab-btn', { active: currentTab === tab.value }]"
              @click="switchTab(tab.value)"
            >
              <i :class="tab.icon"></i>
              <span>{{ tab.label }}</span>
            </button>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <div class="spinner-wrapper">
            <div class="spinner"></div>
            <div class="spinner-ring"></div>
          </div>
          <span class="loading-text">加载中...</span>
        </div>

        <!-- 帖子列表 -->
        <div v-else-if="posts.length > 0" class="posts-list">
          <div
            v-for="(item, index) in posts"
            :key="item.id"
            class="post-card"
            :style="{ animationDelay: `${index * 0.1}s` }"
            @click="goToPost(item.id)"
          >
            <div class="card-glow"></div>
            <div class="post-header">
              <div class="author-wrapper">
                <img :src="item.authorAvatar || defaultAvatar" class="avatar" />
                <div class="author-info">
                  <span class="author-name">{{ item.authorName || '匿名用户' }}</span>
                  <span class="post-time">
                    <i class="bi bi-clock"></i>
                    {{ formatTime(item.createdAt) }}
                  </span>
                </div>
              </div>
              <span class="category-tag" :style="getCategoryStyle(item.categoryName)">
                <i class="bi bi-folder"></i>
                {{ item.categoryName || '未分类' }}
              </span>
            </div>
            
            <div class="post-body">
              <h4 class="post-title">{{ item.title }}</h4>
              <p class="post-summary">{{ item.summary || (item.content ? item.content.substring(0, 100) + '...' : '暂无内容') }}</p>
            </div>
            
            <div class="post-footer">
              <div class="stats">
                <span class="stat-item">
                  <i class="bi bi-eye-fill"></i>
                  <span class="stat-value">{{ formatNumber(item.viewCount || 0) }}</span>
                </span>
                <span class="stat-item">
                  <i class="bi bi-heart-fill"></i>
                  <span class="stat-value">{{ formatNumber(item.likeCount || 0) }}</span>
                </span>
                <span class="stat-item">
                  <i class="bi bi-chat-fill"></i>
                  <span class="stat-value">{{ formatNumber(item.commentCount || 0) }}</span>
                </span>
              </div>
              <div class="read-more">
                <span>阅读更多</span>
                <i class="bi bi-arrow-right"></i>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <div class="empty-icon">
            <i class="bi bi-inbox"></i>
          </div>
          <h4>暂无帖子</h4>
          <p>成为第一个发帖的人吧！</p>
        </div>

        <!-- 加载更多 -->
        <div v-if="posts.length > 0 && hasMore" class="load-more">
          <button @click="loadMore" :disabled="loadingMore" class="load-btn">
            <span v-if="loadingMore" class="btn-spinner"></span>
            <span v-else>
              <i class="bi bi-chevron-down"></i>
              加载更多
            </span>
          </button>
        </div>
      </div>
    </div>

    <!-- 发布按钮 -->
    <button class="fab-btn" @click="goToCreatePost">
      <div class="fab-glow"></div>
      <i class="bi bi-plus-lg"></i>
    </button>

    <!-- 底部导航 -->
    <BottomNav active="home" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '@/utils/request'
import BottomNav from '@/components/BottomNav.vue'

const router = useRouter()

const isDarkMode = ref(false)
const loading = ref(false)
const loadingMore = ref(false)
const posts = ref([])
const currentTab = ref('new')
const page = ref(0)
const hasMore = ref(true)

const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

const tabs = [
  { label: '最新', value: 'new', icon: 'bi bi-clock' },
  { label: '热门', value: 'hot', icon: 'bi bi-fire' },
  { label: '精华', value: 'best', icon: 'bi bi-star' }
]

const quickCategories = [
  { id: 1, name: '技术', icon: 'bi bi-code-slash', color: '#667eea' },
  { id: 2, name: '生活', icon: 'bi bi-cup-hot', color: '#f093fb' },
  { id: 3, name: '娱乐', icon: 'bi bi-film', color: '#4facfe' },
  { id: 4, name: '学习', icon: 'bi bi-book', color: '#43e97b' }
]

function toggleTheme() {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
}

function switchTab(tab) {
  currentTab.value = tab
  page.value = 0
  posts.value = []
  loadPosts()
}

async function loadPosts() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: 10,
      sort: currentTab.value
    }
    const res = await get('/posts', params)
    if (res.code === 200) {
      if (page.value === 0) {
        posts.value = res.data.content || []
      } else {
        posts.value.push(...(res.data.content || []))
      }
      hasMore.value = !res.data.last
    }
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  if (loadingMore.value) return
  loadingMore.value = true
  page.value++
  await loadPosts()
  loadingMore.value = false
}

function goToPost(id) {
  router.push(`/post/${id}`)
}

function goToCategory(id) {
  router.push(`/category/${id}`)
}

function goToSearch() {
  router.push('/search')
}

function goToCreatePost() {
  router.push('/create')
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

onMounted(() => {
  isDarkMode.value = localStorage.getItem('darkMode') === 'true'
  loadPosts()
})
</script>

<style scoped>
.home-page {
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
  -webkit-backdrop-filter: blur(20px);
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .top-nav {
  background: rgba(30, 30, 46, 0.8);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-wrapper {
  width: 42px;
  height: 42px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.logo-wrapper i {
  font-size: 1.4rem;
  color: white;
}

.brand-text {
  font-size: 1.3rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-actions {
  display: flex;
  gap: 12px;
}

.btn-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  color: #4a5568;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 1.1rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.dark-mode .btn-icon {
  background: rgba(45, 45, 68, 0.9);
  color: #a0aec0;
}

.btn-icon:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.search-btn:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.theme-btn:hover {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

/* 欢迎横幅 */
.welcome-banner {
  position: relative;
  margin: 20px;
  padding: 40px 24px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-radius: 24px;
  overflow: hidden;
  border: 1px solid rgba(102, 126, 234, 0.2);
  backdrop-filter: blur(10px);
}

.dark-mode .welcome-banner {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
  border: 1px solid rgba(102, 126, 234, 0.3);
}

.banner-content {
  position: relative;
  z-index: 1;
  text-align: center;
}

.welcome-title {
  font-size: 1.8rem;
  font-weight: 800;
  margin-bottom: 12px;
}

.gradient-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  background-size: 200% 200%;
  animation: gradient-shift 3s ease infinite;
}

@keyframes gradient-shift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.welcome-subtitle {
  font-size: 1rem;
  color: #718096;
  font-weight: 500;
}

.dark-mode .welcome-subtitle {
  color: #a0aec0;
}

.floating-icons {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.floating-icons i {
  position: absolute;
  font-size: 2rem;
  color: rgba(102, 126, 234, 0.2);
  animation: float-icon 6s infinite ease-in-out;
}

.floating-icons i:nth-child(1) {
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.floating-icons i:nth-child(2) {
  top: 60%;
  right: 15%;
  animation-delay: -2s;
}

.floating-icons i:nth-child(3) {
  bottom: 20%;
  left: 20%;
  animation-delay: -4s;
}

@keyframes float-icon {
  0%, 100% { transform: translateY(0) rotate(0deg); opacity: 0.2; }
  50% { transform: translateY(-20px) rotate(10deg); opacity: 0.4; }
}

/* 分类区域 */
.categories-section {
  padding: 0 20px;
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.1rem;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 16px;
}

.dark-mode .section-title {
  color: #e2e8f0;
}

.section-title i {
  color: #667eea;
  font-size: 1.2rem;
}

.quick-categories {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.category-card {
  position: relative;
  background: white;
  border-radius: 16px;
  padding: 20px 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .category-card {
  background: rgba(45, 45, 68, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.category-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.cat-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 10px;
  font-size: 1.4rem;
  color: white;
  position: relative;
  z-index: 1;
}

.cat-0 .cat-icon { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.cat-1 .cat-icon { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.cat-2 .cat-icon { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.cat-3 .cat-icon { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }

.cat-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: #2d3748;
  position: relative;
  z-index: 1;
}

.dark-mode .cat-name {
  color: #e2e8f0;
}

.cat-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 0;
  height: 0;
  border-radius: 50%;
  opacity: 0;
  transition: all 0.5s ease;
}

.cat-0 .cat-glow { background: radial-gradient(circle, rgba(102, 126, 234, 0.3) 0%, transparent 70%); }
.cat-1 .cat-glow { background: radial-gradient(circle, rgba(240, 147, 251, 0.3) 0%, transparent 70%); }
.cat-2 .cat-glow { background: radial-gradient(circle, rgba(79, 172, 254, 0.3) 0%, transparent 70%); }
.cat-3 .cat-glow { background: radial-gradient(circle, rgba(67, 233, 123, 0.3) 0%, transparent 70%); }

.category-card:hover .cat-glow {
  width: 150px;
  height: 150px;
  opacity: 1;
}

/* 帖子区域 */
.posts-section {
  padding: 0 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.filter-tabs {
  display: flex;
  gap: 8px;
  background: rgba(0, 0, 0, 0.05);
  padding: 4px;
  border-radius: 12px;
}

.dark-mode .filter-tabs {
  background: rgba(255, 255, 255, 0.05);
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: #718096;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-btn i {
  font-size: 0.9rem;
}

.tab-btn.active {
  background: white;
  color: #667eea;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.dark-mode .tab-btn.active {
  background: rgba(45, 45, 68, 0.9);
  color: #667eea;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 20px;
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
  font-weight: 500;
}

.dark-mode .loading-text {
  color: #a0aec0;
}

/* 帖子列表 */
.posts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  position: relative;
  background: white;
  border-radius: 20px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(0, 0, 0, 0.05);
  overflow: hidden;
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

.dark-mode .post-card {
  background: rgba(45, 45, 68, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.post-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.card-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.post-card:hover .card-glow {
  opacity: 1;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.author-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(102, 126, 234, 0.2);
  transition: transform 0.3s ease;
}

.post-card:hover .avatar {
  transform: scale(1.1);
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-size: 0.95rem;
  font-weight: 700;
  color: #2d3748;
}

.dark-mode .author-name {
  color: #e2e8f0;
}

.post-time {
  font-size: 0.8rem;
  color: #a0aec0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.category-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  transition: transform 0.3s ease;
}

.post-card:hover .category-tag {
  transform: scale(1.05);
}

.post-body {
  margin-bottom: 16px;
}

.post-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #1a202c;
  margin-bottom: 10px;
  line-height: 1.4;
  transition: color 0.3s ease;
}

.dark-mode .post-title {
  color: #f7fafc;
}

.post-card:hover .post-title {
  color: #667eea;
}

.post-summary {
  font-size: 0.9rem;
  color: #718096;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.dark-mode .post-summary {
  color: #a0aec0;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .post-footer {
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.85rem;
  color: #a0aec0;
  transition: all 0.3s ease;
}

.stat-item:hover {
  color: #667eea;
}

.stat-item i {
  font-size: 1rem;
}

.stat-value {
  font-weight: 600;
}

.read-more {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  color: #667eea;
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.3s ease;
}

.post-card:hover .read-more {
  opacity: 1;
  transform: translateX(0);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
}

.empty-icon {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.empty-icon i {
  font-size: 3rem;
  color: #667eea;
}

.empty-state h4 {
  font-size: 1.2rem;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 8px;
}

.dark-mode .empty-state h4 {
  color: #e2e8f0;
}

.empty-state p {
  font-size: 0.95rem;
  color: #a0aec0;
}

/* 加载更多 */
.load-more {
  text-align: center;
  padding: 30px 20px;
}

.load-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  border: none;
  border-radius: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.load-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.5);
}

.load-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid transparent;
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

/* 发布按钮 */
.fab-btn {
  position: fixed;
  right: 24px;
  bottom: 90px;
  width: 64px;
  height: 64px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 1.8rem;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
  overflow: hidden;
}

.fab-glow {
  position: absolute;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.fab-btn:hover {
  transform: scale(1.1) rotate(90deg);
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.6);
}

.fab-btn:hover .fab-glow {
  opacity: 1;
}

.fab-btn:active {
  transform: scale(0.95);
}

.fab-btn i {
  position: relative;
  z-index: 1;
}
</style>