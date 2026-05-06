<template>
  <div class="manor-login" :class="{ 'night-mode': isDarkMode }">
    <!-- 庄园背景 -->
    <ManorBackground :isDarkMode="isDarkMode" />

    <!-- 主题切换 -->
    <button class="theme-toggle" @click="toggleTheme">
      <i :class="isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-stars-fill'"></i>
    </button>

    <!-- 登录容器 -->
    <div class="login-container">
      <!-- 左侧装饰 -->
      <div class="login-decoration">
        <div class="decoration-content">
          <div class="logo-icon">
            <i class="bi bi-house-heart-fill"></i>
          </div>
          <h2 class="decoration-title">择明庄园</h2>
          <p class="decoration-subtitle">欢迎来到宁静的庄园世界</p>
          <div class="feature-list">
            <div class="feature-item">
              <i class="bi bi-flower1"></i>
              <span>在庄园中分享你的故事</span>
            </div>
            <div class="feature-item">
              <i class="bi bi-tree-fill"></i>
              <span>与志同道合的朋友交流</span>
            </div>
            <div class="feature-item">
              <i class="bi bi-sun-fill"></i>
              <span>享受温暖的社区氛围</span>
            </div>
          </div>
        </div>
        <div class="decoration-shapes">
          <div class="shape shape-1"></div>
          <div class="shape shape-2"></div>
          <div class="shape shape-3"></div>
        </div>
      </div>

      <!-- 右侧表单 -->
      <div class="login-form-wrapper">
        <div class="form-header">
          <h3>欢迎回来</h3>
          <p>登录你的账号，进入庄园</p>
        </div>

        <form @submit.prevent="handleLogin" class="login-form">
          <div class="input-group">
            <label class="input-label">
              <i class="bi bi-person-fill"></i>
              用户名
            </label>
            <div class="input-wrapper">
              <input
                type="text"
                v-model="form.username"
                placeholder="请输入用户名"
                required
                class="form-input"
              />
            </div>
          </div>

          <div class="input-group">
            <label class="input-label">
              <i class="bi bi-lock-fill"></i>
              密码
            </label>
            <div class="input-wrapper">
              <input
                :type="showPassword ? 'text' : 'password'"
                v-model="form.password"
                placeholder="请输入密码"
                required
                class="form-input"
              />
              <button
                type="button"
                class="toggle-password"
                @click="showPassword = !showPassword"
              >
                <i :class="showPassword ? 'bi bi-eye-slash-fill' : 'bi bi-eye-fill'"></i>
              </button>
            </div>
          </div>

          <div class="form-options">
            <label class="remember-me">
              <input type="checkbox" v-model="form.rememberMe" />
              <span class="checkmark"></span>
              <span class="label-text">记住我</span>
            </label>
            <a href="#" class="forgot-link">忘记密码?</a>
          </div>

          <button type="submit" class="submit-btn" :disabled="loading">
            <span v-if="loading" class="btn-spinner"></span>
            <span v-else class="btn-content">
              <span>立即登录</span>
              <i class="bi bi-arrow-right"></i>
            </span>
          </button>
        </form>

        <div class="divider">
          <span>或者</span>
        </div>

        <div class="social-login">
          <button class="social-btn wechat">
            <i class="bi bi-wechat"></i>
          </button>
          <button class="social-btn qq">
            <i class="bi bi-tencent-qq"></i>
          </button>
          <button class="social-btn github">
            <i class="bi bi-github"></i>
          </button>
        </div>

        <div class="register-link">
          还没有账号?
          <router-link to="/register" class="link">立即注册</router-link>
        </div>
      </div>
    </div>

    <!-- 页脚 -->
    <footer class="login-footer">
      <p>&copy; 2024 择明庄园. All rights reserved.</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { post, setTokens } from '@/utils/request'
import ManorBackground from '@/components/ManorBackground.vue'

const router = useRouter()
const loading = ref(false)
const showPassword = ref(false)
const isDarkMode = ref(false)

const form = reactive({
  username: '',
  password: '',
  rememberMe: false
})

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
      // 存储双Token
      setTokens(res.data.accessToken, res.data.refreshToken)
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
  isDarkMode.value = localStorage.getItem('darkMode') === 'true'
})
</script>

<style scoped>
.manor-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  padding: 20px;
}

/* 主题切换 */
.theme-toggle {
  position: fixed;
  top: 20px;
  right: 20px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  color: #5a7c5a;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  transition: all 0.3s ease;
  z-index: 100;
}

.night-mode .theme-toggle {
  background: rgba(40, 45, 55, 0.9);
  color: #8ab88a;
}

.theme-toggle:hover {
  transform: scale(1.1) rotate(15deg);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

/* 登录容器 */
.login-container {
  display: flex;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 30px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  width: 100%;
  max-width: 900px;
  min-height: 600px;
  position: relative;
  z-index: 10;
  border: 1px solid rgba(139, 188, 143, 0.2);
}

.night-mode .login-container {
  background: rgba(40, 45, 55, 0.95);
  border-color: rgba(74, 124, 90, 0.2);
}

/* 左侧装饰 */
.login-decoration {
  flex: 1;
  background: linear-gradient(135deg, #8FBC8F 0%, #7CB87C 100%);
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.decoration-content {
  position: relative;
  z-index: 2;
  color: white;
}

.logo-icon {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30px;
  backdrop-filter: blur(10px);
}

.logo-icon i {
  font-size: 2.5rem;
}

.decoration-title {
  font-size: 2rem;
  font-weight: 800;
  margin-bottom: 10px;
  font-family: 'Georgia', serif;
}

.decoration-subtitle {
  font-size: 1rem;
  opacity: 0.9;
  margin-bottom: 40px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 0.95rem;
}

.feature-item i {
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.8);
}

/* 装饰形状 */
.decoration-shapes {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.shape-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -100px;
}

.shape-2 {
  width: 200px;
  height: 200px;
  bottom: 50px;
  left: -50px;
}

.shape-3 {
  width: 150px;
  height: 150px;
  bottom: -50px;
  right: 50px;
}

/* 右侧表单 */
.login-form-wrapper {
  flex: 1;
  padding: 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  text-align: center;
  margin-bottom: 35px;
}

.form-header h3 {
  font-size: 1.8rem;
  font-weight: 800;
  color: #2d3748;
  margin-bottom: 8px;
  font-family: 'Georgia', serif;
}

.night-mode .form-header h3 {
  color: #e2e8f0;
}

.form-header p {
  color: #6a7c6a;
  font-size: 0.95rem;
}

.night-mode .form-header p {
  color: #9ab89a;
}

/* 表单 */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  font-weight: 600;
  color: #2d3748;
}

.night-mode .input-label {
  color: #e2e8f0;
}

.input-label i {
  color: #8FBC8F;
  font-size: 1rem;
}

.input-wrapper {
  position: relative;
}

.form-input {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid rgba(139, 188, 143, 0.3);
  border-radius: 12px;
  font-size: 0.95rem;
  color: #2d3748;
  background: rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
  outline: none;
}

.night-mode .form-input {
  background: rgba(40, 45, 55, 0.5);
  border-color: rgba(74, 124, 90, 0.3);
  color: #e2e8f0;
}

.form-input:focus {
  border-color: #8FBC8F;
  box-shadow: 0 0 0 4px rgba(139, 188, 143, 0.15);
}

.toggle-password {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #8a9a8a;
  cursor: pointer;
  font-size: 1.1rem;
  transition: color 0.3s ease;
}

.toggle-password:hover {
  color: #8FBC8F;
}

/* 表单选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  position: relative;
}

.remember-me input {
  position: absolute;
  opacity: 0;
  cursor: pointer;
}

.checkmark {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(139, 188, 143, 0.3);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.night-mode .checkmark {
  border-color: rgba(74, 124, 90, 0.3);
}

.remember-me input:checked + .checkmark {
  background: #8FBC8F;
  border-color: #8FBC8F;
}

.checkmark::after {
  content: '';
  width: 6px;
  height: 10px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.remember-me input:checked + .checkmark::after {
  opacity: 1;
}

.label-text {
  font-size: 0.9rem;
  color: #6a7c6a;
}

.forgot-link {
  font-size: 0.9rem;
  color: #8FBC8F;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
}

.forgot-link:hover {
  color: #7CB87C;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  padding: 16px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #8FBC8F 0%, #7CB87C 100%);
  color: white;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 4px 15px rgba(124, 184, 124, 0.4);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(124, 184, 124, 0.5);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid transparent;
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 分隔线 */
.divider {
  display: flex;
  align-items: center;
  margin: 30px 0;
  color: #8a9a8a;
  font-size: 0.85rem;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: rgba(139, 188, 143, 0.2);
}

.night-mode .divider::before,
.night-mode .divider::after {
  background: rgba(74, 124, 90, 0.2);
}

.divider span {
  padding: 0 16px;
}

/* 社交登录 */
.social-login {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 30px;
}

.social-btn {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  border: 2px solid rgba(139, 188, 143, 0.3);
  background: rgba(255, 255, 255, 0.8);
  color: #6a7c6a;
  font-size: 1.3rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.night-mode .social-btn {
  background: rgba(40, 45, 55, 0.5);
  border-color: rgba(74, 124, 90, 0.3);
  color: #9ab89a;
}

.social-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.social-btn.wechat:hover {
  border-color: #07c160;
  color: #07c160;
}

.social-btn.qq:hover {
  border-color: #12b7f5;
  color: #12b7f5;
}

.social-btn.github:hover {
  border-color: #333;
  color: #333;
}

/* 注册链接 */
.register-link {
  text-align: center;
  color: #6a7c6a;
  font-size: 0.95rem;
}

.register-link .link {
  color: #8FBC8F;
  text-decoration: none;
  font-weight: 700;
  margin-left: 6px;
  transition: color 0.3s ease;
}

.register-link .link:hover {
  color: #7CB87C;
}

/* 页脚 */
.login-footer {
  position: fixed;
  bottom: 20px;
  left: 0;
  right: 0;
  text-align: center;
  color: #8a9a8a;
  font-size: 0.85rem;
  z-index: 10;
}

.night-mode .login-footer {
  color: rgba(255, 255, 255, 0.4);
}

/* 响应式 */
@media (max-width: 768px) {
  .login-decoration {
    display: none;
  }

  .login-form-wrapper {
    padding: 30px;
  }
}
</style>
