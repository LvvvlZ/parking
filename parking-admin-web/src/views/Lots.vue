<template>
  <el-card>
    <div class="toolbar">
      <el-button v-if="isSuper" type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon>新建车场
      </el-button>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table :data="lots" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column prop="location" label="位置" min-width="180" />
      <el-table-column prop="totalSpaces" label="车位数" width="80" />
      <el-table-column prop="bindCount" label="绑定车辆" width="90" />
      <el-table-column label="套餐(月/季/年)" width="150">
        <template #default="{ row }">
          {{ row.monthlyPrice ?? '-' }} / {{ row.quarterlyPrice ?? '-' }} / {{ row.yearlyPrice ?? '-' }}
        </template>
      </el-table-column>
      <el-table-column label="临停规则" width="200">
        <template #default="{ row }">
          免费{{ row.freeMinutes }}分钟 · 首{{ row.firstHours }}h {{ row.firstPrice }}元 ·
          每小时{{ row.hourlyPrice }}元{{ row.dailyMax ? ' · 日封顶' + row.dailyMax + '元' : '' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="$router.push('/vehicles/' + row.id)">车辆</el-button>
          <el-button size="small" @click="showQrcode(row)">二维码</el-button>
          <template v-if="isSuper">
            <el-button size="small" type="primary" plain @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除该车场？" @confirm="onDelete(row.id)">
              <template #reference>
                <el-button size="small" type="danger" plain>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <LotFormDialog v-model:visible="dialogVisible" :lot="editingLot" @saved="load" />

    <el-dialog v-model="qrVisible" title="车场二维码内容" width="480px">
      <p style="margin-bottom: 12px; color: #909399; font-size: 13px">
        将以下内容生成二维码供用户扫码（前端/小程序负责渲染图片）：
      </p>
      <el-input :model-value="qrPayload" readonly>
        <template #append>
          <el-button @click="copyQr">复制</el-button>
        </template>
      </el-input>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { myLots, deleteLot, lotQrcode } from '../api'
import { getAuth } from '../store/auth'
import LotFormDialog from '../components/LotFormDialog.vue'

const isSuper = getAuth().role === 'SUPER_ADMIN'
const lots = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingLot = ref({})
const qrVisible = ref(false)
const qrPayload = ref('')

onMounted(load)

async function load() {
  loading.value = true
  try {
    lots.value = await myLots()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingLot.value = {}
  dialogVisible.value = true
}

function openEdit(row) {
  editingLot.value = row
  dialogVisible.value = true
}

async function onDelete(id) {
  await deleteLot(id)
  ElMessage.success('已删除')
  await load()
}

async function showQrcode(row) {
  const data = await lotQrcode(row.id)
  qrPayload.value = data.payload
  qrVisible.value = true
}

function copyQr() {
  navigator.clipboard.writeText(qrPayload.value)
  ElMessage.success('已复制')
}
</script>

<style scoped>
.toolbar { margin-bottom: 12px; display: flex; gap: 8px; }
</style>
