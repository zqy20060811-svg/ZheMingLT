// register.js
const app = getApp();

Page({
  data: {
    username: '',
    password: '',
    email: '',
    phone: '',
    loading: false
  },
  
  // 输入框内容变化
  inputChange: function(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({
      [field]: e.detail.value
    });
  },
  
  // 注册
  register: function(e) {
    const { username, password, email, phone } = this.data;
    
    if (!username || !password || !email || !phone) {
      wx.showToast({
        title: '请填写完整信息',
        icon: 'none'
      });
      return;
    }
    
    this.setData({ loading: true });
    
    app.register({
      username: username,
      password: password,
      email: email,
      phone: phone
    }, (success, data) => {
      this.setData({ loading: false });
      
      if (success) {
        wx.showToast({
          title: '注册成功',
          icon: 'success'
        });
        wx.switchTab({
          url: '/pages/index/index'
        });
      } else {
        wx.showToast({
          title: data.message || '注册失败',
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
    }
  }
})