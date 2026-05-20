<template>
  <div class="login-page">
    <div class="login-bg" aria-hidden="true" />
    <div class="login-card">
      <h1 class="title">校园失物招领</h1>
      <p class="subtitle">管理后台</p>
      <el-form
        :model="form"
        class="login-form"
        label-width="0"
        @submit.prevent="handleLogin"
      >
        <el-form-item>
          <el-input
            v-model="form.username"
            placeholder="用户名"
            size="large"
            autocomplete="username"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            show-password
            autocomplete="current-password"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            native-type="submit"
            class="submit-btn"
          >
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

const router = useRouter()  //后面登录成功后用它跳转
const userStore = useUserStore() //保存用户
const loading = ref(false)
const form = reactive({ username: '', password: '' })

//登录
const handleLogin = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await adminLogin(form)
    userStore.setAuth(res.data)  //把接口返回的数据保存到全局用户状态中
    ElMessage.success('登录成功')
    router.push('/') //登录成功后跳转到后台首页
  } catch (e) {
    ElMessage.error(e || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: fixed;
  inset: 0;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  box-sizing: border-box;
  /* 主渐变：与原先风格一致，全屏可见 */
  background: linear-gradient(135deg, #1989fa 0%, #3d9cf0 40%, #5cadff 100%);
}

.login-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  /* 叠一层高光，让渐变更有层次 */
  background: radial-gradient(
    ellipse 90% 70% at 50% -20%,
    rgba(255, 255, 255, 0.22),
    transparent 55%
  );
}

.login-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 400px;
  padding: 40px 36px 36px;
  background: #fff;
  border-radius: 16px;
  box-shadow:
    0 4px 24px rgba(0, 0, 0, 0.12),
    0 0 0 1px rgba(255, 255, 255, 0.08) inset;
}

.title {
  margin: 0 0 8px;
  text-align: center;
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  letter-spacing: -0.02em;
}

.subtitle {
  margin: 0 0 28px;
  text-align: center;
  font-size: 14px;
  color: #64748b;
}

/* 无 label 时去掉 Element Plus 默认缩进，避免输入框与按钮错位 */
.login-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.login-form :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.login-form :deep(.el-form-item__content) {
  margin-left: 0 !important;
  display: block;
}

.login-form :deep(.el-input__wrapper) {
  width: 100%;
}

.submit-btn {
  width: 100%;
  margin-top: 6px;
  font-weight: 500;
}
</style>
