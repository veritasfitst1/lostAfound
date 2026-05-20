const { get, post, put } = require('../../utils/request')
const { getUserInfo, logout } = require('../../utils/auth')
const { API_BASE, resolveImageUrl } = require('../../utils/config')
//展示用户信息，同时整理成更适合页面显示的格式
function displayUser(raw) {
  if (!raw) return null
  return Object.assign({}, raw, {
    displayAvatarUrl: raw.avatarUrl ? resolveImageUrl(raw.avatarUrl) : ''
  })
}

Page({
  data: {
    user: null,
    unreadCount: 0   //未读信息数目
  },
//加载时执行
  onShow() {
    this.setData({ user: displayUser(getUserInfo()) })
    const u = getUserInfo()
    if (u) {
      get('/api/messages/unread-count').then(res => {
        this.setData({ unreadCount: res.data || 0 })
      }).catch(() => {})
    }
  },
//修改头像
  onChangeAvatar() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],   //只允许选图片
      success: (res) => {
        const file = res.tempFiles[0]  
        wx.showLoading({ title: '上传中' })
        const token = wx.getStorageSync('token')
        wx.uploadFile({
          url: API_BASE + '/api/image/upload',
          filePath: file.tempFilePath,
          name: 'file',
          header: { Authorization: 'Bearer ' + token },
          success: (r) => {
            try {
              const body = JSON.parse(r.data)
              const path = body.data
              if (!path) throw new Error('上传失败')
              this.updateProfile({ avatarUrl: path })
            } catch (e) {
              wx.hideLoading()
              wx.showToast({ title: '上传失败', icon: 'none' })
            }
          },
          fail: () => {
            wx.hideLoading()
            wx.showToast({ title: '上传失败', icon: 'none' })
          }
        })
      }
    })
  },
//绑定微信
  bindWeChat() {
    wx.showLoading({ title: '绑定中' })
    wx.login({
      success: (r) => {
        if (!r.code) {
          wx.hideLoading()
          return wx.showToast({ title: '获取登录凭证失败', icon: 'none' })
        }
        post('/api/users/bind-wx', { code: r.code }) //给后端code  产生openid绑定用户
          .then(res => {
            wx.hideLoading()
            wx.setStorageSync('userInfo', res.data)
            this.setData({ user: displayUser(res.data) })
            wx.showToast({ title: '绑定成功' })
          })
          .catch(err => {
            wx.hideLoading()
            wx.showToast({ title: err.message || '绑定失败', icon: 'none' })
          })
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '微信登录失败', icon: 'none' })
      }
    })
  },
//我的失物
  toMyLost() {
    wx.navigateTo({ url: '/pages/my-lost/my-lost' })
  },
  //我的招领
  toMyFound() {
    wx.navigateTo({ url: '/pages/my-found/my-found' })
  },
//登出
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
  onEditUsername() {
    wx.showModal({
      title: '修改账号',
      editable: true,
      placeholderText: this.data.user?.username || '',
      success: (res) => {
        if (!res.confirm || !res.content) return
        const u = res.content.trim()
        if (u.length < 3 || u.length > 20) {
          return wx.showToast({ title: '用户名须为3-20位', icon: 'none' })
        }
        this.updateProfile({ username: u })
      }
    })
  },
  onEditPassword() {
    wx.navigateTo({ url: '/pages/change-password/change-password' })
  },
  updateProfile(data) {
    put('/api/users/me', data).then(res => {
      wx.hideLoading()
      wx.setStorageSync('userInfo', res.data)
      this.setData({ user: displayUser(res.data) })
      wx.showToast({ title: '更新成功' })
    }).catch(err => {
      wx.hideLoading()
      wx.showToast({ title: err.message || '更新失败', icon: 'none' })
    })
  }
})
