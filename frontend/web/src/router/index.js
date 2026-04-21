import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import PostDetailView from '@/views/PostDetailView.vue'
import CreatePostView from '@/views/CreatePostView.vue'
import HotView from '@/views/HotView.vue'
import CategoryView from '@/views/CategoryView.vue'
import ProfileView from '@/views/ProfileView.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/hot',
    name: 'hot',
    component: HotView
  },
  {
    path: '/category',
    name: 'category',
    component: CategoryView
  },
  {
    path: '/category/:id',
    name: 'category-detail',
    component: HomeView
  },
  {
    path: '/profile',
    name: 'profile',
    component: ProfileView
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView,
    meta: { guest: true }
  },
  {
    path: '/post/:id',
    name: 'post-detail',
    component: PostDetailView
  },
  {
    path: '/create',
    name: 'create-post',
    component: CreatePostView,
    meta: { requiresAuth: true }
  },
  {
    path: '/edit/:id',
    name: 'edit-post',
    component: CreatePostView,
    meta: { requiresAuth: true }
  },
  {
    path: '/my-posts',
    name: 'my-posts',
    component: HomeView,
    meta: { requiresAuth: true }
  },
  {
    path: '/my-collections',
    name: 'my-collections',
    component: HomeView,
    meta: { requiresAuth: true }
  },
  {
    path: '/settings',
    name: 'settings',
    component: ProfileView,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  // 需要登录的页面
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  // 游客页面（登录后不能访问）
  if (to.meta.guest && token) {
    next('/')
    return
  }

  next()
})

export default router
