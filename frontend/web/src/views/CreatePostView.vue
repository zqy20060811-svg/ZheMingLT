<template>
  <div class="create-post-page" :class="{ 'dark-mode': isDarkMode }">
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
      <div class="nav-brand" @click="router.push('/')">
        <div class="logo-wrapper">
          <i class="bi bi-lightbulb-fill"></i>
        </div>
        <span class="brand-text">择明论坛</span>
      </div>
      <div class="nav-actions">
        <button class="btn-icon theme-btn" @click="toggleTheme">
          <i :class="isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-fill'"></i>
        </button>
        <div class="user-menu">
          <div class="user-avatar">
            <i class="bi bi-person-fill"></i>
          </div>
          <span class="username">{{ username }}</span>
          <button class="btn-icon logout-btn" @click="logout" title="退出登录">
            <i class="bi bi-box-arrow-right"></i>
          </button>
        </div>
      </div>
    </nav>

    <!-- 主内容 -->
    <div class="main-content">
      <div class="create-container">
        <!-- 页面标题区 -->
        <div class="page-header">
          <div class="header-icon">
            <i class="bi bi-pencil-square"></i>
          </div>
          <div class="header-content">
            <h1 class="page-title">{{ isEdit ? '编辑帖子' : '发布新帖' }}</h1>
            <p class="page-subtitle">分享你的见解，与社区共同成长</p>
          </div>
        </div>

        <form @submit.prevent="handleSubmit" class="post-form">
          <!-- 标题输入 -->
          <div class="form-section">
            <div class="input-group">
              <label class="input-label">
                <i class="bi bi-type-h1"></i>
                帖子标题
                <span class="required">*</span>
              </label>
              <div class="input-wrapper title-wrapper">
                <input
                  type="text"
                  v-model="form.title"
                  placeholder="请输入一个吸引人的标题..."
                  required
                  maxlength="100"
                  class="form-input title-input"
                />
                <div class="input-focus-border"></div>
                <span class="char-count">{{ form.title.length }}/100</span>
              </div>
            </div>
          </div>

          <!-- 分类选择 -->
          <div class="form-section">
            <div class="input-group">
              <label class="input-label">
                <i class="bi bi-grid"></i>
                选择分类
                <span class="required">*</span>
              </label>
              <div class="category-grid">
                <div
                  v-for="category in categories"
                  :key="category.id"
                  :class="['category-card', { active: form.categoryId === category.id }]"
                  @click="form.categoryId = category.id"
                >
                  <div class="category-icon">
                    <i :class="getCategoryIcon(category.name)"></i>
                  </div>
                  <span class="category-name">{{ category.name }}</span>
                  <div class="category-glow"></div>
                </div>
              </div>
            </div>
          </div>

          <!-- 内容编辑 -->
          <div class="form-section">
            <div class="input-group">
              <label class="input-label">
                <i class="bi bi-file-text"></i>
                帖子内容
                <span class="required">*</span>
              </label>
              <div class="editor-wrapper">
                <div class="editor-toolbar">
                  <button type="button" class="toolbar-btn" title="加粗">
                    <i class="bi bi-type-bold"></i>
                  </button>
                  <button type="button" class="toolbar-btn" title="斜体">
                    <i class="bi bi-type-italic"></i>
                  </button>
                  <button type="button" class="toolbar-btn" title="标题">
                    <i class="bi bi-type-h2"></i>
                  </button>
                  <div class="toolbar-divider"></div>
                  <button type="button" class="toolbar-btn" title="链接">
                    <i class="bi bi-link-45deg"></i>
                  </button>
                  <button type="button" class="toolbar-btn" title="图片">
                    <i class="bi bi-image"></i>
                  </button>
                  <button type="button" class="toolbar-btn" title="代码">
                    <i class="bi bi-code-slash"></i>
                  </button>
                </div>
                <textarea
                  v-model="form.content"
                  placeholder="在这里写下你的精彩内容...&#10;支持 Markdown 格式&#10;&#10;分享你的知识、经验和见解"
                  required
                  class="content-editor"
                ></textarea>
                <div class="editor-footer">
                  <span class="word-count">
                    <i class="bi bi-text-paragraph"></i>
                    {{ contentWordCount }} 字
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- AI摘要生成 -->
          <div class="form-section">
            <div class="input-group">
              <label class="input-label">
                <i class="bi bi-magic"></i>
                帖子摘要
                <span class="optional">（可选）</span>
              </label>
              <div class="summary-box">
                <div class="summary-header">
                  <button
                    type="button"
                    class="btn-ai-generate"
                    @click="generateSummary"
                    :disabled="generatingSummary || !form.content"
                  >
                    <div class="ai-icon" :class="{ spinning: generatingSummary }">
                      <i class="bi bi-stars"></i>
                    </div>
                    <span class="ai-text">
                      {{ generatingSummary ? 'AI 生成中...' : 'AI 智能生成摘要' }}
                    </span>
                    <div class="ai-glow"></div>
                  </button>
                  <span class="ai-hint">基于内容自动生成吸引人的摘要</span>
                </div>
                <div class="summary-input-wrapper">
                  <textarea
                    v-model="form.summary"
                    placeholder="点击上方按钮生成摘要，或手动输入..."
                    maxlength="200"
                    class="summary-input"
                  ></textarea>
                  <span class="char-count">{{ form.summary.length }}/200</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 提交按钮 -->
          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="router.push('/')">
              <i class="bi bi-x-lg"></i>
              <span>取消</span>
            </button>
            <button type="submit" class="btn-submit" :disabled="submitting">
              <div class="btn-glow"></div>
              <span v-if="submitting" class="btn-spinner"></span>
              <template v-else>
                <i :class="isEdit ? 'bi bi-check-lg' : 'bi bi-send-fill'"></i>
                <span>{{ isEdit ? '保存修改' : '发布帖子' }}</span>
              </template>
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 页脚 -->
    <footer class="page-footer">
      <p>&copy; 2024 择明论坛. All rights reserved.</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get, post, put, getAccessToken } from '@/utils/request'

const route = useRoute()
const router = useRouter()

// 状态
const isDarkMode = ref(false)
const username = ref('')
const particlesRef = ref(null)
const categories = ref([])
const submitting = ref(false)
const generatingSummary = ref(false)
const isEdit = ref(false)

const form = reactive({
  title: '',
  categoryId: '',
  content: '',
  summary: ''
})

const postId = ref(null)

// 计算属性
const contentWordCount = computed(() => {
  return form.content.trim().length
})

// 分类图标映射
function getCategoryIcon(name) {
  const iconMap = {
    '技术': 'bi bi-code-square',
    '生活': 'bi bi-house-heart',
    '问答': 'bi bi-question-circle',
    '分享': 'bi bi-share',
    '讨论': 'bi bi-chat-dots',
    '公告': 'bi bi-megaphone',
    '教程': 'bi bi-book',
    '资源': 'bi bi-folder'
  }
  return iconMap[name] || 'bi bi-tag'
}

// 生成粒子
function generateParticles() {
  if (!particlesRef.value) return
  particlesRef.value.innerHTML = ''
  for (let i = 0; i < 50; i++) {
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

function toggleTheme() {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
}

function checkLogin() {
  const token = getAccessToken()
  if (!token) {
    router.push('/login')
    return false
  }
  username.value = localStorage.getItem('username') || ''
  return true
}

async function logout() {
  const token = getAccessToken()
  if (token) {
    await post('/users/logout')
  }
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('userId')
  localStorage.removeItem('username')
  router.push('/login')
}

async function loadCategories() {
  const res = await get('/categories')
  if (res.code === 200) {
    categories.value = res.data
  }
}

async function loadPost(id) {
  const res = await get(`/posts/${id}`)
  if (res.code === 200) {
    const post = res.data
    form.title = post.title
    form.categoryId = post.categoryId
    form.content = post.content
    form.summary = post.summary || ''
  } else {
    alert('帖子不存在')
    router.push('/')
  }
}

async function generateSummary() {
  if (!form.content) {
    alert('请先输入帖子内容')
    return
  }

  generatingSummary.value = true
  try {
    const res = await post('/ai/summary', {
      content: form.content
    })

    if (res.code === 200) {
      form.summary = res.data
    } else {
      alert(res.message || '生成摘要失败')
    }
  } catch (error) {
    alert('生成摘要失败，请检查网络连接')
  } finally {
    generatingSummary.value = false
  }
}

async function handleSubmit() {
  if (!form.title.trim() || !form.categoryId || !form.content.trim()) {
    alert('请填写所有必填项')
    return
  }

  submitting.value = true
  try {
    let res
    const data = {
      title: form.title.trim(),
      categoryId: parseInt(form.categoryId),
      content: form.content.trim(),
      summary: form.summary.trim() || null
    }

    if (isEdit.value) {
      res = await put(`/posts/${postId.value}`, data)
    } else {
      res = await post('/posts', data)
    }

    if (res.code === 200) {
      alert(isEdit.value ? '修改成功' : '发布成功')
      router.push(isEdit.value ? `/post/${postId.value}` : `/post/${res.data.id}`)
    } else {
      alert(res.message || (isEdit.value ? '修改失败' : '发布失败'))
    }
  } catch (error) {
    alert(isEdit.value ? '修改失败' : '发布失败')
  } finally {
    submitting.value = false
  }
}

// 生命周期
onMounted(() => {
  if (!checkLogin()) return

  isDarkMode.value = localStorage.getItem('darkMode') === 'true'
  generateParticles()
  loadCategories()

  // 检查是否是编辑模式
  const id = route.params.id
  if (id) {
    isEdit.value = true
    postId.value = id
    loadPost(id)
  }
})
</script>

<style scoped>
.create-post-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf5 100%);
  position: relative;
  overflow-x: hidden;
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
  opacity: 0.5;
  animation: float 20s infinite ease-in-out;
}

.orb-1 {
  width: 600px;
  height: 600px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  top: -200px;
  right: -200px;
  animation-delay: 0s;
}

.orb-2 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  bottom: -150px;
  left: -150px;
  animation-delay: -5s;
}

.orb-3 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
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
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 70px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  z-index: 100;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.nav-brand:hover {
  transform: scale(1.02);
}

.logo-wrapper {
  width: 42px;
  height: 42px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.logo-wrapper i {
  font-size: 1.4rem;
  color: white;
}

.brand-text {
  font-size: 1.4rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.btn-icon {
  width: 40px;
  height: 40px;
  border: none;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #667eea;
}

.btn-icon:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-2px);
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 5px 15px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 25px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.username {
  font-weight: 500;
  color: #333;
}

/* 主内容 */
.main-content {
  position: relative;
  z-index: 10;
  padding: 100px 20px 40px;
  max-width: 900px;
  margin: 0 auto;
}

.create-container {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

/* 页面头部 */
.page-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
}

.header-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.header-icon i {
  font-size: 1.8rem;
  color: white;
}

.page-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #333;
  margin: 0 0 5px;
}

.page-subtitle {
  font-size: 1rem;
  color: #666;
  margin: 0;
}

/* 表单区域 */
.form-section {
  margin-bottom: 30px;
}

.input-group {
  margin-bottom: 20px;
}

.input-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  font-size: 1rem;
}

.input-label i {
  color: #667eea;
}

.required {
  color: #f5576c;
}

.optional {
  color: #999;
  font-weight: 400;
  font-size: 0.9rem;
}

.input-wrapper {
  position: relative;
}

.form-input {
  width: 100%;
  padding: 14px 18px;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background: white;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.title-input {
  font-size: 1.2rem;
  font-weight: 500;
}

.char-count {
  position: absolute;
  right: 15px;
  bottom: 15px;
  font-size: 0.85rem;
  color: #999;
}

/* 分类网格 */
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 15px;
}

.category-card {
  position: relative;
  padding: 20px 15px;
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
}

.category-card:hover {
  transform: translateY(-3px);
  border-color: #667eea;
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.2);
}

.category-card.active {
  border-color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
}

.category-icon {
  width: 45px;
  height: 45px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 10px;
  color: white;
  font-size: 1.3rem;
}

.category-name {
  font-weight: 500;
  color: #333;
  font-size: 0.95rem;
}

.category-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.3) 0%, transparent 70%);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: all 0.4s ease;
}

.category-card:hover .category-glow {
  width: 150px;
  height: 150px;
}

/* 编辑器 */
.editor-wrapper {
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.editor-wrapper:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.editor-toolbar {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 10px 15px;
  background: #f8f9fa;
  border-bottom: 1px solid #e0e0e0;
}

.toolbar-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #666;
}

.toolbar-btn:hover {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.toolbar-divider {
  width: 1px;
  height: 24px;
  background: #e0e0e0;
  margin: 0 5px;
}

.content-editor {
  width: 100%;
  min-height: 300px;
  padding: 20px;
  border: none;
  resize: vertical;
  font-size: 1rem;
  line-height: 1.8;
  font-family: inherit;
}

.content-editor:focus {
  outline: none;
}

.editor-footer {
  display: flex;
  justify-content: flex-end;
  padding: 10px 15px;
  background: #f8f9fa;
  border-top: 1px solid #e0e0e0;
}

.word-count {
  font-size: 0.9rem;
  color: #666;
  display: flex;
  align-items: center;
  gap: 5px;
}

/* AI摘要 */
.summary-box {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-radius: 16px;
  padding: 20px;
  border: 2px dashed rgba(102, 126, 234, 0.3);
}

.summary-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.btn-ai-generate {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 25px;
  color: white;
  font-weight: 600;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s ease;
}

.btn-ai-generate:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.btn-ai-generate:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.ai-icon {
  font-size: 1.2rem;
}

.ai-icon.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.ai-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, transparent 60%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.btn-ai-generate:hover .ai-glow {
  opacity: 1;
}

.ai-hint {
  font-size: 0.9rem;
  color: #666;
}

.summary-input-wrapper {
  position: relative;
}

.summary-input {
  width: 100%;
  padding: 15px;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  font-size: 0.95rem;
  resize: vertical;
  min-height: 80px;
  transition: all 0.3s ease;
}

.summary-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

/* 提交按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 40px;
  padding-top: 30px;
  border-top: 2px solid rgba(102, 126, 234, 0.1);
}

.btn-cancel {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 28px;
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  font-weight: 600;
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-cancel:hover {
  border-color: #999;
  color: #333;
}

.btn-submit {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  font-weight: 600;
  color: white;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s ease;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
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

.btn-submit:hover .btn-glow {
  opacity: 1;
}

.btn-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

/* 页脚 */
.page-footer {
  position: relative;
  z-index: 10;
  text-align: center;
  padding: 30px;
  color: #666;
}

/* 深色模式 */
.dark-mode {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}

.dark-mode .top-nav {
  background: rgba(30, 30, 46, 0.9);
  border-color: rgba(255, 255, 255, 0.1);
}

.dark-mode .brand-text {
  background: linear-gradient(135deg, #a78bfa 0%, #c084fc 100%);
  -webkit-background-clip: text;
  background-clip: text;
}

.dark-mode .username {
  color: #e0e0e0;
}

.dark-mode .create-container {
  background: rgba(30, 30, 46, 0.9);
  border-color: rgba(255, 255, 255, 0.1);
}

.dark-mode .page-title {
  color: #fff;
}

.dark-mode .page-subtitle {
  color: #999;
}

.dark-mode .input-label {
  color: #e0e0e0;
}

.dark-mode .form-input,
.dark-mode .content-editor,
.dark-mode .summary-input {
  background: rgba(30, 30, 46, 0.8);
  border-color: rgba(255, 255, 255, 0.1);
  color: #e0e0e0;
}

.dark-mode .editor-wrapper {
  background: rgba(30, 30, 46, 0.8);
  border-color: rgba(255, 255, 255, 0.1);
}

.dark-mode .editor-toolbar,
.dark-mode .editor-footer {
  background: rgba(30, 30, 46, 0.9);
  border-color: rgba(255, 255, 255, 0.1);
}

.dark-mode .toolbar-btn {
  color: #999;
}

.dark-mode .toolbar-btn:hover {
  background: rgba(167, 139, 250, 0.2);
  color: #a78bfa;
}

.dark-mode .category-card {
  background: rgba(30, 30, 46, 0.8);
  border-color: rgba(255, 255, 255, 0.1);
}

.dark-mode .category-name {
  color: #e0e0e0;
}

.dark-mode .summary-box {
  background: rgba(167, 139, 250, 0.1);
  border-color: rgba(167, 139, 250, 0.3);
}

.dark-mode .btn-cancel {
  background: rgba(30, 30, 46, 0.8);
  border-color: rgba(255, 255, 255, 0.2);
  color: #e0e0e0;
}

.dark-mode .page-footer {
  color: #666;
}

/* 响应式 */
@media (max-width: 768px) {
  .top-nav {
    padding: 0 15px;
  }

  .brand-text {
    display: none;
  }

  .main-content {
    padding: 90px 15px 30px;
  }

  .create-container {
    padding: 25px;
  }

  .page-header {
    flex-direction: column;
    text-align: center;
  }

  .category-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .form-actions {
    flex-direction: column-reverse;
  }

  .btn-cancel,
  .btn-submit {
    width: 100%;
    justify-content: center;
  }
}
</style>
