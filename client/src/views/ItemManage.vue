<template>
  <div class="item-manage">
    <el-card>
      <template #header>
        <span>物品管理</span>
        <el-select v-model="filterType" placeholder="类型" clearable style="width: 120px; margin-left: 16px" @change="load">
          <el-option label="失物" :value="0" />
          <el-option label="招领" :value="1" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 140px; margin-left: 8px" @change="load">
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
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="['success', 'info', 'info', 'warning'][row.status]">
              {{ ['寻找中', '已找回', '已撤销', '已过期'][row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 3" type="success" link size="small" @click="restore(row)">恢复</el-button>
            <el-button v-if="row.status === 0" type="warning" link size="small" @click="expire(row)">过期</el-button>
            <el-button type="danger" link size="small" @click="del(row)">删除</el-button>
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

    <el-dialog v-model="editVisible" title="编辑物品" width="520px" destroy-on-close @closed="resetForm">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" maxlength="1000" show-word-limit />
        </el-form-item>
        <el-form-item label="地点" prop="location">
          <el-input v-model="form.location" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="联系方式" prop="contact">
          <el-input v-model="form.contact" maxlength="128" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getItems, updateItem, updateItemStatus, deleteItem } from '../api/admin'

const filterType = ref(null)
const filterStatus = ref(null)
const list = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const editVisible = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const form = ref({ title: '', description: '', location: '', contact: '' })

const load = async () => {
  loading.value = true
  try {
    const params = { page: page.value - 1, size: size.value }
    if (filterType.value != null) params.type = filterType.value
    if (filterStatus.value != null) params.status = filterStatus.value
    const res = await getItems(params)
    list.value = res.data?.content || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const openEdit = (row) => {
  editingId.value = row.id
  form.value = {
    title: row.title || '',
    description: row.description || '',
    location: row.location || '',
    contact: row.contact || ''
  }
  editVisible.value = true
}

const resetForm = () => {
  editingId.value = null
  form.value = { title: '', description: '', location: '', contact: '' }
}

const submitEdit = async () => {
  submitting.value = true
  try {
    await updateItem(editingId.value, form.value)
    ElMessage.success('保存成功')
    editVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(typeof e === 'string' ? e : '保存失败')
  } finally {
    submitting.value = false
  }
}

const restore = async (row) => {
  try {
    await ElMessageBox.confirm('确定将该物品恢复为「寻找中」状态？', '恢复')
    await updateItemStatus(row.id, 0)
    ElMessage.success('已恢复')
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(typeof e === 'string' ? e : '操作失败')
  }
}

const expire = async (row) => {
  try {
    await ElMessageBox.confirm('确定将该物品标记为「已过期」？', '过期')
    await updateItemStatus(row.id, 3)
    ElMessage.success('已标记过期')
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(typeof e === 'string' ? e : '操作失败')
  }
}

const del = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该物品？', '提示')
    await deleteItem(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(typeof e === 'string' ? e : '删除失败')
  }
}

onMounted(load)
</script>
