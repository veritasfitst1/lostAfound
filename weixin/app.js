//const { checkLogin } = require('./utils/auth')

App({   
  onLaunch() {
    const token = wx.getStorageSync('token')   //从本地缓存读取数据
    if (!token) {
      wx.reLaunch({ url: '/pages/login/login' })
    }
  },
  globalData: {
    userInfo: null
  }
})
