const { get, post } = require('../../utils/request')

Page({
  data: {
    type: 0,
    categories: [],
    categoryIndex: 0,
    categoryId: null,
    title: '',
    description: '',
    location: '',
    contact: '',
    images: []
  },

  onLoad(opt) {
    this.setData({ type: parseInt(opt.type || 0) })
    this.loadCategories()
  },

  loadCategories() {
    get('/api/categories').then(res => {
      this.setData({ categories: res.data || [] })
    })
  },

  onCategoryChange(e) {
    const idx = parseInt(e.detail.value)
    const cat = this.data.categories[idx]
    this.setData({ categoryIndex: idx, categoryId: cat ? cat.id : null })
  },
  onTitleInput(e) { this.setData({ title: e.detail.value }) },
  onDescInput(e) { this.setData({ description: e.detail.value }) },
  onLocationInput(e) { this.setData({ location: e.detail.value }) },
  onContactInput(e) { this.setData({ contact: e.detail.value }) },

  chooseImage() {
    wx.chooseMedia({
      count: 9 - this.data.images.length,
      mediaType: ['image'],
      success: (res) => {
        const files = res.tempFiles
        const { API_BASE } = require('../../utils/config')
        const upload = (file) => new Promise((resolve, reject) => {
          const token = wx.getStorageSync('token')
          wx.uploadFile({
            url: API_BASE + '/api/image/upload',
            filePath: file.tempFilePath,
            name: 'file',
            header: { Authorization: 'Bearer ' + token },
            success: (r) => {
              try {
                const data = JSON.parse(r.data)
                resolve(data.data)
              } catch { reject() }
            },
            fail: reject
          })
        })
        Promise.all(files.map(f => upload(f))).then(urls => {
          this.setData({ images: [...this.data.images, ...urls] })
        })
      }
    })
  },

  removeImage(e) {
    const idx = e.currentTarget.dataset.idx
    const img = [...this.data.images]
    img.splice(idx, 1)
    this.setData({ images: img })
  },

  submit() {
    const { type, categoryId, title, description, location, contact, images } = this.data
    if (!categoryId) return wx.showToast({ title: '请选择分类', icon: 'none' })
    if (!title.trim()) return wx.showToast({ title: '请输入物品名称', icon: 'none' })

    wx.showLoading({ title: '发布中' })
    post('/api/items', {
      type,
      categoryId,
      title: title.trim(),
      description: description.trim(),
      location: location.trim(),
      contact: contact.trim(),
      images
    }).then(() => {
      wx.hideLoading()
      wx.showToast({ title: '发布成功' })
      setTimeout(() => wx.navigateBack(), 1500)
    }).catch(err => {
      wx.hideLoading()
      wx.showToast({ title: err.message || '发布失败', icon: 'none' })
    })
  }
})
