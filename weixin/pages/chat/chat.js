const { get } = require('../../utils/request')
const { connectChat, sendMessage, closeChat } = require('../../utils/websocket')
const { getUserInfo } = require('../../utils/auth')

Page({
  data: {
    myUserId: null,
    otherUserId: null,
    messages: [],
    inputContent: ''
  },

  onLoad(opt) {
    const myUserId = (getUserInfo() || {}).id
    const otherUserId = parseInt(opt.userId)
    this.setData({ myUserId, otherUserId })
    this.loadHistory()
  },

  onUnload() {
    closeChat()
  },

  loadHistory() {
    get(`/api/messages/conversation/${this.data.otherUserId}`).then(res => {
      const msgs = res.data || []
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
        this.setData({ messages: [...prev, data] })
      }
    })
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
