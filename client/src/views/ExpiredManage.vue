<template>
  <div class="expired-manage">
    <el-card class="expired-card">
      <template #header>过期消息处理</template>
      <div class="expired-body">
        <p class="desc">将超过指定天数仍未找回的物品标记为已过期。</p>
        <el-form inline class="expired-form">
          <el-form-item label="天数">
            <el-input-number v-model="days" :min="7" :max="365" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleExpire">批量标记过期</el-button>
          </el-form-item>
        </el-form>
        <template v-if="processed">
          <el-empty v-if="result.length === 0" description="无过期消息" :image-size="120" />
          <div v-else class="result-section">
            <el-alert type="success" :closable="false" class="result-alert">
              共处理 {{ result.length }} 条记录
            </el-alert>
            <el-table :data="result" stripe style="width: 100%; margin-top: 16px">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="title" label="标题" min-width="140" />
              <el-table-column label="类型" width="80">
                <template #default="{ row }">{{ row.type === 0 ? '失物' : '招领' }}</template>
              </el-table-column>
              <el-table-column prop="categoryName" label="分类" width="100" />
              <el-table-column prop="userNickname" label="发布者" width="100" />
              <el-table-column label="发布时间" width="180">
                <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
              </el-table-column>
            </el-table>
          </div>
        </template>
      </div>
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
const processed = ref(false)

const formatTime = (v) => {
  if (v == null) return '—'
  if (typeof v === 'string') return v.replace('T', ' ').slice(0, 19)
  return String(v)
}

const handleExpire = async () => {
  await ElMessageBox.confirm(`确定将超过 ${days.value} 天未找回的物品标记为已过期？`, '提示')
  loading.value = true
  try {
    const res = await expireItems(days.value)
    result.value = res.data || []
    processed.value = true
    if (result.value.length > 0) {
      ElMessage.success(`已处理 ${result.value.length} 条`)
    } else {
      ElMessage.info('当前无需要过期处理的物品')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.expired-card {
  width: 100%;
}

.expired-body {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.desc {
  margin: 0 0 24px;
  color: #64748b;
  font-size: 14px;
}

.expired-form {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
}

.result-section {
  width: 100%;
  margin-top: 20px;
}
</style>
