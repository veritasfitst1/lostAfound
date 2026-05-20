const { get, post } = require('../../utils/request')
const { resolveItemImages, resolveImageUrl } = require('../../utils/config')
const { getUserInfo } = require('../../utils/auth')

Page({
  data: {
    id: null,
    item: null,
    isOwnItem: false,
    comments: [],
    commentContent: ''
  },
//加载时调用
  onLoad(opt) {  //opt 是页面跳转时带过来的参数
    this.setData({ id: opt.id })
    this.loadDetail()
    this.loadComments()
  },
  onShow() {
    if (this.data.item) {
      this.syncOwnItem(this.data.item)
    }
  },
//加载物品详情
  loadDetail() {
    get(`/api/items/${this.data.id}`).then(res => {  //获取物品详细信息
      const item = resolveItemImages(res.data)
      this.setData({ item }, () => this.syncOwnItem(item))
    }).catch(() => wx.showToast({ title: '加载失败', icon: 'none' }))
  },
  syncOwnItem(item) {
    const me = getUserInfo()
    const isOwnItem = !!(me && item && me.id != null && item.userId != null &&
      Number(me.id) === Number(item.userId))
    this.setData({ isOwnItem })
  },
//加载评论
  loadComments() {
    get(`/api/items/${this.data.id}/comments`).then(res => {
      const list = (res.data || []).map(c => {
        if (c.userAvatarUrl) c.userAvatarUrl = resolveImageUrl(c.userAvatarUrl)
        return c
      })
      this.setData({ comments: list })
    })
  },
//输入评论
  onCommentInput(e) {
    this.setData({ commentContent: e.detail.value })
  },
//提交评论
  submitComment() {
    const content = this.data.commentContent.trim()
    if (!content) return wx.showToast({ title: '请输入留言', icon: 'none' })
    post(`/api/items/${this.data.id}/comments`, { content }).then(() => {
      this.setData({ commentContent: '' })
      this.loadComments()
      wx.showToast({ title: '发表成功' })
    }).catch(err => wx.showToast({ title: err.message || '发表失败', icon: 'none' }))
  },
//举报
  onReport() {
    const item = this.data.item
    if (!item) return
    wx.showActionSheet({//举报什么 物品还是用户
      itemList: ['举报该物品', '举报该用户', '举报物品和用户'],  //弹出举报选项菜单 0 1 2
      success: (res) => {
        const tapIndex = res.tapIndex
        const params = { reason: '' }
        if (tapIndex === 0) {
          params.reportedItemId = item.id
        } else if (tapIndex === 1) {
          params.reportedUserId = item.userId
        } else {
          params.reportedItemId = item.id
          params.reportedUserId = item.userId
        }
        wx.showModal({
          title: '举报理由',
          editable: true,
          placeholderText: '请输入举报理由',
          success: (r) => {
            if (!r.confirm || !r.content || !r.content.trim()) {
              return wx.showToast({ title: '请输入举报理由', icon: 'none' })
            }
            params.reason = r.content.trim()
            post('/api/reports', params).then(() => {
              wx.showToast({ title: '举报已提交' })
            }).catch(err => {
              wx.showToast({ title: err.message || '举报失败', icon: 'none' })
            })
          }
        })
      }
    })
  },
//联系ta
  toChat() {
    if (this.data.isOwnItem) return
    const item = this.data.item
    if (!item) return
    wx.navigateTo({ url: `/pages/chat/chat?userId=${item.userId}` })
  }
})
