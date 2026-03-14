<template>
  <div class="dashboard">
    <div class="stat-cards">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-value">{{ stats.totalUsers }}</div>
        <div class="stat-label">用户总数</div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-value">{{ stats.totalItems }}</div>
        <div class="stat-label">物品总数</div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-value">{{ stats.todayItems }}</div>
        <div class="stat-label">今日新增</div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-value">{{ stats.pendingReports }}</div>
        <div class="stat-label">待审核举报</div>
      </el-card>
    </div>
    <el-row :gutter="24">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>分类分布</template>
          <div ref="pieRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>近7天趋势</template>
          <div ref="lineRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { getStats } from '../api/admin'

const stats = ref({
  totalUsers: 0,
  totalItems: 0,
  todayItems: 0,
  pendingReports: 0,
  categoryDistribution: [],
  recentTrend: []
})

const pieRef = ref(null)
const lineRef = ref(null)

const loadStats = async () => {
  const res = await getStats()
  stats.value = res.data
  initPie()
  initLine()
}

const initPie = () => {
  if (!pieRef.value || !stats.value.categoryDistribution?.length) return
  const chart = echarts.init(pieRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: '60%',
      data: stats.value.categoryDistribution.map(d => ({ name: d.name, value: d.value }))
    }]
  })
}

const initLine = () => {
  if (!lineRef.value || !stats.value.recentTrend?.length) return
  const chart = echarts.init(lineRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: stats.value.recentTrend.map(d => d.date) },
    yAxis: { type: 'value' },
    series: [{ type: 'line', data: stats.value.recentTrend.map(d => d.count), smooth: true }]
  })
}

onMounted(loadStats)
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}

.stat-card {
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #1989fa;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 8px;
}
</style>
