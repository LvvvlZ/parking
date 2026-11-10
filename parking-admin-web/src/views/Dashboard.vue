<template>
  <el-row :gutter="16">
    <el-col :span="6" v-for="lot in lots" :key="lot.id">
      <el-card shadow="hover" style="margin-bottom: 16px">
        <template #header>
          <div class="card-head">
            <b>{{ lot.name }}</b>
            <el-tag size="small">{{ lot.totalSpaces }} 个车位</el-tag>
          </div>
        </template>
        <p class="line">位置：{{ lot.location }}</p>
        <p class="line">绑定车辆：<b class="num">{{ lot.bindCount }}</b> 辆</p>
        <p class="line">
          套餐：月 {{ lot.monthlyPrice ?? '-' }} / 季 {{ lot.quarterlyPrice ?? '-' }} / 年 {{ lot.yearlyPrice ?? '-' }}
        </p>
        <p class="line">临停：免费 {{ lot.freeMinutes }} 分钟，首 {{ lot.firstHours }}h {{ lot.firstPrice }} 元</p>
        <div style="margin-top: 12px; text-align: right">
          <el-button size="small" @click="$router.push('/vehicles/' + lot.id)">车辆管理</el-button>
        </div>
      </el-card>
    </el-col>
    <el-col :span="24" v-if="!lots.length">
      <el-empty description="暂无车场" />
    </el-col>
  </el-row>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { myLots } from '../api'

const lots = ref([])

onMounted(async () => {
  lots.value = await myLots()
})
</script>

<style scoped>
.card-head { display: flex; justify-content: space-between; align-items: center; }
.line { font-size: 13px; color: #606266; margin: 6px 0; }
.num { color: #409eff; }
</style>
