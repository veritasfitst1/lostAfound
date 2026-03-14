const { get } = require('../../utils/request')
const { getUserInfo, logout } = require('../../utils/auth')

Page({
  data: {
    user: null,
    unreadCount: 0
  },

  onShow() {
    const user = getUserInfo()
    this.setData({ user })
    if (user) {
      get('/api/messages/unread-count').then(res => {
        this.setData({ unreadCount: res.data || 0 })
      }).catch(() => {})
    }
  },

  toMyLost() {
    wx.navigateTo({ url: '/pages/my-lost/my-lost' })
  },
  toMyFound() {
    wx.navigateTo({ url: '/pages/my-found/my-found' })
  },
  onLogout() {
    wx.showModal({
      title: '提示',
      content: '确定退出登录？',
      success: (res) => {
        if (res.confirm) {
          logout()
          wx.reLaunch({ url: '/pages/login/login' })
        }
      }
    })
  },
  onEditNickname() {
    wx.showModal({
      title: '修改昵称',
      editable: true,
      placeholderText: this.data.user?.nickname || '',
      success: (res) => {
        if (res.confirm && res.content) {
          this.updateProfile({ nickname: res.content })
        }
      }
    })
  },
  updateProfile(data) {
    const { put } = require('../../utils/request')
    put('/api/users/me', data).then(res => {
      wx.setStorageSync('userInfo', res.data)
      this.setData({ user: res.data })
      wx.showToast({ title: '更新成功' })
    }).catch(err => wx.showToast({ title: err.message || '更新失败', icon: 'none' }))
  }
})
