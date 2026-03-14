const { WS_BASE } = require('./config')

let ws = null
let onMessageCb = null

function connectChat(token, onMessage, onOpen, onClose) {
  if (onMessage) onMessageCb = onMessage
  if (ws && ws.readyState === 1) {
    onOpen && onOpen()
    return ws
  }
  const url = `${WS_BASE.replace(/^http/, 'ws')}/ws/chat?token=${encodeURIComponent(token)}`
  ws = wx.connectSocket({
    url,
    success: () => {}
  })
  ws.onOpen(() => {
    onOpen && onOpen()
  })
  ws.onMessage((e) => {
    try {
      const data = JSON.parse(e.data)
      ;(onMessage || onMessageCb) && (onMessage || onMessageCb)(data)
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
  if (ws && ws.readyState === 1) {
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
