const { wxLogin } = require('../../utils/auth')
const { post } = require('../../utils/request')

Page({
  data: {
    isRegister: false,
    username: '',
    password: '',
    nickname: ''
  },
  //点击登录或注册时触发  clear
  toggleMode() {
    this.setData({ isRegister: !this.data.isRegister, username: '', password: '', nickname: '' })
  },
  onUsernameInput(e) { this.setData({ username: e.detail.value }) },
  onPasswordInput(e) { this.setData({ password: e.detail.value }) },
  onNicknameInput(e) { this.setData({ nickname: e.detail.value }) },

  //微信一键登录
  onWxLogin() {
    wx.showLoading({ title: '登录中' })
    wxLogin()
      .then(() => {   //成功则进入主页
        wx.hideLoading()
        wx.reLaunch({ url: '/pages/index/index' })
      })
      .catch(err => {    
        wx.hideLoading()
        wx.showToast({ title: err.message || '登录失败', icon: 'none' })
      })
  },
//登录键
  onLogin() {
    const { username, password } = this.data
    if (!username.trim() || !password) return wx.showToast({ title: '请输入用户名和密码', icon: 'none' })
    wx.showLoading({ title: '登录中' })
    post('/api/auth/login', { username: username.trim(), password })  //向后端发请求
      .then(res => {   //存返回数据
        wx.setStorageSync('token', res.data.token)
        wx.setStorageSync('userInfo', res.data.user)
        wx.hideLoading()
        wx.reLaunch({ url: '/pages/index/index' })
      })
      .catch(err => {
        wx.hideLoading()
        wx.showToast({ title: err.message || '登录失败', icon: 'none' })
      })
  },
//注册键
  onRegister() {
    const { username, password, nickname } = this.data
    if (!username.trim() || !password) return wx.showToast({ title: '请输入用户名和密码', icon: 'none' })
    if (username.trim().length < 3) return wx.showToast({ title: '用户名至少3位', icon: 'none' })
    if (password.length < 6) return wx.showToast({ title: '密码至少6位', icon: 'none' })
    wx.showLoading({ title: '注册中' })
    post('/api/auth/register', { username: username.trim(), password, nickname: nickname.trim() || undefined })
      .then(res => {
        wx.setStorageSync('token', res.data.token)
        wx.setStorageSync('userInfo', res.data.user)
        wx.hideLoading()
        wx.showToast({ title: '注册成功' })
        setTimeout(() => wx.reLaunch({ url: '/pages/index/index' }), 1000)
      })
      .catch(err => {
        wx.hideLoading()
        wx.showToast({ title: err.message || '注册失败', icon: 'none' })
      })
  }
})
