const { get, put } = require('../../utils/request')
const { resolveItemImages } = require('../../utils/config')

Page({
  data: {
    items: []
  },

  onShow() {
    this.loadItems()
  },

  loadItems() {
    get('/api/items/my/found').then(res => {
      this.setData({ items: (res.data || []).map(resolveItemImages) })
    }).catch(() => this.setData({ items: [] }))
  },

  toDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/item-detail/item-detail?id=${id}` })
  },

  onUndo(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '取消撤销',
      content: '恢复后该信息将重新展示',
      success: (res) => {
        if (res.confirm) {
          put(`/api/items/${id}/status?status=0`).then(() => {
            wx.showToast({ title: '已恢复' })
            this.loadItems()
          })
        }
      }
    })
  },

  onFound(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '确认找回',
      content: '确认该物品已找回？',
      success: (res) => {
        if (res.confirm) {
          put(`/api/items/${id}/status?status=1`).then(() => {
            wx.showToast({ title: '已标记找回' })
            this.loadItems()
          })
        }
      }
    })
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
