<template>
  <div class="report-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>举报审核</span>
          <el-radio-group v-model="listMode" @change="load">
            <el-radio-button label="pending">待审核</el-radio-button>
            <el-radio-button label="all">全部记录</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="reporterNickname" label="举报人" width="100" />
        <el-table-column prop="reason" label="举报理由" min-width="180" />
        <el-table-column prop="reportedUserNickname" label="被举报用户" width="100" />
        <el-table-column prop="reportedItemTitle" label="被举报物品" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : row.status === 1 ? 'success' : 'info'">
              {{ ['待审核', '已通过', '已驳回'][row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" size="small" @click="approve(row)">通过</el-button>
              <el-button type="danger" size="small" @click="reject(row)">驳回</el-button>
            </template>
            <el-button v-if="row.status !== 0" type="warning" size="small" @click="revoke(row)">撤销</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReports, approveReport, rejectReport, revokeReport } from '../api/admin'

const list = ref([])
const loading = ref(false)
/** 默认只看待审核：通过后刷新即从列表消失；可切「全部记录」看历史 */
const listMode = ref('pending')

const load = async () => {
  loading.value = true
  try {
    const res = await getReports(
      listMode.value === 'pending' ? { pendingOnly: true } : {}
    )
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

const approve = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入备注（可选）', '审核通过', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '备注'
    })
    await approveReport(row.id, value || '')
    list.value = list.value.filter((r) => r.id !== row.id)
    ElMessage.success('已通过')
    await load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(typeof e === 'string' ? e : e?.message || '操作失败')
  }
}

const reject = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回理由（可选）', '驳回', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '驳回理由'
    })
    await rejectReport(row.id, value || '')
    list.value = list.value.filter((r) => r.id !== row.id)
    ElMessage.success('已驳回')
    await load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(typeof e === 'string' ? e : e?.message || '操作失败')
  }
}

const revoke = async (row) => {
  try {
    await ElMessageBox.confirm('确定撤销该举报？将回退通过时的处罚操作。', '撤销举报')
    await revokeReport(row.id)
    ElMessage.success('已撤销')
    await load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(typeof e === 'string' ? e : e?.message || '操作失败')
  }
}

onMounted(load)
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}
</style>
