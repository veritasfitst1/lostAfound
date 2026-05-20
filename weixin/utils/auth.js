const { post } = require('./request')
//异步函数，微信登录
async function wxLogin() {
  return new Promise((resolve, reject) => {
    wx.login({   //向微信申请一个临时登录凭证 code
      success: async (res) => {
        if (res.code) {
          try {
            const resp = await post('/api/auth/wx-login', { code: res.code })  //将code传给后端，后端根据 code 去微信服务器换取用户身份信息，后端再把 token、用户信息等返回给前端
            const data = resp.data
            wx.setStorageSync('token', data.token)
            wx.setStorageSync('userInfo', data.user)
            resolve(data)
          } catch (e) {
            reject(e)
          }
        } else {
          reject(new Error('登录失败'))
        }
      },
      fail: reject
    })
  })
}
//检查用户是否登录
function checkLogin() {
  const token = wx.getStorageSync('token')
  return !!token
}
//登出
function logout() {
  wx.removeStorageSync('token')
  wx.removeStorageSync('userInfo')
}
//获取登录的用户信息
function getUserInfo() {
  return wx.getStorageSync('userInfo') || null
}

module.exports = { wxLogin, checkLogin, logout, getUserInfo }
