const { get } = require('../../utils/request')

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
    showFilter: false
  },

  onLoad() {
    this.loadCategories()
    this.loadItems(true)
  },

  onShow() {
    this.loadItems(true)
  },

  loadCategories() {
    get('/api/categories').then(res => {
      this.setData({ categories: res.data || [] })
    }).catch(() => {})
  },

  loadItems(refresh) {
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
      const list = res.data?.content || []
      const prev = refresh ? [] : this.data.items
      this.setData({
        items: [...prev, ...list],
        page: (res.data?.page || 0) + 1,
        hasMore: (res.data?.content?.length || 0) >= size,
        loading: false
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
  }
})
