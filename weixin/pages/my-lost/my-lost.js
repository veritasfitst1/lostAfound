const { get, put } = require('../../utils/request')
const { resolveItemImages } = require('../../utils/config')

Page({
  data: {
    items: []
  },
//加载时调用
  onShow() {
    this.loadItems()
  },
//获取物品列表
  loadItems() {
    get('/api/items/my/lost').then(res => {
      this.setData({ items: (res.data || []).map(resolveItemImages) })
    }).catch(() => this.setData({ items: [] }))
  },
//点击卡片，进入详情页
  toDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/item-detail/item-detail?id=${id}` })
  },
//取消撤销
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
//已找回
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
//撤销
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
