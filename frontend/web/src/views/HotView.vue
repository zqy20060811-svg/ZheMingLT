<template>
  <div class="hot-page" :class="{ 'dark-mode': isDarkMode }">
    <!-- 动态背景 -->
    <div class="animated-bg">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
    </div>

    <!-- 粒子效果 -->
    <div class="particles" ref="particlesRef"></div>

    <!-- 顶部导航 -->
    <nav class="top-nav">
      <div class="nav-brand">
        <div class="brand-icon">
          <i class="bi bi-fire"></i>
        </div>
        <div class="brand-text">
          <span class="brand-title">热门排行</span>
          <span class="brand-subtitle">发现精彩内容</span>
        </div>
      </div>
      <button class="btn-icon theme-btn" @click="toggleTheme">
        <i :class="isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-fill'"></i>
      </button>
    </nav>

    <!-- 时间筛选 -->
    <div class="filter-section">
      <div class="filter-container">
        <button
          v-for="filter in timeFilters"
          :key="filter.value"
          :class="['filter-btn', { active: currentFilter === filter.value }]"
          @click="switchFilter(filter.value)"
        >
          <i :class="filter.icon"></i>
          <span>{{ filter.label }}</span>
          <div class="btn-glow"></div>
        </button>
      </div>
    </div>

    <!-- 热门列表 -->
    <div class="content-area">
      <div v-if="loading" class="loading-state">
        <div class="spinner-wrapper">
          <div class="spinner"></div>
          <div class="spinner-ring"></div>
        </div>
        <span class="loading-text">加载热门内容...</span>
      </div>

      <div v-else-if="hotPosts.length > 0" class="hot-list">
        <div
          v-for="(item, index) in hotPosts"
          :key="item.id"
          class="hot-card"
          :class="{ 'top-three': index < 3 }"
          :style="{ animationDelay: `${index * 0.1}s` }"
          @click="goToPost(item.id)"
        >
          <div class="card-glow"></div>
          <div class="rank-section">
            <div class="rank-badge" :class="`rank-${index + 1}`">
              <div class="rank-inner">
                <span class="rank-number">{{ index + 1 }}</span>
                <i v-if="index < 3" class="bi bi-trophy-fill"></i>
              </div>
              <div class="rank-glow"></div>
            </div>
            <div class="heat-indicator">
              <i class="bi bi-fire"></i>
              <span class="heat-value">{{ calculateHeat(item) }}°</span>
            </div>
          </div>

          <div class="content-section">
            <h4 class="post-title">{{ item.title }}</h4>
            <p class="post-summary" v-if="item.summary">
              {{ item.summary.substring(0, 80) }}...
            </p>
            <div class="post-meta">
              <div class="author-info">
                <div class="author-avatar">
                  <i class="bi bi-person-fill"></i>
                </div>
                <span class="author-name">{{ item.authorName || '匿名用户' }}</span>
              </div>
              <span class="category-tag" :style="getCategoryStyle(item.categoryName)">
                {{ item.categoryName || '未分类' }}
              </span>
            </div>
            <div class="post-stats">
              <div class="stat-item">
                <div class="stat-icon views">
                  <i class="bi bi-eye-fill"></i>
                </div>
                <span class="stat-value">{{ formatNumber(item.viewCount || 0) }}</span>
              </div>
              <div class="stat-item">
                <div class="stat-icon likes">
                  <i class="bi bi-heart-fill"></i>
                </div>
                <span class="stat-value">{{ formatNumber(item.likeCount || 0) }}</span>
              </div>
              <div class="stat-item">
                <div class="stat-icon comments">
                  <i class="bi bi-chat-fill"></i>
                </div>
                <span class="stat-value">{{ formatNumber(item.commentCount || 0) }}</span>
              </div>
            </div>
          </div>

          <div class="arrow-section">
            <i class="bi bi-chevron-right"></i>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <div class="empty-icon">
          <i class="bi bi-inbox"></i>
        </div>
        <h4>暂无热门帖子</h4>
        <p>快去发布精彩内容，成为热门吧！</p>
      </div>
    </div>

    <BottomNav active="hot" />
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
const hotPosts = ref([])
const currentFilter = ref('day')
const particlesRef = ref(null)

const timeFilters = [
  { label: '今日', value: 'day', icon: 'bi bi-sunrise' },
  { label: '本周', value: 'week', icon: 'bi bi-calendar-week' },
  { label: '本月', value: 'month', icon: 'bi bi-calendar-month' },
  { label: '总榜', value: 'all', icon: 'bi bi-trophy' }
]

function toggleTheme() {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
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

function getCategoryStyle(name) {
  const colors = {
    '技术': 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    '生活': 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    '娱乐': 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    '学习': 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    '职场': 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    '兴趣': 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)'
  }
  return { background: colors[name] || 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' }
}

function goToPost(id) {
  router.push(`/post/${id}`)
}

// 生成粒子
function generateParticles() {
  if (!particlesRef.value) return
  particlesRef.value.innerHTML = ''
  for (let i = 0; i < 30; i++) {
    const particle = document.createElement('div')
    particle.className = 'particle'
    particle.style.left = Math.random() * 100 + '%'
    particle.style.top = Math.random() * 100 + '%'
    particle.style.width = Math.random() * 4 + 2 + 'px'
    particle.style.height = particle.style.width
    particle.style.animationDelay = Math.random() * 5 + 's'
    particle.style.animationDuration = Math.random() * 5 + 5 + 's'
    particlesRef.value.appendChild(particle)
  }
}

onMounted(() => {
  isDarkMode.value = localStorage.getItem('darkMode') === 'true'
  generateParticles()
  loadHotPosts()
})
</script>

<style scoped>
.hot-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #fff5f5 0%, #fff 50%, #f8f9fa 100%);
  position: relative;
  overflow-x: hidden;
  padding-bottom: 80px;
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
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  top: -150px;
  right: -150px;
  animation-delay: 0s;
}

.orb-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #feca57 0%, #ff9ff3 100%);
  bottom: -100px;
  left: -100px;
  animation-delay: -5s;
}

.orb-3 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #48dbfb 0%, #0abde3 100%);
  top: 50%;
  left: 30%;
  animation-delay: -10s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(30px, -30px) scale(1.1);
  }
  50% {
    transform: translate(-20px, 20px) scale(0.95);
  }
  75% {
    transform: translate(20px, 10px) scale(1.05);
  }
}

/* 粒子效果 */
.particles {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 1;
}

.particle {
  position: absolute;
  background: rgba(255, 107, 107, 0.3);
  border-radius: 50%;
  animation: particle-float 10s infinite ease-in-out;
}

@keyframes particle-float {
  0%, 100% {
    transform: translateY(0) translateX(0);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100vh) translateX(50px);
    opacity: 0;
  }
}

/* 顶部导航 */
.top-nav {
  position: sticky;
  top: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.5);
  z-index: 100;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-icon {
  width: 45px;
  height: 45px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.4);
}

.brand-text {
  display: flex;
  flex-direction: column;
}

.brand-title {
  font-size: 1.2rem;
  font-weight: 700;
  color: #333;
  line-height: 1.2;
}

.brand-subtitle {
  font-size: 0.8rem;
  color: #999;
}

.btn-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  border: none;
  background: rgba(255, 107, 107, 0.1);
  color: #ff6b6b;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-icon:hover {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  color: white;
  transform: translateY(-2px);
}

/* 筛选区域 */
.filter-section {
  position: relative;
  z-index: 10;
  padding: 20px;
}

.filter-container {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 4px;
}

.filter-btn {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border: none;
  border-radius: 25px;
  background: white;
  color: #666;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.filter-btn i {
  font-size: 1rem;
}

.filter-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.filter-btn.active {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.4);
}

.btn-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, transparent 60%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.filter-btn:hover .btn-glow {
  opacity: 1;
}

/* 内容区域 */
.content-area {
  position: relative;
  z-index: 10;
  padding: 0 20px 20px;
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
  margin-bottom: 20px;
}

.spinner {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: 3px solid rgba(255, 107, 107, 0.2);
  border-top-color: #ff6b6b;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.spinner-ring {
  position: absolute;
  top: -8px;
  left: -8px;
  right: -8px;
  bottom: -8px;
  border: 2px solid transparent;
  border-top-color: rgba(255, 107, 107, 0.3);
  border-radius: 50%;
  animation: spin 2s linear infinite reverse;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 1rem;
  color: #666;
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
  align-items: stretch;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  animation: slideIn 0.5s ease forwards;
  opacity: 0;
  transform: translateY(20px);
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

@keyframes slideIn {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.hot-card:hover {
  transform: translateY(-4px) scale(1.01);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.card-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.1) 0%, transparent 50%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.hot-card:hover .card-glow {
  opacity: 1;
}

.hot-card.top-three {
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.1) 0%, rgba(255, 255, 255, 0.95) 100%);
  border: 1px solid rgba(255, 215, 0, 0.3);
}

/* 排名区域 */
.rank-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding-right: 16px;
  border-right: 1px solid rgba(0, 0, 0, 0.06);
}

.rank-badge {
  position: relative;
  width: 50px;
  height: 50px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.rank-1 {
  background: linear-gradient(135deg, #ffd700 0%, #ffb700 100%);
  box-shadow: 0 4px 15px rgba(255, 215, 0, 0.5);
}

.rank-2 {
  background: linear-gradient(135deg, #c0c0c0 0%, #a0a0a0 100%);
  box-shadow: 0 4px 15px rgba(192, 192, 192, 0.5);
}

.rank-3 {
  background: linear-gradient(135deg, #cd7f32 0%, #b87333 100%);
  box-shadow: 0 4px 15px rgba(205, 127, 50, 0.5);
}

.rank-badge:not(.rank-1):not(.rank-2):not(.rank-3) {
  background: linear-gradient(135deg, #f0f2f5 0%, #e4e7ec 100%);
}

.rank-inner {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  color: white;
}

.rank-badge:not(.rank-1):not(.rank-2):not(.rank-3) .rank-inner {
  color: #666;
}

.rank-number {
  font-size: 1.2rem;
  font-weight: 700;
  line-height: 1;
}

.rank-inner i {
  font-size: 0.7rem;
}

.rank-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.5) 0%, transparent 70%);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: all 0.4s ease;
}

.hot-card:hover .rank-glow {
  width: 80px;
  height: 80px;
}

.heat-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #ff6b6b;
  font-weight: 600;
  font-size: 0.85rem;
}

.heat-indicator i {
  animation: pulse 1.5s ease infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

/* 内容区域 */
.content-section {
  flex: 1;
  padding: 0 16px;
  min-width: 0;
}

.post-title {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-summary {
  font-size: 0.85rem;
  color: #666;
  margin-bottom: 12px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-avatar {
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.7rem;
}

.author-name {
  font-size: 0.85rem;
  color: #666;
}

.category-tag {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.75rem;
  color: white;
  font-weight: 500;
}

/* 统计数据 */
.post-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stat-icon {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
}

.stat-icon.views {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.stat-icon.likes {
  background: rgba(255, 107, 107, 0.1);
  color: #ff6b6b;
}

.stat-icon.comments {
  background: rgba(72, 219, 251, 0.1);
  color: #48dbfb;
}

.stat-value {
  font-size: 0.85rem;
  color: #666;
  font-weight: 500;
}

/* 箭头区域 */
.arrow-section {
  display: flex;
  align-items: center;
  padding-left: 12px;
  color: #ccc;
  transition: all 0.3s ease;
}

.hot-card:hover .arrow-section {
  color: #ff6b6b;
  transform: translateX(4px);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
}

.empty-icon {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.1) 0%, rgba(238, 90, 111, 0.1) 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
}

.empty-icon i {
  font-size: 3rem;
  color: #ff6b6b;
}

.empty-state h4 {
  font-size: 1.2rem;
  color: #333;
  margin-bottom: 8px;
}

.empty-state p {
  font-size: 0.9rem;
  color: #999;
}

/* 深色模式 */
.dark-mode {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #1a1a2e 100%);
}

.dark-mode .top-nav {
  background: rgba(30, 30, 46, 0.9);
  border-color: rgba(255, 255, 255, 0.1);
}

.dark-mode .brand-title {
  color: #fff;
}

.dark-mode .brand-subtitle {
  color: #666;
}

.dark-mode .btn-icon {
  background: rgba(255, 107, 107, 0.2);
}

.dark-mode .filter-btn {
  background: rgba(30, 30, 46, 0.9);
  color: #aaa;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.dark-mode .hot-card {
  background: rgba(30, 30, 46, 0.95);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.dark-mode .hot-card.top-three {
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.1) 0%, rgba(30, 30, 46, 0.95) 100%);
}

.dark-mode .post-title {
  color: #fff;
}

.dark-mode .post-summary {
  color: #aaa;
}

.dark-mode .author-name {
  color: #999;
}

.dark-mode .stat-value {
  color: #aaa;
}

.dark-mode .rank-badge:not(.rank-1):not(.rank-2):not(.rank-3) {
  background: linear-gradient(135deg, #2d2d44 0%, #252538 100%);
}

.dark-mode .rank-badge:not(.rank-1):not(.rank-2):not(.rank-3) .rank-inner {
  color: #999;
}

.dark-mode .empty-icon {
  background: rgba(255, 107, 107, 0.2);
}

.dark-mode .empty-state h4 {
  color: #fff;
}

/* 响应式 */
@media (max-width: 768px) {
  .top-nav {
    padding: 12px 16px;
  }

  .brand-subtitle {
    display: none;
  }

  .filter-section {
    padding: 16px;
  }

  .content-area {
    padding: 0 16px 16px;
  }

  .hot-card {
    padding: 12px;
  }

  .rank-section {
    padding-right: 12px;
  }

  .rank-badge {
    width: 40px;
    height: 40px;
  }

  .content-section {
    padding: 0 12px;
  }

  .post-summary {
    display: none;
  }

  .post-stats {
    gap: 12px;
  }
}
</style>
