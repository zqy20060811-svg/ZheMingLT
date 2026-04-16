// create-post.js
const app = getApp();

Page({
  data: {
    title: '',
    content: '',
    summary: '',
    categories: [],
    categoryIndex: 0,
    selectedCategory: {},
    loading: false
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
  
  // 输入框内容变化
  inputChange: function(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({
      [field]: e.detail.value
    });
  },
  
  // 分类选择
  bindCategoryChange: function(e) {
    const index = e.detail.value;
    this.setData({
      categoryIndex: index,
      selectedCategory: this.data.categories[index]
    });
  },
  
  // 生成摘要
  generateSummary: function() {
    const content = this.data.content;
    if (!content) {
      wx.showToast({
        title: '请先输入帖子内容',
        icon: 'none'
      });
      return;
    }
    
    wx.showLoading({
      title: '生成摘要中...'
    });
    
    wx.request({
      url: app.globalData.baseUrl + '/ai/summary',
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${app.getToken()}`
      },
      data: {
        content: content
      },
      success: (res) => {
        wx.hideLoading();
        
        if (res.data.code === 200) {
          this.setData({
            summary: res.data.data.summary
          });
          wx.showToast({
            title: '摘要生成成功',
            icon: 'success'
          });
        } else {
          wx.showToast({
            title: res.data.message || '摘要生成失败',
            icon: 'none'
          });
        }
      },
      fail: (err) => {
        wx.hideLoading();
        wx.showToast({
          title: '摘要生成失败',
          icon: 'none'
        });
      }
    });
  },
  
  // 发布帖子
  createPost: function(e) {
    const { title, content, selectedCategory } = this.data;
    
    if (!title || !content || !selectedCategory.id) {
      wx.showToast({
        title: '请填写完整信息',
        icon: 'none'
      });
      return;
    }
    
    this.setData({ loading: true });
    
    wx.request({
      url: app.globalData.baseUrl + '/posts',
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${app.getToken()}`
      },
      data: {
        title: title,
        content: content,
        categoryId: selectedCategory.id
      },
      success: (res) => {
        this.setData({ loading: false });
        
        if (res.data.code === 200) {
          wx.showToast({
            title: '发布成功，等待审核',
            icon: 'success'
          });
          wx.switchTab({
            url: '/pages/index/index'
          });
        } else {
          wx.showToast({
            title: res.data.message || '发布失败',
            icon: 'none'
          });
        }
      },
      fail: (err) => {
        this.setData({ loading: false });
        wx.showToast({
          title: '发布失败',
          icon: 'none'
        });
      }
    });
  },
  
  onLoad: function() {
    // 检查登录状态
    if (!app.checkLogin()) {
      wx.redirectTo({
        url: '/pages/login/login'
      });
      return;
    }
    
    this.loadCategories();
  }
})