<template>
  <el-dialog :model-value="visible" :title="form.id ? '编辑车场' : '新建车场'" width="640px"
             @update:model-value="v => $emit('update:visible', v)">
    <el-form :model="form" label-width="110px">
      <el-divider content-position="left">基本信息</el-divider>
      <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="位置" required><el-input v-model="form.location" /></el-form-item>
      <el-form-item label="车位数" required>
        <el-input-number v-model="form.totalSpaces" :min="0" />
      </el-form-item>
      <el-form-item label="绑定上限">
        <el-input-number v-model="form.bindLimit" :min="0" />
      </el-form-item>
      <el-divider content-position="left">套餐价格（元）</el-divider>
      <el-form-item label="包月"><el-input-number v-model="form.monthlyPrice" :min="0" :precision="2" /></el-form-item>
      <el-form-item label="包季"><el-input-number v-model="form.quarterlyPrice" :min="0" :precision="2" /></el-form-item>
      <el-form-item label="包年"><el-input-number v-model="form.yearlyPrice" :min="0" :precision="2" /></el-form-item>
      <el-divider content-position="left">临停规则</el-divider>
      <el-form-item label="免费时长(分)"><el-input-number v-model="form.freeMinutes" :min="0" /></el-form-item>
      <el-form-item label="首时段(小时)"><el-input-number v-model="form.firstHours" :min="1" /></el-form-item>
      <el-form-item label="首时段费用"><el-input-number v-model="form.firstPrice" :min="0" :precision="2" /></el-form-item>
      <el-form-item label="每小时费用"><el-input-number v-model="form.hourlyPrice" :min="0" :precision="2" /></el-form-item>
      <el-form-item label="单日封顶"><el-input-number v-model="form.dailyMax" :min="0" :precision="2" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="saving" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createLot, updateLot } from '../api'

const props = defineProps({ visible: Boolean, lot: Object })
const emit = defineEmits(['update:visible', 'saved'])

const emptyForm = () => ({
  id: null, name: '', location: '', totalSpaces: 0, bindLimit: null,
  monthlyPrice: null, quarterlyPrice: null, yearlyPrice: null,
  freeMinutes: 0, firstHours: 1, firstPrice: 0, hourlyPrice: 0, dailyMax: null
})
const form = reactive(emptyForm())
const saving = ref(false)

watch(() => props.visible, v => {
  if (v) Object.assign(form, emptyForm(), props.lot || {})
})

async function save() {
  if (!form.name || !form.location) {
    ElMessage.warning('请填写名称和位置')
    return
  }
  saving.value = true
  try {
    if (form.id) {
      await updateLot(form.id, form)
    } else {
      await createLot(form)
    }
    ElMessage.success('保存成功')
    emit('update:visible', false)
    emit('saved')
  } finally {
    saving.value = false
  }
}
</script>
