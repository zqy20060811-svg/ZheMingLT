<template>
  <div class="edit-post-page" :class="{ 'dark-mode': isDarkMode }">
    <!-- 动态背景 -->
    <div class="animated-bg">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
    </div>

    <!-- 顶部导航 -->
    <nav class="top-nav">
      <div class="nav-left">
        <button class="btn-icon back-btn" @click="goBack">
          <i class="bi bi-arrow-left"></i>
        </button>
        <span class="nav-title">编辑帖子</span>
      </div>
      <div class="nav-actions">
        <button class="btn-icon theme-btn" @click="toggleTheme">
          <i :class="isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-fill'"></i>
        </button>
      </div>
    </nav>

    <!-- 编辑表单 -->
    <div class="form-section">
      <div class="form-card">
        <div class="form-header">
          <div class="header-icon">
            <i class="bi bi-pencil-square"></i>
          </div>
          <div class="header-info">
            <h2>编辑帖子</h2>
            <p>修改你的帖子内容</p>
          </div>
        </div>

        <form @submit.prevent="submitForm" class="post-form">
          <!-- 标题 -->
          <div class="form-group">
            <label class="form-label">
              <i class="bi bi-type-h1"></i>
              帖子标题
            </label>
            <input
              v-model="form.title"
              type="text"
              class="form-input"
              placeholder="请输入帖子标题（2-100字）"
              maxlength="100"
              required
            />
            <span class="char-count">{{ form.title.length }}/100</span>
          </div>

          <!-- 分类 -->
          <div class="form-group">
            <label class="form-label">
              <i class="bi bi-grid"></i>
              选择分类
            </label>
            <div class="category-grid">
              <button
                v-for="cat in categories"
                :key="cat.id"
                type="button"
                :class="['category-btn', { active: form.categoryId === cat.id }]"
                @click="form.categoryId = cat.id"
              >
                <i :class="cat.icon || 'bi bi-folder'"></i>
                <span>{{ cat.name }}</span>
              </button>
            </div>
          </div>

          <!-- 内容 -->
          <div class="form-group">
            <label class="form-label">
              <i class="bi bi-file-text"></i>
              帖子内容
            </label>
            <textarea
              v-model="form.content"
              class="form-textarea"
              placeholder="请输入帖子内容，支持Markdown语法..."
              rows="12"
              required
            ></textarea>
            <span class="char-count">{{ form.content.length }} 字</span>
          </div>

          <!-- 摘要 -->
          <div class="form-group">
            <label class="form-label">
              <i class="bi bi-card-text"></i>
              帖子摘要（可选）
            </label>
            <textarea
              v-model="form.summary"
              class="form-textarea summary"
              placeholder="请输入帖子摘要，如不填写将自动提取正文前200字"
              rows="3"
              maxlength="200"
            ></textarea>
            <span class="char-count">{{ form.summary.length }}/200</span>
          </div>

          <!-- 操作按钮 -->
          <div class="form-actions">
            <button type="button" class="btn-secondary" @click="goBack">
              <i class="bi bi-x-lg"></i>
              取消
            </button>
            <button type="submit" class="btn-primary" :disabled="submitting">
              <i v-if="submitting" class="bi bi-arrow-repeat spinning"></i>
              <i v-else class="bi bi-check-lg"></i>
              {{ submitting ? '保存中...' : '保存修改' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 底部导航 -->
    <BottomNav />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { get, put } from '@/utils/request'
import BottomNav from '@/components/BottomNav.vue'

const router = useRouter()
const route = useRoute()

// 状态
const isDarkMode = ref(false)
const submitting = ref(false)
const postId = ref(null)

// 表单数据
const form = ref({
  title: '',
  content: '',
  summary: '',
  categoryId: null
})

// 分类列表
const categories = ref([])

// 初始化
onMounted(() => {
  isDarkMode.value = localStorage.getItem('darkMode') === 'true'
  postId.value = route.params.id
  loadCategories()
  loadPost()
})

// 加载分类
async function loadCategories() {
  try {
    const res = await get('/categories')
    if (res.code === 200 && res.data) {
      categories.value = res.data
    }
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

// 加载帖子详情
async function loadPost() {
  try {
    const res = await get(`/posts/${postId.value}`)
    if (res.code === 200 && res.data) {
      const post = res.data
      form.value = {
        title: post.title || '',
        content: post.content || '',
        summary: post.summary || '',
        categoryId: post.categoryId || null
      }
    } else {
      alert('帖子不存在')
      router.back()
    }
  } catch (error) {
    console.error('加载帖子失败', error)
    alert('加载帖子失败')
    router.back()
  }
}

// 提交表单
async function submitForm() {
  // 表单验证
  if (!form.value.title.trim()) {
    alert('请输入帖子标题')
    return
  }
  if (form.value.title.trim().length < 2) {
    alert('标题至少需要2个字符')
    return
  }
  if (!form.value.content.trim()) {
    alert('请输入帖子内容')
    return
  }
  if (!form.value.categoryId) {
    alert('请选择帖子分类')
    return
  }

  submitting.value = true

  try {
    const res = await put(`/posts/${postId.value}`, {
      title: form.value.title.trim(),
      content: form.value.content.trim(),
      summary: form.value.summary.trim() || form.value.content.trim().substring(0, 200),
      categoryId: form.value.categoryId
    })

    if (res.code === 200) {
      alert('帖子修改成功！')
      router.push(`/post/${postId.value}`)
    } else {
      alert(res.message || '修改失败')
    }
  } catch (error) {
    console.error('修改帖子失败', error)
    alert('修改失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 返回上一页
function goBack() {
  if (confirm('确定要放弃修改吗？')) {
    router.back()
  }
}

// 切换主题
function toggleTheme() {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
}
</script>

<style scoped>
.edit-post-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf5 100%);
  position: relative;
  padding-bottom: 80px;
}

.dark-mode {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
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
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  top: -100px;
  right: -100px;
  animation-delay: 0s;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  bottom: 20%;
  left: -50px;
  animation-delay: -7s;
}

.orb-3 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  top: 40%;
  right: 10%;
  animation-delay: -14s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -30px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
}

/* 顶部导航 */
.top-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .top-nav {
  background: rgba(30, 30, 50, 0.8);
  border-bottom-color: rgba(255, 255, 255, 0.05);
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.nav-title {
  font-size: 20px;
  font-weight: 700;
  color: #333;
}

.dark-mode .nav-title {
  color: #e0e0e0;
}

.nav-actions {
  display: flex;
  gap: 10px;
}

.btn-icon {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 10px;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-icon:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-2px);
}

.dark-mode .btn-icon {
  background: rgba(255, 255, 255, 0.1);
  color: #a0a0c0;
}

/* 表单区域 */
.form-section {
  position: relative;
  z-index: 1;
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.form-card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  padding: 30px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .form-card {
  background: rgba(40, 40, 60, 0.9);
  border-color: rgba(255, 255, 255, 0.05);
}

/* 表单头部 */
.form-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .form-header {
  border-bottom-color: rgba(255, 255, 255, 0.05);
}

.header-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.header-info h2 {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
}

.dark-mode .header-info h2 {
  color: #e0e0e0;
}

.header-info p {
  font-size: 14px;
  color: #999;
}

/* 表单组 */
.form-group {
  margin-bottom: 25px;
  position: relative;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
}

.dark-mode .form-label {
  color: #e0e0e0;
}

.form-label i {
  color: #667eea;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 12px 15px;
  border: 2px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  font-size: 15px;
  background: rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
}

.dark-mode .form-input,
.dark-mode .form-textarea {
  background: rgba(30, 30, 50, 0.8);
  border-color: rgba(255, 255, 255, 0.1);
  color: #e0e0e0;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 150px;
  font-family: inherit;
  line-height: 1.6;
}

.form-textarea.summary {
  min-height: 80px;
}

.char-count {
  position: absolute;
  right: 0;
  bottom: -20px;
  font-size: 12px;
  color: #999;
}

/* 分类网格 */
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 10px;
}

.category-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  padding: 15px 10px;
  border: 2px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: all 0.3s ease;
}

.dark-mode .category-btn {
  background: rgba(30, 30, 50, 0.8);
  border-color: rgba(255, 255, 255, 0.1);
  color: #a0a0c0;
}

.category-btn i {
  font-size: 20px;
  color: #667eea;
}

.category-btn span {
  font-size: 13px;
}

.category-btn:hover {
  border-color: #667eea;
  transform: translateY(-2px);
}

.category-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: white;
}

.category-btn.active i {
  color: white;
}

/* 操作按钮 */
.form-actions {
  display: flex;
  gap: 15px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .form-actions {
  border-top-color: rgba(255, 255, 255, 0.05);
}

.btn-primary,
.btn-secondary {
  flex: 1;
  padding: 14px 24px;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-secondary {
  background: rgba(0, 0, 0, 0.05);
  color: #666;
}

.dark-mode .btn-secondary {
  background: rgba(255, 255, 255, 0.1);
  color: #a0a0c0;
}

.btn-secondary:hover {
  background: rgba(0, 0, 0, 0.1);
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 响应式 */
@media (max-width: 768px) {
  .form-section {
    padding: 15px;
  }

  .form-card {
    padding: 20px;
  }

  .category-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .form-actions {
    flex-direction: column;
  }
}
</style>
