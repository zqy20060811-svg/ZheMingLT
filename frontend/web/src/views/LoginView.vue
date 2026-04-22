<template>
  <div class="login-page" :class="{ 'dark-mode': isDarkMode }">
    <!-- 动态背景 -->
    <div class="animated-bg">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
    </div>

    <!-- 粒子效果 -->
    <div class="particles" ref="particlesRef"></div>

    <!-- 主题切换 -->
    <button class="theme-toggle" @click="toggleTheme">
      <i :class="isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-fill'"></i>
    </button>

    <!-- 登录容器 -->
    <div class="login-container">
      <!-- 左侧装饰 -->
      <div class="login-decoration">
        <div class="decoration-content">
          <div class="logo-icon">
            <i class="bi bi-lightbulb-fill"></i>
          </div>
          <h2 class="decoration-title">择明论坛</h2>
          <p class="decoration-subtitle">智慧之光，照亮前行之路</p>
          <div class="feature-list">
            <div class="feature-item">
              <i class="bi bi-check-circle-fill"></i>
              <span>探索知识的海洋</span>
            </div>
            <div class="feature-item">
              <i class="bi bi-check-circle-fill"></i>
              <span>分享你的见解</span>
            </div>
            <div class="feature-item">
              <i class="bi bi-check-circle-fill"></i>
              <span>与志同道合者交流</span>
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
          <p>登录你的账号，开启探索之旅</p>
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
              <div class="input-focus-border"></div>
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
              <div class="input-focus-border"></div>
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
      <p>&copy; 2024 择明论坛. All rights reserved.</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { post, setTokens } from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const showPassword = ref(false)
const isDarkMode = ref(false)
const particlesRef = ref(null)

const form = reactive({
  username: '',
  password: '',
  rememberMe: false
})

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
    particle.style.animationDuration = Math.random() * 3 + 3 + 's'
    particlesRef.value.appendChild(particle)
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
  generateParticles()
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

.dark-mode {
  background: linear-gradient(135deg, #0f0f1e 0%, #1a1a2e 100%);
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
  filter: blur(100px);
  opacity: 0.4;
  animation: float 20s infinite ease-in-out;
}

.orb-1 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  top: -200px;
  left: -200px;
  animation-delay: 0s;
}

.orb-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  bottom: -150px;
  right: -150px;
  animation-delay: -7s;
}

.orb-3 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  top: 50%;
  left: 50%;
  animation-delay: -14s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(50px, -50px) scale(1.1); }
  50% { transform: translate(-30px, 30px) scale(0.9); }
  75% { transform: translate(30px, 50px) scale(1.05); }
}

.dark-mode .gradient-orb {
  opacity: 0.15;
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
  animation: particle-float 5s infinite ease-in-out;
}

@keyframes particle-float {
  0%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.3;
  }
  50% {
    transform: translateY(-100px) scale(0.5);
    opacity: 0;
  }
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
  color: #4a5568;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  transition: all 0.3s ease;
  z-index: 100;
}

.dark-mode .theme-toggle {
  background: rgba(45, 45, 68, 0.9);
  color: #e2e8f0;
}

.theme-toggle:hover {
  transform: scale(1.1) rotate(15deg);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

/* 登录容器 */
.login-container {
  display: flex;
  background: white;
  border-radius: 30px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  width: 100%;
  max-width: 900px;
  min-height: 600px;
  position: relative;
  z-index: 10;
}

.dark-mode .login-container {
  background: rgba(30, 30, 46, 0.95);
}

/* 左侧装饰 */
.login-decoration {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  color: #1a202c;
  margin-bottom: 8px;
}

.dark-mode .form-header h3 {
  color: #f7fafc;
}

.form-header p {
  color: #718096;
  font-size: 0.95rem;
}

.dark-mode .form-header p {
  color: #a0aec0;
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

.dark-mode .input-label {
  color: #e2e8f0;
}

.input-label i {
  color: #667eea;
  font-size: 1rem;
}

.input-wrapper {
  position: relative;
}

.form-input {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 0.95rem;
  color: #2d3748;
  background: white;
  transition: all 0.3s ease;
  outline: none;
}

.dark-mode .form-input {
  background: rgba(45, 45, 68, 0.5);
  border-color: rgba(255, 255, 255, 0.1);
  color: #e2e8f0;
}

.form-input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.toggle-password {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #a0aec0;
  cursor: pointer;
  font-size: 1.1rem;
  transition: color 0.3s ease;
}

.toggle-password:hover {
  color: #667eea;
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
  border: 2px solid #e2e8f0;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.dark-mode .checkmark {
  border-color: rgba(255, 255, 255, 0.2);
}

.remember-me input:checked + .checkmark {
  background: #667eea;
  border-color: #667eea;
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
  color: #718096;
}

.forgot-link {
  font-size: 0.9rem;
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
}

.forgot-link:hover {
  color: #764ba2;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  padding: 16px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.5);
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
  color: #a0aec0;
  font-size: 0.85rem;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #e2e8f0;
}

.dark-mode .divider::before,
.dark-mode .divider::after {
  background: rgba(255, 255, 255, 0.1);
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
  border: 2px solid #e2e8f0;
  background: white;
  color: #718096;
  font-size: 1.3rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dark-mode .social-btn {
  background: rgba(45, 45, 68, 0.5);
  border-color: rgba(255, 255, 255, 0.1);
  color: #a0aec0;
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
  color: #718096;
  font-size: 0.95rem;
}

.register-link .link {
  color: #667eea;
  text-decoration: none;
  font-weight: 700;
  margin-left: 6px;
  transition: color 0.3s ease;
}

.register-link .link:hover {
  color: #764ba2;
}

/* 页脚 */
.login-footer {
  position: fixed;
  bottom: 20px;
  left: 0;
  right: 0;
  text-align: center;
  color: #a0aec0;
  font-size: 0.85rem;
  z-index: 10;
}

.dark-mode .login-footer {
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
