<template>
  <div class="manor-home" :class="{ 'night-mode': isDarkMode }">
    <!-- 庄园背景 -->
    <ManorBackground :isDarkMode="isDarkMode" />
    
    <!-- 顶部导航 -->
    <ManorNav :isDarkMode="isDarkMode" @toggle-theme="toggleTheme" />
    
    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 欢迎语 -->
      <div class="welcome-section">
        <div class="welcome-card">
          <div class="welcome-icon">
            <i class="bi bi-flower1"></i>
          </div>
          <div class="welcome-text">
            <h1>欢迎来到择明庄园</h1>
            <p>在这片宁静的庄园里，分享你的故事与见解</p>
          </div>
        </div>
      </div>
      
      <!-- 帖子列表 -->
      <div class="posts-section">
        <div class="section-header">
          <div class="header-title">
            <i class="bi bi-journal-text"></i>
            <span>最新帖子</span>
          </div>
          <div class="filter-tabs">
            <button 
              :class="['tab-btn', { active: currentTab === 'latest' }]"
              @click="switchTab('latest')"
            >
              最新
            </button>
            <button 
              :class="['tab-btn', { active: currentTab === 'hot' }]"
              @click="switchTab('hot')"
            >
              热门
            </button>
          </div>
        </div>
        
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner">
            <i class="bi bi-flower2"></i>
          </div>
          <p>正在加载...</p>
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
            
            <!-- 标签 -->
            <div v-if="post.tags && post.tags.length > 0" class="post-tags">
              <span 
                v-for="tag in post.tags.slice(0, 3)" 
                :key="tag.id"
                class="tag-item"
                :style="{ backgroundColor: tag.color + '20', color: tag.color }"
              >
                {{ tag.name }}
              </span>
            </div>
            
            <div class="card-footer">
              <div class="author-info">
                <div class="author-avatar">
                  <img v-if="post.authorAvatar" :src="post.authorAvatar" alt="avatar" />
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
        
        <!-- 空状态 -->
        <div v-else class="empty-state">
          <div class="empty-icon">
            <i class="bi bi-flower3"></i>
          </div>
          <p>还没有帖子，来发布第一篇吧~</p>
          <router-link to="/create" class="empty-btn">
            <i class="bi bi-pencil-square"></i>
            发布帖子
          </router-link>
        </div>
        
        <!-- 加载更多 -->
        <div v-if="hasMore" class="load-more">
          <button class="load-btn" @click="loadMore" :disabled="loadingMore">
            <i v-if="loadingMore" class="bi bi-arrow-repeat spinning"></i>
            <span v-else>加载更多</span>
          </button>
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
import ManorNav from '@/components/ManorNav.vue'
import ManorBottomNav from '@/components/ManorBottomNav.vue'

const router = useRouter()
const isDarkMode = ref(localStorage.getItem('darkMode') === 'true')
const loading = ref(false)
const loadingMore = ref(false)
const posts = ref([])
const currentTab = ref('latest')
const currentPage = ref(1)
const hasMore = ref(true)

// 加载帖子
async function loadPosts(reset = false) {
  if (reset) {
    currentPage.value = 1
    posts.value = []
  }
  
  loading.value = true
  try {
    const endpoint = currentTab.value === 'hot' ? '/posts/hot' : '/posts'
    const res = await get(`${endpoint}?page=${currentPage.value}&size=10`)
    
    if (res.code === 200) {
      // 处理不同接口返回的数据格式
      // /posts 返回 { content: [...] }
      // /posts/hot 返回 [...] (直接是数组)
      let newPosts = []
      if (Array.isArray(res.data)) {
        // 热门帖子直接返回数组
        newPosts = res.data
      } else {
        // 普通帖子返回 { list: [...] } 或 { content: [...] }
        newPosts = res.data?.list || res.data?.content || []
      }
      if (reset) {
        posts.value = newPosts
      } else {
        posts.value.push(...newPosts)
      }
      hasMore.value = newPosts.length === 10
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
  await loadPosts()
  loadingMore.value = false
}

// 切换标签
function switchTab(tab) {
  currentTab.value = tab
  loadPosts(true)
}

// 跳转帖子详情
function goToPost(id) {
  router.push(`/post/${id}`)
}

// 切换主题
function toggleTheme() {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
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

onMounted(() => {
  loadPosts(true)
})
</script>

<style scoped>
.manor-home {
  min-height: 100vh;
  padding-top: 70px;
  padding-bottom: 90px;
}

.main-content {
  position: relative;
  z-index: 1;
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

/* 欢迎区 */
.welcome-section {
  margin-bottom: 24px;
}

.welcome-card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(139, 188, 143, 0.2);
}

.night-mode .welcome-card {
  background: rgba(40, 45, 55, 0.9);
  border-color: rgba(100, 140, 120, 0.2);
}

.welcome-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  color: white;
  flex-shrink: 0;
}

.night-mode .welcome-icon {
  background: linear-gradient(135deg, #4a7c5a, #3a6c4a);
}

.welcome-text h1 {
  font-size: 22px;
  font-weight: 700;
  color: #4a6a4a;
  margin-bottom: 6px;
  font-family: 'Georgia', serif;
}

.night-mode .welcome-text h1 {
  color: #8ab88a;
}

.welcome-text p {
  font-size: 14px;
  color: #7a8a7a;
}

.night-mode .welcome-text p {
  color: #9ab89a;
}

/* 帖子区域 */
.posts-section {
  background: rgba(255, 255, 255, 0.85);
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(139, 188, 143, 0.15);
}

.night-mode .posts-section {
  background: rgba(40, 45, 55, 0.85);
  border-color: rgba(100, 140, 120, 0.15);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(139, 188, 143, 0.2);
}

.night-mode .section-header {
  border-bottom-color: rgba(100, 140, 120, 0.2);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #4a6a4a;
  font-family: 'Georgia', serif;
}

.night-mode .header-title {
  color: #8ab88a;
}

.header-title i {
  color: #8FBC8F;
  font-size: 20px;
}

.filter-tabs {
  display: flex;
  gap: 8px;
}

.tab-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 20px;
  background: transparent;
  color: #7a8a7a;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.night-mode .tab-btn {
  color: #9ab89a;
}

.tab-btn.active {
  background: rgba(143, 188, 143, 0.2);
  color: #5a8c5a;
  font-weight: 600;
}

.night-mode .tab-btn.active {
  background: rgba(100, 140, 120, 0.2);
  color: #7ac87a;
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 60px 20px;
}

.loading-spinner {
  font-size: 48px;
  color: #8FBC8F;
  margin-bottom: 16px;
  animation: spin 2s infinite linear;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.loading-state p {
  color: #8a9a8a;
  font-size: 14px;
}

/* 帖子列表 */
.posts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  background: rgba(255, 255, 255, 0.7);
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(139, 188, 143, 0.1);
}

.night-mode .post-card {
  background: rgba(50, 55, 65, 0.7);
  border-color: rgba(100, 140, 120, 0.1);
}

.post-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border-color: rgba(143, 188, 143, 0.3);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.category-badge {
  padding: 4px 12px;
  background: rgba(143, 188, 143, 0.15);
  border-radius: 12px;
  font-size: 12px;
  color: #5a8c5a;
  font-weight: 500;
}

.night-mode .category-badge {
  background: rgba(100, 140, 120, 0.2);
  color: #7ac87a;
}

.post-time {
  font-size: 12px;
  color: #9a9a9a;
}

.night-mode .post-time {
  color: #8a9a9a;
}

.post-title {
  font-size: 17px;
  font-weight: 600;
  color: #3a5a3a;
  margin-bottom: 10px;
  line-height: 1.4;
  font-family: 'Georgia', serif;
}

.night-mode .post-title {
  color: #a8d8a8;
}

.post-summary {
  font-size: 14px;
  color: #6a7a6a;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.night-mode .post-summary {
  color: #9ab89a;
}

.post-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.tag-item {
  padding: 4px 10px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid rgba(139, 188, 143, 0.1);
}

.night-mode .card-footer {
  border-top-color: rgba(100, 140, 120, 0.1);
}

.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  background: rgba(143, 188, 143, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #5a8c5a;
  font-size: 16px;
}

.night-mode .author-avatar {
  background: rgba(100, 140, 120, 0.2);
  color: #7ac87a;
}

.author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.author-name {
  font-size: 13px;
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
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #8a9a8a;
}

.night-mode .stat-item {
  color: #9ab89a;
}

.stat-item i {
  font-size: 14px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  color: #8FBC8F;
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-state p {
  color: #8a9a8a;
  margin-bottom: 20px;
}

.empty-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  border-radius: 25px;
  color: white;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
}

.empty-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(124, 184, 124, 0.4);
}

/* 加载更多 */
.load-more {
  text-align: center;
  margin-top: 24px;
}

.load-btn {
  padding: 12px 32px;
  background: rgba(143, 188, 143, 0.15);
  border: 1px solid rgba(143, 188, 143, 0.3);
  border-radius: 25px;
  color: #5a8c5a;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.night-mode .load-btn {
  background: rgba(100, 140, 120, 0.2);
  border-color: rgba(100, 140, 120, 0.3);
  color: #7ac87a;
}

.load-btn:hover {
  background: rgba(143, 188, 143, 0.25);
}

.spinning {
  animation: spin 1s infinite linear;
}
</style>
