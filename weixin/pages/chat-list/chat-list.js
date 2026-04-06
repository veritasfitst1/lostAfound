const { get } = require('../../utils/request')
const { resolveImageUrl } = require('../../utils/config')

Page({
  data: {
    conversations: []
  },

  onShow() {
    this.loadConversations()
  },

  loadConversations() {
    get('/api/messages/conversations').then(res => {
      const list = (res.data || []).map(item => ({
        ...item,
        otherUserAvatarUrl: item.otherUserAvatarUrl ? resolveImageUrl(item.otherUserAvatarUrl) : '/assets/icons/user.png'
      }))
      this.setData({ conversations: list })
    }).catch(() => this.setData({ conversations: [] }))
  },

  onAvatarError(e) {
    const idx = e.currentTarget.dataset.idx
    const conversations = [...this.data.conversations]
    if (!conversations[idx]) return
    conversations[idx].otherUserAvatarUrl = '/assets/icons/user.png'
    this.setData({ conversations })
  },

  toChat(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/chat/chat?userId=${id}` })
  }
})
