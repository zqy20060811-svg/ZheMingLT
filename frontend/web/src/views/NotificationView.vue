<template>
  <div class="manor-notification" :class="{ 'night-mode': isDarkMode }">
    <!-- 庄园背景 -->
    <ManorBackground :isDarkMode="isDarkMode" />

    <!-- 顶部导航 -->
    <ManorNav title="消息中心" :isDarkMode="isDarkMode" @toggle-theme="toggleTheme">
      <template #left>
        <button class="nav-btn" @click="goBack">
          <i class="bi bi-arrow-left"></i>
        </button>
      </template>
      <template #right>
        <button v-if="unreadCount > 0" class="nav-btn text-btn" @click="markAllAsRead">
          全部已读
        </button>
      </template>
    </ManorNav>

    <!-- 消息统计卡片 -->
    <div class="stats-section">
      <div class="stats-card">
        <div class="stat-item unread">
          <div class="stat-icon">
            <i class="bi bi-bell-fill"></i>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ unreadCount }}</span>
            <span class="stat-label">未读消息</span>
          </div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item total">
          <div class="stat-icon">
            <i class="bi bi-envelope-fill"></i>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ totalCount }}</span>
            <span class="stat-label">全部消息</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 消息类型筛选 -->
    <div class="filter-section">
      <div class="filter-tabs">
        <button
          :class="['tab-btn', { active: activeTab === 'all' }]"
          @click="changeTab('all')"
        >
          <i class="bi bi-grid-fill"></i>
          <span>全部</span>
        </button>
        <button
          :class="['tab-btn', { active: activeTab === 'COMMENT' }]"
          @click="changeTab('COMMENT')"
        >
          <i class="bi bi-chat-dots-fill"></i>
          <span>评论</span>
        </button>
        <button
          :class="['tab-btn', { active: activeTab === 'LIKE' }]"
          @click="changeTab('LIKE')"
        >
          <i class="bi bi-heart-fill"></i>
          <span>点赞</span>
        </button>
        <button
          :class="['tab-btn', { active: activeTab === 'SYSTEM' }]"
          @click="changeTab('SYSTEM')"
        >
          <i class="bi bi-info-circle-fill"></i>
          <span>系统</span>
        </button>
      </div>
    </div>

    <!-- 消息列表 -->
    <div class="notification-content">
      <!-- 加载中 -->
      <div v-if="loading" class="loading-state">
        <div class="manor-spinner">
          <div class="spinner-leaf leaf-1"></div>
          <div class="spinner-leaf leaf-2"></div>
          <div class="spinner-leaf leaf-3"></div>
        </div>
        <p>加载中...</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="filteredNotifications.length === 0" class="empty-state">
        <div class="empty-illustration">
          <i class="bi bi-bell-slash"></i>
          <div class="empty-decoration">
            <span class="leaf leaf-1">🍃</span>
            <span class="leaf leaf-2">🌿</span>
          </div>
        </div>
        <h3>暂无消息</h3>
        <p>{{ emptyMessage }}</p>
      </div>

      <!-- 消息卡片列表 -->
      <div v-else class="notification-list">
        <div
          v-for="notification in filteredNotifications"
          :key="notification.id"
          :class="['notification-card', { unread: !notification.isRead }]"
          @click="handleNotificationClick(notification)"
        >
          <div class="card-glow"></div>
          <div class="card-inner">
            <div class="card-left">
              <div :class="['type-icon', notification.type.toLowerCase()]">
                <i :class="getTypeIcon(notification.type)"></i>
              </div>
              <div v-if="!notification.isRead" class="unread-dot"></div>
            </div>

            <div class="card-body">
              <div class="card-header">
                <span class="notification-title">{{ notification.title }}</span>
                <span class="notification-time">
                  <i class="bi bi-clock"></i>
                  {{ formatTime(notification.createdAt) }}
                </span>
              </div>
              <p class="notification-text">{{ notification.content }}</p>
              <div v-if="notification.fromUserName" class="from-user">
                <div class="user-avatar">
                  <i class="bi bi-person-fill"></i>
                </div>
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
                <i class="bi bi-check-lg"></i>
              </button>
              <button
                class="action-btn delete"
                @click.stop="deleteNotification(notification.id)"
                title="删除"
              >
                <i class="bi bi-x-lg"></i>
              </button>
            </div>
          </div>
        </div>

        <!-- 加载更多 -->
        <div v-if="hasMore" class="load-more">
          <button class="load-btn" @click="loadMore" :disabled="loadingMore">
            <span v-if="loadingMore" class="btn-spinner"></span>
            <span v-else>
              <i class="bi bi-chevron-down"></i>
              加载更多
            </span>
          </button>
        </div>
      </div>
    </div>

    <!-- 底部导航 -->
    <ManorBottomNav :isDarkMode="isDarkMode" active="profile" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get, put, del } from '@/utils/request'
import ManorBackground from '@/components/ManorBackground.vue'
import ManorNav from '@/components/ManorNav.vue'
import ManorBottomNav from '@/components/ManorBottomNav.vue'

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
      return '暂无评论消息，去发布新帖子吧~'
    case 'LIKE':
      return '暂无点赞消息，精彩内容会吸引点赞哦~'
    case 'SYSTEM':
      return '暂无系统消息'
    default:
      return '暂无消息通知，一切都很安静~'
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
.manor-notification {
  min-height: 100vh;
  position: relative;
  padding-bottom: 100px;
}

/* 导航按钮 */
.nav-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 12px;
  background: rgba(139, 115, 85, 0.1);
  color: #5a4a3a;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
}

.nav-btn:hover {
  background: rgba(139, 115, 85, 0.2);
  transform: translateY(-2px);
}

.night-mode .nav-btn {
  background: rgba(139, 115, 85, 0.2);
  color: #d4c5b0;
}

.nav-btn.text-btn {
  width: auto;
  padding: 0 16px;
  font-size: 0.9rem;
  font-weight: 500;
  background: linear-gradient(135deg, #8b7355 0%, #a0826d 100%);
  color: white;
}

.nav-btn.text-btn:hover {
  box-shadow: 0 4px 12px rgba(139, 115, 85, 0.4);
}

/* 统计区域 */
.stats-section {
  padding: 20px;
  position: relative;
  z-index: 1;
}

.stats-card {
  display: flex;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(90, 74, 58, 0.1);
  border: 1px solid rgba(139, 115, 85, 0.1);
}

.night-mode .stats-card {
  background: rgba(40, 45, 60, 0.9);
  border-color: rgba(139, 115, 85, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.stat-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-divider {
  width: 1px;
  background: linear-gradient(180deg, transparent, rgba(139, 115, 85, 0.3), transparent);
  margin: 0 20px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.6rem;
  transition: transform 0.3s ease;
}

.stat-item:hover .stat-icon {
  transform: scale(1.1);
}

.stat-item.unread .stat-icon {
  background: linear-gradient(135deg, #e8a87c 0%, #d4956c 100%);
  color: white;
}

.stat-item.total .stat-icon {
  background: linear-gradient(135deg, #8b7355 0%, #a0826d 100%);
  color: white;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 1.8rem;
  font-weight: 700;
  color: #5a4a3a;
  line-height: 1;
}

.night-mode .stat-value {
  color: #d4c5b0;
}

.stat-label {
  font-size: 0.85rem;
  color: #8b7355;
}

.night-mode .stat-label {
  color: #a09080;
}

/* 筛选区域 */
.filter-section {
  padding: 0 20px 16px;
  position: relative;
  z-index: 1;
}

.filter-tabs {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.filter-tabs::-webkit-scrollbar {
  display: none;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border: none;
  border-radius: 25px;
  background: rgba(255, 255, 255, 0.7);
  color: #6b5b4f;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  border: 1px solid rgba(139, 115, 85, 0.1);
}

.night-mode .tab-btn {
  background: rgba(40, 45, 55, 0.7);
  color: #a09080;
  border-color: rgba(139, 115, 85, 0.2);
}

.tab-btn:hover {
  background: rgba(139, 115, 85, 0.1);
  transform: translateY(-2px);
}

.tab-btn.active {
  background: linear-gradient(135deg, #8b7355 0%, #a0826d 100%);
  color: white;
  border-color: transparent;
  box-shadow: 0 4px 15px rgba(139, 115, 85, 0.3);
}

/* 消息内容区 */
.notification-content {
  padding: 0 20px;
  position: relative;
  z-index: 1;
}

/* 庄园风格加载动画 */
.loading-state {
  text-align: center;
  padding: 60px 20px;
}

.manor-spinner {
  position: relative;
  width: 60px;
  height: 60px;
  margin: 0 auto 20px;
}

.spinner-leaf {
  position: absolute;
  width: 20px;
  height: 20px;
  background: linear-gradient(135deg, #8b7355 0%, #a0826d 100%);
  border-radius: 0 50% 0 50%;
  animation: leaf-spin 1.5s ease-in-out infinite;
}

.spinner-leaf.leaf-1 {
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  animation-delay: 0s;
}

.spinner-leaf.leaf-2 {
  bottom: 10px;
  left: 5px;
  animation-delay: 0.5s;
}

.spinner-leaf.leaf-3 {
  bottom: 10px;
  right: 5px;
  animation-delay: 1s;
}

@keyframes leaf-spin {
  0%, 100% { opacity: 0.3; transform: scale(0.8) rotate(0deg); }
  50% { opacity: 1; transform: scale(1) rotate(180deg); }
}

.loading-state p {
  color: #8b7355;
  font-size: 0.95rem;
}

.night-mode .loading-state p {
  color: #a09080;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-illustration {
  position: relative;
  display: inline-block;
  margin-bottom: 24px;
}

.empty-illustration > i {
  font-size: 4rem;
  color: #c4b5a0;
}

.night-mode .empty-illustration > i {
  color: #6b5b4f;
}

.empty-decoration .leaf {
  position: absolute;
  font-size: 1.2rem;
  opacity: 0.6;
  animation: float-leaf 3s ease-in-out infinite;
}

.empty-decoration .leaf-1 {
  top: -10px;
  left: -20px;
  animation-delay: 0s;
}

.empty-decoration .leaf-2 {
  bottom: 0;
  right: -20px;
  animation-delay: 1.5s;
}

@keyframes float-leaf {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-10px) rotate(10deg); }
}

.empty-state h3 {
  font-size: 1.3rem;
  color: #5a4a3a;
  margin-bottom: 8px;
  font-weight: 600;
}

.night-mode .empty-state h3 {
  color: #d4c5b0;
}

.empty-state p {
  color: #8b7355;
  font-size: 0.9rem;
}

.night-mode .empty-state p {
  color: #a09080;
}

/* 消息列表 */
.notification-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notification-card {
  position: relative;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(139, 115, 85, 0.1);
}

.night-mode .notification-card {
  background: rgba(40, 45, 55, 0.9);
  border-color: rgba(139, 115, 85, 0.2);
}

.notification-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 40px rgba(90, 74, 58, 0.15);
}

.night-mode .notification-card:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
}

.notification-card.unread {
  border-left: 4px solid #8b7355;
}

.card-glow {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(139, 115, 85, 0.05) 0%, transparent 50%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.notification-card:hover .card-glow {
  opacity: 1;
}

.card-inner {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
}

.card-left {
  position: relative;
  flex-shrink: 0;
}

.type-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.3rem;
  transition: transform 0.3s ease;
}

.notification-card:hover .type-icon {
  transform: scale(1.1);
}

.type-icon.comment {
  background: linear-gradient(135deg, #8b7355 0%, #a0826d 100%);
  color: white;
}

.type-icon.like {
  background: linear-gradient(135deg, #e8a87c 0%, #d4956c 100%);
  color: white;
}

.type-icon.system {
  background: linear-gradient(135deg, #6b8e6b 0%, #7a9e7a 100%);
  color: white;
}

.unread-dot {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 14px;
  height: 14px;
  background: linear-gradient(135deg, #e8a87c 0%, #d4956c 100%);
  border-radius: 50%;
  border: 2px solid white;
  animation: pulse-dot 2s ease-in-out infinite;
}

.night-mode .unread-dot {
  border-color: #2a2f3a;
}

@keyframes pulse-dot {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

.card-body {
  flex: 1;
  min-width: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 8px;
}

.notification-title {
  font-size: 1rem;
  font-weight: 600;
  color: #5a4a3a;
  line-height: 1.4;
}

.night-mode .notification-title {
  color: #d4c5b0;
}

.notification-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.8rem;
  color: #a09080;
  white-space: nowrap;
}

.notification-time i {
  font-size: 0.75rem;
}

.notification-text {
  font-size: 0.9rem;
  color: #6b5b4f;
  line-height: 1.5;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.night-mode .notification-text {
  color: #a09080;
}

.from-user {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85rem;
  color: #8b7355;
}

.user-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #8b7355 0%, #a0826d 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
}

.card-actions {
  display: flex;
  flex-direction: column;
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
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
}

.action-btn.read {
  background: rgba(139, 115, 85, 0.1);
  color: #8b7355;
}

.action-btn.read:hover {
  background: linear-gradient(135deg, #8b7355 0%, #a0826d 100%);
  color: white;
  transform: scale(1.1);
}

.action-btn.delete {
  background: rgba(232, 168, 124, 0.1);
  color: #e8a87c;
}

.action-btn.delete:hover {
  background: linear-gradient(135deg, #e8a87c 0%, #d4956c 100%);
  color: white;
  transform: scale(1.1);
}

/* 加载更多 */
.load-more {
  text-align: center;
  padding: 20px 0 40px;
}

.load-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  background: rgba(139, 115, 85, 0.1);
  color: #8b7355;
  border: 1px solid rgba(139, 115, 85, 0.2);
  border-radius: 25px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.night-mode .load-btn {
  background: rgba(139, 115, 85, 0.2);
  color: #c4b5a0;
  border-color: rgba(139, 115, 85, 0.3);
}

.load-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #8b7355 0%, #a0826d 100%);
  color: white;
  border-color: transparent;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(139, 115, 85, 0.3);
}

.load-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-spinner {
  display: inline-block;
  width: 18px;
  height: 18px;
  border: 2px solid rgba(139, 115, 85, 0.3);
  border-top-color: #8b7355;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 响应式 */
@media (max-width: 768px) {
  .stats-card {
    flex-direction: column;
    gap: 20px;
  }

  .stat-divider {
    width: 100%;
    height: 1px;
    margin: 0;
    background: linear-gradient(90deg, transparent, rgba(139, 115, 85, 0.3), transparent);
  }

  .card-inner {
    padding: 16px;
  }

  .card-actions {
    opacity: 1;
    flex-direction: row;
  }

  .type-icon {
    width: 40px;
    height: 40px;
    font-size: 1.1rem;
  }

  .notification-title {
    font-size: 0.95rem;
  }

  .notification-text {
    font-size: 0.85rem;
  }
}
</style>
