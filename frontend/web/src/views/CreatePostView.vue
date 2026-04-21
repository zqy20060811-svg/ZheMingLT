<template>
  <div class="create-post-page" :class="{ 'dark-mode': isDarkMode }">
    <!-- 星光背景 -->
    <div class="starry-background" ref="starryBg"></div>

    <!-- 导航栏 -->
    <nav class="navbar navbar-expand-lg">
      <div class="container">
        <router-link class="navbar-brand" to="/">
          <i class="bi bi-lightbulb"></i> 择明论坛
        </router-link>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
          <ul class="navbar-nav me-auto">
            <li class="nav-item">
              <router-link class="nav-link" to="/">首页</router-link>
            </li>
          </ul>
          <ul class="navbar-nav">
            <li class="nav-item">
              <button class="btn btn-link nav-link" @click="toggleTheme">
                <i :class="isDarkMode ? 'bi bi-sun' : 'bi bi-moon'"></i>
              </button>
            </li>
            <li class="nav-item">
              <span class="nav-link">欢迎, {{ username }}</span>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="#" @click.prevent="logout">退出</a>
            </li>
          </ul>
        </div>
      </div>
    </nav>

    <!-- 主内容 -->
    <div class="container main-content">
      <div class="create-container">
        <h2 class="page-title">
          <i class="bi bi-pencil-square"></i> {{ isEdit ? '编辑帖子' : '发布帖子' }}
        </h2>

        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label for="title">标题 *</label>
            <input
              type="text"
              class="form-control"
              id="title"
              v-model="form.title"
              placeholder="请输入帖子标题"
              required
            >
          </div>

          <div class="form-group">
            <label for="categoryId">分类 *</label>
            <select class="form-control" id="categoryId" v-model="form.categoryId" required>
              <option value="">请选择分类</option>
              <option v-for="category in categories" :key="category.id" :value="category.id">
                {{ category.name }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="content">内容 *</label>
            <textarea
              class="form-control"
              id="content"
              rows="12"
              v-model="form.content"
              placeholder="请输入帖子内容"
              required
            ></textarea>
          </div>

          <div class="form-group">
            <label>摘要（可选）</label>
            <div class="summary-section">
              <button
                type="button"
                class="btn btn-outline-primary btn-generate"
                @click="generateSummary"
                :disabled="generatingSummary || !form.content"
              >
                <span v-if="generatingSummary" class="spinner-border spinner-border-sm"></span>
                <i v-else class="bi bi-magic"></i>
                {{ generatingSummary ? '生成中...' : 'AI生成摘要' }}
              </button>
              <textarea
                class="form-control mt-2"
                rows="2"
                v-model="form.summary"
                placeholder="点击上方按钮生成摘要，或手动输入"
              ></textarea>
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary btn-submit" :disabled="submitting">
              <span v-if="submitting" class="spinner-border spinner-border-sm"></span>
              <span v-else>{{ isEdit ? '保存修改' : '发布帖子' }}</span>
            </button>
            <router-link to="/" class="btn btn-secondary">取消</router-link>
          </div>
        </form>
      </div>
    </div>

    <!-- 页脚 -->
    <footer class="main-footer">
      <div class="container">
        <p>&copy; 2024 择明论坛. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get, post, put } from '@/utils/request'

const route = useRoute()
const router = useRouter()

// 状态
const isDarkMode = ref(false)
const username = ref('')
const starryBg = ref(null)
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

// 方法
function generateStars() {
  if (!starryBg.value) return
  starryBg.value.innerHTML = ''
  for (let i = 0; i < 100; i++) {
    const star = document.createElement('div')
    star.className = 'star'
    star.style.left = Math.random() * 100 + '%'
    star.style.top = Math.random() * 100 + '%'
    star.style.width = Math.random() * 3 + 1 + 'px'
    star.style.height = star.style.width
    star.style.animationDelay = Math.random() * 3 + 's'
    star.style.animationDuration = Math.random() * 3 + 2 + 's'
    starryBg.value.appendChild(star)
  }
}

function toggleTheme() {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
}

function checkLogin() {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return false
  }
  username.value = localStorage.getItem('username') || ''
  return true
}

async function logout() {
  const token = localStorage.getItem('token')
  if (token) {
    await post('/users/logout')
  }
  localStorage.removeItem('token')
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
  generateStars()
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
  background-color: var(--background-color);
  position: relative;
}

.main-content {
  padding-top: 30px;
  padding-bottom: 30px;
}

.create-container {
  max-width: 800px;
  margin: 0 auto;
  background-color: var(--card-background);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow);
  padding: 30px;
}

.page-title {
  color: var(--text-color);
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid var(--primary-color);
}

.page-title i {
  color: var(--primary-color);
  margin-right: 10px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: var(--text-color);
  font-weight: 500;
}

.form-control {
  background-color: var(--card-background);
  border-color: var(--border-color);
  color: var(--text-color);
}

.form-control:focus {
  background-color: var(--card-background);
  border-color: var(--primary-color);
  color: var(--text-color);
  box-shadow: 0 0 0 0.2rem rgba(74, 111, 165, 0.25);
}

.summary-section {
  background-color: var(--hover-color);
  padding: 15px;
  border-radius: var(--border-radius);
}

.btn-generate {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-generate i {
  font-size: 1rem;
}

.form-actions {
  display: flex;
  gap: 15px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
}

.btn-submit {
  min-width: 120px;
}

/* 页脚 */
.main-footer {
  background-color: var(--card-background);
  border-top: 1px solid var(--border-color);
  padding: 20px 0;
  text-align: center;
  color: var(--text-light);
}
</style>
