<template>
  <div class="register-page" :class="{ 'dark-mode': isDarkMode }">
    <!-- 星光背景 -->
    <div class="starry-background" ref="starryBg"></div>
    
    <!-- 主题切换按钮 -->
    <button class="theme-toggle" @click="toggleTheme">
      <i :class="isDarkMode ? 'bi bi-sun' : 'bi bi-moon'"></i>
    </button>
    
    <!-- 注册容器 -->
    <div class="register-container">
      <div class="register-content">
        <!-- Logo -->
        <div class="logo">
          <h1>
            <i class="bi bi-lightbulb"></i>
            择明论坛
          </h1>
          <p class="slogan">加入我们，开启智慧之旅</p>
        </div>
        
        <!-- 注册表单 -->
        <form @submit.prevent="handleRegister">
          <div class="form-group">
            <label for="username">
              <i class="bi bi-person"></i> 用户名 *
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
              <i class="bi bi-lock"></i> 密码 *
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
          
          <div class="form-group">
            <label for="confirmPassword">
              <i class="bi bi-lock-fill"></i> 确认密码 *
            </label>
            <div class="password-input">
              <input 
                :type="showConfirmPassword ? 'text' : 'password'" 
                class="form-control" 
                id="confirmPassword" 
                v-model="form.confirmPassword"
                placeholder="请再次输入密码" 
                required
              >
              <button 
                type="button" 
                class="toggle-password"
                @click="showConfirmPassword = !showConfirmPassword"
              >
                <i :class="showConfirmPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
              </button>
            </div>
          </div>
          
          <div class="form-group">
            <label for="email">
              <i class="bi bi-envelope"></i> 邮箱
            </label>
            <input 
              type="email" 
              class="form-control" 
              id="email" 
              v-model="form.email"
              placeholder="请输入邮箱（可选）"
            >
          </div>
          
          <div class="form-group">
            <label for="phone">
              <i class="bi bi-phone"></i> 手机号
            </label>
            <input 
              type="tel" 
              class="form-control" 
              id="phone" 
              v-model="form.phone"
              placeholder="请输入手机号（可选）"
            >
          </div>
          
          <button type="submit" class="btn btn-primary btn-register" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm"></span>
            <span v-else>注册</span>
          </button>
        </form>
        
        <!-- 登录链接 -->
        <div class="login-link">
          已有账号? <router-link to="/login">立即登录</router-link>
        </div>
      </div>
    </div>
    
    <!-- 页脚 -->
    <footer class="register-footer">
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
const showConfirmPassword = ref(false)
const isDarkMode = ref(false)
const starryBg = ref(null)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: ''
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

// 注册处理
async function handleRegister() {
  // 验证表单
  if (!form.username || !form.password || !form.confirmPassword) {
    alert('请填写必填项')
    return
  }
  
  if (form.password !== form.confirmPassword) {
    alert('两次输入的密码不一致')
    return
  }
  
  if (form.password.length < 6) {
    alert('密码长度至少为6位')
    return
  }
  
  loading.value = true
  try {
    const res = await post('/users/register', {
      username: form.username,
      password: form.password,
      email: form.email || null,
      phone: form.phone || null
    })
    
    if (res.code === 200) {
      alert('注册成功！请登录')
      router.push('/login')
    } else {
      alert(res.message || '注册失败')
    }
  } catch (error) {
    alert('注册失败，请检查网络连接')
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
.register-page {
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

.register-container {
  background-color: var(--card-background);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-lg);
  padding: 2.5rem;
  width: 100%;
  max-width: 450px;
  border: 1px solid var(--border-color);
  position: relative;
  overflow: hidden;
}

.register-container::before {
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

.register-content {
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
  margin-bottom: 1.2rem;
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

.btn-register {
  width: 100%;
  padding: 0.75rem;
  font-size: 1rem;
  font-weight: 600;
  border-radius: var(--border-radius);
  margin-top: 1rem;
}

.login-link {
  text-align: center;
  margin-top: 1.5rem;
  color: var(--text-light);
}

.login-link a {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 600;
}

.login-link a:hover {
  text-decoration: underline;
}

.register-footer {
  position: fixed;
  bottom: 20px;
  left: 0;
  right: 0;
  text-align: center;
  color: var(--text-light);
  font-size: 0.85rem;
}
</style>
