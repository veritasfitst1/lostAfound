Page({
  toPublishLost() {
    wx.navigateTo({ url: '/pages/publish-item/publish-item?type=0' })
  },
  toPublishFound() {
    wx.navigateTo({ url: '/pages/publish-item/publish-item?type=1' })
  },
  toRevoke() {
    wx.showActionSheet({
      itemList: ['撤销失物信息', '撤销招领信息'],
      success: (res) => {
        if (res.tapIndex === 0) {
          wx.navigateTo({ url: '/pages/my-lost/my-lost?mode=revoke' })
        } else {
          wx.navigateTo({ url: '/pages/my-found/my-found?mode=revoke' })
        }
      }
    })
  }
})
