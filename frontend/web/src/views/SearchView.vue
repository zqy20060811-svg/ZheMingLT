<template>
  <div class="search-page" :class="{ 'dark-mode': isDarkMode }">
    <!-- 动态背景 -->
    <div class="animated-bg">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
    </div>

    <!-- 顶部导航 -->
    <nav class="top-nav">
      <div class="nav-brand" @click="goHome">
        <div class="logo-wrapper">
          <i class="bi bi-lightbulb-fill"></i>
        </div>
        <span class="brand-text">择明论坛</span>
      </div>
      <div class="nav-actions">
        <button class="btn-icon theme-btn" @click="toggleTheme">
          <i :class="isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-fill'"></i>
        </button>
      </div>
    </nav>

    <!-- 搜索区域 -->
    <div class="search-section">
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
        <button class="search-btn" @click="handleSearch">搜索</button>
      </div>

      <!-- 筛选选项 -->
      <div class="filter-bar">
        <div class="filter-group">
          <span class="filter-label">分类：</span>
          <select v-model="selectedCategory" class="filter-select" @change="handleSearch">
            <option value="">全部分类</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">
              {{ cat.name }}
            </option>
          </select>
        </div>
        <div class="filter-group">
          <span class="filter-label">排序：</span>
          <div class="sort-options">
            <button
              :class="['sort-btn', { active: sortBy === 'time' }]"
              @click="changeSort('time')"
            >
              <i class="bi bi-clock"></i> 最新
            </button>
            <button
              :class="['sort-btn', { active: sortBy === 'hot' }]"
              @click="changeSort('hot')"
            >
              <i class="bi bi-fire"></i> 最热
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div class="results-section">
      <!-- 搜索提示 -->
      <div v-if="!hasSearched" class="search-tips">
        <div class="tips-icon">
          <i class="bi bi-search-heart"></i>
        </div>
        <h3>开始搜索</h3>
        <p>输入关键词，发现感兴趣的帖子</p>
        <div class="hot-keywords">
          <span class="keyword-label">热门搜索：</span>
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

      <!-- 加载中 -->
      <div v-else-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>搜索中...</p>
      </div>

      <!-- 无结果 -->
      <div v-else-if="results.length === 0" class="empty-state">
        <div class="empty-icon">
          <i class="bi bi-inbox"></i>
        </div>
        <h3>未找到相关帖子</h3>
        <p>换个关键词试试</p>
      </div>

      <!-- 结果列表 -->
      <div v-else class="results-list">
        <div class="results-header">
          <span class="results-count">找到 {{ total }} 个结果</span>
        </div>
        <div
          v-for="post in results"
          :key="post.id"
          class="result-card"
          @click="goToPost(post.id)"
        >
          <div class="result-header">
            <span class="category-tag">{{ post.categoryName }}</span>
            <span class="post-time">{{ formatTime(post.createdAt) }}</span>
          </div>
          <h3 class="result-title" v-html="highlightText(post.title)"></h3>
          <p class="result-content" v-html="highlightText(truncateContent(post.content))"></p>
          <div class="result-footer">
            <div class="author-info">
              <i class="bi bi-person-circle"></i>
              <span>{{ post.authorName }}</span>
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

    <!-- 底部导航 -->
    <BottomNav />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '@/utils/request'
import BottomNav from '@/components/BottomNav.vue'

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
  return content.length > 150 ? content.substring(0, 150) + '...' : content
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
.search-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf5 100%);
  position: relative;
  padding-bottom: 80px;
}

.dark-mode {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
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
  left: -50px;
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
  33% { transform: translate(30px, -30px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
}

/* 顶部导航 */
.top-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .top-nav {
  background: rgba(30, 30, 50, 0.8);
  border-bottom-color: rgba(255, 255, 255, 0.05);
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.logo-wrapper {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
}

.brand-text {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-actions {
  display: flex;
  gap: 10px;
}

.btn-icon {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 10px;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-icon:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-2px);
}

.dark-mode .btn-icon {
  background: rgba(255, 255, 255, 0.1);
  color: #a0a0c0;
}

/* 搜索区域 */
.search-section {
  position: relative;
  z-index: 1;
  padding: 30px 20px;
}

.search-box {
  position: relative;
  max-width: 600px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  padding: 5px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(102, 126, 234, 0.1);
}

.dark-mode .search-box {
  background: rgba(40, 40, 60, 0.9);
  border-color: rgba(255, 255, 255, 0.1);
}

.search-icon {
  padding: 0 15px;
  color: #667eea;
  font-size: 20px;
}

.search-input {
  flex: 1;
  border: none;
  background: none;
  padding: 12px 0;
  font-size: 16px;
  color: #333;
  outline: none;
}

.dark-mode .search-input {
  color: #e0e0e0;
}

.search-input::placeholder {
  color: #999;
}

.clear-btn {
  border: none;
  background: none;
  color: #999;
  cursor: pointer;
  padding: 8px;
  font-size: 18px;
}

.search-btn {
  padding: 10px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

/* 筛选栏 */
.filter-bar {
  max-width: 600px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  font-size: 14px;
  color: #666;
}

.dark-mode .filter-label {
  color: #a0a0c0;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.8);
  color: #333;
  font-size: 14px;
  cursor: pointer;
  outline: none;
}

.dark-mode .filter-select {
  background: rgba(40, 40, 60, 0.8);
  color: #e0e0e0;
  border-color: rgba(255, 255, 255, 0.1);
}

.sort-options {
  display: flex;
  gap: 8px;
}

.sort-btn {
  padding: 8px 16px;
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.5);
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 5px;
}

.dark-mode .sort-btn {
  background: rgba(40, 40, 60, 0.5);
  color: #a0a0c0;
  border-color: rgba(255, 255, 255, 0.1);
}

.sort-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
}

/* 搜索结果区域 */
.results-section {
  position: relative;
  z-index: 1;
  padding: 0 20px 30px;
  max-width: 800px;
  margin: 0 auto;
}

/* 搜索提示 */
.search-tips {
  text-align: center;
  padding: 60px 20px;
}

.tips-icon {
  font-size: 64px;
  color: #667eea;
  opacity: 0.5;
  margin-bottom: 20px;
}

.search-tips h3 {
  font-size: 24px;
  color: #333;
  margin-bottom: 10px;
}

.dark-mode .search-tips h3 {
  color: #e0e0e0;
}

.search-tips p {
  color: #999;
  margin-bottom: 30px;
}

.hot-keywords {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  align-items: center;
}

.keyword-label {
  color: #666;
  font-size: 14px;
}

.dark-mode .keyword-label {
  color: #a0a0c0;
}

.keyword-tag {
  padding: 6px 14px;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.keyword-tag:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-2px);
}

/* 加载和空状态 */
.loading-state,
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(102, 126, 234, 0.2);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-icon {
  font-size: 64px;
  color: #ccc;
  margin-bottom: 20px;
}

.empty-state h3 {
  font-size: 20px;
  color: #666;
  margin-bottom: 10px;
}

.dark-mode .empty-state h3 {
  color: #a0a0c0;
}

.empty-state p {
  color: #999;
}

/* 结果列表 */
.results-header {
  margin-bottom: 20px;
  padding: 0 10px;
}

.results-count {
  font-size: 14px;
  color: #666;
}

.dark-mode .results-count {
  color: #a0a0c0;
}

.result-card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .result-card {
  background: rgba(40, 40, 60, 0.9);
  border-color: rgba(255, 255, 255, 0.05);
}

.result-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.category-tag {
  padding: 4px 10px;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  border-radius: 6px;
  font-size: 12px;
}

.post-time {
  font-size: 12px;
  color: #999;
}

.result-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
  line-height: 1.4;
}

.dark-mode .result-title {
  color: #e0e0e0;
}

.result-title :deep(mark) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 2px 4px;
  border-radius: 4px;
}

.result-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 15px;
}

.dark-mode .result-content {
  color: #a0a0c0;
}

.result-content :deep(mark) {
  background: rgba(102, 126, 234, 0.3);
  color: inherit;
  padding: 2px 4px;
  border-radius: 4px;
}

.result-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .result-footer {
  border-top-color: rgba(255, 255, 255, 0.05);
}

.author-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-size: 14px;
}

.dark-mode .author-info {
  color: #a0a0c0;
}

.post-stats {
  display: flex;
  gap: 15px;
}

.stat-item {
  font-size: 13px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 30px;
  padding: 20px;
}

.page-btn {
  width: 40px;
  height: 40px;
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.8);
  color: #667eea;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dark-mode .page-btn {
  background: rgba(40, 40, 60, 0.8);
  border-color: rgba(255, 255, 255, 0.1);
}

.page-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #666;
}

.dark-mode .page-info {
  color: #a0a0c0;
}

/* 响应式 */
@media (max-width: 768px) {
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

  .result-footer {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
}
</style>
