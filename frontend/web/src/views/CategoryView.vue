<template>
  <div class="category-page" :class="{ 'dark-mode': isDarkMode }">
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
          <i class="bi bi-grid-3x3-gap-fill"></i>
        </div>
        <div class="brand-text">
          <span class="brand-title">分类浏览</span>
          <span class="brand-subtitle">探索不同话题</span>
        </div>
      </div>
      <button class="btn-icon theme-btn" @click="toggleTheme">
        <i :class="isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-fill'"></i>
      </button>
    </nav>

    <!-- 搜索栏 -->
    <div class="search-section">
      <div class="search-box">
        <i class="bi bi-search"></i>
        <input
          type="text"
          v-model="searchQuery"
          placeholder="搜索感兴趣的分类..."
          class="search-input"
        />
        <button v-if="searchQuery" class="clear-btn" @click="searchQuery = ''">
          <i class="bi bi-x-circle-fill"></i>
        </button>
      </div>
    </div>

    <!-- 热门分类网格 -->
    <div class="section-container">
      <div class="section-header">
        <div class="header-title">
          <i class="bi bi-fire"></i>
          <span>热门分类</span>
        </div>
        <span class="header-count">{{ categories.length }} 个分类</span>
      </div>
      <div class="category-grid">
        <div
          v-for="(cat, index) in filteredCategories.slice(0, 6)"
          :key="cat.id"
          class="category-card"
          :style="{ animationDelay: `${index * 0.1}s` }"
          @click="goToCategoryDetail(cat.id)"
        >
          <div class="card-glow"></div>
          <div class="card-icon" :style="{ background: cat.gradient }">
            <i :class="cat.icon"></i>
          </div>
          <div class="card-content">
            <h4 class="card-title">{{ cat.name }}</h4>
            <p class="card-count">{{ cat.postCount || 0 }} 帖子</p>
          </div>
          <div class="card-arrow">
            <i class="bi bi-arrow-right"></i>
          </div>
        </div>
      </div>
    </div>

    <!-- 推荐分类 -->
    <div class="section-container">
      <div class="section-header">
        <div class="header-title">
          <i class="bi bi-stars"></i>
          <span>推荐关注</span>
        </div>
      </div>
      <div class="recommend-scroll">
        <div
          v-for="cat in filteredCategories.slice(0, 8)"
          :key="`rec-${cat.id}`"
          class="recommend-card"
          @click="goToCategoryDetail(cat.id)"
        >
          <div class="rec-icon" :style="{ background: cat.gradient }">
            <i :class="cat.icon"></i>
          </div>
          <span class="rec-name">{{ cat.name }}</span>
          <span class="rec-count">{{ cat.postCount || 0 }} 帖</span>
        </div>
      </div>
    </div>

    <!-- 全部分类列表 -->
    <div class="section-container list-section">
      <div class="section-header">
        <div class="header-title">
          <i class="bi bi-list-ul"></i>
          <span>全部分类</span>
        </div>
      </div>
      <div class="category-list">
        <div
          v-for="(cat, index) in filteredCategories"
          :key="`list-${cat.id}`"
          class="list-item"
          :style="{ animationDelay: `${index * 0.05}s` }"
          @click="goToCategoryDetail(cat.id)"
        >
          <div class="item-glow"></div>
          <div class="item-left">
            <div class="item-icon" :style="{ background: cat.gradient }">
              <i :class="cat.icon"></i>
            </div>
            <div class="item-info">
              <span class="item-title">{{ cat.name }}</span>
              <span class="item-desc">{{ cat.description || '探索更多精彩内容' }}</span>
            </div>
          </div>
          <div class="item-right">
            <div class="item-stats">
              <span class="stat">
                <i class="bi bi-file-text"></i>
                {{ cat.postCount || 0 }}
              </span>
            </div>
            <div class="item-arrow">
              <i class="bi bi-chevron-right"></i>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="filteredCategories.length === 0" class="empty-state">
      <div class="empty-icon">
        <i class="bi bi-search"></i>
      </div>
      <h4>未找到相关分类</h4>
      <p>试试其他关键词</p>
    </div>

    <BottomNav active="category" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '@/utils/request'
import BottomNav from '@/components/BottomNav.vue'

const router = useRouter()

const isDarkMode = ref(false)
const searchQuery = ref('')
const categories = ref([])
const particlesRef = ref(null)

const defaultCategories = [
  { id: 1, name: '技术交流', icon: 'bi bi-code-slash', color: '#4a6fa5', gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', description: '分享技术心得，探讨前沿技术' },
  { id: 2, name: '生活分享', icon: 'bi bi-cup-hot', color: '#e07a5f', gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', description: '记录生活点滴，分享美好时光' },
  { id: 3, name: '娱乐八卦', icon: 'bi bi-film', color: '#81b29a', gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', description: '影视音乐游戏，娱乐资讯分享' },
  { id: 4, name: '学习资料', icon: 'bi bi-book', color: '#f2cc8f', gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)', description: '学习资源分享，知识交流讨论' },
  { id: 5, name: '职场经验', icon: 'bi bi-briefcase', color: '#9b5de5', gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)', description: '职场技能提升，工作经验分享' },
  { id: 6, name: '兴趣爱好', icon: 'bi bi-palette', color: '#00bbf9', gradient: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)', description: '兴趣交流，爱好分享' },
  { id: 7, name: '问答求助', icon: 'bi bi-question-circle', color: '#ff6b6b', gradient: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)', description: '有问题就问，大家来帮忙' },
  { id: 8, name: '资源分享', icon: 'bi bi-folder', color: '#4ecdc4', gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', description: '优质资源分享，互助互利' }
]

// 过滤后的分类
const filteredCategories = computed(() => {
  if (!searchQuery.value) return categories.value
  const query = searchQuery.value.toLowerCase()
  return categories.value.filter(cat =>
    cat.name.toLowerCase().includes(query) ||
    (cat.description && cat.description.toLowerCase().includes(query))
  )
})

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
      categories.value = loaded
    } else {
      categories.value = defaultCategories
    }
  } catch (error) {
    categories.value = defaultCategories
  }
}

function goToCategoryDetail(id) {
  router.push(`/category/${id}`)
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
  loadCategories()
})
</script>

<style scoped>
.category-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 50%, #f8f9fa 100%);
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  top: -150px;
  left: -150px;
  animation-delay: 0s;
}

.orb-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  bottom: -100px;
  right: -100px;
  animation-delay: -5s;
}

.orb-3 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  top: 50%;
  right: 20%;
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
  background: rgba(102, 126, 234, 0.3);
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
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
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-icon:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-2px);
}

/* 搜索栏 */
.search-section {
  position: relative;
  z-index: 10;
  padding: 20px;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
  background: white;
  border-radius: 16px;
  padding: 4px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.search-box:focus-within {
  box-shadow: 0 4px 25px rgba(102, 126, 234, 0.2);
}

.search-box > i {
  padding: 0 16px;
  color: #999;
  font-size: 1.1rem;
}

.search-input {
  flex: 1;
  padding: 14px 0;
  border: none;
  background: transparent;
  font-size: 1rem;
  color: #333;
}

.search-input:focus {
  outline: none;
}

.search-input::placeholder {
  color: #999;
}

.clear-btn {
  padding: 8px 16px;
  border: none;
  background: transparent;
  color: #ccc;
  cursor: pointer;
  transition: color 0.3s ease;
}

.clear-btn:hover {
  color: #999;
}

/* 区块容器 */
.section-container {
  position: relative;
  z-index: 10;
  padding: 0 20px 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.1rem;
  font-weight: 700;
  color: #333;
}

.header-title i {
  color: #667eea;
}

.header-count {
  font-size: 0.85rem;
  color: #999;
}

/* 分类网格 */
.category-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.category-card {
  position: relative;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
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

.category-card:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.card-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, transparent 50%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.category-card:hover .card-glow {
  opacity: 1;
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.6rem;
  flex-shrink: 0;
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.card-count {
  font-size: 0.85rem;
  color: #999;
}

.card-arrow {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(102, 126, 234, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #667eea;
  transition: all 0.3s ease;
}

.category-card:hover .card-arrow {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateX(4px);
}

/* 推荐滚动 */
.recommend-scroll {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding: 4px 4px 12px;
  scrollbar-width: none;
}

.recommend-scroll::-webkit-scrollbar {
  display: none;
}

.recommend-card {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  min-width: 100px;
}

.recommend-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.rec-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
}

.rec-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
}

.rec-count {
  font-size: 0.8rem;
  color: #999;
}

/* 分类列表 */
.list-section {
  padding-bottom: 20px;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list-item {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  animation: slideIn 0.5s ease forwards;
  opacity: 0;
  transform: translateX(-20px);
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.list-item:hover {
  transform: translateX(4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.item-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.list-item:hover .item-glow {
  opacity: 1;
}

.item-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.item-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.3rem;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-title {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
}

.item-desc {
  font-size: 0.85rem;
  color: #999;
}

.item-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.item-stats {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 20px;
  color: #667eea;
  font-size: 0.85rem;
}

.item-arrow {
  color: #ccc;
  transition: all 0.3s ease;
}

.list-item:hover .item-arrow {
  color: #667eea;
  transform: translateX(4px);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
}

.empty-icon i {
  font-size: 3rem;
  color: #667eea;
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
  background: rgba(102, 126, 234, 0.2);
}

.dark-mode .search-box {
  background: rgba(30, 30, 46, 0.9);
}

.dark-mode .search-input {
  color: #e0e0e0;
}

.dark-mode .search-input::placeholder {
  color: #666;
}

.dark-mode .header-title {
  color: #fff;
}

.dark-mode .header-count {
  color: #666;
}

.dark-mode .category-card,
.dark-mode .recommend-card,
.dark-mode .list-item {
  background: rgba(30, 30, 46, 0.95);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.dark-mode .card-title,
.dark-mode .rec-name,
.dark-mode .item-title {
  color: #fff;
}

.dark-mode .card-count,
.dark-mode .rec-count,
.dark-mode .item-desc {
  color: #666;
}

.dark-mode .card-arrow {
  background: rgba(167, 139, 250, 0.2);
  color: #a78bfa;
}

.dark-mode .category-card:hover .card-arrow {
  background: linear-gradient(135deg, #a78bfa 0%, #c084fc 100%);
}

.dark-mode .item-stats {
  background: rgba(167, 139, 250, 0.2);
  color: #a78bfa;
}

.dark-mode .item-arrow {
  color: #666;
}

.dark-mode .list-item:hover .item-arrow {
  color: #a78bfa;
}

.dark-mode .empty-icon {
  background: rgba(167, 139, 250, 0.2);
}

.dark-mode .empty-icon i {
  color: #a78bfa;
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

  .search-section,
  .section-container {
    padding-left: 16px;
    padding-right: 16px;
  }

  .category-grid {
    gap: 12px;
  }

  .category-card {
    padding: 16px;
  }

  .card-icon {
    width: 48px;
    height: 48px;
    font-size: 1.3rem;
  }

  .card-title {
    font-size: 0.95rem;
  }

  .list-item {
    padding: 14px 16px;
  }

  .item-icon {
    width: 40px;
    height: 40px;
    font-size: 1.1rem;
  }

  .item-desc {
    display: none;
  }

  .item-stats {
    display: none;
  }
}
</style>
