<template>
  <div class="manor-create-post" :class="{ 'night-mode': isDarkMode }">
    <!-- 庄园背景 -->
    <ManorBackground :isDarkMode="isDarkMode" />

    <!-- 顶部导航 -->
    <nav class="create-top-nav">
      <div class="nav-left">
        <button class="back-btn" @click="router.push('/')">
          <i class="bi bi-arrow-left"></i>
        </button>
        <div class="nav-brand" @click="router.push('/')">
          <i class="bi bi-house-heart-fill"></i>
          <span>择明庄园</span>
        </div>
      </div>
      <div class="nav-actions">
        <button class="theme-btn" @click="toggleTheme">
          <i :class="isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-stars-fill'"></i>
        </button>
        <div class="user-menu" v-if="username">
          <div class="user-avatar">
            <i class="bi bi-person-fill"></i>
          </div>
          <span class="username">{{ username }}</span>
        </div>
      </div>
    </nav>

    <!-- 主内容 -->
    <main class="create-content">
      <div class="create-card">
        <!-- 页面标题 -->
        <div class="page-header">
          <div class="header-icon">
            <i class="bi bi-pencil-square"></i>
          </div>
          <div class="header-text">
            <h1>{{ isEdit ? '编辑帖子' : '发布新帖' }}</h1>
            <p>在庄园里分享你的见解与故事</p>
          </div>
        </div>

        <form @submit.prevent="handleSubmit" class="post-form">
          <!-- 标题 -->
          <div class="form-group">
            <label class="form-label">
              <i class="bi bi-type-h1"></i>
              帖子标题
              <span class="required">*</span>
            </label>
            <div class="input-wrapper">
              <input
                type="text"
                v-model="form.title"
                placeholder="请输入一个吸引人的标题..."
                required
                maxlength="100"
                class="form-input"
              />
              <span class="char-count">{{ form.title.length }}/100</span>
            </div>
          </div>

          <!-- 分类选择 -->
          <div class="form-group">
            <label class="form-label">
              <i class="bi bi-grid"></i>
              选择分类
              <span class="required">*</span>
            </label>
            <div class="category-grid">
              <div
                v-for="category in categories"
                :key="category.id"
                :class="['category-item', { active: form.categoryId === category.id }]"
                @click="form.categoryId = category.id"
              >
                <div class="category-icon">
                  <i :class="getCategoryIcon(category.name)"></i>
                </div>
                <span class="category-name">{{ category.name }}</span>
              </div>
            </div>
          </div>

          <!-- 内容编辑 -->
          <div class="form-group">
            <label class="form-label">
              <i class="bi bi-file-text"></i>
              帖子内容
              <span class="required">*</span>
            </label>
            <div class="editor-wrapper">
              <textarea
                v-model="form.content"
                placeholder="在这里写下你的精彩内容...&#10;&#10;分享你的知识、经验和见解"
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

          <!-- 标签选择 -->
          <div class="form-group">
            <label class="form-label">
              <i class="bi bi-tags"></i>
              添加标签
              <span class="optional">（可选）</span>
            </label>
            <TagSelector v-model="form.tags" :max-tags="5" placeholder="输入标签，按回车添加..." />
          </div>

          <!-- AI 标题建议 -->
          <AIAssistant
            v-if="form.content.length >= 20"
            type="title"
            :content="form.content"
            @use-title="form.title = $event"
          />

          <!-- AI 标签推荐 -->
          <AIAssistant
            v-if="form.content.length >= 20"
            type="tags"
            :title="form.title"
            :content="form.content"
            @select-tag="addTag"
          />

          <!-- 摘要 -->
          <div class="form-group">
            <label class="form-label">
              <i class="bi bi-text-paragraph"></i>
              帖子摘要
              <span class="optional">（可选）</span>
            </label>
            <div class="summary-wrapper">
              <button
                type="button"
                class="generate-btn"
                @click="generateSummary"
                :disabled="generatingSummary || !form.content"
              >
                <i class="bi bi-magic" :class="{ spinning: generatingSummary }"></i>
                {{ generatingSummary ? '生成中...' : '智能生成摘要' }}
              </button>
              <textarea
                v-model="form.summary"
                placeholder="点击上方按钮生成摘要，或手动输入..."
                maxlength="200"
                class="summary-input"
              ></textarea>
              <span class="char-count summary-count">{{ form.summary.length }}/200</span>
            </div>
          </div>

          <!-- 提交按钮 -->
          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="router.push('/')">
              <i class="bi bi-x-lg"></i>
              <span>取消</span>
            </button>
            <button type="submit" class="btn-submit" :disabled="submitting">
              <i v-if="submitting" class="bi bi-arrow-repeat spinning"></i>
              <template v-else>
                <i :class="isEdit ? 'bi bi-check-lg' : 'bi bi-send-fill'"></i>
                <span>{{ isEdit ? '保存修改' : '发布帖子' }}</span>
              </template>
            </button>
          </div>
        </form>
      </div>
    </main>

    <!-- 底部导航 -->
    <ManorBottomNav :isDarkMode="isDarkMode" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get, post, put, getAccessToken } from '@/utils/request'
import TagSelector from '@/components/TagSelector.vue'
import ManorBackground from '@/components/ManorBackground.vue'
import ManorBottomNav from '@/components/ManorBottomNav.vue'
import AIAssistant from '@/components/AIAssistant.vue'

const route = useRoute()
const router = useRouter()

// 状态
const isDarkMode = ref(false)
const username = ref('')
const categories = ref([])
const submitting = ref(false)
const generatingSummary = ref(false)
const isEdit = ref(false)

const form = reactive({
  title: '',
  categoryId: '',
  content: '',
  summary: '',
  tags: []
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

async function loadCategories() {
  const res = await get('/categories')
  if (res.code === 200) {
    categories.value = res.data
  }
}

function addTag(tag) {
  if (!form.tags.includes(tag) && form.tags.length < 5) {
    form.tags.push(tag)
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
.manor-create-post {
  min-height: 100vh;
  position: relative;
  padding-bottom: 100px;
}

/* 顶部导航 */
.create-top-nav {
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

.night-mode .create-top-nav {
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
  background: rgba(139, 188, 143, 0.15);
  color: #5a7c5a;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.night-mode .back-btn {
  background: rgba(74, 124, 90, 0.2);
  color: #8ab88a;
}

.back-btn:hover {
  background: rgba(139, 188, 143, 0.3);
  transform: translateX(-2px);
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-size: 20px;
  font-weight: 700;
  color: #5a7c5a;
  font-family: 'Georgia', serif;
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

.nav-actions {
  display: flex;
  align-items: center;
  gap: 16px;
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

.user-menu {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 14px;
  background: rgba(139, 188, 143, 0.1);
  border-radius: 25px;
}

.night-mode .user-menu {
  background: rgba(74, 124, 90, 0.15);
}

.user-avatar {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
}

.username {
  font-weight: 500;
  color: #5a7c5a;
  font-size: 14px;
}

.night-mode .username {
  color: #8ab88a;
}

/* 主内容 */
.create-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 90px 20px 40px;
  position: relative;
  z-index: 1;
}

.create-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(139, 188, 143, 0.2);
}

.night-mode .create-card {
  background: rgba(40, 45, 55, 0.95);
  border-color: rgba(74, 124, 90, 0.2);
}

/* 页面头部 */
.page-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid rgba(139, 188, 143, 0.2);
}

.night-mode .page-header {
  border-color: rgba(74, 124, 90, 0.2);
}

.header-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(124, 184, 124, 0.3);
}

.header-icon i {
  font-size: 28px;
  color: white;
}

.header-text h1 {
  font-size: 24px;
  font-weight: 700;
  color: #2d3748;
  margin: 0 0 6px;
  font-family: 'Georgia', serif;
}

.night-mode .header-text h1 {
  color: #e2e8f0;
}

.header-text p {
  font-size: 14px;
  color: #8a9a8a;
  margin: 0;
}

.night-mode .header-text p {
  color: #7a9a8a;
}

/* 表单 */
.form-group {
  margin-bottom: 24px;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 12px;
  font-size: 15px;
}

.night-mode .form-label {
  color: #e2e8f0;
}

.form-label i {
  color: #8FBC8F;
}

.night-mode .form-label i {
  color: #7ac87a;
}

.required {
  color: #ff6b6b;
}

.optional {
  color: #8a9a8a;
  font-weight: 400;
  font-size: 13px;
}

.night-mode .optional {
  color: #7a9a8a;
}

.input-wrapper {
  position: relative;
}

.form-input {
  width: 100%;
  padding: 14px 18px;
  border: 2px solid rgba(139, 188, 143, 0.2);
  border-radius: 16px;
  font-size: 16px;
  transition: all 0.3s ease;
  background: white;
  color: #2d3748;
}

.night-mode .form-input {
  background: rgba(30, 35, 45, 0.8);
  border-color: rgba(74, 124, 90, 0.2);
  color: #e2e8f0;
}

.form-input:focus {
  outline: none;
  border-color: #8FBC8F;
  box-shadow: 0 0 0 4px rgba(139, 188, 143, 0.15);
}

.char-count {
  position: absolute;
  right: 15px;
  bottom: 15px;
  font-size: 12px;
  color: #8a9a8a;
}

.night-mode .char-count {
  color: #7a9a8a;
}

/* 分类网格 */
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
}

.category-item {
  padding: 16px 12px;
  background: rgba(139, 188, 143, 0.08);
  border: 2px solid transparent;
  border-radius: 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.night-mode .category-item {
  background: rgba(74, 124, 90, 0.1);
}

.category-item:hover {
  transform: translateY(-2px);
  border-color: rgba(139, 188, 143, 0.4);
  box-shadow: 0 4px 12px rgba(139, 188, 143, 0.15);
}

.category-item.active {
  border-color: #8FBC8F;
  background: rgba(139, 188, 143, 0.15);
}

.night-mode .category-item.active {
  border-color: #4a7c5a;
  background: rgba(74, 124, 90, 0.2);
}

.category-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 8px;
  color: white;
  font-size: 20px;
}

.category-name {
  font-weight: 500;
  color: #5a7c5a;
  font-size: 13px;
}

.night-mode .category-name {
  color: #8ab88a;
}

/* 编辑器 */
.editor-wrapper {
  background: white;
  border: 2px solid rgba(139, 188, 143, 0.2);
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.night-mode .editor-wrapper {
  background: rgba(30, 35, 45, 0.8);
  border-color: rgba(74, 124, 90, 0.2);
}

.editor-wrapper:focus-within {
  border-color: #8FBC8F;
  box-shadow: 0 0 0 4px rgba(139, 188, 143, 0.15);
}

.content-editor {
  width: 100%;
  min-height: 200px;
  padding: 16px;
  border: none;
  background: transparent;
  resize: vertical;
  font-size: 15px;
  line-height: 1.7;
  color: #2d3748;
  font-family: inherit;
  outline: none;
}

.night-mode .content-editor {
  color: #e2e8f0;
}

.content-editor::placeholder {
  color: #8a9a8a;
}

.editor-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 10px 16px;
  background: rgba(139, 188, 143, 0.05);
  border-top: 1px solid rgba(139, 188, 143, 0.1);
}

.night-mode .editor-footer {
  background: rgba(74, 124, 90, 0.08);
  border-color: rgba(74, 124, 90, 0.1);
}

.word-count {
  font-size: 13px;
  color: #8a9a8a;
  display: flex;
  align-items: center;
  gap: 6px;
}

.night-mode .word-count {
  color: #7a9a8a;
}

/* 摘要 */
.summary-wrapper {
  position: relative;
}

.generate-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  margin-bottom: 12px;
  border: none;
  border-radius: 20px;
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.generate-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(124, 184, 124, 0.3);
}

.generate-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.generate-btn i.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.summary-input {
  width: 100%;
  padding: 14px 18px;
  border: 2px solid rgba(139, 188, 143, 0.2);
  border-radius: 16px;
  font-size: 15px;
  background: white;
  color: #2d3748;
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
  outline: none;
}

.night-mode .summary-input {
  background: rgba(30, 35, 45, 0.8);
  border-color: rgba(74, 124, 90, 0.2);
  color: #e2e8f0;
}

.summary-input:focus {
  border-color: #8FBC8F;
  box-shadow: 0 0 0 4px rgba(139, 188, 143, 0.15);
}

.summary-count {
  bottom: 10px;
}

/* 提交按钮 */
.form-actions {
  display: flex;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid rgba(139, 188, 143, 0.2);
}

.night-mode .form-actions {
  border-color: rgba(74, 124, 90, 0.2);
}

.btn-cancel {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 28px;
  background: rgba(139, 188, 143, 0.1);
  border: 2px solid rgba(139, 188, 143, 0.2);
  border-radius: 12px;
  font-weight: 600;
  color: #6a7c6a;
  cursor: pointer;
  transition: all 0.3s ease;
  flex: 1;
}

.night-mode .btn-cancel {
  background: rgba(74, 124, 90, 0.15);
  border-color: rgba(74, 124, 90, 0.2);
  color: #9ab89a;
}

.btn-cancel:hover {
  background: rgba(139, 188, 143, 0.2);
  border-color: rgba(139, 188, 143, 0.4);
}

.btn-submit {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 14px 32px;
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  border: none;
  border-radius: 12px;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  flex: 2;
  box-shadow: 0 4px 15px rgba(124, 184, 124, 0.3);
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(124, 184, 124, 0.4);
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-submit i.spinning {
  animation: spin 1s linear infinite;
}

/* 响应式 */
@media (max-width: 640px) {
  .create-content {
    padding: 80px 16px 40px;
  }

  .create-card {
    padding: 20px;
  }

  .page-header {
    flex-direction: column;
    text-align: center;
  }

  .category-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .form-actions {
    flex-direction: column-reverse;
  }

  .btn-cancel,
  .btn-submit {
    width: 100%;
  }
}
</style>