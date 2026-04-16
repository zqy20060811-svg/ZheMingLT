// app.js
App({
  globalData: {
    userInfo: null,
    token: null,
    baseUrl: 'http://localhost:8080/api',
    theme: 'light' // 默认亮色主题
  },
  
  onLaunch: function() {
    // 从本地存储获取token
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    
    if (token && userInfo) {
      this.globalData.token = token;
      this.globalData.userInfo = userInfo;
    }
    
    // 从本地存储获取主题设置
    const savedTheme = wx.getStorageSync('theme');
    if (savedTheme) {
      this.globalData.theme = savedTheme;
      this.applyTheme(savedTheme);
    }
  },
  
  // 应用主题
  applyTheme: function(theme) {
    if (theme === 'dark') {
      wx.setNavigationBarColor({
        frontColor: '#ffffff',
        backgroundColor: '#121212'
      });
      wx.setTabBarStyle({
        backgroundColor: '#1e1e1e',
        selectedColor: '#6b8cbe',
        color: '#b0b0b0'
      });
    } else {
      wx.setNavigationBarColor({
        frontColor: '#000000',
        backgroundColor: '#4a6fa5'
      });
      wx.setTabBarStyle({
        backgroundColor: '#ffffff',
        selectedColor: '#4a6fa5',
        color: '#666666'
      });
    }
  },
  
  // 切换主题
  toggleTheme: function() {
    const newTheme = this.globalData.theme === 'light' ? 'dark' : 'light';
    this.globalData.theme = newTheme;
    wx.setStorageSync('theme', newTheme);
    this.applyTheme(newTheme);
    return newTheme;
  },
  
  // 获取当前主题
  getTheme: function() {
    return this.globalData.theme;
  },
  
  // 登录方法
  login: function(username, password, callback) {
    wx.request({
      url: this.globalData.baseUrl + '/users/login',
      method: 'POST',
      data: {
        username: username,
        password: password
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.globalData.token = res.data.data.token;
          this.globalData.userInfo = res.data.data;
          wx.setStorageSync('token', res.data.data.token);
          wx.setStorageSync('userInfo', res.data.data);
          callback(true, res.data);
        } else {
          callback(false, res.data);
        }
      },
      fail: (err) => {
        callback(false, err);
      }
    });
  },
  
  // 注册方法
  register: function(userInfo, callback) {
    wx.request({
      url: this.globalData.baseUrl + '/users/register',
      method: 'POST',
      data: userInfo,
      success: (res) => {
        if (res.data.code === 200) {
          this.globalData.token = res.data.data.token;
          this.globalData.userInfo = res.data.data;
          wx.setStorageSync('token', res.data.data.token);
          wx.setStorageSync('userInfo', res.data.data);
          callback(true, res.data);
        } else {
          callback(false, res.data);
        }
      },
      fail: (err) => {
        callback(false, err);
      }
    });
  },
  
  // 退出登录
  logout: function() {
    this.globalData.token = null;
    this.globalData.userInfo = null;
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
  },
  
  // 检查登录状态
  checkLogin: function() {
    return this.globalData.token !== null;
  },
  
  // 获取用户信息
  getUserInfo: function() {
    return this.globalData.userInfo;
  },
  
  // 获取token
  getToken: function() {
    return this.globalData.token;
  }
})