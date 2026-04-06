// 后端 API 地址，开发时改为本机地址
const API_BASE = 'http://127.0.0.1:8080'
const WS_BASE = 'ws://127.0.0.1:8080'

function resolveImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return API_BASE + url
}

function resolveItemImages(item) {
  if (!item) return item
  if (Array.isArray(item.images)) {
    item.images = item.images.map(resolveImageUrl)
  }
  if (item.userAvatarUrl) {
    item.userAvatarUrl = resolveImageUrl(item.userAvatarUrl)
  }
  if (item.avatarUrl) {
    item.avatarUrl = resolveImageUrl(item.avatarUrl)
  }
  return item
}

module.exports = {
  API_BASE,
  WS_BASE,
  resolveImageUrl,
  resolveItemImages
}
