<template>
  <div class="user-manage">
    <el-card>
      <template #header>
        <span>用户管理</span>
        <el-input v-model="keyword" placeholder="搜索昵称/用户名" clearable style="width: 200px; margin-left: 16px" @clear="load" @keyup.enter="load" />
        <el-button type="primary" style="margin-left: 8px" @click="load">搜索</el-button>
      </template>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="role" label="角色" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '正常' : '封禁' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-if="row.role !== 'ADMIN'" :type="row.status === 0 ? 'warning' : 'success'" size="small" @click="toggleBan(row)">
              {{ row.status === 0 ? '封禁' : '解封' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 16px"
        @current-change="load"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsers, toggleBan as apiToggleBan } from '../api/admin'

const keyword = ref('')
const list = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const load = async () => {
  loading.value = true
  try {
    const res = await getUsers({ keyword: keyword.value || undefined, page: page.value - 1, size: size.value })
    list.value = res.data?.content || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const toggleBan = async (row) => {
  await ElMessageBox.confirm(row.status === 0 ? '确定封禁该用户？' : '确定解封该用户？', '提示')
  await apiToggleBan(row.id)
  ElMessage.success('操作成功')
  load()
}

onMounted(load)
</script>
