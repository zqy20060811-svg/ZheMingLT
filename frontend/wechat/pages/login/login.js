// login.js
const app = getApp();

Page({
  data: {
    username: '',
    password: '',
    loading: false,
    theme: 'light'
  },
  
  // 输入变化
  inputChange: function(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({
      [field]: e.detail.value
    });
  },
  
  // 切换主题
  toggleTheme: function() {
    const newTheme = app.toggleTheme();
    this.setData({
      theme: newTheme
    });
  },
  
  // 登录
  login: function(e) {
    const { username, password } = e.detail.value;
    
    if (!username || !password) {
      wx.showToast({
        title: '请输入用户名和密码',
        icon: 'none'
      });
      return;
    }
    
    this.setData({ loading: true });
    
    app.login(username, password, (success, data) => {
      this.setData({ loading: false });
      
      if (success) {
        wx.showToast({
          title: '登录成功',
          icon: 'success'
        });
        wx.switchTab({
          url: '/pages/index/index'
        });
      } else {
        wx.showToast({
          title: data.message || '登录失败',
          icon: 'none'
        });
      }
    });
  },
  
  onLoad: function() {
    // 检查是否已登录
    if (app.checkLogin()) {
      wx.switchTab({
        url: '/pages/index/index'
      });
      return;
    }
    
    // 获取当前主题
    this.setData({
      theme: app.getTheme()
    });
  }
})