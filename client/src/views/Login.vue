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
    // #region agent log
    fetch('http://127.0.0.1:7452/ingest/49c51c8e-b700-498f-abd5-e19a65322b20',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'5795f3'},body:JSON.stringify({sessionId:'5795f3',hypothesisId:'A,B',location:'Login.vue:handleLogin',message:'before adminLogin',data:{username:form.username},timestamp:Date.now()})}).catch(()=>{});
    // #endregion
    const res = await adminLogin(form)
    // #region agent log
    fetch('http://127.0.0.1:7452/ingest/49c51c8e-b700-498f-abd5-e19a65322b20',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'5795f3'},body:JSON.stringify({sessionId:'5795f3',hypothesisId:'B',location:'Login.vue:handleLogin',message:'adminLogin success',data:{res:JSON.stringify(res)},timestamp:Date.now()})}).catch(()=>{});
    // #endregion
    userStore.setAuth(res.data)
    // #region agent log
    fetch('http://127.0.0.1:7452/ingest/49c51c8e-b700-498f-abd5-e19a65322b20',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'5795f3'},body:JSON.stringify({sessionId:'5795f3',hypothesisId:'D',location:'Login.vue:handleLogin',message:'after setAuth, pushing to /',data:{isLoggedIn:userStore.isLoggedIn(),token:!!userStore.token},timestamp:Date.now()})}).catch(()=>{});
    // #endregion
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    // #region agent log
    fetch('http://127.0.0.1:7452/ingest/49c51c8e-b700-498f-abd5-e19a65322b20',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'5795f3'},body:JSON.stringify({sessionId:'5795f3',hypothesisId:'A',location:'Login.vue:handleLogin:catch',message:'login error',data:{error:String(e),type:typeof e},timestamp:Date.now()})}).catch(()=>{});
    // #endregion
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
