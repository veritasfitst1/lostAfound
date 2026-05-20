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
//加载时自动调用
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
//离开页面执行
  onUnload() {
    closeChat()   //关闭websocket连接
  },
//加载历史聊天记录
  loadHistory() {
    get(`/api/messages/conversation/${this.data.otherUserId}`).then(res => {
      const msgs = (res.data || []).map(this.decorateMessage.bind(this))
      this.setData({ messages: msgs })
      this.connectWs()
    }).catch(() => this.connectWs())
  },
//建立实时聊天连接
  connectWs() {
    const token = wx.getStorageSync('token')
    if (!token) return
    connectChat(token, (data) => {   //websocket连接不断开会一直在，有数据触发
      if (data.type === 'message' && (data.senderId === this.data.otherUserId || data.receiverId === this.data.otherUserId)) {
        const prev = this.data.messages  
        this.setData({ messages: [...prev, this.decorateMessage(data)] })  //取出旧消息，追加新消息
      }
    })
  },
//加工每一条消息，显示发送者头像
  decorateMessage(msg) {
    const senderAvatar = msg.senderAvatarUrl ? resolveImageUrl(msg.senderAvatarUrl) : '/assets/icons/user.png'
    return {
      ...msg,
      avatarUrl: msg.senderId === this.data.myUserId ? this.data.myAvatarUrl : senderAvatar
    }
  },
//头像没有就显示默认
  onAvatarError(e) {
    const idx = e.currentTarget.dataset.idx
    const messages = [...this.data.messages]
    if (!messages[idx]) return
    messages[idx].avatarUrl = '/assets/icons/user.png'
    this.setData({ messages })
  },
//输入消息
  onInput(e) {
    this.setData({ inputContent: e.detail.value })
  },
//发送
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
