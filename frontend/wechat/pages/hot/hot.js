const app = getApp()

Page({
  data: {
    isDarkMode: false,
    loading: false,
    hotPosts: [],
    currentFilter: 'day',
    timeFilters: [
      { label: '今日', value: 'day' },
      { label: '本周', value: 'week' },
      { label: '本月', value: 'month' },
      { label: '总榜', value: 'all' }
    ]
  },

  onLoad() {
    this.setData({
      isDarkMode: wx.getStorageSync('darkMode') || false
    })
    this.loadHotPosts()
  },

  onShow() {
    this.setData({
      isDarkMode: wx.getStorageSync('darkMode') || false
    })
  },

  switchFilter(e) {
    const filter = e.currentTarget.dataset.filter
    this.setData({ currentFilter: filter })
    this.loadHotPosts()
  },

  async loadHotPosts() {
    this.setData({ loading: true })
    try {
      const res = await app.request({
        url: '/posts/hot',
        data: {
          period: this.data.currentFilter,
          size: 50
        }
      })
      if (res.code === 200) {
        const posts = (res.data || []).map(post => ({
          ...post,
          heat: this.calculateHeat(post)
        }))
        this.setData({ hotPosts: posts })
      }
    } finally {
      this.setData({ loading: false })
    }
  },

  calculateHeat(post) {
    const views = post.viewCount || 0
    const likes = post.likeCount || 0
    const comments = post.commentCount || 0
    return Math.floor(views * 0.5 + likes * 2 + comments * 3)
  },

  goToPost(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/post-detail/post-detail?id=${id}`
    })
  }
})
