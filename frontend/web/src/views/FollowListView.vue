<template>
  <div class="follow-list-page" :class="{ 'dark-mode': isDarkMode }">
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
        <span class="nav-title">{{ pageTitle }}</span>
      </div>
      <div class="nav-actions">
        <button class="btn-icon theme-btn" @click="toggleTheme">
          <i :class="isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-fill'"></i>
        </button>
      </div>
    </nav>

    <!-- 标签切换 -->
    <div class="tab-bar">
      <button
        :class="['tab-btn', { active: activeTab === 'following' }]"
        @click="switchTab('following')"
      >
        关注 {{ stats.following > 0 ? `(${stats.following})` : '' }}
      </button>
      <button
        :class="['tab-btn', { active: activeTab === 'followers' }]"
        @click="switchTab('followers')"
      >
        粉丝 {{ stats.followers > 0 ? `(${stats.followers})` : '' }}
      </button>
    </div>

    <!-- 用户列表 -->
    <div class="user-list">
      <!-- 加载中 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="userList.length === 0" class="empty-state">
        <div class="empty-icon">
          <i :class="activeTab === 'following' ? 'bi bi-people' : 'bi bi-person-heart'"></i>
        </div>
        <h3>{{ emptyTitle }}</h3>
        <p>{{ emptyDesc }}</p>
      </div>

      <!-- 用户卡片列表 -->
      <div v-else class="list-content">
        <div
          v-for="user in userList"
          :key="user.id"
          class="user-card"
          @click="goToUserProfile(user.followerId || user.followingId)"
        >
          <div class="user-avatar">
            <img :src="user.followerAvatar || user.followingAvatar || defaultAvatar" />
            <div v-if="user.mutualFollow" class="mutual-badge" title="互相关注">
              <i class="bi bi-arrow-left-right"></i>
            </div>
          </div>

          <div class="user-info">
            <h4 class="username">{{ user.followerName || user.followingName }}</h4>
            <p class="follow-time">{{ activeTab === 'following' ? '关注于' : '关注你于' }} {{ formatTime(user.createdAt) }}</p>
          </div>

          <div class="user-actions">
            <button
              v-if="isCurrentUser && activeTab === 'following'"
              :class="['follow-btn', { following: true }]"
              @click.stop="unfollow(user.followingId)"
            >
              <i class="bi bi-check2"></i>
              <span>已关注</span>
            </button>
            <button
              v-else-if="isCurrentUser && activeTab === 'followers' && !user.mutualFollow"
              class="follow-btn"
              @click.stop="follow(user.followerId)"
            >
              <i class="bi bi-plus"></i>
              <span>回关</span>
            </button>
            <button
              v-else-if="isCurrentUser && activeTab === 'followers' && user.mutualFollow"
              :class="['follow-btn', { following: true }]"
              @click.stop="unfollow(user.followerId)"
            >
              <i class="bi bi-check2"></i>
              <span>互相关注</span>
            </button>
          </div>
        </div>

        <!-- 加载更多 -->
        <div v-if="hasMore" class="load-more">
          <button class="load-btn" @click="loadMore" :disabled="loadingMore">
            <span v-if="loadingMore">加载中...</span>
            <span v-else>加载更多</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 底部导航 -->
    <BottomNav />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { get, post, del } from '@/utils/request'
import BottomNav from '@/components/BottomNav.vue'

const router = useRouter()
const route = useRoute()

// 状态
const isDarkMode = ref(false)
const loading = ref(false)
const loadingMore = ref(false)
const activeTab = ref('following')
const userList = ref([])
const page = ref(1)
const size = ref(10)
const hasMore = ref(false)
const stats = ref({ following: 0, followers: 0 })
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

// 计算属性
const userId = computed(() => route.params.userId)
const isCurrentUser = computed(() => {
  const currentUserId = localStorage.getItem('userId')
  return currentUserId && currentUserId === userId.value
})

const pageTitle = computed(() => {
  return activeTab.value === 'following' ? '关注列表' : '粉丝列表'
})

const emptyTitle = computed(() => {
  return activeTab.value === 'following' ? '还没有关注任何人' : '还没有粉丝'
})

const emptyDesc = computed(() => {
  return activeTab.value === 'following'
    ? '去发现更多有趣的用户吧'
    : '多发优质内容，吸引更多粉丝'
})

// 初始化
onMounted(() => {
  isDarkMode.value = localStorage.getItem('darkMode') === 'true'
  // 从URL参数确定初始标签
  const path = route.path
  if (path.includes('/followers')) {
    activeTab.value = 'followers'
  } else {
    activeTab.value = 'following'
  }
  loadStats()
  loadUserList()
})

// 监听标签变化
watch(activeTab, () => {
  page.value = 1
  userList.value = []
  loadUserList()
})

// 加载统计
async function loadStats() {
  try {
    const res = await get(`/follows/${userId.value}/stats`)
    if (res.code === 200 && res.data) {
      stats.value = {
        following: res.data.followingCount || 0,
        followers: res.data.followerCount || 0
      }
    }
  } catch (error) {
    console.error('加载统计失败', error)
  }
}

// 加载用户列表
async function loadUserList() {
  loading.value = true
  page.value = 1

  try {
    const endpoint = activeTab.value === 'following'
      ? `/follows/${userId.value}/following`
      : `/follows/${userId.value}/followers`

    const res = await get(endpoint, { page: page.value, size: size.value })
    if (res.code === 200 && res.data) {
      userList.value = res.data.list || []
      hasMore.value = page.value < res.data.pages
    }
  } catch (error) {
    console.error('加载列表失败', error)
  } finally {
    loading.value = false
  }
}

// 加载更多
async function loadMore() {
  if (loadingMore.value) return

  loadingMore.value = true
  page.value++

  try {
    const endpoint = activeTab.value === 'following'
      ? `/follows/${userId.value}/following`
      : `/follows/${userId.value}/followers`

    const res = await get(endpoint, { page: page.value, size: size.value })
    if (res.code === 200 && res.data) {
      userList.value.push(...(res.data.list || []))
      hasMore.value = page.value < res.data.pages
    }
  } catch (error) {
    console.error('加载更多失败', error)
  } finally {
    loadingMore.value = false
  }
}

// 切换标签
function switchTab(tab) {
  activeTab.value = tab
  // 更新URL
  const newPath = tab === 'following'
    ? `/follows/${userId.value}/following`
    : `/follows/${userId.value}/followers`
  router.replace(newPath)
}

// 关注用户
async function follow(targetUserId) {
  try {
    const res = await post(`/follows/${targetUserId}`)
    if (res.code === 200) {
      // 更新列表中的互相关注状态
      const user = userList.value.find(u => u.followerId === targetUserId)
      if (user) {
        user.mutualFollow = true
      }
      // 更新统计
      stats.value.following++
    }
  } catch (error) {
    console.error('关注失败', error)
    alert('关注失败，请重试')
  }
}

// 取消关注
async function unfollow(targetUserId) {
  try {
    const res = await del(`/follows/${targetUserId}`)
    if (res.code === 200) {
      if (activeTab.value === 'following') {
        // 从列表中移除
        userList.value = userList.value.filter(u => u.followingId !== targetUserId)
        stats.value.following--
      } else {
        // 更新互相关注状态
        const user = userList.value.find(u => u.followerId === targetUserId)
        if (user) {
          user.mutualFollow = false
        }
        stats.value.following--
      }
    }
  } catch (error) {
    console.error('取消关注失败', error)
    alert('取消关注失败，请重试')
  }
}

// 跳转到用户主页
function goToUserProfile(targetUserId) {
  router.push(`/user/${targetUserId}`)
}

// 返回上一页
function goBack() {
  router.back()
}

// 切换主题
function toggleTheme() {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
}

// 格式化时间
function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  if (diff < 86400000) return '今天'
  if (diff < 172800000) return '昨天'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return date.toLocaleDateString()
}
</script>

<style scoped>
.follow-list-page {
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

/* 标签栏 */
.tab-bar {
  position: relative;
  z-index: 1;
  display: flex;
  padding: 20px;
  gap: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .tab-bar {
  border-bottom-color: rgba(255, 255, 255, 0.05);
}

.tab-btn {
  flex: 1;
  padding: 12px 20px;
  border: none;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.5);
  color: #666;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.dark-mode .tab-btn {
  background: rgba(40, 40, 60, 0.5);
  color: #a0a0c0;
}

.tab-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

/* 用户列表 */
.user-list {
  position: relative;
  z-index: 1;
  padding: 20px;
}

/* 加载和空状态 */
.loading-state,
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(102, 126, 234, 0.2);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-icon {
  font-size: 64px;
  color: #ccc;
  margin-bottom: 20px;
}

.empty-state h3 {
  font-size: 20px;
  color: #666;
  margin-bottom: 10px;
}

.dark-mode .empty-state h3 {
  color: #a0a0c0;
}

.empty-state p {
  color: #999;
}

/* 用户卡片 */
.user-card {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  margin-bottom: 15px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .user-card {
  background: rgba(40, 40, 60, 0.9);
  border-color: rgba(255, 255, 255, 0.05);
}

.user-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.user-avatar {
  position: relative;
  width: 56px;
  height: 56px;
  flex-shrink: 0;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(102, 126, 234, 0.2);
}

.mutual-badge {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 20px;
  height: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 10px;
  border: 2px solid white;
}

.dark-mode .mutual-badge {
  border-color: #1a1a2e;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.username {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.dark-mode .username {
  color: #e0e0e0;
}

.follow-time {
  font-size: 13px;
  color: #999;
}

.user-actions {
  flex-shrink: 0;
}

.follow-btn {
  padding: 8px 16px;
  border: 1px solid #667eea;
  border-radius: 20px;
  background: transparent;
  color: #667eea;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 4px;
}

.follow-btn:hover {
  background: rgba(102, 126, 234, 0.1);
}

.follow-btn.following {
  background: rgba(102, 126, 234, 0.1);
  border-color: transparent;
  color: #666;
}

.dark-mode .follow-btn.following {
  color: #a0a0c0;
}

.follow-btn.following:hover {
  background: rgba(255, 107, 107, 0.1);
  color: #ff6b6b;
}

/* 加载更多 */
.load-more {
  text-align: center;
  padding: 20px;
}

.load-btn {
  padding: 12px 30px;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 25px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.load-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
}

.load-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 响应式 */
@media (max-width: 768px) {
  .tab-bar {
    padding: 15px;
  }

  .user-card {
    padding: 15px;
  }

  .user-avatar {
    width: 48px;
    height: 48px;
  }
}
</style>
