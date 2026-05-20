const { get } = require('../../utils/request')
const { resolveItemImages } = require('../../utils/config')   //处理图片地址

Page({
  data: {
    keyword: '',   //搜索关键词
    categoryId: null,    //分类筛选id
    type: null,     //物品类型
    items: [],     //物品数据
    categories: [],    //分类类别
    loading: true,
    page: 0,
    size: 10,
    hasMore: true,    //是否还有更多数据
    showFilter: false,   //是否显示分类筛选面板
    aiSearching: false,    //是否正在进行 AI 图片识别
    aiKeywords: '',    //AI 识别出来的关键词
    scrollIntoView: ''   //控制滚动到哪个元素
  },
//加载页面时自动执行
  onLoad() {
    this.loadCategories()
    this.loadItems(true)
  },
//读取图片识别结果并显示
  onShow() {
    const imageSearchResult = wx.getStorageSync('imageSearchResult')
    if (imageSearchResult && imageSearchResult.ts) {  //如果有结果
      wx.removeStorageSync('imageSearchResult')    //读取之后立刻删除缓存，避免下次再重复使用这份旧结果
      const keywords = (imageSearchResult.keywords || []).slice(0, 3).join('、') //只取前 3 个关键词 
      const list = (imageSearchResult.items || []).map(resolveItemImages)   //把 items 数组中的每一项都用 resolveItemImages 处理一遍
      this.setData({
        aiSearching: false,
        aiKeywords: keywords || (imageSearchResult.success ? '识别完成' : ''),
        items: list,
        loading: false,
        page: 1,
        hasMore: false   //只显示一批
      }, () => {
        this.setData({ scrollIntoView: '' }, () => {  //让结果列表自动滚动到顶部
          setTimeout(() => this.setData({ scrollIntoView: 'result-top' }), 30)
        })
      })
      return
    }
    this.loadItems(true)
  },
//加载分类，保存到categories
  loadCategories() {
    get('/api/categories').then(res => {
      this.setData({ categories: res.data || [] })
    }).catch(() => {})
  },
//加载物品   当页面刚打开   点击搜索   切换分类   切换失物/招领时     都会调用
//refresh 是否重新加载  true为刷新 false为翻页   jumpToResult 是否加载完后自动滚动到结果顶部
  loadItems(refresh, jumpToResult = false) {
    if (refresh) {
      this.setData({ page: 0, hasMore: true })
    }
    if (!this.data.hasMore && !refresh) return  //如果没有更多数据，而且不是刷新，就直接返回
    const { keyword, categoryId, type, page, size } = this.data  //从页面中提取信息
    const params = { page: refresh ? 0 : page, size }
    if (keyword) params.keyword = keyword  //如果用户输入了关键词，就带上 keyword
    if (categoryId) params.categoryId = categoryId
    if (type != null) params.type = type

    this.setData({ loading: true })  //显示“加载中...”
    get('/api/items', params).then(res => {
      const list = (res.data?.content || []).map(resolveItemImages)
      const prev = refresh ? [] : this.data.items  //追加数据，如果刷新就为空
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
//输入栏
  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value })
  },
  //执行搜索
  onSearch() {
    this.loadItems(true)
  },
//点击某个分类后
  onFilterCategory(e) {
    const id = e.currentTarget.dataset.id  //记录当前分类 对应前端data-id
    this.setData({ categoryId: id || null, showFilter: false })   //收起分类面板
    this.loadItems(true)  //重新刷新列表
  },
//点击失物or招领后的类型筛选（dataset 中 data-type 为字符串，需转成数字才能与模板里 type===0/1 一致）
  onFilterType(e) {
    const raw = e.currentTarget.dataset.type
    let type = null
    if (raw === '' || raw === undefined) {
      type = null
    } else {
      const n = Number(raw)
      type = Number.isNaN(n) ? null : n
    }
    this.setData({ type })
    this.loadItems(true)
  },
//筛选面板是否显示
  toggleFilter() {
    this.setData({ showFilter: !this.data.showFilter })
  },
//跳转物品详情
  toDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/item-detail/item-detail?id=${id}` })
  },
//分页加载更多
  onReachBottom() {
    this.loadItems(false)
  },
//图片搜索入口跳转
  onImageSearch() {
    wx.navigateTo({ url: '/pages/image-search/image-search' })
  },
//清空 AI 搜索状态，并恢复普通列表
  clearAiSearch() {
    wx.removeStorageSync('imageSearchResult')
    this.setData({
      aiKeywords: '',
      aiSearching: false,
      items: []
    }, () => this.loadItems(true))
  }
})
