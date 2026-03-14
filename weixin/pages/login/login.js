const { wxLogin } = require('../../utils/auth')

Page({
  onLogin() {
    wx.showLoading({ title: '登录中' })
    wxLogin()
      .then(() => {
        wx.hideLoading()
        wx.reLaunch({ url: '/pages/index/index' })
      })
      .catch(err => {
        wx.hideLoading()
        wx.showToast({ title: err.message || '登录失败', icon: 'none' })
      })
  }
})
