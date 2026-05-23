const { createApp, ref, reactive, computed, onMounted, onUnmounted, watch } = Vue;
const { createRouter, createWebHashHistory } = VueRouter;

const API_BASE = 'http://localhost:8080/api';

const request = async (url, options = {}) => {
    const token = localStorage.getItem('accessToken');
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };
    // 注册、登录、刷新接口不携带token
    if (token && !url.includes('/users/register') && !url.includes('/users/login') && !url.includes('/users/refresh')) {
        headers['Authorization'] = 'Bearer ' + token;
    }

    try {
        let response = await fetch(API_BASE + url, {
            ...options,
            headers
        });
        let data = await response.json();

        // Token过期，尝试刷新 (支持 401/401001/401003)
        if (data.code === 401 || data.code === 401001 || data.code === 401003 || data.code === 1003) {
            const refreshToken = localStorage.getItem('refreshToken');
            if (refreshToken && !window._isRefreshing) {
                window._isRefreshing = true;
                try {
                    const refreshRes = await fetch(API_BASE + '/users/refresh', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'X-Refresh-Token': 'Bearer ' + refreshToken
                        }
                    });
                    const refreshData = await refreshRes.json();

                    if (refreshData.code === 200) {
                        // 刷新成功，更新token并重试原请求
                        localStorage.setItem('accessToken', refreshData.data.accessToken);
                        localStorage.setItem('refreshToken', refreshData.data.refreshToken);
                        // 重新构造headers，确保使用新token
                        const newHeaders = {
                            'Content-Type': 'application/json',
                            'Authorization': 'Bearer ' + refreshData.data.accessToken
                        };

                        // 必须重新构造完整请求配置，避免options中残留旧headers引用
                        const retryOptions = {
                            method: options.method,
                            body: options.body,
                            headers: newHeaders
                        };
                        const retryResponse = await fetch(API_BASE + url, retryOptions);
                        const retryData = await retryResponse.json();
                        return retryData;
                    }
                } finally {
                    window._isRefreshing = false;
                }
            }

            // 刷新失败，清除登录状态并跳转登录页
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            localStorage.removeItem('userId');
            localStorage.removeItem('username');
            window.location.hash = '#/login';
            // 阻止后续执行，避免返回旧的401数据给调用方
            return { code: 401, message: '登录已过期，请重新登录', data: null };
        }

        return data;
    } catch (error) {
        return { code: 500, message: '网络错误: ' + (error.message || '无法连接到服务器'), data: null };
    }
};

const httpGet = (url) => request(url, { method: 'GET' });
const httpPost = (url, body) => request(url, { method: 'POST', body: JSON.stringify(body) });
const httpPut = (url, body) => request(url, { method: 'PUT', body: JSON.stringify(body) });
const httpDelete = (url) => request(url, { method: 'DELETE' });

const Navbar = {
    template: `
        <nav class="navbar navbar-expand-lg">
            <div class="container">
                <router-link class="navbar-brand" to="/">择明论坛</router-link>
                <button class="navbar-toggler" type="button" @click="showMenu = !showMenu">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" :class="{show: showMenu}">
                    <ul class="navbar-nav me-auto">
                        <li class="nav-item">
                            <router-link class="nav-link" to="/">首页</router-link>
                        </li>
                        <li class="nav-item">
                            <router-link class="nav-link" to="/hot">热门</router-link>
                        </li>
                        <li class="nav-item">
                            <router-link class="nav-link" to="/categories">分类</router-link>
                        </li>
                        <li class="nav-item" v-if="isLoggedIn">
                            <router-link class="nav-link" to="/create">发布</router-link>
                        </li>
                    </ul>
                    <ul class="navbar-nav">
                        <template v-if="isLoggedIn">
                            <li class="nav-item">
                                <router-link class="nav-link" to="/notifications">
                                    <i class="bi bi-bell"></i>
                                    <span v-if="unreadCount > 0" class="badge bg-danger">{{ unreadCount }}</span>
                                </router-link>
                            </li>
                            <li class="nav-item dropdown">
                                <a class="nav-link dropdown-toggle" href="#" @click.prevent="toggleDropdown">
                                    {{ username }}
                                </a>
                                <ul class="dropdown-menu" :class="{show: showDropdown}">
                                    <li><router-link class="dropdown-item" :to="'/user/' + userId" @click="closeDropdown">个人中心</router-link></li>
                                    <li><router-link class="dropdown-item" to="/profile" @click="closeDropdown">我的资料</router-link></li>
                                    <li><router-link class="dropdown-item" to="/myposts" @click="closeDropdown">我的帖子</router-link></li>
                                    <li><router-link class="dropdown-item" to="/mycollections" @click="closeDropdown">我的收藏</router-link></li>
                                    <li><hr class="dropdown-divider"></li>
                                    <li><a class="dropdown-item" href="#" @click.prevent="logout">退出登录</a></li>
                                </ul>
                            </li>
                        </template>
                        <template v-else>
                            <li class="nav-item">
                                <router-link class="nav-link" to="/login">登录</router-link>
                            </li>
                            <li class="nav-item">
                                <router-link class="nav-link" to="/register">注册</router-link>
                            </li>
                        </template>
                    </ul>
                </div>
            </div>
        </nav>
    `,
    setup() {
        const showMenu = ref(false);
        const showDropdown = ref(false);
        const isLoggedIn = computed(() => !!localStorage.getItem('accessToken'));
        const username = ref(localStorage.getItem('username') || '');
        const userId = ref(localStorage.getItem('userId') || '');
        const unreadCount = ref(0);

        const toggleDropdown = () => {
            showDropdown.value = !showDropdown.value;
        };

        const closeDropdown = () => {
            showDropdown.value = false;
        };

        const logout = async () => {
            await httpPost('/users/logout');
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            localStorage.removeItem('userId');
            localStorage.removeItem('username');
            window.location.hash = '#/login';
            window.location.reload();
        };

        const loadUnreadCount = async () => {
            if (isLoggedIn.value) {
                const res = await httpGet('/notifications/unread-count');
                if (res.code === 200) unreadCount.value = res.data;
            }
        };

        let unreadInterval = null;

        onMounted(() => {
            loadUnreadCount();
            unreadInterval = setInterval(loadUnreadCount, 30000);
        });

        onUnmounted(() => {
            if (unreadInterval) {
                clearInterval(unreadInterval);
                unreadInterval = null;
            }
        });

        return { showMenu, showDropdown, isLoggedIn, username, userId, unreadCount, toggleDropdown, closeDropdown, logout };
    }
};

const Home = {
    template: `
        <div class="container mt-4">
            <div class="row">
                <div class="col-md-8">
                    <div class="card">
                        <div class="card-header d-flex justify-content-between align-items-center">
                            <span>最新帖子</span>
                            <div class="btn-group btn-group-sm">
                                <button class="btn" :class="sort === 'newest' ? 'btn-primary' : 'btn-outline-primary'" @click="sort = 'newest'">最新</button>
                                <button class="btn" :class="sort === 'hot' ? 'btn-primary' : 'btn-outline-primary'" @click="sort = 'hot'">热门</button>
                            </div>
                        </div>
                        <div class="card-body p-0">
                            <div v-if="loading" class="loading">加载中...</div>
                            <div v-else-if="posts.length === 0" class="empty">暂无帖子</div>
                            <div v-else>
                                <div v-for="post in posts" :key="post.id" class="post-item">
                                    <router-link :to="'/post/' + post.id" class="post-title">{{ post.title }}</router-link>
                                    <p class="text-muted mt-1" style="font-size: 0.9rem;">{{ post.summary || (post.content ? post.content.substring(0, 100) : '') }}...</p>
                                    <div class="post-meta">
                                        <span><i class="bi bi-person"></i> {{ post.authorName }}</span>
                                        <span><i class="bi bi-folder"></i> {{ post.categoryName }}</span>
                                        <span><i class="bi bi-eye"></i> {{ post.viewCount }}</span>
                                        <span><i class="bi bi-heart"></i> {{ post.likeCount }}</span>
                                        <span><i class="bi bi-chat"></i> {{ post.commentCount }}</span>
                                        <span>{{ formatDate(post.createdAt) }}</span>
                                    </div>
                                    <div v-if="post.tags && post.tags.length" class="mt-2">
                                        <span v-for="tag in post.tags" :key="tag.id" class="tag">{{ tag.name }}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="card-footer" v-if="!loading && posts.length > 0">
                            <nav>
                                <ul class="pagination justify-content-center mb-0">
                                    <li class="page-item" :class="{disabled: page <= 1}">
                                        <a class="page-link" href="#" @click.prevent="page--">上一页</a>
                                    </li>
                                    <li class="page-item active"><span class="page-link">{{ page }}</span></li>
                                    <li class="page-item" :class="{disabled: isLast}">
                                        <a class="page-link" href="#" @click.prevent="page++">下一页</a>
                                    </li>
                                </ul>
                            </nav>
                        </div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="card">
                        <div class="card-header">热门标签</div>
                        <div class="card-body">
                            <div v-if="hotTags.length === 0" class="empty">暂无标签</div>
                            <span v-for="tag in hotTags" :key="tag.id" class="tag cursor-pointer" @click="searchByTag(tag.id)">{{ tag.name }} ({{ tag.postCount }})</span>
                        </div>
                    </div>
                    <div class="card mt-3">
                        <div class="card-header">分类</div>
                        <div class="card-body">
                            <div v-if="categories.length === 0" class="empty">暂无分类</div>
                            <div v-else>
                                <div v-for="cat in categories" :key="cat.id" class="d-flex justify-content-between py-2 border-bottom">
                                    <router-link :to="'/category/' + cat.id" class="text-decoration-none text-dark">{{ cat.name }}</router-link>
                                    <span class="text-muted">{{ cat.postCount || 0 }}篇</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `,
    setup() {
        const posts = ref([]);
        const hotTags = ref([]);
        const categories = ref([]);
        const loading = ref(false);
        const page = ref(1);
        const size = ref(10);
        const isLast = ref(false);
        const sort = ref('newest');

        const loadPosts = async () => {
            loading.value = true;
            const res = await httpGet('/posts?page=' + page.value + '&size=' + size.value + '&sort=' + sort.value);
            if (res.code === 200) {
                posts.value = res.data.content || [];
                isLast.value = res.data.last;
            }
            loading.value = false;
        };

        const loadHotTags = async () => {
            const res = await httpGet('/tags/hot?limit=10');
            if (res.code === 200) hotTags.value = res.data || [];
        };

        const loadCategories = async () => {
            const res = await httpGet('/categories');
            if (res.code === 200) categories.value = res.data || [];
        };

        const searchByTag = (tagId) => {
            window.location.hash = '#/tag/' + tagId;
        };

        const formatDate = (date) => {
            if (!date) return '';
            return new Date(date).toLocaleDateString('zh-CN');
        };

        watch(page, loadPosts);
        watch(sort, () => { page.value = 1; loadPosts(); });

        onMounted(() => {
            loadPosts();
            loadHotTags();
            loadCategories();
        });

        return { posts, hotTags, categories, loading, page, isLast, sort, searchByTag, formatDate };
    }
};

const PostDetail = {
    template: `
        <div class="container mt-4">
            <div class="row">
                <div class="col-md-8">
                    <div v-if="loading" class="loading">加载中...</div>
                    <div v-else-if="!post" class="empty">帖子不存在</div>
                    <template v-else>
                        <div class="card">
                            <div class="card-body">
                                <h4>{{ post.title }}</h4>
                                <div class="post-meta mb-3">
                                    <router-link :to="'/user/' + post.userId" class="text-decoration-none">
                                        <img :src="post.authorAvatar || 'https://via.placeholder.com/40'" class="avatar avatar-sm me-2">
                                        {{ post.authorName }}
                                    </router-link>
                                    <span class="ms-3"><i class="bi bi-folder"></i> {{ post.categoryName }}</span>
                                    <span class="ms-3">{{ formatDate(post.createdAt) }}</span>
                                </div>
                                <div class="content" style="line-height: 1.8; white-space: pre-wrap;">{{ post.content }}</div>
                                <div v-if="post.tags && post.tags.length" class="mt-3">
                                    <span v-for="tag in post.tags" :key="tag.id" class="tag">{{ tag.name }}</span>
                                </div>
                                <div class="d-flex gap-3 mt-4">
                                    <button class="btn" :class="post.isLiked ? 'btn-danger' : 'btn-outline-danger'" @click="toggleLike">
                                        <i class="bi bi-heart" :class="{liked: post.isLiked}"></i> {{ post.likeCount }}
                                    </button>
                                    <button class="btn" :class="post.isCollected ? 'btn-warning' : 'btn-outline-warning'" @click="toggleCollect">
                                        <i class="bi bi-star" :class="{collected: post.isCollected}"></i> {{ post.collectCount || 0 }}
                                    </button>
                                    <button class="btn btn-outline-secondary" @click="showComment = !showComment">
                                        <i class="bi bi-chat"></i> {{ post.commentCount }}
                                    </button>
                                    <button v-if="isAuthor" class="btn btn-outline-danger ms-auto" @click="deletePost">删除</button>
                                </div>
                            </div>
                        </div>

                        <div class="card mt-3" v-if="showComment">
                            <div class="card-header">发表评论</div>
                            <div class="card-body">
                                <div v-if="!isLoggedIn" class="text-center py-3">
                                    请先<router-link to="/login">登录</router-link>后评论
                                </div>
                                <div v-else>
                                    <textarea class="form-control" rows="3" v-model="commentContent" placeholder="写下你的评论..."></textarea>
                                    <button class="btn btn-primary mt-2" @click="submitComment" :disabled="!commentContent.trim()">发表评论</button>
                                </div>
                            </div>
                        </div>

                        <div class="card mt-3">
                            <div class="card-header">评论 ({{ comments.length }})</div>
                            <div class="card-body p-0">
                                <div v-if="comments.length === 0" class="empty">暂无评论</div>
                                <div v-else>
                                    <div v-for="comment in comments" :key="comment.id" class="comment-item">
                                        <div class="d-flex">
                                            <img :src="comment.authorAvatar || 'https://via.placeholder.com/32'" class="avatar avatar-sm me-2">
                                            <div class="flex-grow-1">
                                                <div class="d-flex justify-content-between">
                                                    <strong>{{ comment.authorName }}</strong>
                                                    <span class="text-muted" style="font-size: 0.8rem;">{{ formatDate(comment.createdAt) }}</span>
                                                </div>
                                                <p class="mb-1 mt-1">{{ comment.content }}</p>
                                                <div>
                                                    <button class="btn btn-sm btn-link text-decoration-none p-0" @click="likeComment(comment.id)">
                                                        <i class="bi bi-heart" :class="{liked: comment.isLiked}"></i> {{ comment.likeCount }}
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </template>
                </div>
                <div class="col-md-4">
                    <div class="card">
                        <div class="card-header">作者信息</div>
                        <div class="card-body text-center" v-if="author">
                            <img :src="author.avatar || 'https://via.placeholder.com/80'" class="avatar mb-2" style="width: 80px; height: 80px;">
                            <h5>{{ author.username }}</h5>
                            <p class="text-muted">{{ author.bio || '暂无简介' }}</p>
                            <div class="d-flex justify-content-around">
                                <div><strong>{{ authorStats.posts }}</strong><br><small>帖子</small></div>
                                <div><strong>{{ authorStats.likes }}</strong><br><small>获赞</small></div>
                                <div><strong>{{ authorStats.followers }}</strong><br><small>粉丝</small></div>
                            </div>
                            <button v-if="isLoggedIn && !isAuthor" class="btn btn-primary mt-3 w-100" @click="toggleFollow">
                                {{ isFollowing ? '取消关注' : '关注' }}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `,
    setup() {
        const post = ref(null);
        const comments = ref([]);
        const author = ref(null);
        const authorStats = ref({ posts: 0, likes: 0, followers: 0 });
        const loading = ref(false);
        const showComment = ref(false);
        const commentContent = ref('');
        const isFollowing = ref(false);
        const isLoggedIn = computed(() => !!localStorage.getItem('accessToken'));
        const currentUserId = computed(() => localStorage.getItem('userId'));
        const isAuthor = computed(() => post.value && post.value.userId == currentUserId.value);

        const postId = computed(() => {
            const match = window.location.hash.match(/\/post\/(\d+)/);
            return match ? match[1] : null;
        });

        const loadPost = async () => {
            if (!postId.value) return;
            loading.value = true;
            const res = await httpGet('/posts/' + postId.value);
            if (res.code === 200) {
                post.value = res.data;
                loadAuthor(res.data.userId);
            }
            loading.value = false;
        };

        const loadAuthor = async (userId) => {
            const res = await httpGet('/users/' + userId);
            if (res.code === 200) author.value = res.data;
            const statsRes = await httpGet('/users/' + userId + '/stats');
            if (statsRes.code === 200) authorStats.value = statsRes.data;
            if (isLoggedIn.value && !isAuthor.value) {
                const followRes = await httpGet('/follows/check/' + userId);
                if (followRes.code === 200) isFollowing.value = followRes.data;
            }
        };

        const loadComments = async () => {
            if (!postId.value) return;
            const res = await httpGet('/comments/post/' + postId.value);
            if (res.code === 200) comments.value = res.data?.content || [];
        };

        const toggleLike = async () => {
            if (!isLoggedIn.value) { window.location.hash = '#/login'; return; }
            const url = '/interactions/like/post/' + post.value.id;
            const res = await (post.value.isLiked ? httpDelete(url) : httpPost(url));
            if (res.code === 200) {
                post.value.isLiked = !post.value.isLiked;
                post.value.likeCount += post.value.isLiked ? 1 : -1;
            }
        };

        const toggleCollect = async () => {
            if (!isLoggedIn.value) { window.location.hash = '#/login'; return; }
            const url = '/interactions/collect/' + post.value.id;
            const res = await (post.value.isCollected ? httpDelete(url) : httpPost(url));
            if (res.code === 200) {
                post.value.isCollected = !post.value.isCollected;
                post.value.collectCount = (post.value.collectCount || 0) + (post.value.isCollected ? 1 : -1);
            }
        };

        const toggleFollow = async () => {
            const url = '/follows/' + post.value.userId;
            const res = await (isFollowing.value ? httpDelete(url) : httpPost(url));
            if (res.code === 200) isFollowing.value = !isFollowing.value;
        };

        const submitComment = async () => {
            if (!commentContent.value.trim()) return;
            const res = await httpPost('/comments', {
                postId: parseInt(postId.value),
                content: commentContent.value
            });
            if (res.code === 200) {
                commentContent.value = '';
                loadComments();
                post.value.commentCount++;
            }
        };

        const likeComment = async (commentId) => {
            const res = await httpPost('/interactions/like/comment/' + commentId);
            if (res.code === 200) loadComments();
        };

        const deletePost = async () => {
            if (!confirm('确定要删除这个帖子吗？')) return;
            const res = await httpDelete('/posts/' + post.value.id);
            if (res.code === 200) window.location.hash = '/';
        };

        const formatDate = (date) => {
            if (!date) return '';
            return new Date(date).toLocaleString('zh-CN');
        };

        onMounted(() => {
            loadPost();
            loadComments();
        });

        return {
            post, comments, author, authorStats, loading, showComment, commentContent,
            isFollowing, isLoggedIn, isAuthor, toggleLike, toggleCollect, toggleFollow,
            submitComment, likeComment, deletePost, formatDate
        };
    }
};

const Login = {
    template: `
        <div class="container mt-5">
            <div class="row justify-content-center">
                <div class="col-md-4">
                    <div class="text-center mb-4">
                        <h2 class="text-primary">择明论坛</h2>
                        <p class="text-muted">欢迎来到择明论坛，请登录</p>
                    </div>
                    <div class="card">
                        <div class="card-header text-center">
                            <h5 class="mb-0">用户登录</h5>
                        </div>
                        <div class="card-body">
                            <div class="mb-3">
                                <label class="form-label">用户名</label>
                                <input type="text" class="form-control" v-model="form.username" placeholder="请输入用户名">
                            </div>
                            <div class="mb-3">
                                <label class="form-label">密码</label>
                                <input type="password" class="form-control" v-model="form.password" placeholder="请输入密码">
                            </div>
                            <button class="btn btn-primary w-100" @click="login" :disabled="loading">
                                <span v-if="loading">登录中...</span>
                                <span v-else>登录</span>
                            </button>
                            <div class="text-center mt-3">
                                <router-link to="/register">还没有账号？立即注册</router-link>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `,
    setup() {
        const form = reactive({ username: '', password: '' });
        const loading = ref(false);

        const login = async () => {
            if (!form.username || !form.password) {
                alert('请输入用户名和密码');
                return;
            }
            loading.value = true;
            const res = await httpPost('/users/login', form);
            loading.value = false;
            if (res.code === 200) {
                localStorage.setItem('accessToken', res.data.accessToken);
                localStorage.setItem('refreshToken', res.data.refreshToken);
                localStorage.setItem('userId', res.data.user.id);
                localStorage.setItem('username', res.data.user.username);
                window.location.hash = '/';
                window.location.reload();
            } else {
                alert(res.message || '登录失败');
            }
        };

        return { form, loading, login };
    }
};

const Register = {
    template: `
        <div class="container mt-5">
            <div class="row justify-content-center">
                <div class="col-md-4">
                    <div class="card">
                        <div class="card-header text-center">
                            <h5 class="mb-0">用户注册</h5>
                        </div>
                        <div class="card-body">
                            <div class="mb-3">
                                <label class="form-label">用户名</label>
                                <input type="text" class="form-control" v-model="form.username" placeholder="请输入用户名">
                            </div>
                            <div class="mb-3">
                                <label class="form-label">密码</label>
                                <input type="password" class="form-control" v-model="form.password" placeholder="请输入密码">
                            </div>
                            <div class="mb-3">
                                <label class="form-label">邮箱</label>
                                <input type="email" class="form-control" v-model="form.email" placeholder="请输入邮箱（可选）">
                            </div>
                            <button class="btn btn-primary w-100" @click="register" :disabled="loading">
                                <span v-if="loading">注册中...</span>
                                <span v-else>注册</span>
                            </button>
                            <div class="text-center mt-3">
                                <router-link to="/login">已有账号？立即登录</router-link>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `,
    setup() {
        const form = reactive({ username: '', password: '', email: '' });
        const loading = ref(false);

        const register = async () => {
            if (!form.username || !form.password) {
                alert('请输入用户名和密码');
                return;
            }
            loading.value = true;
            const res = await httpPost('/users/register', form);
            loading.value = false;
            if (res.code === 200) {
                alert('注册成功，请登录');
                window.location.hash = '#/login';
            } else {
                alert(res.message || '注册失败');
            }
        };

        return { form, loading, register };
    }
};

const CreatePost = {
    template: `
        <div class="container mt-4">
            <div class="row justify-content-center">
                <div class="col-md-8">
                    <div class="card">
                        <div class="card-header">
                            <h5 class="mb-0">发布帖子</h5>
                        </div>
                        <div class="card-body">
                            <div class="mb-3">
                                <label class="form-label">标题</label>
                                <input type="text" class="form-control" v-model="form.title" placeholder="请输入标题">
                            </div>
                            <div class="mb-3">
                                <label class="form-label">分类</label>
                                <select class="form-select" v-model="form.categoryId">
                                    <option value="">请选择分类</option>
                                    <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
                                </select>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">内容</label>
                                <textarea class="form-control" rows="10" v-model="form.content" placeholder="请输入内容"></textarea>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">摘要（可选）</label>
                                <div class="input-group">
                                    <input type="text" class="form-control" v-model="form.summary" placeholder="请输入摘要">
                                    <button class="btn btn-outline-primary" type="button" @click="generateSummary" :disabled="loading">
                                        <span v-if="loading">生成中...</span>
                                        <span v-else>AI生成</span>
                                    </button>
                                </div>
                            </div>
                            <div class="d-flex gap-2">
                                <button class="btn btn-primary" @click="submit" :disabled="loading">
                                    <span v-if="loading">发布中...</span>
                                    <span v-else>发布</span>
                                </button>
                                <button class="btn btn-outline-secondary" @click="$router.back()">取消</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `,
    setup() {
        const form = reactive({ title: '', content: '', categoryId: '', summary: '' });
        const categories = ref([]);
        const loading = ref(false);
        const isLoggedIn = computed(() => !!localStorage.getItem('accessToken'));

        onMounted(() => {
            if (!isLoggedIn.value) {
                alert('请先登录');
                window.location.hash = '#/login';
                return;
            }
            loadCategories();
        });

        const loadCategories = async () => {
            const res = await httpGet('/categories');
            if (res.code === 200) categories.value = res.data || [];
        };

        const submit = async () => {
            if (!form.title || !form.content || !form.categoryId) {
                alert('请填写完整信息');
                return;
            }
            loading.value = true;
            const res = await httpPost('/posts', {
                ...form,
                categoryId: parseInt(form.categoryId)
            });
            loading.value = false;
            if (res.code === 401) {
                alert('登录已过期，请重新登录');
                window.location.hash = '#/login';
                return;
            }
            if (res.code === 200) {
                window.location.hash = '/';
            } else {
                alert(res.message || '发布失败');
            }
        };

        const generateSummary = async () => {
            if (!form.content) {
                alert('请先输入内容');
                return;
            }
            loading.value = true;
            const res = await httpPost('/ai/summary', { content: form.content });
            loading.value = false;
            if (res.code === 200) {
                form.summary = res.data;
            } else {
                alert(res.message || '生成摘要失败');
            }
        };

        return { form, categories, loading, submit, generateSummary };
    }
};

const UserProfile = {
    template: `
        <div class="container mt-4">
            <div class="row">
                <div class="col-md-4">
                    <div class="card">
                        <div class="card-body text-center">
                            <img :src="user.avatar || 'https://via.placeholder.com/100'" class="avatar mb-3" style="width: 100px; height: 100px;">
                            <h5>{{ user.username }}</h5>
                            <p class="text-muted">{{ user.bio || '暂无简介' }}</p>
                            <div class="d-flex justify-content-around mt-3">
                                <div><strong>{{ stats.posts }}</strong><br><small>帖子</small></div>
                                <div><strong>{{ stats.likes }}</strong><br><small>获赞</small></div>
                                <div><strong>{{ stats.following }}</strong><br><small>关注</small></div>
                                <div><strong>{{ stats.followers }}</strong><br><small>粉丝</small></div>
                            </div>
                            <button v-if="isLoggedIn && !isCurrentUser" class="btn btn-primary mt-3 w-100" @click="toggleFollow">
                                {{ isFollowing ? '取消关注' : '关注' }}
                            </button>
                        </div>
                    </div>
                </div>
                <div class="col-md-8">
                    <div class="card">
                        <div class="card-header">发布的帖子</div>
                        <div class="card-body p-0">
                            <div v-if="posts.length === 0" class="empty">暂无帖子</div>
                            <div v-else>
                                <div v-for="post in posts" :key="post.id" class="post-item">
                                    <router-link :to="'/post/' + post.id" class="post-title">{{ post.title }}</router-link>
                                    <div class="post-meta">
                                        <span><i class="bi bi-eye"></i> {{ post.viewCount }}</span>
                                        <span><i class="bi bi-heart"></i> {{ post.likeCount }}</span>
                                        <span><i class="bi bi-chat"></i> {{ post.commentCount }}</span>
                                        <span>{{ formatDate(post.createdAt) }}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `,
    setup() {
        const user = ref({});
        const stats = ref({ posts: 0, likes: 0, following: 0, followers: 0 });
        const posts = ref([]);
        const isFollowing = ref(false);
        const isLoggedIn = computed(() => !!localStorage.getItem('accessToken'));
        const currentUserId = computed(() => localStorage.getItem('userId'));

        const userId = computed(() => {
            const match = window.location.hash.match(/\/user\/(\d+)/);
            return match ? match[1] : null;
        });

        const isCurrentUser = computed(() => userId.value == currentUserId.value);

        const loadData = async () => {
            if (!userId.value) return;
            const userRes = await httpGet('/users/' + userId.value);
            if (userRes.code === 200) user.value = userRes.data;
            const statsRes = await httpGet('/users/' + userId.value + '/stats');
            if (statsRes.code === 200) stats.value = statsRes.data;
            const postsRes = await httpGet('/users/' + userId.value + '/posts');
            if (postsRes.code === 200) posts.value = postsRes.data?.content || postsRes.data || [];
            if (isLoggedIn.value && !isCurrentUser.value) {
                const followRes = await httpGet('/follows/check/' + userId.value);
                if (followRes.code === 200) isFollowing.value = followRes.data;
            }
        };

        const toggleFollow = async () => {
            const url = '/follows/' + userId.value;
            const res = await (isFollowing.value ? httpDelete(url) : httpPost(url));
            if (res.code === 200) isFollowing.value = !isFollowing.value;
        };

        const formatDate = (date) => {
            if (!date) return '';
            return new Date(date).toLocaleDateString('zh-CN');
        };

        onMounted(loadData);

        return { user, stats, posts, isFollowing, isLoggedIn, isCurrentUser, toggleFollow, formatDate };
    }
};

const Profile = {
    template: `
        <div class="container mt-4">
            <div class="row justify-content-center">
                <div class="col-md-6">
                    <div class="card">
                        <div class="card-header">
                            <h5 class="mb-0">个人资料</h5>
                        </div>
                        <div class="card-body">
                            <div class="text-center mb-4">
                                <img :src="user.avatar || 'https://via.placeholder.com/100'" class="avatar mb-2" style="width: 100px; height: 100px;">
                                <div>
                                    <input type="file" ref="fileInput" @change="uploadAvatar" accept="image/*" style="display: none;">
                                    <button class="btn btn-sm btn-outline-primary" @click="$refs.fileInput.click()">更换头像</button>
                                </div>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">用户名</label>
                                <input type="text" class="form-control" v-model="form.username">
                            </div>
                            <div class="mb-3">
                                <label class="form-label">邮箱</label>
                                <input type="email" class="form-control" v-model="form.email">
                            </div>
                            <div class="mb-3">
                                <label class="form-label">个人简介</label>
                                <textarea class="form-control" rows="3" v-model="form.bio"></textarea>
                            </div>
                            <button class="btn btn-primary w-100" @click="save" :disabled="loading">保存修改</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `,
    setup() {
        const user = ref({});
        const form = reactive({ username: '', email: '', bio: '' });
        const loading = ref(false);
        const fileInput = ref(null);

        const loadUser = async () => {
            const res = await httpGet('/users/info');
            if (res.code === 200) {
                user.value = res.data;
                form.username = res.data.username || '';
                form.email = res.data.email || '';
                form.bio = res.data.bio || '';
            }
        };

        const save = async () => {
            loading.value = true;
            const res = await httpPut('/users/info', form);
            loading.value = false;
            if (res.code === 200) {
                alert('保存成功');
                localStorage.setItem('username', form.username);
            } else {
                alert(res.message || '保存失败');
            }
        };

        const uploadAvatar = async (e) => {
            const file = e.target.files[0];
            if (!file) return;
            const formData = new FormData();
            formData.append('file', file);
            const token = localStorage.getItem('accessToken');
            const res = await fetch(API_BASE + '/users/avatar', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + token },
                body: formData
            });
            const data = await res.json();
            if (data.code === 200) {
                user.value.avatar = data.data.avatar;
                alert('头像上传成功');
            } else {
                alert(data.message || '上传失败');
            }
        };

        onMounted(loadUser);

        return { user, form, loading, fileInput, save, uploadAvatar };
    }
};

const Notifications = {
    template: `
        <div class="container mt-4">
            <div class="row justify-content-center">
                <div class="col-md-8">
                    <div class="card">
                        <div class="card-header d-flex justify-content-between align-items-center">
                            <span>消息通知</span>
                            <button class="btn btn-sm btn-outline-primary" @click="markAllRead">全部已读</button>
                        </div>
                        <div class="card-body p-0">
                            <div v-if="notifications.length === 0" class="empty">暂无通知</div>
                            <div v-else>
                                <div v-for="notif in notifications" :key="notif.id" class="post-item" :class="{'bg-light': !notif.isRead}">
                                    <div class="d-flex justify-content-between">
                                        <div>
                                            <p class="mb-1">{{ notif.content }}</p>
                                            <small class="text-muted">{{ formatDate(notif.createdAt) }}</small>
                                        </div>
                                        <div>
                                            <button v-if="!notif.isRead" class="btn btn-sm btn-outline-primary" @click="markRead(notif.id)">标为已读</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `,
    setup() {
        const notifications = ref([]);

        const loadNotifications = async () => {
            const res = await httpGet('/notifications');
            if (res.code === 200) notifications.value = res.data?.content || [];
        };

        const markRead = async (id) => {
            const res = await httpPut('/notifications/' + id + '/read');
            if (res.code === 200) loadNotifications();
        };

        const markAllRead = async () => {
            const res = await httpPut('/notifications/read-all');
            if (res.code === 200) loadNotifications();
        };

        const formatDate = (date) => {
            if (!date) return '';
            return new Date(date).toLocaleString('zh-CN');
        };

        onMounted(loadNotifications);

        return { notifications, markRead, markAllRead, formatDate };
    }
};

const HotPosts = {
    template: `
        <div class="container mt-4">
            <div class="card">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <span>热门帖子</span>
                    <div class="btn-group btn-group-sm">
                        <button class="btn" :class="filter === 'day' ? 'btn-primary' : 'btn-outline-primary'" @click="filter = 'day'">今日</button>
                        <button class="btn" :class="filter === 'week' ? 'btn-primary' : 'btn-outline-primary'" @click="filter = 'week'">本周</button>
                        <button class="btn" :class="filter === 'month' ? 'btn-primary' : 'btn-outline-primary'" @click="filter = 'month'">本月</button>
                        <button class="btn" :class="filter === 'all' ? 'btn-primary' : 'btn-outline-primary'" @click="filter = 'all'">总榜</button>
                    </div>
                </div>
                <div class="card-body p-0">
                    <div v-if="loading" class="loading">加载中...</div>
                    <div v-else-if="posts.length === 0" class="empty">暂无帖子</div>
                    <div v-else>
                        <div v-for="(post, index) in posts" :key="post.id" class="post-item">
                            <span class="badge me-2" :class="index < 3 ? 'bg-danger' : 'bg-secondary'">{{ index + 1 }}</span>
                            <router-link :to="'/post/' + post.id" class="post-title">{{ post.title }}</router-link>
                            <div class="post-meta">
                                <span><i class="bi bi-person"></i> {{ post.authorName }}</span>
                                <span><i class="bi bi-eye"></i> {{ post.viewCount }}</span>
                                <span><i class="bi bi-heart"></i> {{ post.likeCount }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `,
    setup() {
        const posts = ref([]);
        const loading = ref(false);
        const filter = ref('all');

        const loadPosts = async () => {
            loading.value = true;
            const res = await httpGet('/posts/hot?filter=' + filter.value);
            if (res.code === 200) posts.value = res.data || [];
            loading.value = false;
        };

        watch(filter, loadPosts);
        onMounted(loadPosts);

        return { posts, loading, filter };
    }
};

const Categories = {
    template: `
        <div class="container mt-4">
            <div class="card">
                <div class="card-header">所有分类</div>
                <div class="card-body">
                    <div v-if="categories.length === 0" class="empty">暂无分类</div>
                    <div v-else class="row">
                        <div v-for="cat in categories" :key="cat.id" class="col-md-4 mb-3">
                            <div class="card h-100">
                                <div class="card-body">
                                    <h5 class="card-title">
                                        <router-link :to="'/category/' + cat.id" class="text-decoration-none">{{ cat.name }}</router-link>
                                    </h5>
                                    <p class="card-text text-muted">{{ cat.description || '暂无描述' }}</p>
                                    <span class="text-muted">{{ cat.postCount || 0 }} 篇帖子</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `,
    setup() {
        const categories = ref([]);

        const loadCategories = async () => {
            const res = await httpGet('/categories');
            if (res.code === 200) categories.value = res.data || [];
        };

        onMounted(loadCategories);

        return { categories };
    }
};

const CategoryPosts = {
    template: `
        <div class="container mt-4">
            <div class="card">
                <div class="card-header">{{ categoryName }}</div>
                <div class="card-body p-0">
                    <div v-if="loading" class="loading">加载中...</div>
                    <div v-else-if="posts.length === 0" class="empty">该分类暂无帖子</div>
                    <div v-else>
                        <div v-for="post in posts" :key="post.id" class="post-item">
                            <router-link :to="'/post/' + post.id" class="post-title">{{ post.title }}</router-link>
                            <p class="text-muted mt-1" style="font-size: 0.9rem;">{{ post.summary || (post.content ? post.content.substring(0, 100) : '') }}...</p>
                            <div class="post-meta">
                                <span><i class="bi bi-person"></i> {{ post.authorName }}</span>
                                <span><i class="bi bi-eye"></i> {{ post.viewCount }}</span>
                                <span><i class="bi bi-heart"></i> {{ post.likeCount }}</span>
                                <span>{{ formatDate(post.createdAt) }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `,
    setup() {
        const posts = ref([]);
        const categoryName = ref('');
        const loading = ref(false);

        const categoryId = computed(() => {
            const match = window.location.hash.match(/\/category\/(\d+)/);
            return match ? match[1] : null;
        });

        const loadPosts = async () => {
            if (!categoryId.value) return;
            loading.value = true;
            const catRes = await httpGet('/categories/' + categoryId.value);
            if (catRes.code === 200) categoryName.value = catRes.data?.name || '分类';
            const res = await httpGet('/posts?categoryId=' + categoryId.value);
            if (res.code === 200) posts.value = res.data?.content || [];
            loading.value = false;
        };

        const formatDate = (date) => {
            if (!date) return '';
            return new Date(date).toLocaleDateString('zh-CN');
        };

        onMounted(loadPosts);

        return { posts, categoryName, loading, formatDate };
    }
};

const MyPosts = {
    template: `
        <div class="container mt-4">
            <div class="card">
                <div class="card-header">我的帖子</div>
                <div class="card-body p-0">
                    <div v-if="posts.length === 0" class="empty">暂无帖子</div>
                    <div v-else>
                        <div v-for="post in posts" :key="post.id" class="post-item">
                            <router-link :to="'/post/' + post.id" class="post-title">{{ post.title }}</router-link>
                            <div class="post-meta">
                                <span><i class="bi bi-eye"></i> {{ post.viewCount }}</span>
                                <span><i class="bi bi-heart"></i> {{ post.likeCount }}</span>
                                <span><i class="bi bi-chat"></i> {{ post.commentCount }}</span>
                                <span>{{ formatDate(post.createdAt) }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `,
    setup() {
        const posts = ref([]);

        const loadPosts = async () => {
            const userId = localStorage.getItem('userId');
            const res = await httpGet('/users/' + userId + '/posts');
            if (res.code === 200) posts.value = res.data?.content || res.data || [];
        };

        const formatDate = (date) => {
            if (!date) return '';
            return new Date(date).toLocaleDateString('zh-CN');
        };

        onMounted(loadPosts);

        return { posts, formatDate };
    }
};

const MyCollections = {
    template: `
        <div class="container mt-4">
            <div class="card">
                <div class="card-header">我的收藏</div>
                <div class="card-body p-0">
                    <div v-if="collections.length === 0" class="empty">暂无收藏</div>
                    <div v-else>
                        <div v-for="item in collections" :key="item.id" class="post-item">
                            <router-link :to="'/post/' + item.postId" class="post-title">{{ item.postTitle }}</router-link>
                            <div class="post-meta">
                                <span><i class="bi bi-person"></i> {{ item.authorName }}</span>
                                <span>{{ formatDate(item.createdAt) }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `,
    setup() {
        const collections = ref([]);

        const loadCollections = async () => {
            const res = await httpGet('/interactions/collects');
            if (res.code === 200) collections.value = res.data?.content || [];
        };

        const formatDate = (date) => {
            if (!date) return '';
            return new Date(date).toLocaleDateString('zh-CN');
        };

        onMounted(loadCollections);

        return { collections, formatDate };
    }
};

const Search = {
    template: `
        <div class="container mt-4">
            <div class="card">
                <div class="card-header">
                    <div class="input-group">
                        <input type="text" class="form-control" v-model="keyword" placeholder="搜索帖子..." @keyup.enter="search">
                        <button class="btn btn-primary" @click="search">搜索</button>
                    </div>
                </div>
                <div class="card-body p-0">
                    <div v-if="loading" class="loading">搜索中...</div>
                    <div v-else-if="posts.length === 0 && searched" class="empty">未找到相关帖子</div>
                    <div v-else>
                        <div v-for="post in posts" :key="post.id" class="post-item">
                            <router-link :to="'/post/' + post.id" class="post-title">{{ post.title }}</router-link>
                            <p class="text-muted mt-1" style="font-size: 0.9rem;">{{ post.summary || (post.content ? post.content.substring(0, 100) : '') }}...</p>
                            <div class="post-meta">
                                <span><i class="bi bi-person"></i> {{ post.authorName }}</span>
                                <span><i class="bi bi-folder"></i> {{ post.categoryName }}</span>
                                <span>{{ formatDate(post.createdAt) }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `,
    setup() {
        const keyword = ref('');
        const posts = ref([]);
        const loading = ref(false);
        const searched = ref(false);

        const search = async () => {
            if (!keyword.value.trim()) return;
            loading.value = true;
            searched.value = true;
            const res = await httpGet('/posts/search?keyword=' + encodeURIComponent(keyword.value));
            if (res.code === 200) posts.value = res.data?.content || [];
            loading.value = false;
        };

        const formatDate = (date) => {
            if (!date) return '';
            return new Date(date).toLocaleDateString('zh-CN');
        };

        return { keyword, posts, loading, searched, search, formatDate };
    }
};

const routes = [
    { path: '/', component: Home },
    { path: '/login', component: Login },
    { path: '/register', component: Register },
    { path: '/post/:id', component: PostDetail },
    { path: '/create', component: CreatePost },
    { path: '/user/:id', component: UserProfile },
    { path: '/profile', component: Profile },
    { path: '/notifications', component: Notifications },
    { path: '/hot', component: HotPosts },
    { path: '/categories', component: Categories },
    { path: '/category/:id', component: CategoryPosts },
    { path: '/myposts', component: MyPosts },
    { path: '/mycollections', component: MyCollections },
    { path: '/search', component: Search },
    { path: '/:pathMatch(.*)*', redirect: '/' }
];

const router = createRouter({
    history: createWebHashHistory(),
    routes
});

const App = {
    template: `
        <div>
            <Navbar v-if="showNavbar" />
            <div style="min-height: 100vh; background: #f5f5f5;">
                <router-view v-slot="{ Component }">
                    <component :is="Component" />
                </router-view>
            </div>
        </div>
    `,
    components: { Navbar },
    setup() {
        const route = VueRouter.useRoute();
        const showNavbar = computed(() => {
            return route.path !== '/login' && route.path !== '/register';
        });
        return { showNavbar };
    }
};

createApp(App).use(router).mount('#app');
