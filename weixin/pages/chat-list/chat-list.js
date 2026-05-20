const { get } = require('../../utils/request')
const { resolveImageUrl } = require('../../utils/config')

Page({
  data: {
    conversations: []
  },

  onShow() {
    this.loadConversations()
  },
//加载会话列表
  loadConversations() {
    get('/api/messages/conversations').then(res => {
      const list = (res.data || []).map(item => ({
        ...item,
        otherUserAvatarUrl: item.otherUserAvatarUrl ? resolveImageUrl(item.otherUserAvatarUrl) : '/assets/icons/user.png'
      }))
      this.setData({ conversations: list })
    }).catch(() => this.setData({ conversations: [] }))
  },
//头像加载失败处理
  onAvatarError(e) {
    const idx = e.currentTarget.dataset.idx
    const conversations = [...this.data.conversations]
    if (!conversations[idx]) return
    conversations[idx].otherUserAvatarUrl = '/assets/icons/user.png'  //改成默认头像
    this.setData({ conversations })
  },
//点击卡片进入聊天界面
  toChat(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/chat/chat?userId=${id}` })
  }
})
