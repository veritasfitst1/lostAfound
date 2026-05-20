const { get, post } = require('../../utils/request')
const { API_BASE, resolveImageUrl } = require('../../utils/config')

Page({
  data: {
    type: 0,  //0失物  1招领
    categories: [],   //类别
    categoryIndex: 0,    //选中的分类下标
    categoryId: null,   //分类主键 
    title: '',       //标题
    description: '',   //描述
    location: '',
    contact: '',    //联系电话
    images: []   //图片
  },
//加载
  onLoad(opt) {
    this.setData({ type: parseInt(opt.type || 0) })   //0 发表失物 1 发表招领   公用一个界面
    this.loadCategories()
  },
//从数据库中载入类别
  loadCategories() {
    get('/api/categories').then(res => {
      this.setData({ categories: res.data || [] })
    })
  },
//切换分类时触发
  onCategoryChange(e) {
    const idx = parseInt(e.detail.value)     //选择的分类对应的下标
    const cat = this.data.categories[idx]   //取出对应下标的分类对象
    this.setData({ categoryIndex: idx, categoryId: cat ? cat.id : null })
  },
  //输入数据
  onTitleInput(e) { this.setData({ title: e.detail.value }) },
  onDescInput(e) { this.setData({ description: e.detail.value }) },
  onLocationInput(e) { this.setData({ location: e.detail.value }) },
  onContactInput(e) { this.setData({ contact: e.detail.value }) },
//选择并上传图片
  chooseImage() {
    wx.chooseMedia({    //微信小程序选择媒体文件的 API
      count: 9 - this.data.images.length,   //最多9张
      mediaType: ['image'],   //只允许选图片
      success: (res) => { 
        const files = res.tempFiles    //res.tempFiles为用户本次选中的文件数组 （本地临时文件）
        //定义upload函数，传入file参数：把一张本地图片上传到服务器，并返回上传后的图片地址
        const upload = (file) => new Promise((resolve, reject) => {  //返回promise对象（url字符串） 代表异步操作
          const token = wx.getStorageSync('token')
          wx.uploadFile({
            url: API_BASE + '/api/image/upload',
            filePath: file.tempFilePath,  //tempFilePath微信系统返回
            name: 'file',
            header: { Authorization: 'Bearer ' + token },
            success: (r) => {
              try {
                const data = JSON.parse(r.data)   //解析返回的数据r，
                resolve(resolveImageUrl(data.data))   //data.data表示后端返回的图片路径  标记为 resolve成功 -》promise 
              } catch { reject() }
            },
            fail: reject
          })
        })
        //上传所有文件  map把数组里的每一项拿出来，执行一次你给它的函数放回f，执行upload，然后组成一个新数组
        Promise.all(files.map(f => upload(f))).then(urls => {
          this.setData({ images: [...this.data.images, ...urls] })
        })
      }
    })
  },
//删除上传的图片
  removeImage(e) {
    const idx = e.currentTarget.dataset.idx
    const img = [...this.data.images]   //复制一份数组
    img.splice(idx, 1)
    this.setData({ images: img })
  },
//发布键
  submit() {
    const { type, categoryId, title, description, location, contact, images } = this.data
    if (!categoryId) return wx.showToast({ title: '请选择分类', icon: 'none' })
    if (!title.trim()) return wx.showToast({ title: '请输入物品名称', icon: 'none' })

    wx.showLoading({ title: '发布中' })
    post('/api/items', {
      type,
      categoryId,
      title: title.trim(),
      description: description.trim(),
      location: location.trim(),
      contact: contact.trim(),
      images: images.map(u => u.startsWith(API_BASE) ? u.slice(API_BASE.length) : u)
    }).then(() => {
      wx.hideLoading()
      wx.showToast({ title: '发布成功' })
      setTimeout(() => wx.navigateBack(), 1500)
    }).catch(err => {
      wx.hideLoading()
      wx.showToast({ title: err.message || '发布失败', icon: 'none' })
    })
  }
})
