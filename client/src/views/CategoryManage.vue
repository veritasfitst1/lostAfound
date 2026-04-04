<template>
  <div class="category-manage">
    <el-card>
      <template #header>
        <span>分类管理</span>
        <el-button type="primary" style="margin-left: 16px" @click="openCreate">新增分类</el-button>
      </template>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="icon" label="图标标识" min-width="120">
          <template #default="{ row }">{{ row.icon || '—' }}</template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑分类' : '新增分类'" width="440px" destroy-on-close @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" maxlength="32" show-word-limit placeholder="分类名称" />
        </el-form-item>
        <el-form-item label="图标标识" prop="icon">
          <el-input v-model="form.icon" maxlength="64" show-word-limit placeholder="可选，如 icon 名" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" controls-position="right" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminCategories, createCategory, updateCategory, deleteCategory } from '../api/admin'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const submitting = ref(false)

const form = ref({
  name: '',
  icon: '',
  sortOrder: 0
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请设置排序', trigger: 'change' }]
}

const formatTime = (v) => {
  if (v == null) return '—'
  if (typeof v === 'string') return v.replace('T', ' ').slice(0, 19)
  return String(v)
}

const load = async () => {
  loading.value = true
  try {
    const res = await getAdminCategories()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  editingId.value = null
  form.value = { name: '', icon: '', sortOrder: 0 }
  formRef.value?.resetFields?.()
}

const openCreate = () => {
  editingId.value = null
  form.value = { name: '', icon: '', sortOrder: list.value.length ? Math.max(...list.value.map((c) => c.sortOrder || 0)) + 1 : 0 }
  dialogVisible.value = true
}

const openEdit = (row) => {
  editingId.value = row.id
  form.value = {
    name: row.name,
    icon: row.icon || '',
    sortOrder: row.sortOrder ?? 0
  }
  dialogVisible.value = true
}

const submit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  const payload = {
    name: form.value.name.trim(),
    icon: form.value.icon?.trim() || null,
    sortOrder: form.value.sortOrder
  }
  submitting.value = true
  try {
    if (editingId.value) {
      await updateCategory(editingId.value, payload)
      ElMessage.success('已保存')
    } else {
      await createCategory(payload)
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(typeof e === 'string' ? e : '操作失败')
  } finally {
    submitting.value = false
  }
}

const del = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该分类？若分类下仍有物品将无法删除。', '提示')
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(typeof e === 'string' ? e : '删除失败')
  }
}

onMounted(load)
</script>
