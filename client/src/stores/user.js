import { defineStore } from 'pinia'
import { ref } from 'vue'
//给router用
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')   
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null')) 

  //登录成功时调用
  function setAuth(data) {
    token.value = data.token
    userInfo.value = data.user
    localStorage.setItem('token', data.token)  //存token
    localStorage.setItem('userInfo', JSON.stringify(data.user))  //存用户信息
  }

  //登出
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  //判断是否登录
  const isLoggedIn = () => !!token.value

  return { token, userInfo, setAuth, logout, isLoggedIn }
})
