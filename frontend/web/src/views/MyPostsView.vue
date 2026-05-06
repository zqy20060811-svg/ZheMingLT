<template>
  <div class="manor-my-posts" :class="{ 'night-mode': isDarkMode }">
    <!-- 庄园背景 -->
    <ManorBackground :isDarkMode="isDarkMode" />

    <!-- 顶部导航 -->
    <ManorNav title="我的帖子" :isDarkMode="isDarkMode" @toggle-theme="toggleTheme">
      <template #left>
        <button class="nav-btn" @click="goBack">
          <i class="bi bi-arrow-left"></i>
        </button>
      </template>
    </ManorNav>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stats-card">
        <div class="stat-icon">
          <i class="bi bi-file-text-fill"></i>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ posts.length }}</span>
          <span class="stat-label">已发布帖子</span>
        </div>
      </div>
    </div>

    <!-- 帖子列表 -->
    <div class="content-wrapper">
      <!-- 加载中 -->
      <div v-if="loading" class="loading-state">
        <div class="manor-spinner">
          <div class="spinner-leaf leaf-1"></div>
          <div class="spinner-leaf leaf-2"></div>
          <div class="spinner-leaf leaf-3"></div>
        </div>
        <p>加载中...</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="posts.length === 0" class="empty-state">
        <div class="empty-illustration">
          <i class="bi bi-file-earmark-text"></i>
          <div class="empty-decoration">
            <span class="leaf leaf-1">🍃</span>
            <span class="leaf leaf-2">🌿</span>
          </div>
        </div>
        <h3>还没有发布帖子</h3>
        <p>去发布你的第一篇帖子吧~</p>
        <button class="action-btn" @click="goToCreate">
          <i class="bi bi-plus-lg"></i>
          发布帖子
        </button>
      </div>

      <!-- 帖子列表 -->
      <div v-else class="posts-list">
        <div
          v-for="post in posts"
          :key="post.id"
          class="post-card"
          @click="goToPost(post.id)"
        >
          <div class="card-glow"></div>
          <div class="card-content">
            <div class="post-header">
              <span class="post-category" v-if="post.categoryName">
                <i class="bi bi-folder-fill"></i>
                {{ post.categoryName }}
              </span>
              <span class="post-time">
                <i class="bi bi-clock"></i>
                {{ formatTime(post.createdAt) }}
              </span>
            </div>
            <h3 class="post-title">{{ post.title }}</h3>
            <p class="post-summary">{{ post.summary }}</p>
            <div class="post-stats">
              <span class="stat-item">
                <i class="bi bi-eye-fill"></i>
                {{ post.viewCount || 0 }}
              </span>
              <span class="stat-item">
                <i class="bi bi-heart-fill"></i>
                {{ post.likeCount || 0 }}
              </span>
              <span class="stat-item">
                <i class="bi bi-chat-fill"></i>
                {{ post.commentCount || 0 }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 加载更多 -->
      <div v-if="hasMore" class="load-more">
        <button class="load-btn" @click="loadMore" :disabled="loadingMore">
          <span v-if="loadingMore" class="btn-spinner"></span>
          <span v-else>
            <i class="bi bi-chevron-down"></i>
            加载更多
          </span>
        </button>
      </div>
    </div>

    <!-- 悬浮发布按钮 -->
    <button class="fab-btn" @click="goToCreate" title="发布帖子">
      <i class="bi bi-plus-lg"></i>
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '@/utils/request'
import ManorBackground from '@/components/ManorBackground.vue'
import ManorNav from '@/components/ManorNav.vue'

const router = useRouter()
const isDarkMode = ref(localStorage.getItem('darkMode') === 'true')
const loading = ref(false)
const loadingMore = ref(false)
const posts = ref([])
const currentPage = ref(1)
const hasMore = ref(true)
const userId = ref(null)

// 切换主题
function toggleTheme() {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
}

// 获取当前用户信息
async function loadUserInfo() {
  try {
    const res = await get('/users/profile')
    if (res.code === 200) {
      userId.value = res.data.id
      loadPosts()
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
}

// 加载帖子列表
async function loadPosts() {
  if (!userId.value) return

  loading.value = true
  try {
    const res = await get(`/users/${userId.value}/posts?page=${currentPage.value}&size=10`)
    if (res.code === 200) {
      const pageData = res.data
      if (currentPage.value === 1) {
        posts.value = pageData.list || []
      } else {
        posts.value.push(...(pageData.list || []))
      }
      hasMore.value = posts.value.length < (pageData.total || 0)
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

// 返回
function goBack() {
  router.back()
}

// 去发布帖子
function goToCreate() {
  router.push('/create')
}

// 查看帖子详情
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

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.manor-my-posts {
  min-height: 100vh;
  position: relative;
  padding-bottom: 100px;
}

/* 导航按钮 */
.nav-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 12px;
  background: rgba(139, 115, 85, 0.1);
  color: #5a4a3a;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
}

.nav-btn:hover {
  background: rgba(139, 115, 85, 0.2);
  transform: translateY(-2px);
}

.night-mode .nav-btn {
  background: rgba(139, 115, 85, 0.2);
  color: #d4c5b0;
}

/* 统计区域 */
.stats-section {
  padding: 20px;
  position: relative;
  z-index: 1;
}

.stats-card {
  display: flex;
  align-items: center;
  gap: 20px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(90, 74, 58, 0.1);
  border: 1px solid rgba(139, 115, 85, 0.1);
}

.night-mode .stats-card {
  background: rgba(40, 45, 60, 0.9);
  border-color: rgba(139, 115, 85, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.stats-card .stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  background: linear-gradient(135deg, #8b7355 0%, #a0826d 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
}

.stats-card .stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stats-card .stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: #5a4a3a;
  line-height: 1;
}

.night-mode .stats-card .stat-value {
  color: #d4c5b0;
}

.stats-card .stat-label {
  font-size: 0.9rem;
  color: #8b7355;
}

.night-mode .stats-card .stat-label {
  color: #a09080;
}

/* 内容区域 */
.content-wrapper {
  padding: 0 20px;
  position: relative;
  z-index: 1;
}

/* 庄园风格加载动画 */
.loading-state {
  text-align: center;
  padding: 60px 20px;
}

.manor-spinner {
  position: relative;
  width: 60px;
  height: 60px;
  margin: 0 auto 20px;
}

.spinner-leaf {
  position: absolute;
  width: 20px;
  height: 20px;
  background: linear-gradient(135deg, #8b7355 0%, #a0826d 100%);
  border-radius: 0 50% 0 50%;
  animation: leaf-spin 1.5s ease-in-out infinite;
}

.spinner-leaf.leaf-1 {
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  animation-delay: 0s;
}

.spinner-leaf.leaf-2 {
  bottom: 10px;
  left: 5px;
  animation-delay: 0.5s;
}

.spinner-leaf.leaf-3 {
  bottom: 10px;
  right: 5px;
  animation-delay: 1s;
}

@keyframes leaf-spin {
  0%, 100% { opacity: 0.3; transform: scale(0.8) rotate(0deg); }
  50% { opacity: 1; transform: scale(1) rotate(180deg); }
}

.loading-state p {
  color: #8b7355;
  font-size: 0.95rem;
}

.night-mode .loading-state p {
  color: #a09080;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-illustration {
  position: relative;
  display: inline-block;
  margin-bottom: 24px;
}

.empty-illustration > i {
  font-size: 4rem;
  color: #c4b5a0;
}

.night-mode .empty-illustration > i {
  color: #6b5b4f;
}

.empty-decoration .leaf {
  position: absolute;
  font-size: 1.2rem;
  opacity: 0.6;
  animation: float-leaf 3s ease-in-out infinite;
}

.empty-decoration .leaf-1 {
  top: -10px;
  left: -20px;
  animation-delay: 0s;
}

.empty-decoration .leaf-2 {
  bottom: 0;
  right: -20px;
  animation-delay: 1.5s;
}

@keyframes float-leaf {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-10px) rotate(10deg); }
}

.empty-state h3 {
  font-size: 1.3rem;
  color: #5a4a3a;
  margin-bottom: 8px;
  font-weight: 600;
}

.night-mode .empty-state h3 {
  color: #d4c5b0;
}

.empty-state p {
  color: #8b7355;
  font-size: 0.9rem;
  margin-bottom: 24px;
}

.night-mode .empty-state p {
  color: #a09080;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 28px;
  border: none;
  border-radius: 25px;
  background: linear-gradient(135deg, #8b7355 0%, #a0826d 100%);
  color: white;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(139, 115, 85, 0.3);
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(139, 115, 85, 0.4);
}

/* 帖子列表 */
.posts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  position: relative;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(139, 115, 85, 0.1);
}

.night-mode .post-card {
  background: rgba(40, 45, 55, 0.9);
  border-color: rgba(139, 115, 85, 0.2);
}

.post-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 40px rgba(90, 74, 58, 0.15);
}

.night-mode .post-card:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
}

.card-glow {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(139, 115, 85, 0.05) 0%, transparent 50%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.post-card:hover .card-glow {
  opacity: 1;
}

.card-content {
  position: relative;
  padding: 20px;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.post-category {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  background: rgba(139, 115, 85, 0.1);
  color: #8b7355;
  font-size: 0.8rem;
  font-weight: 500;
}

.night-mode .post-category {
  background: rgba(139, 115, 85, 0.2);
  color: #c4b5a0;
}

.post-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.8rem;
  color: #a09080;
}

.post-time i {
  font-size: 0.75rem;
}

.post-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #5a4a3a;
  margin-bottom: 8px;
  line-height: 1.4;
}

.night-mode .post-title {
  color: #d4c5b0;
}

.post-summary {
  font-size: 0.9rem;
  color: #6b5b4f;
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.night-mode .post-summary {
  color: #a09080;
}

.post-stats {
  display: flex;
  gap: 20px;
}

.post-stats .stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.85rem;
  color: #8b7355;
}

.night-mode .post-stats .stat-item {
  color: #a09080;
}

.post-stats .stat-item i {
  font-size: 0.9rem;
}

/* 加载更多 */
.load-more {
  text-align: center;
  padding: 20px 0 40px;
}

.load-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  background: rgba(139, 115, 85, 0.1);
  color: #8b7355;
  border: 1px solid rgba(139, 115, 85, 0.2);
  border-radius: 25px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.night-mode .load-btn {
  background: rgba(139, 115, 85, 0.2);
  color: #c4b5a0;
  border-color: rgba(139, 115, 85, 0.3);
}

.load-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #8b7355 0%, #a0826d 100%);
  color: white;
  border-color: transparent;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(139, 115, 85, 0.3);
}

.load-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-spinner {
  display: inline-block;
  width: 18px;
  height: 18px;
  border: 2px solid rgba(139, 115, 85, 0.3);
  border-top-color: #8b7355;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 悬浮按钮 */
.fab-btn {
  position: fixed;
  bottom: 100px;
  right: 20px;
  width: 56px;
  height: 56px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #8b7355 0%, #a0826d 100%);
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(139, 115, 85, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.fab-btn:hover {
  transform: scale(1.1) rotate(90deg);
  box-shadow: 0 6px 25px rgba(139, 115, 85, 0.5);
}

/* 响应式 */
@media (max-width: 768px) {
  .stats-card {
    padding: 20px;
  }

  .stats-card .stat-icon {
    width: 50px;
    height: 50px;
    font-size: 1.5rem;
  }

  .stats-card .stat-value {
    font-size: 1.6rem;
  }

  .card-content {
    padding: 16px;
  }

  .post-title {
    font-size: 1rem;
  }

  .post-summary {
    font-size: 0.85rem;
  }
}
</style>
