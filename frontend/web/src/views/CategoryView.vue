<template>
  <div class="category-page" :class="{ 'dark-mode': isDarkMode }">
    <!-- 顶部导航 -->
    <nav class="top-nav">
      <div class="nav-brand">
        <i class="bi bi-grid"></i>
        <span>分类浏览</span>
      </div>
      <button class="btn-icon" @click="toggleTheme">
        <i :class="isDarkMode ? 'bi bi-sun' : 'bi bi-moon'"></i>
      </button>
    </nav>

    <!-- 分类网格 -->
    <div class="category-grid">
      <div
        v-for="cat in categories"
        :key="cat.id"
        class="category-card"
        :style="{ background: cat.gradient }"
        @click="goToCategoryDetail(cat.id)"
      >
        <div class="card-icon">
          <i :class="cat.icon"></i>
        </div>
        <div class="card-info">
          <h4>{{ cat.name }}</h4>
          <p>{{ cat.postCount || 0 }} 帖子</p>
        </div>
      </div>
    </div>

    <!-- 推荐分类 -->
    <div class="recommend-section">
      <h3 class="section-title">
        <i class="bi bi-stars"></i>
        推荐分类
      </h3>
      <div class="recommend-list">
        <div
          v-for="cat in recommendCategories"
          :key="cat.id"
          class="recommend-item"
          @click="goToCategoryDetail(cat.id)"
        >
          <div class="item-icon" :style="{ backgroundColor: cat.color }">
            <i :class="cat.icon"></i>
          </div>
          <span class="item-name">{{ cat.name }}</span>
        </div>
      </div>
    </div>

    <!-- 全部分类列表 -->
    <div class="all-categories">
      <h3 class="section-title">
        <i class="bi bi-list"></i>
        全部分类
      </h3>
      <div class="category-list">
        <div
          v-for="cat in allCategories"
          :key="cat.id"
          class="list-item"
          @click="goToCategoryDetail(cat.id)"
        >
          <div class="item-left">
            <div class="item-icon-small" :style="{ backgroundColor: cat.color }">
              <i :class="cat.icon"></i>
            </div>
            <div class="item-info">
              <span class="item-title">{{ cat.name }}</span>
              <span class="item-desc">{{ cat.description || '暂无描述' }}</span>
            </div>
          </div>
          <div class="item-right">
            <span class="post-count">{{ cat.postCount || 0 }} 帖</span>
            <i class="bi bi-chevron-right"></i>
          </div>
        </div>
      </div>
    </div>

    <BottomNav active="category" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '@/utils/request'
import BottomNav from '@/components/BottomNav.vue'

const router = useRouter()

const isDarkMode = ref(false)
const categories = ref([])
const recommendCategories = ref([])
const allCategories = ref([])

const defaultCategories = [
  { id: 1, name: '技术交流', icon: 'bi bi-code-slash', color: '#4a6fa5', gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { id: 2, name: '生活分享', icon: 'bi bi-cup-hot', color: '#e07a5f', gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { id: 3, name: '娱乐八卦', icon: 'bi bi-film', color: '#81b29a', gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
  { id: 4, name: '学习资料', icon: 'bi bi-book', color: '#f2cc8f', gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' },
  { id: 5, name: '职场经验', icon: 'bi bi-briefcase', color: '#9b5de5', gradient: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)' },
  { id: 6, name: '兴趣爱好', icon: 'bi bi-palette', color: '#00bbf9', gradient: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)' }
]

function toggleTheme() {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
}

async function loadCategories() {
  try {
    const res = await get('/categories')
    if (res.code === 200 && res.data && res.data.length > 0) {
      const loaded = res.data.map((cat, index) => ({
        ...cat,
        ...defaultCategories[index % defaultCategories.length]
      }))
      categories.value = loaded.slice(0, 6)
      recommendCategories.value = loaded.slice(0, 4)
      allCategories.value = loaded
    } else {
      categories.value = defaultCategories.slice(0, 6)
      recommendCategories.value = defaultCategories.slice(0, 4)
      allCategories.value = defaultCategories
    }
  } catch (error) {
    categories.value = defaultCategories.slice(0, 6)
    recommendCategories.value = defaultCategories.slice(0, 4)
    allCategories.value = defaultCategories
  }
}

function goToCategoryDetail(id) {
  router.push(`/category/${id}`)
}

onMounted(() => {
  isDarkMode.value = localStorage.getItem('darkMode') === 'true'
  loadCategories()
})
</script>

<style scoped>
.category-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8f9fa 0%, #e9ecef 100%);
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
  color: #4a6fa5;
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

/* 分类网格 */
.category-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 16px;
}

.category-card {
  border-radius: 16px;
  padding: 20px;
  color: white;
  cursor: pointer;
  transition: transform 0.2s;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.category-card:active {
  transform: scale(0.95);
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
}

.card-info h4 {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 4px;
}

.card-info p {
  font-size: 0.8rem;
  opacity: 0.9;
}

/* 推荐分类 */
.recommend-section {
  padding: 0 16px 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.dark-mode .section-title {
  color: #fff;
}

.section-title i {
  color: #4a6fa5;
}

.recommend-list {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.recommend-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.item-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
  transition: transform 0.2s;
}

.recommend-item:active .item-icon {
  transform: scale(0.9);
}

.item-name {
  font-size: 0.8rem;
  color: #666;
  white-space: nowrap;
}

.dark-mode .item-name {
  color: #aaa;
}

/* 全部分类 */
.all-categories {
  padding: 0 16px;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: white;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.dark-mode .list-item {
  background: #1e1e2e;
}

.list-item:active {
  transform: scale(0.98);
}

.item-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-icon-small {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
}

.item-info {
  display: flex;
  flex-direction: column;
}

.item-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: #333;
}

.dark-mode .item-title {
  color: #fff;
}

.item-desc {
  font-size: 0.75rem;
  color: #999;
  margin-top: 2px;
}

.item-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.post-count {
  font-size: 0.8rem;
  color: #999;
}

.item-right i {
  color: #ccc;
  font-size: 0.9rem;
}
</style>
