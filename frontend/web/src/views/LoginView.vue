<template>
  <div class="login-page" :class="{ 'dark-mode': isDarkMode }">
    <!-- 星光背景 -->
    <div class="starry-background" ref="starryBg"></div>
    
    <!-- 主题切换按钮 -->
    <button class="theme-toggle" @click="toggleTheme">
      <i :class="isDarkMode ? 'bi bi-sun' : 'bi bi-moon'"></i>
    </button>
    
    <!-- 登录容器 -->
    <div class="login-container">
      <div class="login-content">
        <!-- Logo -->
        <div class="logo">
          <h1>
            <i class="bi bi-lightbulb"></i>
            择明论坛
          </h1>
          <p class="slogan">智慧之光，照亮前行之路</p>
        </div>
        
        <!-- 登录表单 -->
        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <label for="username">
              <i class="bi bi-person"></i> 用户名
            </label>
            <input 
              type="text" 
              class="form-control" 
              id="username" 
              v-model="form.username"
              placeholder="请输入用户名" 
              required
            >
          </div>
          
          <div class="form-group">
            <label for="password">
              <i class="bi bi-lock"></i> 密码
            </label>
            <div class="password-input">
              <input 
                :type="showPassword ? 'text' : 'password'" 
                class="form-control" 
                id="password" 
                v-model="form.password"
                placeholder="请输入密码" 
                required
              >
              <button 
                type="button" 
                class="toggle-password"
                @click="showPassword = !showPassword"
              >
                <i :class="showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
              </button>
            </div>
          </div>
          
          <div class="form-options">
            <label class="remember-me">
              <input type="checkbox" v-model="form.rememberMe"> 记住我
            </label>
            <a href="#" class="forgot-password">忘记密码?</a>
          </div>
          
          <button type="submit" class="btn btn-primary btn-login" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm"></span>
            <span v-else>登录</span>
          </button>
        </form>
        
        <!-- 注册链接 -->
        <div class="register-link">
          还没有账号? <router-link to="/register">立即注册</router-link>
        </div>
      </div>
    </div>
    
    <!-- 页脚 -->
    <footer class="login-footer">
      <p>&copy; 2024 择明论坛. All rights reserved.</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { post } from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const showPassword = ref(false)
const isDarkMode = ref(false)
const starryBg = ref(null)

const form = reactive({
  username: '',
  password: '',
  rememberMe: false
})

// 生成星光背景
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

// 主题切换
function toggleTheme() {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
}

// 登录处理
async function handleLogin() {
  if (!form.username || !form.password) {
    alert('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    console.log('登录请求:', { username: form.username, password: form.password })
    const res = await post('/users/login', {
      username: form.username,
      password: form.password
    })
    console.log('登录响应:', res)

    if (res.code === 200) {
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('userId', res.data.user.id)
      localStorage.setItem('username', res.data.user.username)
      router.push('/')
    } else {
      alert(res.message || '登录失败')
    }
  } catch (error) {
    console.error('登录错误:', error)
    alert('登录失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 检查保存的主题
  isDarkMode.value = localStorage.getItem('darkMode') === 'true'
  generateStars()
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--background-color);
  position: relative;
  padding: 20px;
}

.theme-toggle {
  position: fixed;
  top: 20px;
  right: 20px;
  width: 45px;
  height: 45px;
  border-radius: 50%;
  border: none;
  background-color: var(--card-background);
  color: var(--text-color);
  box-shadow: var(--shadow);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  transition: var(--transition);
  z-index: 100;
}

.theme-toggle:hover {
  transform: scale(1.1);
}

.login-container {
  background-color: var(--card-background);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-lg);
  padding: 2.5rem;
  width: 100%;
  max-width: 420px;
  border: 1px solid var(--border-color);
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent, var(--primary-color), transparent);
  opacity: 0.05;
  transform: rotate(45deg);
  animation: shimmer 8s infinite linear;
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%) translateY(-100%) rotate(45deg);
  }
  100% {
    transform: translateX(100%) translateY(100%) rotate(45deg);
  }
}

.login-content {
  position: relative;
  z-index: 1;
}

.logo {
  text-align: center;
  margin-bottom: 2rem;
}

.logo h1 {
  color: var(--primary-color);
  font-size: 2rem;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.logo h1 i {
  font-size: 2.5rem;
}

.slogan {
  color: var(--text-light);
  font-size: 0.9rem;
  margin-top: 0.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--text-color);
  font-weight: 500;
}

.form-group label i {
  margin-right: 0.5rem;
  color: var(--primary-color);
}

.password-input {
  position: relative;
}

.password-input .form-control {
  padding-right: 45px;
}

.toggle-password {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--text-light);
  cursor: pointer;
  padding: 5px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-light);
  cursor: pointer;
}

.forgot-password {
  color: var(--primary-color);
  text-decoration: none;
  font-size: 0.9rem;
}

.forgot-password:hover {
  text-decoration: underline;
}

.btn-login {
  width: 100%;
  padding: 0.75rem;
  font-size: 1rem;
  font-weight: 600;
  border-radius: var(--border-radius);
}

.register-link {
  text-align: center;
  margin-top: 1.5rem;
  color: var(--text-light);
}

.register-link a {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 600;
}

.register-link a:hover {
  text-decoration: underline;
}

.login-footer {
  position: fixed;
  bottom: 20px;
  left: 0;
  right: 0;
  text-align: center;
  color: var(--text-light);
  font-size: 0.85rem;
}
</style>
