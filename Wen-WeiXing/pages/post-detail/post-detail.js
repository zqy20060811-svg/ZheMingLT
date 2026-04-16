// post-detail.js
const app = getApp();

Page({
  data: {
    post: {},
    comments: [],
    commentContent: '',
    replyContent: '',
    replyTo: null,
    isLiked: false,
    isCollected: false
  },
  
  // 加载帖子详情
  loadPostDetail: function() {
    const postId = this.options.id;
    
    wx.request({
      url: app.globalData.baseUrl + `/posts/${postId}`,
      header: {
        'Authorization': `Bearer ${app.getToken()}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            post: res.data.data
          });
        }
      }
    });
  },
  
  // 加载评论
  loadComments: function() {
    const postId = this.options.id;
    
    wx.request({
      url: app.globalData.baseUrl + `/comments?postId=${postId}&page=1&size=20`,
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            comments: res.data.data.list
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
  
  // 显示回复表单
  showReplyForm: function(e) {
    const commentId = e.currentTarget.dataset.id;
    this.setData({
      replyTo: this.data.replyTo === commentId ? null : commentId,
      replyContent: ''
    });
  },
  
  // 提交评论
  submitComment: function() {
    const content = this.data.commentContent;
    if (!content) {
      wx.showToast({
        title: '请输入评论内容',
        icon: 'none'
      });
      return;
    }
    
    const postId = this.options.id;
    wx.request({
      url: app.globalData.baseUrl + '/comments',
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${app.getToken()}`
      },
      data: {
        postId: postId,
        content: content,
        parentId: 0
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            commentContent: ''
          });
          this.loadComments();
          wx.showToast({
            title: '评论成功',
            icon: 'success'
          });
        } else {
          wx.showToast({
            title: res.data.message || '评论失败',
            icon: 'none'
          });
        }
      }
    });
  },
  
  // 提交回复
  submitReply: function(e) {
    const content = this.data.replyContent;
    if (!content) {
      wx.showToast({
        title: '请输入回复内容',
        icon: 'none'
      });
      return;
    }
    
    const postId = this.options.id;
    const commentId = e.currentTarget.dataset.id;
    
    wx.request({
      url: app.globalData.baseUrl + '/comments',
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${app.getToken()}`
      },
      data: {
        postId: postId,
        content: content,
        parentId: commentId
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            replyContent: '',
            replyTo: null
          });
          this.loadComments();
          wx.showToast({
            title: '回复成功',
            icon: 'success'
          });
        } else {
          wx.showToast({
            title: res.data.message || '回复失败',
            icon: 'none'
          });
        }
      }
    });
  },
  
  // 点赞帖子
  likePost: function() {
    const postId = this.options.id;
    
    wx.request({
      url: app.globalData.baseUrl + '/likes',
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${app.getToken()}`
      },
      data: {
        postId: postId,
        commentId: null
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            isLiked: true,
            'post.likeCount': this.data.post.likeCount + 1
          });
          wx.showToast({
            title: '点赞成功',
            icon: 'success'
          });
        } else {
          wx.showToast({
            title: res.data.message || '点赞失败',
            icon: 'none'
          });
        }
      }
    });
  },
  
  // 收藏帖子
  collectPost: function() {
    const postId = this.options.id;
    
    wx.request({
      url: app.globalData.baseUrl + '/collections',
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${app.getToken()}`
      },
      data: {
        postId: postId
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            isCollected: true
          });
          wx.showToast({
            title: '收藏成功',
            icon: 'success'
          });
        } else {
          wx.showToast({
            title: res.data.message || '收藏失败',
            icon: 'none'
          });
        }
      }
    });
  },
  
  onLoad: function(options) {
    // 检查登录状态
    if (!app.checkLogin()) {
      wx.redirectTo({
        url: '/pages/login/login'
      });
      return;
    }
    
    this.loadPostDetail();
    this.loadComments();
  }
})