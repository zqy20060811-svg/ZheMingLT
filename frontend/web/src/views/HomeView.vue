<template>
  <div class="home-page" :class="{ 'dark-mode': isDarkMode }">
    <!-- 顶部导航 -->
    <nav class="top-nav">
      <div class="nav-brand">
        <i class="bi bi-lightbulb"></i>
        <span>择明论坛</span>
      </div>
      <div class="nav-actions">
        <button class="btn-icon" @click="toggleTheme">
          <i :class="isDarkMode ? 'bi bi-sun' : 'bi bi-moon'"></i>
        </button>
        <button class="btn-icon" @click="goToSearch">
          <i class="bi bi-search"></i>
        </button>
      </div>
    </nav>

    <!-- 内容区域 -->
    <div class="content-area">
      <!-- 轮播 Banner -->
      <div class="banner-section">
        <div class="banner-slide" :style="{ background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' }">
          <div class="banner-content">
            <h2>智慧之光</h2>
            <p>照亮前行之路</p>
          </div>
        </div>
      </div>

      <!-- 快速分类 -->
      <div class="quick-categories">
        <div
          v-for="cat in quickCategories"
          :key="cat.id"
          class="category-chip"
          :style="{ backgroundColor: cat.color }"
          @click="goToCategory(cat.id)"
        >
          <i :class="cat.icon"></i>
          <span>{{ cat.name }}</span>
        </div>
      </div>

      <!-- 帖子列表 -->
      <div class="posts-section">
        <div class="section-header">
          <h3>最新帖子</h3>
          <div class="filter-tabs">
            <button
              v-for="tab in tabs"
              :key="tab.value"
              :class="['tab-btn', { active: currentTab === tab.value }]"
              @click="switchTab(tab.value)"
            >
              {{ tab.label }}
            </button>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
          <span>加载中...</span>
        </div>

        <!-- 帖子列表 -->
        <div v-else-if="posts.length > 0" class="posts-list">
          <div
            v-for="item in posts"
            :key="item.id"
            class="post-card"
            @click="goToPost(item.id)"
          >
            <div class="post-header">
              <img :src="item.authorAvatar || defaultAvatar" class="avatar" />
              <div class="post-info">
                <span class="author-name">{{ item.authorName }}</span>
                <span class="post-time">{{ formatTime(item.createdAt) }}</span>
              </div>
              <span class="category-tag">{{ item.categoryName }}</span>
            </div>
            <h4 class="post-title">{{ item.title }}</h4>
            <p class="post-summary">{{ item.summary || item.content.substring(0, 80) + '...' }}</p>
            <div class="post-footer">
              <div class="stats">
                <span><i class="bi bi-eye"></i> {{ item.viewCount || 0 }}</span>
                <span><i class="bi bi-heart"></i> {{ item.likeCount || 0 }}</span>
                <span><i class="bi bi-chat"></i> {{ item.commentCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <i class="bi bi-inbox"></i>
          <p>暂无帖子</p>
        </div>

        <!-- 加载更多 -->
        <div v-if="posts.length > 0 && hasMore" class="load-more">
          <button @click="loadMore" :disabled="loadingMore">
            {{ loadingMore ? '加载中...' : '加载更多' }}
          </button>
        </div>
      </div>
    </div>

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
  { label: '最新', value: 'new' },
  { label: '热门', value: 'hot' },
  { label: '精华', value: 'best' }
]

const quickCategories = [
  { id: 1, name: '技术', icon: 'bi bi-code-slash', color: '#4a6fa5' },
  { id: 2, name: '生活', icon: 'bi bi-cup-hot', color: '#e07a5f' },
  { id: 3, name: '娱乐', icon: 'bi bi-film', color: '#81b29a' },
  { id: 4, name: '学习', icon: 'bi bi-book', color: '#f2cc8f' }
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

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  isDarkMode.value = localStorage.getItem('darkMode') === 'true'
  loadPosts()
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8f9fa 0%, #e9ecef 100%);
  padding-bottom: 70px;
}

.dark-mode {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
}

/* 顶部导航 */
.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  position: sticky;
  top: 0;
  z-index: 100;
}

.dark-mode .top-nav {
  background: rgba(30, 30, 46, 0.9);
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.2rem;
  font-weight: bold;
  color: #4a6fa5;
}

.nav-brand i {
  font-size: 1.5rem;
}

.nav-actions {
  display: flex;
  gap: 12px;
}

.btn-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: #f0f2f5;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
}

.dark-mode .btn-icon {
  background: #2d2d44;
  color: #fff;
}

/* Banner */
.banner-section {
  padding: 16px;
}

.banner-slide {
  border-radius: 16px;
  padding: 24px;
  color: white;
}

.banner-content h2 {
  font-size: 1.5rem;
  margin-bottom: 8px;
}

.banner-content p {
  opacity: 0.9;
}

/* 快速分类 */
.quick-categories {
  display: flex;
  gap: 12px;
  padding: 0 16px 16px;
  overflow-x: auto;
}

.category-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border-radius: 20px;
  color: white;
  font-size: 0.9rem;
  white-space: nowrap;
  cursor: pointer;
  transition: transform 0.3s;
}

.category-chip:active {
  transform: scale(0.95);
}

/* 帖子区域 */
.posts-section {
  padding: 0 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  font-size: 1.1rem;
  color: #333;
}

.dark-mode .section-header h3 {
  color: #fff;
}

.filter-tabs {
  display: flex;
  gap: 8px;
}

.tab-btn {
  padding: 6px 14px;
  border: none;
  border-radius: 16px;
  background: #e9ecef;
  color: #666;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s;
}

.tab-btn.active {
  background: #4a6fa5;
  color: white;
}

.dark-mode .tab-btn {
  background: #2d2d44;
  color: #aaa;
}

/* 帖子卡片 */
.posts-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.post-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  cursor: pointer;
  transition: transform 0.2s;
}

.dark-mode .post-card {
  background: #1e1e2e;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
}

.post-card:active {
  transform: scale(0.98);
}

.post-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.post-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: #333;
}

.dark-mode .author-name {
  color: #fff;
}

.post-time {
  font-size: 0.75rem;
  color: #999;
}

.category-tag {
  padding: 4px 10px;
  background: #e3f2fd;
  color: #1976d2;
  border-radius: 12px;
  font-size: 0.75rem;
}

.post-title {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.4;
}

.dark-mode .post-title {
  color: #fff;
}

.post-summary {
  font-size: 0.85rem;
  color: #666;
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.dark-mode .post-summary {
  color: #aaa;
}

.post-footer .stats {
  display: flex;
  gap: 16px;
}

.post-footer .stats span {
  font-size: 0.8rem;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
  color: #999;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e9ecef;
  border-top-color: #4a6fa5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-state i {
  font-size: 3rem;
  margin-bottom: 12px;
}

/* 加载更多 */
.load-more {
  text-align: center;
  padding: 20px;
}

.load-more button {
  padding: 10px 24px;
  border: none;
  border-radius: 20px;
  background: #4a6fa5;
  color: white;
  cursor: pointer;
}
</style>
