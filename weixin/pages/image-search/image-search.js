const { post } = require('../../utils/request')
const { API_BASE } = require('../../utils/config')

Page({
  data: {
    loading: false
  },

  goBack() {
    wx.navigateBack()
  },

  onUploadImage() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      success: (res) => {
        const file = res.tempFiles && res.tempFiles[0]
        if (!file) return
        const token = wx.getStorageSync('token')
        this.setData({ loading: true })
        wx.uploadFile({
          url: API_BASE + '/api/image/upload',
          filePath: file.tempFilePath,
          name: 'file',
          header: { Authorization: 'Bearer ' + token },
          success: (r) => {
            let imageUrl = ''
            try {
              const uploadResp = JSON.parse(r.data)
              imageUrl = uploadResp.data
            } catch (e) {
              this.setData({ loading: false })
              wx.showToast({ title: '上传失败', icon: 'none' })
              return
            }
            post('/api/image/recognize', { imageUrl }).then(resp => {
              this.setData({ loading: false })
              const data = resp.data || {}
              const keywords = data.keywords || []
              const items = data.items || []
              wx.setStorageSync('imageSearchResult', {
                ts: Date.now(),
                success: true,
                keywords: keywords,
                items: items,
                total: data.total || 0
              })
              wx.showToast({ title: '识别成功', icon: 'success' })
              setTimeout(() => {
                wx.switchTab({ url: '/pages/index/index' })
              }, 800)
            }).catch(() => {
              this.setData({ loading: false })
              wx.showToast({ title: '识别失败', icon: 'none' })
            })
          },
          fail: () => {
            this.setData({ loading: false })
            wx.showToast({ title: '上传失败', icon: 'none' })
          }
        })
      }
    })
  }
})
