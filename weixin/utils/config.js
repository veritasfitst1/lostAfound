// 后端 API 地址
const API_BASE = 'http://127.0.0.1:8080'
const WS_BASE = 'ws://127.0.0.1:8080'
//如果是完整地址就直接用，如果是相对路径就拼上服务器地址，统一补全成可访问的绝对 URL
function resolveImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return API_BASE + url
}
//修改图片地址
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
