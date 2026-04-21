const app = getApp()

Page({
  data: {
    isDarkMode: false,
    isLoggedIn: false,
    user: {},
    stats: {
      posts: 0,
      likes: 0,
      following: 0,
      followers: 0
    },
    defaultAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'
  },

  onLoad() {
    this.setData({
      isDarkMode: wx.getStorageSync('darkMode') || false
    })
    this.loadUserInfo()
  },

  onShow() {
    this.setData({
      isDarkMode: wx.getStorageSync('darkMode') || false
    })
    this.loadUserInfo()
  },

  async loadUserInfo() {
    const token = wx.getStorageSync('token')
    if (!token) {
      this.setData({ isLoggedIn: false })
      return
    }

    try {
      const res = await app.request({
        url: '/users/profile'
      })
      if (res.code === 200) {
        this.setData({
          user: res.data,
          isLoggedIn: true
        })
        this.loadStats()
      } else {
        this.setData({ isLoggedIn: false })
      }
    } catch (error) {
      this.setData({ isLoggedIn: false })
    }
  },

  async loadStats() {
    try {
      const res = await app.request({
        url: '/users/stats'
      })
      if (res.code === 200) {
        this.setData({ stats: res.data })
      }
    } catch (error) {
      console.error('加载统计失败', error)
    }
  },

  toggleTheme() {
    const newMode = !this.data.isDarkMode
    this.setData({ isDarkMode: newMode })
    wx.setStorageSync('darkMode', newMode)
  },

  editProfile() {
    wx.navigateTo({
      url: '/pages/settings/settings'
    })
  },

  goToMyPosts() {
    if (!this.checkLogin()) return
    wx.navigateTo({
      url: '/pages/my-posts/my-posts'
    })
  },

  goToMyComments() {
    if (!this.checkLogin()) return
    wx.navigateTo({
      url: '/pages/my-comments/my-comments'
    })
  },

  goToMyCollections() {
    if (!this.checkLogin()) return
    wx.navigateTo({
      url: '/pages/my-collections/my-collections'
    })
  },

  goToHistory() {
    if (!this.checkLogin()) return
    wx.navigateTo({
      url: '/pages/history/history'
    })
  },

  goToSettings() {
    if (!this.checkLogin()) return
    wx.navigateTo({
      url: '/pages/settings/settings'
    })
  },

  goToAbout() {
    wx.navigateTo({
      url: '/pages/about/about'
    })
  },

  goToLogin() {
    wx.navigateTo({
      url: '/pages/login/login'
    })
  },

  checkLogin() {
    if (!this.data.isLoggedIn) {
      wx.navigateTo({
        url: '/pages/login/login'
      })
      return false
    }
    return true
  },

  async logout() {
    const token = wx.getStorageSync('token')
    if (token) {
      await app.request({
        url: '/users/logout',
        method: 'POST'
      })
    }
    wx.removeStorageSync('token')
    wx.removeStorageSync('userId')
    wx.removeStorageSync('username')
    this.setData({
      isLoggedIn: false,
      user: {}
    })
    wx.switchTab({
      url: '/pages/index/index'
    })
  }
})
