const { get, post } = require('../../utils/request')
const { resolveItemImages, resolveImageUrl } = require('../../utils/config')

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
      this.setData({ item: resolveItemImages(res.data) })
    }).catch(() => wx.showToast({ title: '加载失败', icon: 'none' }))
  },

  loadComments() {
    get(`/api/items/${this.data.id}/comments`).then(res => {
      const list = (res.data || []).map(c => {
        if (c.userAvatarUrl) c.userAvatarUrl = resolveImageUrl(c.userAvatarUrl)
        return c
      })
      this.setData({ comments: list })
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

  onReport() {
    const item = this.data.item
    if (!item) return
    wx.showActionSheet({
      itemList: ['举报该物品', '举报该用户', '举报物品和用户'],
      success: (res) => {
        const tapIndex = res.tapIndex
        const params = { reason: '' }
        if (tapIndex === 0) {
          params.reportedItemId = item.id
        } else if (tapIndex === 1) {
          params.reportedUserId = item.userId
        } else {
          params.reportedItemId = item.id
          params.reportedUserId = item.userId
        }
        wx.showModal({
          title: '举报理由',
          editable: true,
          placeholderText: '请输入举报理由',
          success: (r) => {
            if (!r.confirm || !r.content || !r.content.trim()) {
              return wx.showToast({ title: '请输入举报理由', icon: 'none' })
            }
            params.reason = r.content.trim()
            post('/api/reports', params).then(() => {
              wx.showToast({ title: '举报已提交' })
            }).catch(err => {
              wx.showToast({ title: err.message || '举报失败', icon: 'none' })
            })
          }
        })
      }
    })
  },

  toChat() {
    const item = this.data.item
    if (!item) return
    wx.navigateTo({ url: `/pages/chat/chat?userId=${item.userId}` })
  }
})
