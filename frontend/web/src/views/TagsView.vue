<template>
  <div class="manor-tags" :class="{ 'night-mode': isDarkMode }">
    <!-- 庄园背景 -->
    <ManorBackground :isDarkMode="isDarkMode" />

    <!-- 顶部导航 -->
    <nav class="tags-top-nav">
      <div class="nav-left">
        <button class="back-btn" @click="goBack">
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
    <main class="tags-content">
      <!-- 页面标题 -->
      <div class="page-header" v-if="!selectedTag">
        <div class="header-icon">
          <i class="bi bi-tags-fill"></i>
        </div>
        <div class="header-text">
          <h1>标签花园</h1>
          <p>在花园中探索各种话题标签</p>
        </div>
      </div>

      <!-- 搜索栏 -->
      <div class="search-section">
        <div class="search-box">
          <i class="bi bi-search"></i>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索标签..."
            class="search-input"
            @input="searchTags"
          />
          <button v-if="searchQuery" class="clear-btn" @click="clearSearch">
            <i class="bi bi-x-circle-fill"></i>
          </button>
        </div>
      </div>

      <!-- 标签云 -->
      <div v-if="!selectedTag" class="tags-cloud-section">
        <div class="cloud-card">
          <div class="cloud-header">
            <i class="bi bi-flower1"></i>
            <span>热门标签</span>
          </div>
          <div class="tags-container">
            <span
              v-for="tag in filteredTags"
              :key="tag.id"
              class="tag-item"
              :style="{ 
                backgroundColor: tag.color + '25',
                color: tag.color,
                borderColor: tag.color + '40',
                fontSize: getTagSize(tag.usageCount) + 'px'
              }"
              @click="selectTag(tag)"
            >
              <i class="bi bi-tag-fill"></i>
              {{ tag.name }}
              <span class="tag-count">{{ tag.usageCount }}</span>
            </span>
          </div>
        </div>
      </div>

      <!-- 选中标签的帖子列表 -->
      <div v-else class="tag-posts-section">
        <div class="tag-header-card">
          <button class="btn-back" @click="selectedTag = null">
            <i class="bi bi-arrow-left"></i>
            <span>返回标签花园</span>
          </button>
          <div class="selected-tag-info">
            <span
              class="tag-badge-large"
              :style="{ backgroundColor: selectedTag.color }"
            >
              <i class="bi bi-tag-fill"></i>
              {{ selectedTag.name }}
            </span>
            <span class="post-count">
              <i class="bi bi-file-text"></i>
              {{ totalPosts }} 个帖子
            </span>
          </div>
        </div>

        <!-- 加载中 -->
        <div v-if="loading" class="loading-state">
          <div class="loading-flower">
            <i class="bi bi-flower2"></i>
          </div>
          <span>加载中...</span>
        </div>

        <!-- 帖子列表 -->
        <div v-else-if="posts.length > 0" class="posts-list">
          <article
            v-for="post in posts"
            :key="post.id"
            class="post-card"
            @click="goToPost(post.id)"
          >
            <div class="card-header">
              <span class="category-badge">{{ post.categoryName }}</span>
              <span class="post-time">{{ formatTime(post.createdAt) }}</span>
            </div>
            <h3 class="post-title">{{ post.title }}</h3>
            <p class="post-summary">{{ post.summary }}</p>
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

          <!-- 加载更多 -->
          <div v-if="hasMore" class="load-more">
            <button class="btn-load-more" @click="loadMore" :disabled="loadingMore">
              <i v-if="loadingMore" class="bi bi-arrow-repeat spinning"></i>
              <span v-else>加载更多</span>
            </button>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <div class="empty-icon">
            <i class="bi bi-flower3"></i>
          </div>
          <h3>该标签下暂无帖子</h3>
          <p>去发布一篇相关帖子吧~</p>
        </div>
      </div>
    </main>

    <!-- 底部导航 -->
    <ManorBottomNav :isDarkMode="isDarkMode" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '@/utils/request'
import ManorBackground from '@/components/ManorBackground.vue'
import ManorBottomNav from '@/components/ManorBottomNav.vue'

const router = useRouter()
const isDarkMode = ref(localStorage.getItem('darkMode') === 'true')
const searchQuery = ref('')
const tags = ref([])
const selectedTag = ref(null)
const posts = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const currentPage = ref(1)
const hasMore = ref(true)
const totalPosts = ref(0)

// 过滤后的标签
const filteredTags = computed(() => {
  if (!searchQuery.value) return tags.value
  const query = searchQuery.value.toLowerCase()
  return tags.value.filter(tag => tag.name.toLowerCase().includes(query))
})

// 加载所有标签
async function loadTags() {
  try {
    const res = await get('/tags')
    if (res.code === 200) {
      tags.value = res.data || []
    }
  } catch (error) {
    console.error('加载标签失败', error)
  }
}

// 搜索标签
async function searchTags() {
  if (!searchQuery.value) {
    loadTags()
    return
  }
  try {
    const res = await get(`/tags/search?keyword=${searchQuery.value}`)
    if (res.code === 200) {
      tags.value = res.data || []
    }
  } catch (error) {
    console.error('搜索标签失败', error)
  }
}

// 清空搜索
function clearSearch() {
  searchQuery.value = ''
  loadTags()
}

// 选择标签
async function selectTag(tag) {
  selectedTag.value = tag
  currentPage.value = 1
  posts.value = []
  await loadPostsByTag()
}

// 加载标签下的帖子
async function loadPostsByTag() {
  if (!selectedTag.value) return
  
  loading.value = true
  try {
    const res = await get(`/tags/${selectedTag.value.id}/posts?page=${currentPage.value}&size=10`)
    if (res.code === 200) {
      const pageData = res.data
      if (currentPage.value === 1) {
        posts.value = pageData.list || []
      } else {
        posts.value.push(...(pageData.list || []))
      }
      totalPosts.value = pageData.total || 0
      hasMore.value = posts.value.length < totalPosts.value
    }
  } catch (error) {
    console.error('加载帖子失败', error)
  } finally {
    loading.value = false
  }
}

// 加载更多
async function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  currentPage.value++
  await loadPostsByTag()
  loadingMore.value = false
}

// 根据使用次数计算标签大小
function getTagSize(count) {
  const max = Math.max(...tags.value.map(t => t.usageCount), 1)
  const min = Math.min(...tags.value.map(t => t.usageCount), 0)
  const normalized = (count - min) / (max - min || 1)
  return 14 + normalized * 8
}

// 返回
function goBack() {
  router.back()
}

// 返回首页
function goHome() {
  router.push('/')
}

// 查看帖子
function goToPost(id) {
  router.push(`/post/${id}`)
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

function toggleTheme() {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
}

onMounted(() => {
  loadTags()
})
</script>

<style scoped>
.manor-tags {
  min-height: 100vh;
  position: relative;
  padding-bottom: 100px;
}

/* 顶部导航 */
.tags-top-nav {
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

.night-mode .tags-top-nav {
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
.tags-content {
  max-width: 900px;
  margin: 0 auto;
  padding: 90px 20px 40px;
  position: relative;
  z-index: 1;
}

/* 页面标题 */
.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.header-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #DDA0DD, #DA70D6);
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

/* 搜索栏 */
.search-section {
  margin-bottom: 24px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 6px 6px 6px 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(139, 188, 143, 0.2);
}

.night-mode .search-box {
  background: rgba(40, 45, 55, 0.95);
  border-color: rgba(74, 124, 90, 0.2);
}

.search-box i {
  font-size: 20px;
  color: #8FBC8F;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 16px;
  color: #2d3748;
  outline: none;
  padding: 12px 0;
}

.night-mode .search-input {
  color: #e2e8f0;
}

.search-input::placeholder {
  color: #8a9a8a;
}

.clear-btn {
  background: none;
  border: none;
  color: #8a9a8a;
  cursor: pointer;
  font-size: 18px;
  padding: 8px;
  transition: all 0.3s ease;
}

.clear-btn:hover {
  color: #ff6b6b;
}

/* 标签云 */
.tags-cloud-section {
  margin-bottom: 24px;
}

.cloud-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(139, 188, 143, 0.2);
}

.night-mode .cloud-card {
  background: rgba(40, 45, 55, 0.95);
  border-color: rgba(74, 124, 90, 0.2);
}

.cloud-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
  font-family: 'Georgia', serif;
}

.night-mode .cloud-header {
  color: #e2e8f0;
}

.cloud-header i {
  color: #8FBC8F;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
  border: 1px solid transparent;
}

.tag-item:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.tag-count {
  font-size: 12px;
  opacity: 0.8;
  background: rgba(255, 255, 255, 0.3);
  padding: 2px 8px;
  border-radius: 10px;
}

/* 标签帖子区域 */
.tag-posts-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.tag-header-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 20px 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(139, 188, 143, 0.2);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.night-mode .tag-header-card {
  background: rgba(40, 45, 55, 0.95);
  border-color: rgba(74, 124, 90, 0.2);
}

.btn-back {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  background: rgba(139, 188, 143, 0.15);
  border-radius: 12px;
  color: #5a7c5a;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  width: fit-content;
  font-weight: 500;
}

.night-mode .btn-back {
  background: rgba(74, 124, 90, 0.2);
  color: #8ab88a;
}

.btn-back:hover {
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  color: white;
}

.selected-tag-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.tag-badge-large {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 24px;
  border-radius: 24px;
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.post-count {
  color: #6a7c6a;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.night-mode .post-count {
  color: #9ab89a;
}

/* 帖子列表 */
.posts-list {
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

.post-summary {
  font-size: 14px;
  color: #6a7c6a;
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.night-mode .post-summary {
  color: #9ab89a;
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

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 60px 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(139, 188, 143, 0.2);
}

.night-mode .loading-state {
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

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(139, 188, 143, 0.2);
}

.night-mode .empty-state {
  background: rgba(40, 45, 55, 0.95);
  border-color: rgba(74, 124, 90, 0.2);
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

/* 加载更多 */
.load-more {
  text-align: center;
  padding: 20px;
}

.btn-load-more {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 32px;
  border-radius: 24px;
  border: 1px solid rgba(139, 188, 143, 0.3);
  background: rgba(255, 255, 255, 0.9);
  color: #5a7c5a;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.night-mode .btn-load-more {
  background: rgba(40, 45, 55, 0.9);
  border-color: rgba(74, 124, 90, 0.3);
  color: #8ab88a;
}

.btn-load-more:hover:not(:disabled) {
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  color: white;
  border-color: transparent;
}

.btn-load-more:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 响应式 */
@media (max-width: 640px) {
  .tags-content {
    padding: 80px 16px 40px;
  }

  .page-header {
    flex-direction: column;
    text-align: center;
  }

  .header-icon {
    width: 48px;
    height: 48px;
    font-size: 24px;
  }

  .cloud-card {
    padding: 20px 16px;
  }

  .tag-header-card {
    padding: 16px;
  }

  .selected-tag-info {
    flex-direction: column;
    align-items: flex-start;
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
