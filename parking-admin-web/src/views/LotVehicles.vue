<template>
  <el-card>
    <div class="toolbar">
      <el-button @click="$router.push('/lots')">返回车场</el-button>
      <el-button @click="load">刷新</el-button>
      <span class="lot-name">{{ lotName }}</span>
    </div>

    <el-table :data="vehicles" v-loading="loading" border>
      <el-table-column prop="plateNo" label="车牌号" width="140" />
      <el-table-column prop="lotName" label="所属车场" min-width="140" />
      <el-table-column label="套餐状态" width="110">
        <template #default="{ row }">
          <el-tag v-if="row.subStatus === 'VALID'" type="success">有效</el-tag>
          <el-tag v-else-if="row.subStatus === 'EXPIRED'" type="danger">已到期</el-tag>
          <el-tag v-else type="info">未订阅</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="到期时间" width="170">
        <template #default="{ row }">{{ row.latestExpire?.replace('T', ' ').slice(0, 16) || '-' }}</template>
      </el-table-column>
      <el-table-column label="绑定人数" width="90">
        <template #default="{ row }">{{ row.userIds?.length || 0 }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-popconfirm title="解绑该车辆的所有绑定人（车辆将作废）？" @confirm="unbindAll(row)">
            <template #reference>
              <el-button size="small" type="danger" plain>解绑</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { lotVehicles, adminUnbind, myLots } from '../api'

const route = useRoute()
const lotId = computed(() => route.params.lotId)
const vehicles = ref([])
const loading = ref(false)
const lotName = ref('')

onMounted(async () => {
  await load()
  const lots = await myLots()
  lotName.value = lots.find(l => String(l.id) === String(lotId.value))?.name || ''
})

async function load() {
  loading.value = true
  try {
    vehicles.value = await lotVehicles(lotId.value)
  } finally {
    loading.value = false
  }
}

async function unbindAll(row) {
  const userIds = row.userIds || []
  for (const uid of userIds) {
    await adminUnbind(row.id, uid)
  }
  ElMessage.success('已解绑')
  await load()
}
</script>

<style scoped>
.toolbar { margin-bottom: 12px; display: flex; gap: 8px; align-items: center; }
.lot-name { font-weight: 600; color: #303133; }
</style>
