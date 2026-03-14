const { get, put } = require('../../utils/request')

Page({
  data: {
    items: []
  },

  onShow() {
    this.loadItems()
  },

  loadItems() {
    get('/api/items/my/lost').then(res => {
      this.setData({ items: res.data || [] })
    }).catch(() => this.setData({ items: [] }))
  },

  toDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/item-detail/item-detail?id=${id}` })
  },

  onRevoke(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '确认撤销',
      content: '撤销后该信息将不再展示',
      success: (res) => {
        if (res.confirm) {
          put(`/api/items/${id}/status?status=2`).then(() => {
            wx.showToast({ title: '已撤销' })
            this.loadItems()
          })
        }
      }
    })
  }
})
