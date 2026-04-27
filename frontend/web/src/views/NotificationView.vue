<template>
  <div class="notification-page" :class="{ 'dark-mode': isDarkMode }">
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
        <span class="nav-title">消息中心</span>
      </div>
      <div class="nav-actions">
        <button v-if="unreadCount > 0" class="btn-text" @click="markAllAsRead">
          全部已读
        </button>
        <button class="btn-icon theme-btn" @click="toggleTheme">
          <i :class="isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-fill'"></i>
        </button>
      </div>
    </nav>

    <!-- 消息统计 -->
    <div class="stats-bar">
      <div class="stat-item">
        <div class="stat-icon unread">
          <i class="bi bi-bell-fill"></i>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ unreadCount }}</span>
          <span class="stat-label">未读消息</span>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon total">
          <i class="bi bi-envelope-fill"></i>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ totalCount }}</span>
          <span class="stat-label">全部消息</span>
        </div>
      </div>
    </div>

    <!-- 消息类型筛选 -->
    <div class="filter-tabs">
      <button
        :class="['tab-btn', { active: activeTab === 'all' }]"
        @click="changeTab('all')"
      >
        全部
      </button>
      <button
        :class="['tab-btn', { active: activeTab === 'COMMENT' }]"
        @click="changeTab('COMMENT')"
      >
        <i class="bi bi-chat-dots"></i> 评论
      </button>
      <button
        :class="['tab-btn', { active: activeTab === 'LIKE' }]"
        @click="changeTab('LIKE')"
      >
        <i class="bi bi-heart"></i> 点赞
      </button>
      <button
        :class="['tab-btn', { active: activeTab === 'SYSTEM' }]"
        @click="changeTab('SYSTEM')"
      >
        <i class="bi bi-info-circle"></i> 系统
      </button>
    </div>

    <!-- 消息列表 -->
    <div class="notification-list">
      <!-- 加载中 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="filteredNotifications.length === 0" class="empty-state">
        <div class="empty-icon">
          <i class="bi bi-bell-slash"></i>
        </div>
        <h3>暂无消息</h3>
        <p>{{ emptyMessage }}</p>
      </div>

      <!-- 消息卡片 -->
      <div
        v-for="notification in filteredNotifications"
        :key="notification.id"
        :class="['notification-card', { unread: !notification.isRead }]"
        @click="handleNotificationClick(notification)"
      >
        <div class="card-left">
          <div :class="['type-icon', notification.type.toLowerCase()]">
            <i :class="getTypeIcon(notification.type)"></i>
          </div>
          <div v-if="!notification.isRead" class="unread-dot"></div>
        </div>

        <div class="card-content">
          <div class="card-header">
            <span class="notification-title">{{ notification.title }}</span>
            <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
          </div>
          <p class="notification-content">{{ notification.content }}</p>
          <div v-if="notification.fromUserName" class="from-user">
            <i class="bi bi-person-circle"></i>
            <span>{{ notification.fromUserName }}</span>
          </div>
        </div>

        <div class="card-actions">
          <button
            v-if="!notification.isRead"
            class="action-btn read"
            @click.stop="markAsRead(notification.id)"
            title="标记已读"
          >
            <i class="bi bi-check-circle"></i>
          </button>
          <button
            class="action-btn delete"
            @click.stop="deleteNotification(notification.id)"
            title="删除"
          >
            <i class="bi bi-trash"></i>
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

    <!-- 底部导航 -->
    <BottomNav />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get, put, del } from '@/utils/request'
import BottomNav from '@/components/BottomNav.vue'

const router = useRouter()

// 状态
const isDarkMode = ref(false)
const notifications = ref([])
const unreadCount = ref(0)
const totalCount = ref(0)
const loading = ref(false)
const loadingMore = ref(false)
const activeTab = ref('all')
const page = ref(1)
const size = ref(10)
const hasMore = ref(false)

// 计算属性
const filteredNotifications = computed(() => {
  if (activeTab.value === 'all') {
    return notifications.value
  }
  return notifications.value.filter(n => n.type === activeTab.value)
})

const emptyMessage = computed(() => {
  switch (activeTab.value) {
    case 'COMMENT':
      return '暂无评论消息'
    case 'LIKE':
      return '暂无点赞消息'
    case 'SYSTEM':
      return '暂无系统消息'
    default:
      return '暂无消息通知'
  }
})

// 初始化
onMounted(() => {
  isDarkMode.value = localStorage.getItem('darkMode') === 'true'
  loadNotifications()
  loadUnreadCount()
})

// 加载通知列表
async function loadNotifications() {
  loading.value = true
  page.value = 1

  try {
    const res = await get('/notifications', { page: page.value, size: size.value })
    if (res.code === 200 && res.data) {
      notifications.value = res.data.list || []
      totalCount.value = res.data.total || 0
      hasMore.value = page.value < res.data.pages
    }
  } catch (error) {
    console.error('加载消息失败', error)
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
    const res = await get('/notifications', { page: page.value, size: size.value })
    if (res.code === 200 && res.data) {
      notifications.value.push(...(res.data.list || []))
      hasMore.value = page.value < res.data.pages
    }
  } catch (error) {
    console.error('加载更多失败', error)
  } finally {
    loadingMore.value = false
  }
}

// 加载未读数量
async function loadUnreadCount() {
  try {
    const res = await get('/notifications/unread-count')
    if (res.code === 200) {
      unreadCount.value = res.data || 0
    }
  } catch (error) {
    console.error('加载未读数量失败', error)
  }
}

// 标记已读
async function markAsRead(id) {
  try {
    const res = await put(`/notifications/${id}/read`)
    if (res.code === 200) {
      const notification = notifications.value.find(n => n.id === id)
      if (notification) {
        notification.isRead = true
      }
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
  } catch (error) {
    console.error('标记已读失败', error)
  }
}

// 标记全部已读
async function markAllAsRead() {
  try {
    const res = await put('/notifications/read-all')
    if (res.code === 200) {
      notifications.value.forEach(n => n.isRead = true)
      unreadCount.value = 0
    }
  } catch (error) {
    console.error('标记全部已读失败', error)
  }
}

// 删除通知
async function deleteNotification(id) {
  if (!confirm('确定要删除这条消息吗？')) return

  try {
    const res = await del(`/notifications/${id}`)
    if (res.code === 200) {
      const index = notifications.value.findIndex(n => n.id === id)
      if (index > -1) {
        const notification = notifications.value[index]
        notifications.value.splice(index, 1)
        totalCount.value--
        if (!notification.isRead) {
          unreadCount.value = Math.max(0, unreadCount.value - 1)
        }
      }
    }
  } catch (error) {
    console.error('删除消息失败', error)
  }
}

// 处理通知点击
function handleNotificationClick(notification) {
  // 标记已读
  if (!notification.isRead) {
    markAsRead(notification.id)
  }

  // 跳转到相关页面
  if (notification.postId) {
    router.push(`/post/${notification.postId}`)
  }
}

// 切换标签
function changeTab(tab) {
  activeTab.value = tab
}

// 获取类型图标
function getTypeIcon(type) {
  switch (type) {
    case 'COMMENT':
      return 'bi bi-chat-dots-fill'
    case 'LIKE':
      return 'bi bi-heart-fill'
    case 'SYSTEM':
      return 'bi bi-info-circle-fill'
    default:
      return 'bi bi-bell-fill'
  }
}

// 格式化时间
function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return date.toLocaleDateString()
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
</script>

<style scoped>
.notification-page {
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
  align-items: center;
}

.btn-text {
  padding: 8px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-text:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
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

/* 统计栏 */
.stats-bar {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 20px;
  padding: 20px;
  margin: 20px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.dark-mode .stats-bar {
  background: rgba(40, 40, 60, 0.9);
}

.stat-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-icon.unread {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  color: white;
}

.stat-icon.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #333;
}

.dark-mode .stat-value {
  color: #e0e0e0;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

/* 筛选标签 */
.filter-tabs {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 10px;
  padding: 0 20px 20px;
  overflow-x: auto;
}

.tab-btn {
  padding: 10px 20px;
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 25px;
  background: rgba(255, 255, 255, 0.5);
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.dark-mode .tab-btn {
  background: rgba(40, 40, 60, 0.5);
  color: #a0a0c0;
  border-color: rgba(255, 255, 255, 0.1);
}

.tab-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
}

/* 消息列表 */
.notification-list {
  position: relative;
  z-index: 1;
  padding: 0 20px;
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

/* 消息卡片 */
.notification-card {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 20px;
  margin-bottom: 15px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .notification-card {
  background: rgba(40, 40, 60, 0.9);
  border-color: rgba(255, 255, 255, 0.05);
}

.notification-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.notification-card.unread {
  background: rgba(102, 126, 234, 0.05);
  border-left: 3px solid #667eea;
}

.dark-mode .notification-card.unread {
  background: rgba(102, 126, 234, 0.1);
}

.card-left {
  position: relative;
}

.type-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.type-icon.comment {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.type-icon.like {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  color: white;
}

.type-icon.system {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.unread-dot {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 12px;
  height: 12px;
  background: #ff6b6b;
  border-radius: 50%;
  border: 2px solid white;
}

.dark-mode .unread-dot {
  border-color: #1a1a2e;
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notification-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.dark-mode .notification-title {
  color: #e0e0e0;
}

.notification-time {
  font-size: 12px;
  color: #999;
}

.notification-content {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 8px;
}

.dark-mode .notification-content {
  color: #a0a0c0;
}

.from-user {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #667eea;
}

.card-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.notification-card:hover .card-actions {
  opacity: 1;
}

.action-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn.read {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.action-btn.read:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.action-btn.delete {
  background: rgba(255, 107, 107, 0.1);
  color: #ff6b6b;
}

.action-btn.delete:hover {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  color: white;
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
  .stats-bar {
    flex-direction: column;
    gap: 15px;
  }

  .card-actions {
    opacity: 1;
  }

  .notification-card {
    padding: 15px;
  }

  .type-icon {
    width: 36px;
    height: 36px;
    font-size: 16px;
  }
}
</style>
