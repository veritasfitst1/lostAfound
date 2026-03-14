<template>
  <div class="item-manage">
    <el-card>
      <template #header>
        <span>物品管理</span>
        <el-select v-model="filterType" placeholder="类型" clearable style="width: 100px; margin-left: 16px" @change="load">
          <el-option label="失物" :value="0" />
          <el-option label="招领" :value="1" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 120px; margin-left: 8px" @change="load">
          <el-option label="寻找中" :value="0" />
          <el-option label="已找回" :value="1" />
          <el-option label="已撤销" :value="2" />
          <el-option label="已过期" :value="3" />
        </el-select>
      </template>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="120" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">{{ row.type === 0 ? '失物' : '招领' }}</template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="userNickname" label="发布者" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="['success', 'info', 'info', 'warning'][row.status]">
              {{ ['寻找中', '已找回', '已撤销', '已过期'][row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="del(row)">删除</el-button>
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
import { getItems, deleteItem } from '../api/admin'

const filterType = ref(null)
const filterStatus = ref(null)
const list = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const load = async () => {
  loading.value = true
  try {
    const res = await getItems({
      type: filterType.value,
      status: filterStatus.value,
      page: page.value - 1,
      size: size.value
    })
    list.value = res.data?.content || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const del = async (row) => {
  await ElMessageBox.confirm('确定删除该物品？', '提示')
  await deleteItem(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>
