<template>
  <div class="report-manage">
    <el-card>
      <template #header>举报审核</template>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="reporterNickname" label="举报人" width="100" />
        <el-table-column prop="reason" label="举报理由" min-width="180" />
        <el-table-column prop="reportedUserNickname" label="被举报用户" width="100" />
        <el-table-column prop="reportedItemTitle" label="被举报物品" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : row.status === 1 ? 'danger' : 'info'">
              {{ ['待审核', '已通过', '已驳回'][row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" size="small" @click="approve(row)">通过</el-button>
              <el-button type="danger" size="small" @click="reject(row)">驳回</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReports, approveReport, rejectReport } from '../api/admin'

const list = ref([])
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    const res = await getReports()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

const approve = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入备注（可选）', '审核通过', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '备注'
  })
  await approveReport(row.id, value || '')
  ElMessage.success('已通过')
  load()
}

const reject = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入驳回理由（可选）', '驳回', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '驳回理由'
  })
  await rejectReport(row.id, value || '')
  ElMessage.success('已驳回')
  load()
}

onMounted(load)
</script>
