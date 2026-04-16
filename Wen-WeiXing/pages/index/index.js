// index.js
const app = getApp();

Page({
  data: {
    posts: [],
    categories: [],
    currentCategory: 0,
    page: 1,
    size: 10,
    loading: false,
    hasMore: true,
    theme: 'light'
  },
  
  // 加载分类
  loadCategories: function() {
    wx.request({
      url: app.globalData.baseUrl + '/categories',
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            categories: res.data.data
          });
        }
      }
    });
  },
  
  // 加载帖子
  loadPosts: function(loadMore = false) {
    if (this.data.loading) return;
    
    if (!loadMore) {
      this.setData({
        page: 1,
        posts: [],
        hasMore: true
      });
    }
    
    if (!this.data.hasMore) return;
    
    this.setData({ loading: true });
    
    let url = app.globalData.baseUrl + `/posts?page=${this.data.page}&size=${this.data.size}`;
    if (this.data.currentCategory > 0) {
      url += `&categoryId=${this.data.currentCategory}`;
    }
    
    wx.request({
      url: url,
      header: {
        'Authorization': `Bearer ${app.getToken()}`
      },
      success: (res) => {
        this.setData({ loading: false });
        
        if (res.data.code === 200) {
          const newPosts = res.data.data.list;
          const allPosts = loadMore ? [...this.data.posts, ...newPosts] : newPosts;
          
          this.setData({
            posts: allPosts,
            hasMore: newPosts.length === this.data.size,
            page: this.data.page + 1
          });
        }
      },
      fail: (err) => {
        this.setData({ loading: false });
        console.error('加载帖子失败:', err);
      }
    });
  },
  
  // 切换分类
  switchCategory: function(e) {
    const categoryId = parseInt(e.currentTarget.dataset.id);
    this.setData({
      currentCategory: categoryId
    });
    this.loadPosts();
  },
  
  // 跳转到帖子详情
  goToPostDetail: function(e) {
    const postId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/post-detail/post-detail?id=${postId}`
    });
  },
  
  // 切换主题
  toggleTheme: function() {
    const newTheme = app.toggleTheme();
    this.setData({
      theme: newTheme
    });
  },
  
  // 下拉刷新
  onPullDownRefresh: function() {
    this.loadPosts();
    wx.stopPullDownRefresh();
  },
  
  // 上拉加载更多
  onReachBottom: function() {
    this.loadPosts(true);
  },
  
  onLoad: function() {
    // 检查登录状态
    if (!app.checkLogin()) {
      wx.redirectTo({
        url: '/pages/login/login'
      });
      return;
    }
    
    // 获取当前主题
    this.setData({
      theme: app.getTheme()
    });
    
    this.loadCategories();
    this.loadPosts();
  }
})