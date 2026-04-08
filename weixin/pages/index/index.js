const { get } = require('../../utils/request')
const { resolveItemImages } = require('../../utils/config')

Page({
  data: {
    keyword: '',
    categoryId: null,
    type: null,
    items: [],
    categories: [],
    loading: true,
    page: 0,
    size: 10,
    hasMore: true,
    showFilter: false,
    aiSearching: false,
    aiKeywords: '',
    aiMatchedCategoryId: null,
    scrollIntoView: ''
  },

  onLoad() {
    this.loadCategories()
    this.loadItems(true)
  },

  onShow() {
    const imageSearchResult = wx.getStorageSync('imageSearchResult')
    if (imageSearchResult && imageSearchResult.ts) {
      wx.removeStorageSync('imageSearchResult')
      const nextCategoryId = imageSearchResult.suggestedCategoryId || null
      const keywords = (imageSearchResult.keywords || []).slice(0, 3).join('、')
      this.setData({
        aiSearching: false,
        aiKeywords: keywords || (imageSearchResult.success ? '识别完成' : ''),
        aiMatchedCategoryId: nextCategoryId,
        categoryId: nextCategoryId
      }, () => this.loadItems(true, true))
      return
    }
    this.loadItems(true)
  },

  loadCategories() {
    get('/api/categories').then(res => {
      this.setData({ categories: res.data || [] })
    }).catch(() => {})
  },

  loadItems(refresh, jumpToResult = false) {
    if (refresh) {
      this.setData({ page: 0, hasMore: true })
    }
    if (!this.data.hasMore && !refresh) return
    const { keyword, categoryId, type, page, size } = this.data
    const params = { page: refresh ? 0 : page, size }
    if (keyword) params.keyword = keyword
    if (categoryId) params.categoryId = categoryId
    if (type != null) params.type = type

    this.setData({ loading: true })
    get('/api/items', params).then(res => {
      const list = (res.data?.content || []).map(resolveItemImages)
      const prev = refresh ? [] : this.data.items
      this.setData({
        items: [...prev, ...list],
        page: (res.data?.page || 0) + 1,
        hasMore: (res.data?.content?.length || 0) >= size,
        loading: false
      }, () => {
        if (jumpToResult) {
          this.setData({ scrollIntoView: '' }, () => {
            setTimeout(() => this.setData({ scrollIntoView: 'result-top' }), 30)
          })
        }
      })
    }).catch(() => this.setData({ loading: false }))
  },

  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value })
  },
  onSearch() {
    this.loadItems(true)
  },

  onFilterCategory(e) {
    const id = e.currentTarget.dataset.id
    this.setData({ categoryId: id || null, showFilter: false })
    this.loadItems(true)
  },

  onFilterType(e) {
    const type = e.currentTarget.dataset.type
    this.setData({ type: type !== undefined ? type : null })
    this.loadItems(true)
  },

  toggleFilter() {
    this.setData({ showFilter: !this.data.showFilter })
  },

  toDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/item-detail/item-detail?id=${id}` })
  },

  onReachBottom() {
    this.loadItems(false)
  },

  onImageSearch() {
    wx.navigateTo({ url: '/pages/image-search/image-search' })
  },

  clearAiSearch() {
    this.setData({
      aiKeywords: '',
      aiSearching: false,
      categoryId: this.data.categoryId === this.data.aiMatchedCategoryId ? null : this.data.categoryId,
      aiMatchedCategoryId: null
    }, () => this.loadItems(true))
  }
})
