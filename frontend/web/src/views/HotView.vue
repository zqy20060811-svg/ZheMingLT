<template>
  <div class="hot-page" :class="{ 'dark-mode': isDarkMode }">
    <!-- 顶部导航 -->
    <nav class="top-nav">
      <div class="nav-brand">
        <i class="bi bi-fire"></i>
        <span>热门排行</span>
      </div>
      <button class="btn-icon" @click="toggleTheme">
        <i :class="isDarkMode ? 'bi bi-sun' : 'bi bi-moon'"></i>
      </button>
    </nav>

    <!-- 时间筛选 -->
    <div class="time-filter">
      <button
        v-for="filter in timeFilters"
        :key="filter.value"
        :class="['filter-btn', { active: currentFilter === filter.value }]"
        @click="switchFilter(filter.value)"
      >
        {{ filter.label }}
      </button>
    </div>

    <!-- 热门列表 -->
    <div class="hot-list">
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <span>加载中...</span>
      </div>

      <div v-else-if="hotPosts.length > 0" class="posts-list">
        <div
          v-for="(item, index) in hotPosts"
          :key="item.id"
          class="hot-item"
          @click="goToPost(item.id)"
        >
          <div class="rank-badge" :class="{ top: index < 3 }">
            {{ index + 1 }}
          </div>
          <div class="item-content">
            <h4 class="item-title">{{ item.title }}</h4>
            <div class="item-meta">
              <span class="author">{{ item.authorName }}</span>
              <span class="divider">·</span>
              <span class="category">{{ item.categoryName }}</span>
            </div>
            <div class="item-stats">
              <span class="heat">
                <i class="bi bi-fire"></i>
                {{ calculateHeat(item) }}°
              </span>
              <span><i class="bi bi-eye"></i> {{ item.viewCount || 0 }}</span>
              <span><i class="bi bi-heart"></i> {{ item.likeCount || 0 }}</span>
              <span><i class="bi bi-chat"></i> {{ item.commentCount || 0 }}</span>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <i class="bi bi-inbox"></i>
        <p>暂无热门帖子</p>
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

function goToPost(id) {
  router.push(`/post/${id}`)
}

onMounted(() => {
  isDarkMode.value = localStorage.getItem('darkMode') === 'true'
  loadHotPosts()
})
</script>

<style scoped>
.hot-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fff5f5 0%, #fff 100%);
  padding-bottom: 70px;
}

.dark-mode {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
}

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
  color: #e74c3c;
}

.nav-brand i {
  font-size: 1.5rem;
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
}

.dark-mode .btn-icon {
  background: #2d2d44;
  color: #fff;
}

/* 时间筛选 */
.time-filter {
  display: flex;
  gap: 12px;
  padding: 16px;
  overflow-x: auto;
}

.filter-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 20px;
  background: #f0f2f5;
  color: #666;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}

.filter-btn.active {
  background: linear-gradient(135deg, #e74c3c, #c0392b);
  color: white;
}

.dark-mode .filter-btn {
  background: #2d2d44;
  color: #aaa;
}

/* 热门列表 */
.hot-list {
  padding: 0 16px;
}

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
  border-top-color: #e74c3c;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.hot-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: white;
  border-radius: 12px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  cursor: pointer;
  transition: transform 0.2s;
}

.dark-mode .hot-item {
  background: #1e1e2e;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
}

.hot-item:active {
  transform: scale(0.98);
}

.rank-badge {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #e9ecef;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 0.9rem;
  flex-shrink: 0;
}

.rank-badge.top {
  background: linear-gradient(135deg, #e74c3c, #c0392b);
  color: white;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.dark-mode .item-title {
  color: #fff;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.8rem;
  color: #999;
  margin-bottom: 8px;
}

.item-stats {
  display: flex;
  gap: 12px;
  font-size: 0.8rem;
  color: #999;
}

.item-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.item-stats .heat {
  color: #e74c3c;
  font-weight: 600;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-state i {
  font-size: 3rem;
  margin-bottom: 12px;
}
</style>
