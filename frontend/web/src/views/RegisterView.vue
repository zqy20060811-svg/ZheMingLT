<template>
  <div class="manor-register" :class="{ 'night-mode': isDarkMode }">
    <!-- 庄园背景 -->
    <ManorBackground :isDarkMode="isDarkMode" />

    <!-- 主题切换 -->
    <button class="theme-toggle" @click="toggleTheme">
      <i :class="isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-stars-fill'"></i>
    </button>

    <!-- 返回按钮 -->
    <button class="back-btn" @click="goBack">
      <i class="bi bi-arrow-left"></i>
    </button>

    <!-- 注册容器 -->
    <div class="register-container">
      <!-- 左侧装饰 -->
      <div class="register-decoration">
        <div class="decoration-content">
          <div class="logo-icon">
            <i class="bi bi-house-heart-fill"></i>
          </div>
          <h2 class="decoration-title">加入庄园</h2>
          <p class="decoration-subtitle">开启你的庄园之旅</p>
          <div class="feature-list">
            <div class="feature-item">
              <i class="bi bi-flower1"></i>
              <span>分享你的故事与想法</span>
            </div>
            <div class="feature-item">
              <i class="bi bi-tree-fill"></i>
              <span>结识志同道合的朋友</span>
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
      <div class="register-form-wrapper">
        <div class="form-header">
          <h3>创建账号</h3>
          <p>填写信息，加入我们的庄园</p>
        </div>

        <form @submit.prevent="handleRegister" class="register-form">
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
                placeholder="请输入密码（至少6位）"
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

          <div class="input-group">
            <label class="input-label">
              <i class="bi bi-shield-lock-fill"></i>
              确认密码
            </label>
            <div class="input-wrapper">
              <input
                :type="showConfirmPassword ? 'text' : 'password'"
                v-model="form.confirmPassword"
                placeholder="请再次输入密码"
                required
                class="form-input"
              />
              <button
                type="button"
                class="toggle-password"
                @click="showConfirmPassword = !showConfirmPassword"
              >
                <i :class="showConfirmPassword ? 'bi bi-eye-slash-fill' : 'bi bi-eye-fill'"></i>
              </button>
            </div>
          </div>

          <div class="input-row">
            <div class="input-group half">
              <label class="input-label">
                <i class="bi bi-envelope-fill"></i>
                邮箱
              </label>
              <div class="input-wrapper">
                <input
                  type="email"
                  v-model="form.email"
                  placeholder="选填"
                  class="form-input"
                />
              </div>
            </div>

            <div class="input-group half">
              <label class="input-label">
                <i class="bi bi-phone-fill"></i>
                手机号
              </label>
              <div class="input-wrapper">
                <input
                  type="tel"
                  v-model="form.phone"
                  placeholder="选填"
                  class="form-input"
                />
              </div>
            </div>
          </div>

          <button type="submit" class="submit-btn" :disabled="loading">
            <span v-if="loading" class="btn-spinner"></span>
            <span v-else class="btn-content">
              <span>立即注册</span>
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

        <div class="login-link">
          已有账号?
          <router-link to="/login" class="link">立即登录</router-link>
        </div>
      </div>
    </div>

    <!-- 页脚 -->
    <footer class="register-footer">
      <p>&copy; 2024 择明庄园. All rights reserved.</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { post } from '@/utils/request'
import ManorBackground from '@/components/ManorBackground.vue'

const router = useRouter()
const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const isDarkMode = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: ''
})

// 主题切换
function toggleTheme() {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
}

// 返回
function goBack() {
  router.back()
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
})
</script>

<style scoped>
.manor-register {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  padding: 20px;
  overflow: hidden;
}

/* 主题切换按钮 */
.theme-toggle {
  position: fixed;
  top: 20px;
  right: 20px;
  width: 45px;
  height: 45px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  color: #5a4a3a;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  transition: all 0.3s ease;
  z-index: 100;
}

.theme-toggle:hover {
  transform: scale(1.1) rotate(15deg);
}

.night-mode .theme-toggle {
  background: rgba(40, 45, 60, 0.9);
  color: #d4c5b0;
}

/* 返回按钮 */
.back-btn {
  position: fixed;
  top: 20px;
  left: 20px;
  width: 45px;
  height: 45px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  color: #5a4a3a;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  transition: all 0.3s ease;
  z-index: 100;
}

.back-btn:hover {
  transform: translateX(-3px);
}

.night-mode .back-btn {
  background: rgba(40, 45, 60, 0.9);
  color: #d4c5b0;
}

/* 注册容器 */
.register-container {
  display: flex;
  width: 100%;
  max-width: 900px;
  min-height: 600px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(90, 74, 58, 0.15),
              0 0 0 1px rgba(139, 115, 85, 0.1);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.night-mode .register-container {
  background: rgba(35, 40, 55, 0.95);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4),
              0 0 0 1px rgba(139, 115, 85, 0.2);
}

/* 左侧装饰 */
.register-decoration {
  flex: 0 0 45%;
  background: linear-gradient(135deg, #8b7355 0%, #a0826d 50%, #6b5b4f 100%);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  overflow: hidden;
}

.night-mode .register-decoration {
  background: linear-gradient(135deg, #2a2520 0%, #3a3530 50%, #1a1815 100%);
}

.decoration-content {
  position: relative;
  z-index: 2;
  text-align: center;
  color: white;
}

.logo-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.decoration-title {
  font-size: 2rem;
  font-weight: 600;
  margin-bottom: 10px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.decoration-subtitle {
  font-size: 1rem;
  opacity: 0.9;
  margin-bottom: 30px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 0.95rem;
  opacity: 0.9;
}

.feature-item i {
  font-size: 1.2rem;
}

.decoration-shapes {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.shape-1 {
  width: 200px;
  height: 200px;
  top: -50px;
  right: -50px;
  animation: pulse 4s ease-in-out infinite;
}

.shape-2 {
  width: 150px;
  height: 150px;
  bottom: 50px;
  left: -30px;
  animation: pulse 4s ease-in-out infinite 1s;
}

.shape-3 {
  width: 100px;
  height: 100px;
  bottom: -20px;
  right: 30px;
  animation: pulse 4s ease-in-out infinite 2s;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.1; }
  50% { transform: scale(1.1); opacity: 0.2; }
}

/* 右侧表单 */
.register-form-wrapper {
  flex: 1;
  padding: 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  text-align: center;
  margin-bottom: 30px;
}

.form-header h3 {
  font-size: 1.8rem;
  color: #5a4a3a;
  margin-bottom: 8px;
  font-weight: 600;
}

.night-mode .form-header h3 {
  color: #d4c5b0;
}

.form-header p {
  color: #8b7355;
  font-size: 0.95rem;
}

.night-mode .form-header p {
  color: #a09080;
}

/* 表单样式 */
.register-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.input-group.half {
  flex: 1;
}

.input-row {
  display: flex;
  gap: 15px;
}

.input-label {
  font-size: 0.85rem;
  color: #6b5b4f;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
}

.night-mode .input-label {
  color: #b0a090;
}

.input-label i {
  color: #8b7355;
}

.input-wrapper {
  position: relative;
}

.form-input {
  width: 100%;
  padding: 12px 15px;
  border: 2px solid rgba(139, 115, 85, 0.2);
  border-radius: 12px;
  font-size: 0.95rem;
  background: rgba(255, 255, 255, 0.8);
  color: #5a4a3a;
  transition: all 0.3s ease;
}

.night-mode .form-input {
  background: rgba(30, 35, 45, 0.8);
  border-color: rgba(139, 115, 85, 0.3);
  color: #d4c5b0;
}

.form-input:focus {
  outline: none;
  border-color: #8b7355;
  background: rgba(255, 255, 255, 1);
  box-shadow: 0 0 0 4px rgba(139, 115, 85, 0.1);
}

.night-mode .form-input:focus {
  background: rgba(40, 45, 55, 0.9);
  box-shadow: 0 0 0 4px rgba(139, 115, 85, 0.2);
}

.form-input::placeholder {
  color: #a09080;
}

.toggle-password {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #8b7355;
  cursor: pointer;
  padding: 5px;
  font-size: 1rem;
  opacity: 0.7;
  transition: opacity 0.2s;
}

.toggle-password:hover {
  opacity: 1;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  padding: 14px;
  margin-top: 10px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #8b7355 0%, #a0826d 100%);
  color: white;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(139, 115, 85, 0.3);
}

.night-mode .submit-btn {
  background: linear-gradient(135deg, #6b5b4f 0%, #5a4a3a 100%);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(139, 115, 85, 0.4);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
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
  margin: 25px 0;
  color: #a09080;
  font-size: 0.85rem;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(139, 115, 85, 0.3), transparent);
}

.divider span {
  padding: 0 15px;
}

/* 社交登录 */
.social-login {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-bottom: 25px;
}

.social-btn {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  border: 2px solid rgba(139, 115, 85, 0.2);
  background: rgba(255, 255, 255, 0.8);
  color: #8b7355;
  font-size: 1.2rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.night-mode .social-btn {
  background: rgba(40, 45, 55, 0.8);
  border-color: rgba(139, 115, 85, 0.3);
  color: #b0a090;
}

.social-btn:hover {
  transform: translateY(-3px);
  border-color: #8b7355;
  box-shadow: 0 4px 12px rgba(139, 115, 85, 0.2);
}

.social-btn.wechat:hover {
  color: #07c160;
  border-color: #07c160;
}

.social-btn.qq:hover {
  color: #12b7f5;
  border-color: #12b7f5;
}

.social-btn.github:hover {
  color: #333;
  border-color: #333;
}

.night-mode .social-btn.github:hover {
  color: #fff;
  border-color: #fff;
}

/* 登录链接 */
.login-link {
  text-align: center;
  color: #8b7355;
  font-size: 0.9rem;
}

.night-mode .login-link {
  color: #a09080;
}

.login-link .link {
  color: #8b7355;
  font-weight: 600;
  text-decoration: none;
  margin-left: 5px;
  position: relative;
}

.night-mode .login-link .link {
  color: #c4b5a0;
}

.login-link .link::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #8b7355, #a0826d);
  transition: width 0.3s ease;
}

.login-link .link:hover::after {
  width: 100%;
}

/* 页脚 */
.register-footer {
  position: fixed;
  bottom: 20px;
  left: 0;
  right: 0;
  text-align: center;
  color: #8b7355;
  font-size: 0.85rem;
  opacity: 0.7;
  z-index: 1;
}

.night-mode .register-footer {
  color: #a09080;
}

/* 响应式 */
@media (max-width: 768px) {
  .register-container {
    flex-direction: column;
    max-width: 400px;
  }

  .register-decoration {
    flex: none;
    padding: 30px;
    min-height: 200px;
  }

  .logo-icon {
    font-size: 3rem;
  }

  .decoration-title {
    font-size: 1.5rem;
  }

  .feature-list {
    display: none;
  }

  .register-form-wrapper {
    padding: 30px;
  }

  .input-row {
    flex-direction: column;
    gap: 18px;
  }
}
</style>
