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

const list = ref([])  //分类数据
const loading = ref(false)
const dialogVisible = ref(false)  //控制编辑界面显示隐藏
const editingId = ref(null)   //记录当前正在编辑的分类 ID
const formRef = ref(null)   //表单
const submitting = ref(false)   //是否正在提交表单

const form = ref({
  name: '',
  sortOrder: 0
})

const rules = {//表单验证规则
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请设置排序', trigger: 'change' }]
}

//处理表格里的时间createdAt
const formatTime = (v) => {
  if (v == null) return '—'
  if (typeof v === 'string') return v.replace('T', ' ').slice(0, 19)
  return String(v)
}

//加载
const load = async () => {
  loading.value = true
  try {
    const res = await getAdminCategories()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

//关闭表单时用
const resetForm = () => {
  editingId.value = null
  form.value = { name: '', sortOrder: 0 }
  formRef.value?.resetFields?.()
}

//新增分类键
const openCreate = () => {
  editingId.value = null 
  form.value = { name: '', sortOrder: list.value.length ? Math.max(...list.value.map((c) => c.sortOrder || 0)) + 1 : 0 }
  dialogVisible.value = true
}

//编辑键
const openEdit = (row) => {
  editingId.value = row.id
  form.value = {
    name: row.name,
    sortOrder: row.sortOrder ?? 0
  }
  dialogVisible.value = true
}

//分类创建or修改提交
const submit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  const payload = {
    name: form.value.name.trim(),
    sortOrder: form.value.sortOrder
  }
  submitting.value = true
  try {
    if (editingId.value) {  //编辑已有分类
      await updateCategory(editingId.value, payload)
      ElMessage.success('已保存')
    } else {    //创建新分类
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

//删除键
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
