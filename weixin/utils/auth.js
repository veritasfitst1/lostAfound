const { post } = require('./request')

async function wxLogin() {
  return new Promise((resolve, reject) => {
    wx.login({
      success: async (res) => {
        if (res.code) {
          try {
            const resp = await post('/api/auth/wx-login', { code: res.code })
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

function checkLogin() {
  const token = wx.getStorageSync('token')
  return !!token
}

function logout() {
  wx.removeStorageSync('token')
  wx.removeStorageSync('userInfo')
}

function getUserInfo() {
  return wx.getStorageSync('userInfo') || null
}

module.exports = { wxLogin, checkLogin, logout, getUserInfo }
