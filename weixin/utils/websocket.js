const { WS_BASE } = require('./config')

let ws = null   //连接对象
let onMessageCb = null     //存储“收到消息时要做什么”
//建立聊天连接
function connectChat(token, onMessage, onOpen, onClose) {  //传入onMessage
  if (onMessage) onMessageCb = onMessage  //没收到消息就null
  if (ws && ws.readyState === 1) { //已经连接了
    onOpen && onOpen()
    return ws
  }
  const url = `${WS_BASE.replace(/^http/, 'ws')}/ws/chat?token=${encodeURIComponent(token)}`
  ws = wx.connectSocket({
    url,
    success: () => {}
  })
  //连接成功
  ws.onOpen(() => {
    onOpen && onOpen()
  })
  //收到消息
  ws.onMessage((e) => {
    try {
      const data = JSON.parse(e.data)
      ;(onMessage || onMessageCb) && (onMessage || onMessageCb)(data)  //优先用传的 onMessage，否则用 onMessageCb
    } catch (err) {}
  })
  ws.onClose(() => {
    onClose && onClose()
  })
  ws.onError(() => {
    onClose && onClose()
  })
  return ws
}

function sendMessage(data) {
  if (ws && ws.readyState === 1) { //连上才发
    ws.send({ data: JSON.stringify(data) })
  }
}

function closeChat() {
  if (ws) {
    try { ws.close() } catch (e) {}
    ws = null
  }
  onMessageCb = null
}

module.exports = { connectChat, sendMessage, closeChat }
