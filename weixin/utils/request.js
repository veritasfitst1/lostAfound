const { API_BASE } = require('./config')

function request(options) {
  const token = wx.getStorageSync('token')
  return new Promise((resolve, reject) => {
    wx.request({
      ...options,
      url: (options.url.startsWith('http') ? '' : API_BASE) + options.url,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: 'Bearer ' + token } : {}),
        ...options.header
      },
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data)
        } else {
          const msg = res.data?.message || '请求失败'
          if (res.statusCode === 401) {
            wx.removeStorageSync('token')
            wx.removeStorageSync('userInfo')
            wx.reLaunch({ url: '/pages/login/login' })
            reject(new Error('请重新登录'))
          } else {
            reject(new Error(msg))
          }
        }
      },
      fail: (err) => reject(err)
    })
  })
}

function get(url, data) {
  return request({ url, method: 'GET', data })
}

function post(url, data) {
  return request({ url, method: 'POST', data })
}

function put(url, data) {
  return request({ url, method: 'PUT', data })
}

function del(url) {
  return request({ url, method: 'DELETE' })
}

module.exports = { request, get, post, put, del }
