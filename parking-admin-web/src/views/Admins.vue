<template>
  <el-card>
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>开通管理员
      </el-button>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table :data="admins" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="账号" width="140" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="分配车场" min-width="180">
        <template #default="{ row }">
          <el-tag v-for="l in (row.lots || [])" :key="l.id" size="small" style="margin: 2px">
            {{ l.name }}
          </el-tag>
          <span v-if="!(row.lots && row.lots.length)" style="color: #c0c4cc">未分配</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" plain @click="openAssign(row)">分配车场</el-button>
          <el-button size="small" :type="row.status === 1 ? 'danger' : 'success'" plain
                     @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 开通账号 -->
    <el-dialog v-model="createVisible" title="开通管理员账号" width="460px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="账号" required><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码" required><el-input v-model="form.password" show-password /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">开通</el-button>
      </template>
    </el-dialog>

    <!-- 分配车场 -->
    <el-dialog v-model="assignVisible" title="分配车场" width="460px">
      <el-checkbox-group v-model="selectedLotIds">
        <el-checkbox v-for="l in lots" :key="l.id" :value="l.id">{{ l.name }}（{{ l.location }}）</el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="doAssign">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { listAdmins, createAdmin, updateAdminStatus, assignLots, adminLots, myLots } from '../api'

const admins = ref([])
const lots = ref([])
const loading = ref(false)
const saving = ref(false)
const createVisible = ref(false)
const assignVisible = ref(false)
const selectedLotIds = ref([])
const assigningAdminId = ref(null)
const form = reactive({ username: '', password: '', realName: '', phone: '' })

onMounted(load)

async function load() {
  loading.value = true
  try {
    const list = await listAdmins()
    lots.value = await myLots()
    for (const a of list) {
      a.lots = await adminLots(a.id)
    }
    admins.value = list
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, { username: '', password: '', realName: '', phone: '' })
  createVisible.value = true
}

async function save() {
  if (!form.username || !form.password) {
    ElMessage.warning('请填写账号和密码')
    return
  }
  saving.value = true
  try {
    await createAdmin(form)
    ElMessage.success('开通成功')
    createVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

function openAssign(row) {
  assigningAdminId.value = row.id
  selectedLotIds.value = (row.lots || []).map(l => l.id)
  assignVisible.value = true
}

async function doAssign() {
  saving.value = true
  try {
    await assignLots({ adminId: assigningAdminId.value, lotIds: selectedLotIds.value })
    ElMessage.success('分配成功')
    assignVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  await updateAdminStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('操作成功')
  await load()
}
</script>

<style scoped>
.toolbar { margin-bottom: 12px; display: flex; gap: 8px; }
</style>
