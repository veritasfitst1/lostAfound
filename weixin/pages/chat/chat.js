const { get } = require('../../utils/request')
const { connectChat, sendMessage, closeChat } = require('../../utils/websocket')
const { getUserInfo } = require('../../utils/auth')
const { resolveImageUrl } = require('../../utils/config')

Page({
  data: {
    myUserId: null,
    otherUserId: null,
    messages: [],
    inputContent: '',
    myAvatarUrl: '/assets/icons/user.png'
  },

  onLoad(opt) {
    const myUser = getUserInfo() || {}
    const myUserId = myUser.id
    const otherUserId = parseInt(opt.userId)
    this.setData({
      myUserId,
      otherUserId,
      myAvatarUrl: myUser.avatarUrl ? resolveImageUrl(myUser.avatarUrl) : '/assets/icons/user.png'
    })
    this.loadHistory()
  },

  onUnload() {
    closeChat()
  },

  loadHistory() {
    get(`/api/messages/conversation/${this.data.otherUserId}`).then(res => {
      const msgs = (res.data || []).map(this.decorateMessage.bind(this))
      this.setData({ messages: msgs })
      this.connectWs()
    }).catch(() => this.connectWs())
  },

  connectWs() {
    const token = wx.getStorageSync('token')
    if (!token) return
    connectChat(token, (data) => {
      if (data.type === 'message' && (data.senderId === this.data.otherUserId || data.receiverId === this.data.otherUserId)) {
        const prev = this.data.messages
        this.setData({ messages: [...prev, this.decorateMessage(data)] })
      }
    })
  },

  decorateMessage(msg) {
    const senderAvatar = msg.senderAvatarUrl ? resolveImageUrl(msg.senderAvatarUrl) : '/assets/icons/user.png'
    return {
      ...msg,
      avatarUrl: msg.senderId === this.data.myUserId ? this.data.myAvatarUrl : senderAvatar
    }
  },

  onAvatarError(e) {
    const idx = e.currentTarget.dataset.idx
    const messages = [...this.data.messages]
    if (!messages[idx]) return
    messages[idx].avatarUrl = '/assets/icons/user.png'
    this.setData({ messages })
  },

  onInput(e) {
    this.setData({ inputContent: e.detail.value })
  },

  send() {
    const content = this.data.inputContent.trim()
    if (!content) return
    const token = wx.getStorageSync('token')
    connectChat(token, null, null, null)
    sendMessage({
      receiverId: this.data.otherUserId,
      content,
      msgType: 0
    })
    this.setData({ inputContent: '' })
  }
})
