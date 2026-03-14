<template>
  <div class="expired-manage">
    <el-card>
      <template #header>过期消息处理</template>
      <p>将超过指定天数仍未找回的物品标记为已过期。</p>
      <el-form inline>
        <el-form-item label="天数">
          <el-input-number v-model="days" :min="7" :max="365" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleExpire">批量标记过期</el-button>
        </el-form-item>
      </el-form>
      <el-alert v-if="result.length" type="info" :closable="false" style="margin-top: 16px">
        共处理 {{ result.length }} 条记录
      </el-alert>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { expireItems } from '../api/admin'

const days = ref(30)
const loading = ref(false)
const result = ref([])

const handleExpire = async () => {
  await ElMessageBox.confirm(`确定将超过 ${days.value} 天未找回的物品标记为已过期？`, '提示')
  loading.value = true
  try {
    const res = await expireItems(days.value)
    result.value = res.data || []
    ElMessage.success(`已处理 ${result.value.length} 条`)
  } finally {
    loading.value = false
  }
}
</script>
