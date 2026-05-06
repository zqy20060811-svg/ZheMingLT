<template>
  <div class="manor-search" :class="{ 'night-mode': isDarkMode }">
    <!-- 庄园背景 -->
    <ManorBackground :isDarkMode="isDarkMode" />

    <!-- 顶部导航 -->
    <nav class="search-top-nav">
      <div class="nav-left">
        <button class="back-btn" @click="goHome">
          <i class="bi bi-arrow-left"></i>
        </button>
        <div class="nav-brand" @click="goHome">
          <i class="bi bi-house-heart-fill"></i>
          <span>择明庄园</span>
        </div>
      </div>
      <button class="theme-btn" @click="toggleTheme">
        <i :class="isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-stars-fill'"></i>
      </button>
    </nav>

    <!-- 主内容 -->
    <main class="search-content">
      <!-- 搜索区域 -->
      <div class="search-section">
        <div class="search-card">
          <div class="search-header">
            <div class="header-icon">
              <i class="bi bi-search-heart"></i>
            </div>
            <div class="header-text">
              <h1>探索庄园</h1>
              <p>寻找你感兴趣的话题与故事</p>
            </div>
          </div>

          <!-- 搜索框 -->
          <div class="search-box">
            <i class="bi bi-search search-icon"></i>
            <input
              v-model="searchQuery"
              type="text"
              class="search-input"
              placeholder="搜索帖子标题或内容..."
              @keyup.enter="handleSearch"
            />
            <button v-if="searchQuery" class="clear-btn" @click="clearSearch">
              <i class="bi bi-x-circle-fill"></i>
            </button>
            <button class="search-btn" @click="handleSearch">
              <i class="bi bi-search"></i>
              <span>搜索</span>
            </button>
          </div>

          <!-- 筛选栏 -->
          <div class="filter-bar">
            <div class="filter-group">
              <span class="filter-label">
                <i class="bi bi-grid"></i>
                分类
              </span>
              <select v-model="selectedCategory" class="filter-select" @change="handleSearch">
                <option value="">全部分类</option>
                <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                  {{ cat.name }}
                </option>
              </select>
            </div>
            <div class="filter-group">
              <span class="filter-label">
                <i class="bi bi-sort-down"></i>
                排序
              </span>
              <div class="sort-options">
                <button
                  :class="['sort-btn', { active: sortBy === 'time' }]"
                  @click="changeSort('time')"
                >
                  <i class="bi bi-clock"></i>
                  <span>最新</span>
                </button>
                <button
                  :class="['sort-btn', { active: sortBy === 'hot' }]"
                  @click="changeSort('hot')"
                >
                  <i class="bi bi-fire"></i>
                  <span>最热</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 搜索结果 -->
      <div class="results-section">
        <!-- 搜索提示 -->
        <div v-if="!hasSearched" class="search-tips">
          <div class="tips-icon">
            <i class="bi bi-flower1"></i>
          </div>
          <h3>开始探索</h3>
          <p>在庄园中寻找你感兴趣的内容</p>
          <div class="hot-keywords">
            <span class="keyword-label">
              <i class="bi bi-stars"></i>
              热门搜索
            </span>
            <div class="keywords-list">
              <span
                v-for="keyword in hotKeywords"
                :key="keyword"
                class="keyword-tag"
                @click="quickSearch(keyword)"
              >
                {{ keyword }}
              </span>
            </div>
          </div>
        </div>

        <!-- 加载中 -->
        <div v-else-if="loading" class="loading-state">
          <div class="loading-flower">
            <i class="bi bi-flower2"></i>
          </div>
          <span>搜索中...</span>
        </div>

        <!-- 无结果 -->
        <div v-else-if="results.length === 0" class="empty-state">
          <div class="empty-icon">
            <i class="bi bi-flower3"></i>
          </div>
          <h3>未找到相关内容</h3>
          <p>换个关键词试试，或许会有新的发现</p>
        </div>

        <!-- 结果列表 -->
        <div v-else class="results-list">
          <div class="results-header">
            <span class="results-count">
              <i class="bi bi-check-circle-fill"></i>
              找到 {{ total }} 个结果
            </span>
          </div>

          <div class="posts-grid">
            <article
              v-for="post in results"
              :key="post.id"
              class="post-card"
              @click="goToPost(post.id)"
            >
              <div class="card-header">
                <span class="category-badge">{{ post.categoryName }}</span>
                <span class="post-time">{{ formatTime(post.createdAt) }}</span>
              </div>
              <h3 class="post-title" v-html="highlightText(post.title)"></h3>
              <p class="post-summary" v-html="highlightText(truncateContent(post.content))"></p>
              <div class="card-footer">
                <div class="author-info">
                  <div class="author-avatar">
                    <img v-if="post.authorAvatar" :src="post.authorAvatar" />
                    <i v-else class="bi bi-person-fill"></i>
                  </div>
                  <span class="author-name">{{ post.authorName }}</span>
                </div>
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
            </article>
          </div>

          <!-- 分页 -->
          <div v-if="pages > 1" class="pagination">
            <button
              :disabled="page <= 1"
              class="page-btn"
              @click="changePage(page - 1)"
            >
              <i class="bi bi-chevron-left"></i>
            </button>
            <span class="page-info">{{ page }} / {{ pages }}</span>
            <button
              :disabled="page >= pages"
              class="page-btn"
              @click="changePage(page + 1)"
            >
              <i class="bi bi-chevron-right"></i>
            </button>
          </div>
        </div>
      </div>
    </main>

    <!-- 底部导航 -->
    <ManorBottomNav :isDarkMode="isDarkMode" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '@/utils/request'
import ManorBackground from '@/components/ManorBackground.vue'
import ManorBottomNav from '@/components/ManorBottomNav.vue'

const router = useRouter()

// 状态
const isDarkMode = ref(false)
const searchQuery = ref('')
const selectedCategory = ref('')
const sortBy = ref('time')
const categories = ref([])
const results = ref([])
const loading = ref(false)
const hasSearched = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const pages = ref(0)

// 热门搜索词
const hotKeywords = ['Vue3', 'Spring Boot', '面试', '学习笔记', '项目分享']

// 初始化
onMounted(() => {
  isDarkMode.value = localStorage.getItem('darkMode') === 'true'
  loadCategories()
})

// 加载分类
async function loadCategories() {
  try {
    const res = await get('/categories')
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

// 执行搜索
async function handleSearch() {
  if (!searchQuery.value.trim()) {
    return
  }

  loading.value = true
  hasSearched.value = true
  page.value = 1

  try {
    const params = {
      keyword: searchQuery.value.trim(),
      sortBy: sortBy.value,
      page: page.value,
      size: size.value
    }

    if (selectedCategory.value) {
      params.categoryId = selectedCategory.value
    }

    const res = await get('/posts/search', params)
    if (res.code === 200 && res.data) {
      results.value = res.data.list || []
      total.value = res.data.total || 0
      pages.value = res.data.pages || 0
    } else {
      results.value = []
      total.value = 0
      pages.value = 0
    }
  } catch (error) {
    console.error('搜索失败', error)
    results.value = []
  } finally {
    loading.value = false
  }
}

// 切换排序
function changeSort(sort) {
  sortBy.value = sort
  if (hasSearched.value) {
    handleSearch()
  }
}

// 切换页码
async function changePage(newPage) {
  if (newPage < 1 || newPage > pages.value) return
  page.value = newPage
  loading.value = true

  try {
    const params = {
      keyword: searchQuery.value.trim(),
      sortBy: sortBy.value,
      page: page.value,
      size: size.value
    }

    if (selectedCategory.value) {
      params.categoryId = selectedCategory.value
    }

    const res = await get('/posts/search', params)
    if (res.code === 200 && res.data) {
      results.value = res.data.list || []
    }
  } catch (error) {
    console.error('加载失败', error)
  } finally {
    loading.value = false
  }
}

// 快速搜索
function quickSearch(keyword) {
  searchQuery.value = keyword
  handleSearch()
}

// 清空搜索
function clearSearch() {
  searchQuery.value = ''
  results.value = []
  hasSearched.value = false
}

// 高亮搜索词
function highlightText(text) {
  if (!text || !searchQuery.value) return text
  const regex = new RegExp(`(${searchQuery.value})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}

// 截断内容
function truncateContent(content) {
  if (!content) return ''
  return content.length > 100 ? content.substring(0, 100) + '...' : content
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

// 跳转帖子详情
function goToPost(id) {
  router.push(`/post/${id}`)
}

// 返回首页
function goHome() {
  router.push('/')
}

// 切换主题
function toggleTheme() {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
}
</script>

<style scoped>
.manor-search {
  min-height: 100vh;
  position: relative;
  padding-bottom: 100px;
}

/* 顶部导航 */
.search-top-nav {
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

.night-mode .search-top-nav {
  background: rgba(30, 35, 45, 0.9);
  border-bottom-color: rgba(100, 110, 130, 0.2);
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
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

.night-mode .back-btn {
  background: rgba(200, 180, 150, 0.1);
  color: #D4C596;
}

.back-btn:hover {
  background: rgba(139, 115, 85, 0.2);
  transform: translateX(-2px);
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
  color: #5a7c5a;
  font-family: 'Georgia', serif;
  cursor: pointer;
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
.search-content {
  max-width: 900px;
  margin: 0 auto;
  padding: 90px 20px 40px;
  position: relative;
  z-index: 1;
}

/* 搜索区域 */
.search-section {
  margin-bottom: 32px;
}

.search-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(139, 188, 143, 0.2);
}

.night-mode .search-card {
  background: rgba(40, 45, 55, 0.95);
  border-color: rgba(74, 124, 90, 0.2);
}

.search-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.header-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
}

.header-text h1 {
  font-size: 24px;
  font-weight: 700;
  color: #2d3748;
  font-family: 'Georgia', serif;
  margin-bottom: 4px;
}

.night-mode .header-text h1 {
  color: #e2e8f0;
}

.header-text p {
  font-size: 14px;
  color: #6a7c6a;
}

.night-mode .header-text p {
  color: #9ab89a;
}

/* 搜索框 */
.search-box {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(139, 188, 143, 0.08);
  border-radius: 16px;
  padding: 6px 6px 6px 16px;
  margin-bottom: 20px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.night-mode .search-box {
  background: rgba(74, 124, 90, 0.1);
}

.search-box:focus-within {
  border-color: #8FBC8F;
  background: rgba(139, 188, 143, 0.12);
}

.search-icon {
  color: #8FBC8F;
  font-size: 20px;
}

.search-input {
  flex: 1;
  border: none;
  background: none;
  padding: 12px 0;
  font-size: 16px;
  color: #2d3748;
  outline: none;
}

.night-mode .search-input {
  color: #e2e8f0;
}

.search-input::placeholder {
  color: #8a9a8a;
}

.clear-btn {
  border: none;
  background: none;
  color: #8a9a8a;
  cursor: pointer;
  padding: 8px;
  font-size: 18px;
  transition: all 0.3s ease;
}

.clear-btn:hover {
  color: #ff6b6b;
}

.search-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  color: white;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(124, 184, 124, 0.4);
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #5a7c5a;
  font-weight: 500;
}

.night-mode .filter-label {
  color: #8ab88a;
}

.filter-select {
  padding: 10px 16px;
  border: 1px solid rgba(139, 188, 143, 0.3);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
  color: #2d3748;
  font-size: 14px;
  cursor: pointer;
  outline: none;
  transition: all 0.3s ease;
}

.night-mode .filter-select {
  background: rgba(40, 45, 55, 0.8);
  color: #e2e8f0;
  border-color: rgba(74, 124, 90, 0.3);
}

.filter-select:focus {
  border-color: #8FBC8F;
}

.sort-options {
  display: flex;
  gap: 8px;
}

.sort-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border: 1px solid rgba(139, 188, 143, 0.3);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.5);
  color: #6a7c6a;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.night-mode .sort-btn {
  background: rgba(40, 45, 55, 0.5);
  color: #9ab89a;
  border-color: rgba(74, 124, 90, 0.3);
}

.sort-btn.active {
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  color: white;
  border-color: transparent;
}

/* 搜索结果区域 */
.results-section {
  position: relative;
  z-index: 1;
}

/* 搜索提示 */
.search-tips {
  text-align: center;
  padding: 60px 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(139, 188, 143, 0.2);
}

.night-mode .search-tips {
  background: rgba(40, 45, 55, 0.95);
  border-color: rgba(74, 124, 90, 0.2);
}

.tips-icon {
  font-size: 64px;
  color: #8FBC8F;
  margin-bottom: 20px;
}

.night-mode .tips-icon {
  color: #7ac87a;
}

.search-tips h3 {
  font-size: 24px;
  color: #2d3748;
  margin-bottom: 8px;
  font-family: 'Georgia', serif;
}

.night-mode .search-tips h3 {
  color: #e2e8f0;
}

.search-tips p {
  color: #6a7c6a;
  margin-bottom: 32px;
}

.night-mode .search-tips p {
  color: #9ab89a;
}

.hot-keywords {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.keyword-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #5a7c5a;
  font-size: 14px;
  font-weight: 500;
}

.night-mode .keyword-label {
  color: #8ab88a;
}

.keywords-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
}

.keyword-tag {
  padding: 8px 16px;
  background: rgba(139, 188, 143, 0.15);
  color: #5a7c5a;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(139, 188, 143, 0.2);
}

.night-mode .keyword-tag {
  background: rgba(74, 124, 90, 0.2);
  color: #8ab88a;
  border-color: rgba(74, 124, 90, 0.3);
}

.keyword-tag:hover {
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  color: white;
  transform: translateY(-2px);
  border-color: transparent;
}

/* 加载和空状态 */
.loading-state,
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(139, 188, 143, 0.2);
}

.night-mode .loading-state,
.night-mode .empty-state {
  background: rgba(40, 45, 55, 0.95);
  border-color: rgba(74, 124, 90, 0.2);
}

.loading-flower {
  font-size: 48px;
  color: #8FBC8F;
  margin-bottom: 16px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.1); opacity: 0.7; }
}

.loading-state span {
  color: #6a7c6a;
}

.night-mode .loading-state span {
  color: #9ab89a;
}

.empty-icon {
  font-size: 64px;
  color: #8FBC8F;
  margin-bottom: 20px;
  opacity: 0.6;
}

.empty-state h3 {
  font-size: 20px;
  color: #2d3748;
  margin-bottom: 8px;
  font-family: 'Georgia', serif;
}

.night-mode .empty-state h3 {
  color: #e2e8f0;
}

.empty-state p {
  color: #6a7c6a;
}

.night-mode .empty-state p {
  color: #9ab89a;
}

/* 结果列表 */
.results-header {
  margin-bottom: 20px;
  padding: 0 10px;
}

.results-count {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #5a7c5a;
  font-weight: 500;
}

.night-mode .results-count {
  color: #8ab88a;
}

.results-count i {
  color: #8FBC8F;
}

/* 帖子网格 */
.posts-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(139, 188, 143, 0.2);
}

.night-mode .post-card {
  background: rgba(40, 45, 55, 0.95);
  border-color: rgba(74, 124, 90, 0.2);
}

.post-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.category-badge {
  padding: 4px 12px;
  background: rgba(139, 188, 143, 0.15);
  color: #5a7c5a;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.night-mode .category-badge {
  background: rgba(74, 124, 90, 0.2);
  color: #8ab88a;
}

.post-time {
  font-size: 12px;
  color: #8a9a8a;
}

.night-mode .post-time {
  color: #7a9a8a;
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 10px;
  line-height: 1.4;
  font-family: 'Georgia', serif;
}

.night-mode .post-title {
  color: #e2e8f0;
}

.post-title :deep(mark) {
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
}

.post-summary {
  font-size: 14px;
  color: #6a7c6a;
  line-height: 1.6;
  margin-bottom: 16px;
}

.night-mode .post-summary {
  color: #9ab89a;
}

.post-summary :deep(mark) {
  background: rgba(139, 188, 143, 0.4);
  color: inherit;
  padding: 2px 4px;
  border-radius: 4px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid rgba(139, 188, 143, 0.15);
}

.night-mode .card-footer {
  border-top-color: rgba(74, 124, 90, 0.2);
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(139, 188, 143, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #5a7c5a;
  font-size: 14px;
  overflow: hidden;
}

.author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.author-name {
  font-size: 14px;
  color: #5a7c5a;
  font-weight: 500;
}

.night-mode .author-name {
  color: #8ab88a;
}

.post-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  font-size: 13px;
  color: #8a9a8a;
  display: flex;
  align-items: center;
  gap: 4px;
}

.night-mode .stat-item {
  color: #7a9a8a;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 32px;
  padding: 20px;
}

.page-btn {
  width: 44px;
  height: 44px;
  border: 1px solid rgba(139, 188, 143, 0.3);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  color: #5a7c5a;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.night-mode .page-btn {
  background: rgba(40, 45, 55, 0.9);
  border-color: rgba(74, 124, 90, 0.3);
  color: #8ab88a;
}

.page-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  color: white;
  border-color: transparent;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #5a7c5a;
  font-weight: 500;
}

.night-mode .page-info {
  color: #8ab88a;
}

/* 响应式 */
@media (max-width: 640px) {
  .search-content {
    padding: 80px 16px 40px;
  }

  .search-card {
    padding: 24px 20px;
  }

  .search-header {
    flex-direction: column;
    text-align: center;
  }

  .header-icon {
    width: 48px;
    height: 48px;
    font-size: 24px;
  }

  .search-box {
    flex-wrap: wrap;
  }

  .search-input {
    min-width: 0;
  }

  .search-btn span {
    display: none;
  }

  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-group {
    justify-content: space-between;
  }

  .sort-options {
    flex: 1;
    justify-content: flex-end;
  }

  .post-card {
    padding: 20px 16px;
  }

  .card-footer {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>
