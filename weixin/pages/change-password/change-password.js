const { put } = require('../../utils/request')

Page({
  data: {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  },

  onOldPasswordInput(e) {
    this.setData({ oldPassword: e.detail.value })
  },

  onNewPasswordInput(e) {
    this.setData({ newPassword: e.detail.value })
  },

  onConfirmPasswordInput(e) {
    this.setData({ confirmPassword: e.detail.value })
  },

  onSubmit() {
    const { oldPassword, newPassword, confirmPassword } = this.data
    if (!oldPassword.trim()) {
      return wx.showToast({ title: '请输入原密码', icon: 'none' })
    }
    if (!newPassword) {
      return wx.showToast({ title: '请输入新密码', icon: 'none' })
    }
    if (!confirmPassword) {
      return wx.showToast({ title: '请再次输入新密码', icon: 'none' })
    }
    if (newPassword.length < 6 || newPassword.length > 32) {
      return wx.showToast({ title: '新密码须为6-32位', icon: 'none' })
    }
    if (newPassword !== confirmPassword) {
      return wx.showToast({ title: '两次新密码不一致', icon: 'none' })
    }
    wx.showLoading({ title: '提交中' })
    put('/api/users/me', { oldPassword: oldPassword.trim(), password: newPassword })
      .then((res) => {
        wx.hideLoading()
        if (res.data) {
          wx.setStorageSync('userInfo', res.data)
        }
        wx.showToast({ title: '修改成功' })
        setTimeout(() => wx.navigateBack(), 1200)
      })
      .catch((err) => {
        wx.hideLoading()
        wx.showToast({ title: err.message || '修改失败', icon: 'none' })
      })
  }
})
