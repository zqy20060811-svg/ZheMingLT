const app = getApp()

Page({
  data: {
    isDarkMode: false,
    categories: [],
    recommendCategories: [],
    allCategories: []
  },

  defaultCategories: [
    { id: 1, name: '技术交流', icon: '💻', color: '#4a6fa5', gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
    { id: 2, name: '生活分享', icon: '☕', color: '#e07a5f', gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
    { id: 3, name: '娱乐八卦', icon: '🎬', color: '#81b29a', gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
    { id: 4, name: '学习资料', icon: '📚', color: '#f2cc8f', gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' },
    { id: 5, name: '职场经验', icon: '💼', color: '#9b5de5', gradient: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)' },
    { id: 6, name: '兴趣爱好', icon: '🎨', color: '#00bbf9', gradient: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)' }
  ],

  onLoad() {
    this.setData({
      isDarkMode: wx.getStorageSync('darkMode') || false
    })
    this.loadCategories()
  },

  onShow() {
    this.setData({
      isDarkMode: wx.getStorageSync('darkMode') || false
    })
  },

  async loadCategories() {
    try {
      const res = await app.request({
        url: '/categories'
      })
      if (res.code === 200 && res.data && res.data.length > 0) {
        const loaded = res.data.map((cat, index) => ({
          ...cat,
          ...this.defaultCategories[index % this.defaultCategories.length]
        }))
        this.setData({
          categories: loaded.slice(0, 6),
          recommendCategories: loaded.slice(0, 4),
          allCategories: loaded
        })
      } else {
        this.setData({
          categories: this.defaultCategories.slice(0, 6),
          recommendCategories: this.defaultCategories.slice(0, 4),
          allCategories: this.defaultCategories
        })
      }
    } catch (error) {
      this.setData({
        categories: this.defaultCategories.slice(0, 6),
        recommendCategories: this.defaultCategories.slice(0, 4),
        allCategories: this.defaultCategories
      })
    }
  },

  goToCategoryDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/category-detail/category-detail?id=${id}`
    })
  }
})
