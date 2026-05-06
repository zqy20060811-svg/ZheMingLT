<template>
  <div class="manor-category" :class="{ 'night-mode': isDarkMode }">
    <!-- 庄园背景 -->
    <ManorBackground :isDarkMode="isDarkMode" />

    <!-- 顶部导航 -->
    <ManorNav title="分类花园" :isDarkMode="isDarkMode" @toggle-theme="toggleTheme">
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
          <i class="bi bi-grid-fill"></i>
        </div>
        <div class="header-text">
          <h1>分类花园</h1>
          <p>在花园中探索不同的话题分类</p>
        </div>
      </div>

      <!-- 搜索栏 -->
      <div class="search-section">
        <div class="search-box">
          <i class="bi bi-search"></i>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索分类..."
            class="search-input"
            @input="searchCategories"
          />
          <button v-if="searchQuery" class="clear-btn" @click="clearSearch">
            <i class="bi bi-x-circle-fill"></i>
          </button>
        </div>
      </div>

      <!-- 分类统计 -->
      <div class="stats-section">
        <div class="stats-card">
          <div class="stat-icon">
            <i class="bi bi-folder-fill"></i>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ filteredCategories.length }}</span>
            <span class="stat-label">分类数量</span>
          </div>
        </div>
      </div>

      <!-- 分类列表 -->
      <div class="categories-section">
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
        <div v-else-if="filteredCategories.length === 0" class="empty-state">
          <div class="empty-illustration">
            <i class="bi bi-folder"></i>
            <div class="empty-decoration">
              <span class="leaf leaf-1">🍃</span>
              <span class="leaf leaf-2">🌿</span>
            </div>
          </div>
          <h3>未找到相关分类</h3>
          <p>试试其他关键词</p>
        </div>

        <!-- 分类网格 -->
        <div v-else class="categories-grid">
          <div
            v-for="(cat, index) in filteredCategories"
            :key="cat.id"
            class="category-card"
            :style="{ animationDelay: `${index * 0.05}s` }"
            @click="goToCategoryDetail(cat.id)"
          >
            <div class="card-glow"></div>
            <div class="card-inner">
              <div class="card-icon" :style="{ background: getGradient(cat.id) }">
                <i :class="cat.icon || 'bi bi-folder'"></i>
              </div>
              <div class="card-content">
                <h3 class="category-name">{{ cat.name }}</h3>
                <p class="category-desc">{{ cat.description || '探索更多精彩内容' }}</p>
                <div class="category-stats">
                  <span class="stat-item">
                    <i class="bi bi-file-text"></i>
                    {{ cat.postCount || 0 }} 帖子
                  </span>
                </div>
              </div>
              <div class="card-arrow">
                <i class="bi bi-chevron-right"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 底部导航 -->
    <ManorBottomNav :isDarkMode="isDarkMode" active="category" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '@/utils/request'
import ManorBackground from '@/components/ManorBackground.vue'
import ManorNav from '@/components/ManorNav.vue'
import ManorBottomNav from '@/components/ManorBottomNav.vue'

const router = useRouter()
const isDarkMode = ref(localStorage.getItem('darkMode') === 'true')
const loading = ref(false)
const searchQuery = ref('')
const categories = ref([])

const gradients = [
  'linear-gradient(135deg, #8FBC8F, #7CB87C)',
  'linear-gradient(135deg, #DEB887, #D2B48C)',
  'linear-gradient(135deg, #BC8F8F, #CD853F)',
  'linear-gradient(135deg, #9ACD32, #8FBC8F)',
  'linear-gradient(135deg, #DDA0DD, #D8BFD8)',
  'linear-gradient(135deg, #F0E68C, #BDB76B)',
  'linear-gradient(135deg, #87CEEB, #5F9EA0)',
  'linear-gradient(135deg, #FFA07A, #FA8072)'
]

const defaultCategories = [
  { id: 1, name: '技术交流', icon: 'bi bi-code-slash', description: '分享技术心得，探讨前沿技术' },
  { id: 2, name: '生活分享', icon: 'bi bi-cup-hot', description: '记录生活点滴，分享美好时光' },
  { id: 3, name: '娱乐八卦', icon: 'bi bi-film', description: '影视音乐游戏，娱乐资讯分享' },
  { id: 4, name: '学习资料', icon: 'bi bi-book', description: '学习资源分享，知识交流讨论' },
  { id: 5, name: '职场经验', icon: 'bi bi-briefcase', description: '职场技能提升，工作经验分享' },
  { id: 6, name: '兴趣爱好', icon: 'bi bi-palette', description: '兴趣交流，爱好分享' },
  { id: 7, name: '问答求助', icon: 'bi bi-question-circle', description: '有问题就问，大家来帮忙' },
  { id: 8, name: '资源分享', icon: 'bi bi-folder', description: '优质资源分享，互助互利' }
]

const filteredCategories = computed(() => {
  if (!searchQuery.value) return categories.value
  const query = searchQuery.value.toLowerCase()
  return categories.value.filter(cat =>
    cat.name.toLowerCase().includes(query) ||
    (cat.description && cat.description.toLowerCase().includes(query))
  )
})

function getGradient(id) {
  return gradients[(id - 1) % gradients.length]
}

function toggleTheme() {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
}

function goBack() {
  router.back()
}

function clearSearch() {
  searchQuery.value = ''
}

function searchCategories() {
  // 搜索通过 computed 自动处理
}

async function loadCategories() {
  loading.value = true
  try {
    const res = await get('/categories')
    if (res.code === 200 && res.data && res.data.length > 0) {
      categories.value = res.data.map((cat, index) => ({
        ...cat,
        icon: defaultCategories[index % defaultCategories.length]?.icon || 'bi bi-folder'
      }))
    } else {
      categories.value = defaultCategories
    }
  } catch (error) {
    console.error('加载分类失败', error)
    categories.value = defaultCategories
  } finally {
    loading.value = false
  }
}

function goToCategoryDetail(id) {
  router.push(`/category/${id}`)
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.manor-category {
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
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  box-shadow: 0 4px 15px rgba(124, 184, 124, 0.3);
}

.night-mode .header-icon {
  background: linear-gradient(135deg, #4a7c5a, #3a6c4a);
  box-shadow: 0 4px 15px rgba(60, 120, 80, 0.4);
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

/* 搜索栏 */
.search-section {
  margin-bottom: 20px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  border: 1px solid rgba(139, 188, 143, 0.2);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.night-mode .search-box {
  background: rgba(40, 45, 55, 0.9);
  border-color: rgba(100, 140, 120, 0.2);
}

.search-box i {
  color: #8FBC8F;
  font-size: 18px;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 15px;
  color: #4a6a4a;
  outline: none;
}

.night-mode .search-input {
  color: #a8d8a8;
}

.search-input::placeholder {
  color: #9a9a9a;
}

.clear-btn {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  padding: 4px;
}

.clear-btn:hover {
  color: #666;
}

/* 统计卡片 */
.stats-section {
  margin-bottom: 24px;
}

.stats-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  border: 1px solid rgba(139, 188, 143, 0.2);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.night-mode .stats-card {
  background: rgba(40, 45, 55, 0.9);
  border-color: rgba(100, 140, 120, 0.2);
}

.stat-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.night-mode .stat-icon {
  background: linear-gradient(135deg, #4a7c5a, #3a6c4a);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #4a6a4a;
}

.night-mode .stat-value {
  color: #8ab88a;
}

.stat-label {
  font-size: 14px;
  color: #7a8a7a;
}

.night-mode .stat-label {
  color: #9ab89a;
}

/* 分类列表 */
.categories-section {
  background: rgba(255, 255, 255, 0.85);
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(139, 188, 143, 0.15);
}

.night-mode .categories-section {
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
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
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
  color: #c8d8c8;
}

.night-mode .empty-illustration > i {
  color: #4a5a5a;
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

/* 分类网格 */
.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.category-card {
  position: relative;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 16px;
  padding: 4px;
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

.night-mode .category-card {
  background: rgba(50, 55, 65, 0.7);
  border-color: rgba(100, 140, 120, 0.1);
}

.category-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border-color: rgba(143, 188, 143, 0.3);
}

.card-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(143, 188, 143, 0.1), transparent);
  border-radius: 16px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.category-card:hover .card-glow {
  opacity: 1;
}

.card-inner {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 12px;
}

.night-mode .card-inner {
  background: rgba(40, 45, 55, 0.5);
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: white;
  flex-shrink: 0;
}

.card-content {
  flex: 1;
  min-width: 0;
}

.category-name {
  font-size: 16px;
  font-weight: 600;
  color: #3a5a3a;
  margin-bottom: 4px;
  font-family: 'Georgia', serif;
}

.night-mode .category-name {
  color: #a8d8a8;
}

.category-desc {
  font-size: 12px;
  color: #7a8a7a;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.night-mode .category-desc {
  color: #9ab89a;
}

.category-stats {
  display: flex;
  gap: 12px;
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
}

.category-card:hover .card-arrow {
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
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

  .categories-grid {
    grid-template-columns: 1fr;
  }
}
</style>
