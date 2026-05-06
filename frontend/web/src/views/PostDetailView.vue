<template>
  <div class="manor-post-detail" :class="{ 'night-mode': isDarkMode }">
    <!-- 庄园背景 -->
    <ManorBackground :isDarkMode="isDarkMode" />

    <!-- 顶部导航 -->
    <nav class="detail-top-nav">
      <div class="nav-left">
        <button class="back-btn" @click="goBack">
          <i class="bi bi-arrow-left"></i>
        </button>
        <div class="nav-brand" @click="goHome">
          <i class="bi bi-house-heart-fill"></i>
          <span>择明庄园</span>
        </div>
      </div>
      <div class="nav-actions">
        <button class="theme-btn" @click="toggleTheme">
          <i :class="isDarkMode ? 'bi bi-sun-fill' : 'bi bi-moon-stars-fill'"></i>
        </button>
        <button class="share-btn" @click="sharePost">
          <i class="bi bi-share-fill"></i>
        </button>
      </div>
    </nav>

    <!-- 主内容 -->
    <main class="detail-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="loading-flower">
          <i class="bi bi-flower1"></i>
        </div>
        <span>加载中...</span>
      </div>

      <template v-else-if="postDetail">
        <!-- 帖子卡片 -->
        <article class="post-detail-card">
          <!-- 分类标签 -->
          <div class="category-tag" :style="getCategoryStyle(postDetail.categoryName)">
            <i class="bi bi-folder-fill"></i>
            {{ postDetail.categoryName || '未分类' }}
          </div>

          <!-- 标题 -->
          <h1 class="post-title">{{ postDetail.title }}</h1>

          <!-- 作者信息 -->
          <div class="author-bar">
            <div class="author-info" @click="goToAuthorProfile">
              <div class="author-avatar">
                <img v-if="postDetail.authorAvatar" :src="postDetail.authorAvatar" />
                <i v-else class="bi bi-person-fill"></i>
              </div>
              <div class="author-meta">
                <span class="author-name">{{ postDetail.authorName || '匿名用户' }}</span>
                <span class="post-time">
                  <i class="bi bi-clock"></i>
                  {{ formatTime(postDetail.createdAt) }}
                </span>
              </div>
            </div>
            <div class="author-actions">
              <button 
                v-if="!isAuthor && postDetail.userId" 
                class="follow-btn"
                :class="{ following: postDetail.isFollowing }"
                @click.stop="toggleFollow"
              >
                <i class="bi" :class="postDetail.isFollowing ? 'bi-person-check-fill' : 'bi-person-plus-fill'"></i>
                {{ postDetail.isFollowing ? '已关注' : '关注' }}
              </button>
              <span class="view-count">
                <i class="bi bi-eye-fill"></i>
                {{ formatNumber(postDetail.viewCount || 0) }}
              </span>
            </div>
          </div>

          <!-- 帖子内容 -->
          <div class="post-body">
            {{ postDetail.content }}
          </div>

          <!-- AI 内容总结 -->
          <AIAssistant
            v-if="postDetail.content && postDetail.content.length > 100"
            type="summary"
            :content="postDetail.content"
          />

          <!-- 标签 -->
          <div v-if="postDetail.tags && postDetail.tags.length > 0" class="post-tags">
            <span 
              v-for="tag in postDetail.tags" 
              :key="tag.id"
              class="tag-item"
              :style="{ backgroundColor: tag.color + '20', color: tag.color, borderColor: tag.color + '40' }"
            >
              <i class="bi bi-tag-fill"></i>
              {{ tag.name }}
            </span>
          </div>

          <!-- 互动按钮 -->
          <div class="interaction-bar">
            <button
              class="interaction-btn like-btn"
              :class="{ active: postDetail.isLiked }"
              @click="toggleLike"
            >
              <div class="btn-icon">
                <i class="bi" :class="postDetail.isLiked ? 'bi-heart-fill' : 'bi-heart'"></i>
              </div>
              <span class="btn-label">{{ postDetail.isLiked ? '已点赞' : '点赞' }}</span>
              <span class="btn-count">{{ formatNumber(postDetail.likeCount || 0) }}</span>
            </button>
            
            <button
              class="interaction-btn collect-btn"
              :class="{ active: postDetail.isCollected }"
              @click="toggleCollect"
            >
              <div class="btn-icon">
                <i class="bi" :class="postDetail.isCollected ? 'bi-bookmark-fill' : 'bi-bookmark'"></i>
              </div>
              <span class="btn-label">{{ postDetail.isCollected ? '已收藏' : '收藏' }}</span>
              <span class="btn-count">{{ formatNumber(postDetail.collectCount || 0) }}</span>
            </button>
            
            <button class="interaction-btn comment-btn" @click="scrollToComments">
              <div class="btn-icon">
                <i class="bi bi-chat-heart-fill"></i>
              </div>
              <span class="btn-label">评论</span>
              <span class="btn-count">{{ formatNumber(comments.length || 0) }}</span>
            </button>
          </div>

          <!-- 作者操作 -->
          <div v-if="isAuthor" class="owner-actions">
            <button class="owner-btn edit-btn" @click="editPost">
              <i class="bi bi-pencil-square"></i>
              编辑帖子
            </button>
            <button class="owner-btn delete-btn" @click="deletePost">
              <i class="bi bi-trash-fill"></i>
              删除帖子
            </button>
          </div>
        </article>

        <!-- 评论区 -->
        <section id="comments-section" class="comments-section">
          <div class="section-header">
            <div class="header-title">
              <i class="bi bi-chat-dots-fill"></i>
              <h3>评论交流</h3>
              <span class="comment-badge">{{ comments.length }}</span>
            </div>
          </div>

          <!-- AI 回复建议 -->
          <AIAssistant
            v-if="isLoggedIn && postDetail.content"
            type="reply"
            :content="postDetail.content"
            :existing-comments="comments.map(c => c.content)"
            @select-suggestion="newComment = $event"
          />

          <!-- 评论输入 -->
          <div v-if="isLoggedIn" class="comment-input-wrapper">
            <div class="current-user-avatar">
              <img v-if="currentUserAvatar" :src="currentUserAvatar" />
              <i v-else class="bi bi-person-fill"></i>
            </div>
            <div class="input-area">
              <textarea
                v-model="newComment"
                placeholder="写下你的想法，与大家交流..."
                rows="3"
                @keydown.enter.ctrl="submitComment"
              ></textarea>
              <div class="input-actions">
                <span class="input-hint">Ctrl + Enter 发送</span>
                <button
                  class="submit-comment-btn"
                  @click="submitComment"
                  :disabled="!newComment.trim()"
                >
                  <i class="bi bi-send-fill"></i>
                  发表评论
                </button>
              </div>
            </div>
          </div>
          
          <div v-else class="login-prompt">
            <i class="bi bi-person-circle"></i>
            <p>登录后即可参与讨论</p>
            <router-link to="/login" class="login-link">立即登录</router-link>
          </div>

          <!-- 评论列表 -->
          <div v-if="comments.length > 0" class="comments-list">
            <div
              v-for="(comment, index) in comments"
              :key="comment.id"
              class="comment-card"
              :style="{ animationDelay: `${index * 0.1}s` }"
            >
              <div class="comment-user-avatar">
                <img v-if="comment.authorAvatar" :src="comment.authorAvatar" />
                <i v-else class="bi bi-person-fill"></i>
              </div>
              <div class="comment-content-wrapper">
                <div class="comment-header">
                  <span class="comment-author">{{ comment.authorName }}</span>
                  <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                </div>
                <div class="comment-text">{{ comment.content }}</div>
                <div class="comment-actions">
                  <button class="action-link" @click="replyTo(comment)">
                    <i class="bi bi-reply-fill"></i>
                    回复
                  </button>
                  <button 
                    v-if="comment.authorId === currentUserId" 
                    class="action-link delete-link"
                    @click="deleteComment(comment.id)"
                  >
                    <i class="bi bi-trash"></i>
                    删除
                  </button>
                </div>

                <!-- 回复列表 -->
                <div v-if="comment.replies && comment.replies.length > 0" class="replies-list">
                  <div
                    v-for="reply in comment.replies"
                    :key="reply.id"
                    class="reply-card"
                  >
                    <div class="reply-user-avatar">
                      <img v-if="reply.authorAvatar" :src="reply.authorAvatar" />
                      <i v-else class="bi bi-person-fill"></i>
                    </div>
                    <div class="reply-content-wrapper">
                      <div class="reply-header">
                        <span class="reply-author">{{ reply.authorName }}</span>
                        <span class="reply-time">{{ formatTime(reply.createdAt) }}</span>
                      </div>
                      <div class="reply-text">{{ reply.content }}</div>
                    </div>
                  </div>
                </div>

                <!-- 回复输入 -->
                <div v-if="replyingTo === comment.id" class="reply-input-wrapper">
                  <textarea
                    v-model="replyContent"
                    :placeholder="`回复 @${comment.authorName}...`"
                    rows="2"
                    @keydown.enter.ctrl="submitReply(comment.id)"
                  ></textarea>
                  <div class="reply-actions">
                    <button class="cancel-reply-btn" @click="cancelReply">取消</button>
                    <button 
                      class="submit-reply-btn" 
                      @click="submitReply(comment.id)"
                      :disabled="!replyContent.trim()"
                    >
                      发送回复
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div v-else class="empty-comments">
            <div class="empty-icon">
              <i class="bi bi-chat-square-text"></i>
            </div>
            <h4>暂无评论</h4>
            <p>成为第一个评论的人吧！</p>
          </div>
        </section>
      </template>

      <!-- 帖子不存在 -->
      <div v-else class="not-found">
        <div class="empty-icon">
          <i class="bi bi-exclamation-circle-fill"></i>
        </div>
        <h3>帖子不存在或已被删除</h3>
        <p>该帖子可能已被作者删除或不存在</p>
        <button class="back-home-btn" @click="goHome">
          <i class="bi bi-house-fill"></i>
          返回首页
        </button>
      </div>
    </main>

    <!-- 底部导航 -->
    <ManorBottomNav :isDarkMode="isDarkMode" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get, post, del, getAccessToken } from '@/utils/request'
import ManorBackground from '@/components/ManorBackground.vue'
import ManorBottomNav from '@/components/ManorBottomNav.vue'
import AIAssistant from '@/components/AIAssistant.vue'

const route = useRoute()
const router = useRouter()

// 状态
const loading = ref(true)
const isDarkMode = ref(false)
const isLoggedIn = ref(false)
const username = ref('')
const currentUserId = ref(null)
const currentUserAvatar = ref('https://api.dicebear.com/7.x/avataaars/svg?seed=user')

const postDetail = ref(null)
const comments = ref([])
const newComment = ref('')
const replyingTo = ref(null)
const replyContent = ref('')

const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

// 计算属性
const isAuthor = computed(() => {
  return postDetail.value && postDetail.value.userId === currentUserId.value
})

// 方法
function toggleTheme() {
  isDarkMode.value = !isDarkMode.value
  localStorage.setItem('darkMode', isDarkMode.value)
}

function checkLogin() {
  const token = getAccessToken()
  isLoggedIn.value = !!token
  username.value = localStorage.getItem('username') || ''
  currentUserId.value = parseInt(localStorage.getItem('userId')) || null
}

async function loadPost() {
  const id = route.params.id
  if (!id) {
    loading.value = false
    return
  }

  try {
    const res = await get(`/posts/${id}`)
    if (res.code === 200) {
      postDetail.value = res.data
      await loadComments(id)
    } else {
      postDetail.value = null
    }
  } finally {
    loading.value = false
  }
}

async function loadComments(postId) {
  const res = await get(`/comments/post/${postId}`)
  if (res.code === 200) {
    const pageData = res.data
    if (pageData && pageData.content) {
      comments.value = pageData.content
    } else if (Array.isArray(pageData)) {
      comments.value = pageData
    } else {
      comments.value = []
    }
  }
}

async function toggleLike() {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }

  if (postDetail.value.isLiked) {
    const res = await del(`/interactions/like/post/${postDetail.value.id}`)
    if (res.code === 200) {
      postDetail.value.isLiked = false
      postDetail.value.likeCount = Math.max(0, (postDetail.value.likeCount || 0) - 1)
    }
  } else {
    const res = await post(`/interactions/like/post/${postDetail.value.id}`)
    if (res.code === 200) {
      postDetail.value.isLiked = true
      postDetail.value.likeCount = (postDetail.value.likeCount || 0) + 1
    }
  }
}

async function toggleCollect() {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }

  if (postDetail.value.isCollected) {
    const res = await del(`/interactions/collect/${postDetail.value.id}`)
    if (res.code === 200) {
      postDetail.value.isCollected = false
      postDetail.value.collectCount = Math.max(0, (postDetail.value.collectCount || 0) - 1)
    }
  } else {
    const res = await post(`/interactions/collect/${postDetail.value.id}`)
    if (res.code === 200) {
      postDetail.value.isCollected = true
      postDetail.value.collectCount = (postDetail.value.collectCount || 0) + 1
    }
  }
}

function goToAuthorProfile() {
  if (postDetail.value && postDetail.value.userId) {
    router.push(`/user/${postDetail.value.userId}`)
  }
}

async function toggleFollow() {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }

  const authorId = postDetail.value.userId
  if (!authorId) return

  if (postDetail.value.isFollowing) {
    const res = await del(`/follows/${authorId}`)
    if (res.code === 200) {
      postDetail.value.isFollowing = false
    }
  } else {
    const res = await post(`/follows/${authorId}`)
    if (res.code === 200) {
      postDetail.value.isFollowing = true
    }
  }
}

function sharePost() {
  if (navigator.share) {
    navigator.share({
      title: postDetail.value.title,
      text: postDetail.value.summary || postDetail.value.content.substring(0, 100),
      url: window.location.href
    })
  } else {
    navigator.clipboard.writeText(window.location.href)
    alert('链接已复制到剪贴板')
  }
}

function scrollToComments() {
  document.getElementById('comments-section')?.scrollIntoView({ behavior: 'smooth' })
}

function editPost() {
  router.push(`/edit/${postDetail.value.id}`)
}

async function deletePost() {
  if (!confirm('确定要删除这篇帖子吗？此操作不可恢复。')) return

  const res = await del(`/posts/${postDetail.value.id}`)
  if (res.code === 200) {
    alert('删除成功')
    router.push('/')
  } else {
    alert(res.message || '删除失败')
  }
}

async function submitComment() {
  if (!newComment.value.trim()) return

  const res = await post('/comments', {
    postId: postDetail.value.id,
    content: newComment.value.trim()
  })

  if (res.code === 200) {
    newComment.value = ''
    await loadComments(postDetail.value.id)
  } else {
    alert(res.message || '评论失败')
  }
}

function replyTo(comment) {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }
  replyingTo.value = comment.id
  replyContent.value = ''
}

function cancelReply() {
  replyingTo.value = null
  replyContent.value = ''
}

async function submitReply(parentId) {
  if (!replyContent.value.trim()) return

  const res = await post('/comments', {
    postId: postDetail.value.id,
    content: replyContent.value.trim(),
    parentId: parentId
  })

  if (res.code === 200) {
    cancelReply()
    await loadComments(postDetail.value.id)
  } else {
    alert(res.message || '回复失败')
  }
}

async function deleteComment(commentId) {
  if (!confirm('确定要删除这条评论吗？')) return

  const res = await del(`/comments/${commentId}`)
  if (res.code === 200) {
    await loadComments(postDetail.value.id)
  } else {
    alert(res.message || '删除失败')
  }
}

function goBack() {
  router.back()
}

function goHome() {
  router.push('/')
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  return date.toLocaleDateString('zh-CN')
}

function formatNumber(num) {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

function getCategoryStyle(categoryName) {
  const colors = {
    '技术': { bg: '#e8f5e9', color: '#4a7c59', border: '#8FBC8F' },
    '生活': { bg: '#fce4ec', color: '#ad1457', border: '#F48FB1' },
    '娱乐': { bg: '#e3f2fd', color: '#1565c0', border: '#90CAF9' },
    '学习': { bg: '#e8f5e9', color: '#2e7d32', border: '#A5D6A7' }
  }
  const style = colors[categoryName] || { bg: '#f5f5f5', color: '#616161', border: '#e0e0e0' }
  return {
    backgroundColor: style.bg,
    color: style.color,
    borderColor: style.border
  }
}

// 生命周期
onMounted(() => {
  isDarkMode.value = localStorage.getItem('darkMode') === 'true'
  checkLogin()
  loadPost()
})
</script>

<style scoped>
.manor-post-detail {
  min-height: 100vh;
  position: relative;
  padding-bottom: 100px;
}

/* 顶部导航 */
.detail-top-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 70px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(139, 115, 85, 0.15);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  z-index: 100;
}

.night-mode .detail-top-nav {
  background: rgba(30, 35, 45, 0.9);
  border-bottom-color: rgba(100, 110, 130, 0.2);
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: rgba(139, 188, 143, 0.15);
  color: #5a7c5a;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.night-mode .back-btn {
  background: rgba(74, 124, 90, 0.2);
  color: #8ab88a;
}

.back-btn:hover {
  background: rgba(139, 188, 143, 0.3);
  transform: translateX(-2px);
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-size: 20px;
  font-weight: 700;
  color: #5a7c5a;
  font-family: 'Georgia', serif;
}

.night-mode .nav-brand {
  color: #8ab88a;
}

.nav-brand i {
  font-size: 24px;
  color: #8FBC8F;
}

.night-mode .nav-brand i {
  color: #7ac87a;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.theme-btn, .share-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: rgba(139, 115, 85, 0.1);
  color: #8B7355;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.night-mode .theme-btn,
.night-mode .share-btn {
  background: rgba(200, 180, 150, 0.1);
  color: #D4C596;
}

.theme-btn:hover, .share-btn:hover {
  background: rgba(139, 115, 85, 0.2);
  transform: scale(1.05);
}

/* 主内容 */
.detail-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 90px 20px 40px;
  position: relative;
  z-index: 1;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 20px;
  gap: 16px;
}

.loading-flower {
  width: 60px;
  height: 60px;
  background: rgba(139, 188, 143, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  color: #8FBC8F;
  animation: pulse 2s infinite ease-in-out;
}

.night-mode .loading-flower {
  background: rgba(74, 124, 90, 0.2);
  color: #7ac87a;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.1); opacity: 0.7; }
}

.loading-state span {
  color: #6a7c6a;
  font-size: 15px;
}

.night-mode .loading-state span {
  color: #9ab89a;
}

/* 帖子卡片 */
.post-detail-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  padding: 28px;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(139, 188, 143, 0.2);
}

.night-mode .post-detail-card {
  background: rgba(40, 45, 55, 0.95);
  border-color: rgba(74, 124, 90, 0.2);
}

.category-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 16px;
  border: 1px solid;
}

.post-title {
  font-size: 26px;
  font-weight: 700;
  color: #2d3748;
  line-height: 1.4;
  margin-bottom: 20px;
  font-family: 'Georgia', serif;
}

.night-mode .post-title {
  color: #e2e8f0;
}

/* 作者栏 */
.author-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-top: 1px solid rgba(139, 188, 143, 0.15);
  border-bottom: 1px solid rgba(139, 188, 143, 0.15);
  margin-bottom: 24px;
}

.night-mode .author-bar {
  border-color: rgba(74, 124, 90, 0.15);
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: opacity 0.3s ease;
}

.author-info:hover {
  opacity: 0.8;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #8FBC8F;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(143, 188, 143, 0.2);
  color: #5a7c5a;
  font-size: 24px;
}

.night-mode .author-avatar {
  border-color: #4a7c5a;
  background: rgba(74, 124, 90, 0.2);
  color: #8ab88a;
}

.author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.author-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-weight: 600;
  color: #5a7c5a;
  font-size: 15px;
}

.night-mode .author-name {
  color: #8ab88a;
}

.post-time {
  font-size: 13px;
  color: #8a9a8a;
  display: flex;
  align-items: center;
  gap: 4px;
}

.night-mode .post-time {
  color: #7a9a8a;
}

.author-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.follow-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 20px;
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  color: white;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.follow-btn.following {
  background: rgba(139, 188, 143, 0.2);
  color: #5a7c5a;
}

.night-mode .follow-btn.following {
  background: rgba(74, 124, 90, 0.2);
  color: #8ab88a;
}

.follow-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(124, 184, 124, 0.3);
}

.view-count {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #8a9a8a;
}

.night-mode .view-count {
  color: #7a9a8a;
}

/* 帖子内容 */
.post-body {
  font-size: 16px;
  line-height: 1.8;
  color: #4a5568;
  white-space: pre-wrap;
  margin-bottom: 24px;
}

.night-mode .post-body {
  color: #cbd5e0;
}

/* 标签 */
.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid;
}

/* 互动栏 */
.interaction-bar {
  display: flex;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid rgba(139, 188, 143, 0.15);
}

.night-mode .interaction-bar {
  border-color: rgba(74, 124, 90, 0.15);
}

.interaction-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 16px;
  border: none;
  border-radius: 16px;
  background: rgba(139, 188, 143, 0.1);
  color: #6a7c6a;
  cursor: pointer;
  transition: all 0.3s ease;
}

.night-mode .interaction-btn {
  background: rgba(74, 124, 90, 0.15);
  color: #9ab89a;
}

.interaction-btn:hover {
  transform: translateY(-2px);
}

.btn-icon {
  font-size: 18px;
}

.btn-label {
  font-size: 14px;
  font-weight: 500;
}

.btn-count {
  font-size: 13px;
  opacity: 0.8;
}

.like-btn.active {
  background: rgba(255, 107, 107, 0.15);
  color: #ff6b6b;
}

.collect-btn.active {
  background: rgba(255, 193, 7, 0.15);
  color: #ffc107;
}

.comment-btn:hover {
  background: rgba(139, 188, 143, 0.2);
  color: #5a7c5a;
}

/* 作者操作 */
.owner-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(139, 188, 143, 0.15);
}

.night-mode .owner-actions {
  border-color: rgba(74, 124, 90, 0.15);
}

.owner-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 16px;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.edit-btn {
  background: rgba(139, 188, 143, 0.15);
  color: #5a7c5a;
}

.night-mode .edit-btn {
  background: rgba(74, 124, 90, 0.2);
  color: #8ab88a;
}

.edit-btn:hover {
  background: #8FBC8F;
  color: white;
}

.delete-btn {
  background: rgba(255, 107, 107, 0.1);
  color: #ff6b6b;
}

.delete-btn:hover {
  background: #ff6b6b;
  color: white;
}

/* 评论区 */
.comments-section {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(139, 188, 143, 0.2);
}

.night-mode .comments-section {
  background: rgba(40, 45, 55, 0.95);
  border-color: rgba(74, 124, 90, 0.2);
}

.section-header {
  margin-bottom: 20px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-title i {
  font-size: 20px;
  color: #8FBC8F;
}

.night-mode .header-title i {
  color: #7ac87a;
}

.header-title h3 {
  font-size: 18px;
  font-weight: 700;
  color: #2d3748;
  font-family: 'Georgia', serif;
}

.night-mode .header-title h3 {
  color: #e2e8f0;
}

.comment-badge {
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

/* 评论输入 */
.comment-input-wrapper {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.current-user-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #8FBC8F;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(143, 188, 143, 0.2);
  color: #5a7c5a;
  font-size: 20px;
}

.night-mode .current-user-avatar {
  border-color: #4a7c5a;
  background: rgba(74, 124, 90, 0.2);
  color: #8ab88a;
}

.current-user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.input-area {
  flex: 1;
  background: rgba(139, 188, 143, 0.08);
  border-radius: 16px;
  padding: 16px;
}

.night-mode .input-area {
  background: rgba(74, 124, 90, 0.1);
}

.input-area textarea {
  width: 100%;
  border: none;
  background: transparent;
  resize: none;
  font-size: 15px;
  color: #2d3748;
  outline: none;
  font-family: inherit;
}

.night-mode .input-area textarea {
  color: #e2e8f0;
}

.input-area textarea::placeholder {
  color: #8a9a8a;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.input-hint {
  font-size: 12px;
  color: #8a9a8a;
}

.night-mode .input-hint {
  color: #7a9a8a;
}

.submit-comment-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border: none;
  border-radius: 20px;
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.submit-comment-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(124, 184, 124, 0.3);
}

.submit-comment-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 登录提示 */
.login-prompt {
  background: rgba(139, 188, 143, 0.1);
  border-radius: 16px;
  padding: 30px;
  margin-bottom: 24px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.night-mode .login-prompt {
  background: rgba(74, 124, 90, 0.15);
}

.login-prompt i {
  font-size: 40px;
  color: #8FBC8F;
}

.night-mode .login-prompt i {
  color: #7ac87a;
}

.login-prompt p {
  color: #6a7c6a;
  font-size: 15px;
}

.night-mode .login-prompt p {
  color: #9ab89a;
}

.login-link {
  padding: 10px 24px;
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  color: white;
  border-radius: 20px;
  text-decoration: none;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
}

.login-link:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(124, 184, 124, 0.3);
}

/* 评论列表 */
.comments-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-card {
  display: flex;
  gap: 12px;
  animation: slide-up 0.5s ease forwards;
  opacity: 0;
  transform: translateY(20px);
}

@keyframes slide-up {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.comment-user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid rgba(143, 188, 143, 0.3);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(143, 188, 143, 0.15);
  color: #5a7c5a;
  font-size: 18px;
}

.night-mode .comment-user-avatar {
  border-color: rgba(74, 124, 90, 0.3);
  background: rgba(74, 124, 90, 0.15);
  color: #8ab88a;
}

.comment-user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.comment-content-wrapper {
  flex: 1;
  background: rgba(139, 188, 143, 0.06);
  border-radius: 16px;
  padding: 14px;
}

.night-mode .comment-content-wrapper {
  background: rgba(74, 124, 90, 0.08);
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 600;
  color: #5a7c5a;
  font-size: 14px;
}

.night-mode .comment-author {
  color: #8ab88a;
}

.comment-time {
  font-size: 12px;
  color: #8a9a8a;
}

.night-mode .comment-time {
  color: #7a9a8a;
}

.comment-text {
  color: #4a5568;
  line-height: 1.6;
  font-size: 14px;
  margin-bottom: 10px;
}

.night-mode .comment-text {
  color: #cbd5e0;
}

.comment-actions {
  display: flex;
  gap: 16px;
}

.action-link {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: #8a9a8a;
  font-size: 13px;
  cursor: pointer;
  transition: color 0.3s ease;
}

.night-mode .action-link {
  color: #7a9a8a;
}

.action-link:hover {
  color: #8FBC8F;
}

.night-mode .action-link:hover {
  color: #7ac87a;
}

.action-link.delete-link:hover {
  color: #ff6b6b;
}

/* 回复列表 */
.replies-list {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(139, 188, 143, 0.1);
}

.night-mode .replies-list {
  border-color: rgba(74, 124, 90, 0.1);
}

.reply-card {
  display: flex;
  gap: 10px;
  padding: 10px 0;
}

.reply-user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(143, 188, 143, 0.15);
  color: #5a7c5a;
  font-size: 14px;
}

.night-mode .reply-user-avatar {
  background: rgba(74, 124, 90, 0.15);
  color: #8ab88a;
}

.reply-user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.reply-content-wrapper {
  flex: 1;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.reply-author {
  font-weight: 600;
  color: #5a7c5a;
  font-size: 13px;
}

.night-mode .reply-author {
  color: #8ab88a;
}

.reply-time {
  font-size: 11px;
  color: #8a9a8a;
}

.night-mode .reply-time {
  color: #7a9a8a;
}

.reply-text {
  color: #4a5568;
  font-size: 13px;
  line-height: 1.5;
}

.night-mode .reply-text {
  color: #cbd5e0;
}

/* 回复输入 */
.reply-input-wrapper {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(139, 188, 143, 0.1);
}

.night-mode .reply-input-wrapper {
  border-color: rgba(74, 124, 90, 0.1);
}

.reply-input-wrapper textarea {
  width: 100%;
  border: 1px solid rgba(139, 188, 143, 0.2);
  border-radius: 12px;
  padding: 12px;
  background: white;
  resize: none;
  font-size: 14px;
  color: #2d3748;
  outline: none;
  margin-bottom: 10px;
  font-family: inherit;
}

.night-mode .reply-input-wrapper textarea {
  background: rgba(30, 35, 45, 0.8);
  border-color: rgba(74, 124, 90, 0.2);
  color: #e2e8f0;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.cancel-reply-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  background: rgba(139, 188, 143, 0.1);
  color: #6a7c6a;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.night-mode .cancel-reply-btn {
  background: rgba(74, 124, 90, 0.15);
  color: #9ab89a;
}

.cancel-reply-btn:hover {
  background: rgba(139, 188, 143, 0.2);
}

.submit-reply-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  color: white;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.submit-reply-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(124, 184, 124, 0.3);
}

.submit-reply-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 空评论 */
.empty-comments {
  text-align: center;
  padding: 50px 20px;
}

.empty-comments .empty-icon {
  width: 70px;
  height: 70px;
  background: rgba(139, 188, 143, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.empty-comments .empty-icon i {
  font-size: 32px;
  color: #8FBC8F;
}

.night-mode .empty-comments .empty-icon i {
  color: #7ac87a;
}

.empty-comments h4 {
  font-size: 16px;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 8px;
  font-family: 'Georgia', serif;
}

.night-mode .empty-comments h4 {
  color: #e2e8f0;
}

.empty-comments p {
  color: #8a9a8a;
  font-size: 14px;
}

.night-mode .empty-comments p {
  color: #7a9a8a;
}

/* 404 */
.not-found {
  text-align: center;
  padding: 80px 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.night-mode .not-found {
  background: rgba(40, 45, 55, 0.95);
}

.not-found .empty-icon {
  width: 80px;
  height: 80px;
  background: rgba(255, 107, 107, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.not-found .empty-icon i {
  font-size: 36px;
  color: #ff6b6b;
}

.not-found h3 {
  font-size: 20px;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 8px;
  font-family: 'Georgia', serif;
}

.night-mode .not-found h3 {
  color: #e2e8f0;
}

.not-found p {
  color: #8a9a8a;
  margin-bottom: 24px;
}

.night-mode .not-found p {
  color: #7a9a8a;
}

.back-home-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #8FBC8F, #7CB87C);
  color: white;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.back-home-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(124, 184, 124, 0.3);
}

/* 响应式 */
@media (max-width: 640px) {
  .detail-content {
    padding: 80px 16px 40px;
  }

  .post-detail-card {
    padding: 20px;
  }

  .post-title {
    font-size: 22px;
  }

  .author-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .author-actions {
    width: 100%;
    justify-content: space-between;
  }

  .interaction-bar {
    flex-wrap: wrap;
  }

  .interaction-btn {
    flex: 1;
    min-width: 80px;
    padding: 10px 12px;
  }

  .btn-label {
    display: none;
  }
}
</style>