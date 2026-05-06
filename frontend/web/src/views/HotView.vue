<template>
  <div class="manor-hot" :class="{ 'night-mode': isDarkMode }">
    <!-- 庄园背景 -->
    <ManorBackground :isDarkMode="isDarkMode" />

    <!-- 顶部导航 -->
    <ManorNav title="热门排行" :isDarkMode="isDarkMode" @toggle-theme="toggleTheme">
      <template #left>
        <button class="nav-btn" @click="goBack">
          <i class="bi bi-arrow-left"></i>
        </button>
      </template>
    </ManorNav>

    <!-- 主内容 -->
    <main class="content-wrapper">
      <!-- 页面标题 -->
      <div class="page-header">
        <div class="header-icon">
          <i class="bi bi-fire"></i>
        </div>
        <div class="header-text">
          <h1>热门排行</h1>
          <p>发现庄园中最受欢迎的内容</p>
        </div>
      </div>

      <!-- 时间筛选 -->
      <div class="filter-section">
        <div class="filter-tabs">
          <button
            v-for="filter in timeFilters"
            :key="filter.value"
            :class="['filter-tab', { active: currentFilter === filter.value }]"
            @click="switchFilter(filter.value)"
          >
            {{ filter.label }}
          </button>
        </div>
      </div>

      <!-- 热门列表 -->
      <div class="hot-section">
        <!-- 加载中 -->
        <div v-if="loading" class="loading-state">
          <div class="manor-spinner">
            <div class="spinner-leaf leaf-1"></div>
            <div class="spinner-leaf leaf-2"></div>
            <div class="spinner-leaf leaf-3"></div>
          </div>
          <p>加载热门内容...</p>
        </div>

        <!-- 空状态 -->
        <div v-else-if="hotPosts.length === 0" class="empty-state">
          <div class="empty-illustration">
            <i class="bi bi-fire"></i>
            <div class="empty-decoration">
              <span class="leaf leaf-1">🍃</span>
              <span class="leaf leaf-2">🌿</span>
            </div>
          </div>
          <h3>暂无热门帖子</h3>
          <p>快去发布精彩内容，成为热门吧！</p>
        </div>

        <!-- 热门列表 -->
        <div v-else class="hot-list">
          <div
            v-for="(item, index) in hotPosts"
            :key="item.id"
            class="hot-card"
            :class="{ 'top-three': index < 3 }"
            :style="{ animationDelay: `${index * 0.05}s` }"
            @click="goToPost(item.id)"
          >
            <div class="card-glow"></div>
            
            <!-- 排名 -->
            <div class="rank-badge" :class="`rank-${index + 1}`">
              <span class="rank-number">{{ index + 1 }}</span>
            </div>

            <!-- 内容 -->
            <div class="card-content">
              <h3 class="post-title">{{ item.title }}</h3>
              <p class="post-summary" v-if="item.summary">
                {{ item.summary.substring(0, 60) }}...
              </p>
              
              <div class="post-meta">
                <span class="author-name">
                  <i class="bi bi-person-circle"></i>
                  {{ item.authorName || '匿名用户' }}
                </span>
                <span class="category-tag" v-if="item.categoryName">
                  {{ item.categoryName }}
                </span>
              </div>

              <div class="post-stats">
                <span class="stat-item heat">
                  <i class="bi bi-fire"></i>
                  {{ calculateHeat(item) }}°
                </span>
                <span class="stat-item">
                  <i class="bi bi-eye"></i>
                  {{ formatNumber(item.viewCount || 0) }}
                </span>
                <span class="stat-item">
                  <i class="bi bi-heart"></i>
                  {{ formatNumber(item.likeCount || 0) }}
                </span>
                <span class="stat-item">
                  <i class="bi bi-chat"></i>
                  {{ formatNumber(item.commentCount || 0) }}
                </span>
              </div>
            </div>

            <div class="card-arrow">
              <i class="bi bi-chevron-right"></i>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 底部导航 -->
    <ManorBottomNav :isDarkMode="isDarkMode" active="hot" />
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
const hotPosts = ref([])
const currentFilter = ref('day')

const timeFilters = [
  { label: '今日', value: 'day' },
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '总榜', value: 'all' }
]

function toggleTheme() {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
}

function goBack() {
  router.back()
}

function switchFilter(filter) {
  currentFilter.value = filter
  loadHotPosts()
}

async function loadHotPosts() {
  loading.value = true
  try {
    const res = await get('/posts/hot', {
      period: currentFilter.value,
      size: 50
    })
    if (res.code === 200) {
      hotPosts.value = res.data || []
    }
  } catch (error) {
    console.error('加载热门帖子失败', error)
  } finally {
    loading.value = false
  }
}

function calculateHeat(post) {
  const views = post.viewCount || 0
  const likes = post.likeCount || 0
  const comments = post.commentCount || 0
  return Math.floor(views * 0.5 + likes * 2 + comments * 3)
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

function goToPost(id) {
  router.push(`/post/${id}`)
}

onMounted(() => {
  loadHotPosts()
})
</script>

<style scoped>
.manor-hot {
  min-height: 100vh;
  padding-top: 70px;
  padding-bottom: 90px;
}

.content-wrapper {
  position: relative;
  z-index: 1;
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
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
  background: linear-gradient(135deg, #e8a87c, #deb887);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  box-shadow: 0 4px 15px rgba(232, 168, 124, 0.3);
}

.night-mode .header-icon {
  background: linear-gradient(135deg, #b08060, #8b7355);
  box-shadow: 0 4px 15px rgba(139, 115, 85, 0.4);
}

.header-text h1 {
  font-size: 24px;
  font-weight: 700;
  color: #4a6a4a;
  margin-bottom: 4px;
  font-family: 'Georgia', serif;
}

.night-mode .header-text h1 {
  color: #8ab88a;
}

.header-text p {
  font-size: 14px;
  color: #7a8a7a;
}

.night-mode .header-text p {
  color: #9ab89a;
}

/* 筛选标签 */
.filter-section {
  margin-bottom: 24px;
}

.filter-tabs {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-tab {
  padding: 10px 24px;
  border: none;
  border-radius: 25px;
  background: rgba(255, 255, 255, 0.8);
  color: #5a7c5a;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(139, 188, 143, 0.3);
}

.night-mode .filter-tab {
  background: rgba(40, 45, 55, 0.8);
  color: #8ab88a;
  border-color: rgba(100, 140, 120, 0.3);
}

.filter-tab:hover {
  background: rgba(143, 188, 143, 0.2);
  transform: translateY(-2px);
}

.filter-tab.active {
  background: linear-gradient(135deg, #e8a87c, #deb887);
  color: white;
  border-color: transparent;
  box-shadow: 0 4px 15px rgba(232, 168, 124, 0.3);
}

.night-mode .filter-tab.active {
  background: linear-gradient(135deg, #b08060, #8b7355);
  box-shadow: 0 4px 15px rgba(139, 115, 85, 0.4);
}

/* 热门区域 */
.hot-section {
  background: rgba(255, 255, 255, 0.85);
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(139, 188, 143, 0.15);
}

.night-mode .hot-section {
  background: rgba(40, 45, 55, 0.85);
  border-color: rgba(100, 140, 120, 0.15);
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 60px 20px;
}

.manor-spinner {
  position: relative;
  width: 50px;
  height: 50px;
  margin: 0 auto 16px;
}

.spinner-leaf {
  position: absolute;
  width: 20px;
  height: 20px;
  background: linear-gradient(135deg, #e8a87c, #deb887);
  border-radius: 0 50% 50% 50%;
  transform-origin: center;
}

.leaf-1 {
  top: 0;
  left: 50%;
  transform: translateX(-50%) rotate(0deg);
  animation: spin-leaf 1s infinite ease-in-out;
}

.leaf-2 {
  bottom: 10%;
  left: 10%;
  transform: rotate(120deg);
  animation: spin-leaf 1s infinite ease-in-out 0.33s;
}

.leaf-3 {
  bottom: 10%;
  right: 10%;
  transform: rotate(240deg);
  animation: spin-leaf 1s infinite ease-in-out 0.66s;
}

@keyframes spin-leaf {
  0%, 100% { opacity: 1; transform: scale(1) rotate(var(--rotation, 0deg)); }
  50% { opacity: 0.5; transform: scale(0.8) rotate(var(--rotation, 0deg)); }
}

.loading-state p {
  color: #8a9a8a;
  font-size: 14px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-illustration {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-illustration > i {
  font-size: 60px;
  color: #e8d8c8;
}

.night-mode .empty-illustration > i {
  color: #5a4a3a;
}

.empty-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
}

.empty-decoration .leaf {
  position: absolute;
  font-size: 20px;
  opacity: 0.6;
  animation: float-leaf 3s ease-in-out infinite;
}

.leaf-1 {
  top: 0;
  right: 0;
  animation-delay: 0s;
}

.leaf-2 {
  bottom: 10px;
  left: 0;
  animation-delay: 1.5s;
}

@keyframes float-leaf {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-10px) rotate(10deg); }
}

.empty-state h3 {
  font-size: 18px;
  color: #5a7c5a;
  margin-bottom: 8px;
}

.night-mode .empty-state h3 {
  color: #8ab88a;
}

.empty-state p {
  font-size: 14px;
  color: #8a9a8a;
}

/* 热门列表 */
.hot-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hot-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(139, 188, 143, 0.1);
  animation: slideIn 0.4s ease forwards;
  opacity: 0;
  transform: translateY(20px);
}

@keyframes slideIn {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.night-mode .hot-card {
  background: rgba(50, 55, 65, 0.7);
  border-color: rgba(100, 140, 120, 0.1);
}

.hot-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border-color: rgba(143, 188, 143, 0.3);
}

.hot-card.top-three {
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.08) 0%, rgba(255, 255, 255, 0.7) 100%);
  border: 1px solid rgba(255, 215, 0, 0.2);
}

.night-mode .hot-card.top-three {
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.1) 0%, rgba(50, 55, 65, 0.7) 100%);
  border-color: rgba(255, 215, 0, 0.15);
}

.card-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(232, 168, 124, 0.1), transparent);
  border-radius: 16px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.hot-card:hover .card-glow {
  opacity: 1;
}

/* 排名徽章 */
.rank-badge {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  flex-shrink: 0;
  background: rgba(139, 188, 143, 0.2);
  color: #5a7c5a;
}

.night-mode .rank-badge {
  background: rgba(100, 140, 120, 0.2);
  color: #8ab88a;
}

.rank-badge.rank-1 {
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: white;
  box-shadow: 0 4px 15px rgba(255, 215, 0, 0.4);
}

.rank-badge.rank-2 {
  background: linear-gradient(135deg, #C0C0C0, #A0A0A0);
  color: white;
  box-shadow: 0 4px 15px rgba(192, 192, 192, 0.4);
}

.rank-badge.rank-3 {
  background: linear-gradient(135deg, #CD7F32, #B87333);
  color: white;
  box-shadow: 0 4px 15px rgba(205, 127, 50, 0.4);
}

/* 卡片内容 */
.card-content {
  flex: 1;
  min-width: 0;
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  color: #3a5a3a;
  margin-bottom: 6px;
  font-family: 'Georgia', serif;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.night-mode .post-title {
  color: #a8d8a8;
}

.post-summary {
  font-size: 13px;
  color: #7a8a7a;
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.night-mode .post-summary {
  color: #9ab89a;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.author-name {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #8a9a8a;
}

.author-name i {
  color: #8FBC8F;
}

.category-tag {
  padding: 2px 8px;
  background: rgba(139, 188, 143, 0.15);
  border-radius: 10px;
  font-size: 11px;
  color: #5a8c5a;
}

.night-mode .category-tag {
  background: rgba(100, 140, 120, 0.2);
  color: #7ac87a;
}

.post-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #8a9a8a;
}

.stat-item i {
  color: #8FBC8F;
}

.stat-item.heat {
  color: #e8a87c;
  font-weight: 600;
}

.stat-item.heat i {
  color: #e8a87c;
}

/* 箭头 */
.card-arrow {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(139, 188, 143, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8FBC8F;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.hot-card:hover .card-arrow {
  background: linear-gradient(135deg, #e8a87c, #deb887);
  color: white;
}

/* 返回按钮 */
.nav-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: rgba(139, 188, 143, 0.2);
  color: #5a7c5a;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.night-mode .nav-btn {
  background: rgba(100, 140, 120, 0.2);
  color: #8ab88a;
}

.nav-btn:hover {
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  color: white;
  transform: scale(1.05);
}

/* 响应式 */
@media (max-width: 640px) {
  .content-wrapper {
    padding: 16px;
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

  .filter-tabs {
    justify-content: center;
  }

  .filter-tab {
    padding: 8px 16px;
    font-size: 13px;
  }

  .hot-card {
    padding: 12px;
  }

  .rank-badge {
    width: 32px;
    height: 32px;
    font-size: 14px;
  }

  .post-title {
    font-size: 14px;
  }

  .post-stats {
    gap: 12px;
  }
}
</style>
