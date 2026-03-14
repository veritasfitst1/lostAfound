const { get, post } = require('../../utils/request')

Page({
  data: {
    id: null,
    item: null,
    comments: [],
    commentContent: ''
  },

  onLoad(opt) {
    this.setData({ id: opt.id })
    this.loadDetail()
    this.loadComments()
  },

  loadDetail() {
    get(`/api/items/${this.data.id}`).then(res => {
      this.setData({ item: res.data })
    }).catch(() => wx.showToast({ title: '加载失败', icon: 'none' }))
  },

  loadComments() {
    get(`/api/items/${this.data.id}/comments`).then(res => {
      this.setData({ comments: res.data || [] })
    })
  },

  onCommentInput(e) {
    this.setData({ commentContent: e.detail.value })
  },

  submitComment() {
    const content = this.data.commentContent.trim()
    if (!content) return wx.showToast({ title: '请输入留言', icon: 'none' })
    post(`/api/items/${this.data.id}/comments`, { content }).then(() => {
      this.setData({ commentContent: '' })
      this.loadComments()
      wx.showToast({ title: '发表成功' })
    }).catch(err => wx.showToast({ title: err.message || '发表失败', icon: 'none' }))
  },

  toChat() {
    const item = this.data.item
    if (!item) return
    wx.navigateTo({ url: `/pages/chat/chat?userId=${item.userId}` })
  }
})
