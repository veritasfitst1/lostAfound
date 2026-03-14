<template>
  <div class="login-page">
    <div class="login-card">
      <h1>校园失物招领</h1>
      <p class="subtitle">管理后台</p>
      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleLogin" style="width: 100%">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminLogin } from '../api/auth'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })

const handleLogin = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await adminLogin(form)
    userStore.setAuth(res.data)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    ElMessage.error(e || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1989fa 0%, #5cadff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-card {
  background: #fff;
  border-radius: 16px;
  padding: 48px;
  width: 100%;
  max-width: 400px;
}

.login-card h1 {
  text-align: center;
  margin-bottom: 8px;
  font-size: 24px;
}

.subtitle {
  text-align: center;
  color: #666;
  margin-bottom: 32px;
}
</style>
