const { get } = require('../../utils/request')

Page({
  data: {
    conversations: []
  },

  onShow() {
    this.loadConversations()
  },

  loadConversations() {
    get('/api/messages/conversations').then(res => {
      this.setData({ conversations: res.data || [] })
    }).catch(() => this.setData({ conversations: [] }))
  },

  toChat(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/chat/chat?userId=${id}` })
  }
})
