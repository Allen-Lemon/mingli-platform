<template>
  <div class="dayun">
    <div class="dy-meta">
      <el-tag size="small" type="danger" effect="plain">{{ chart.daYunDirection }}</el-tag>
      <span class="text-muted text-small">{{ chart.qiYunText }}</span>
    </div>

    <el-table :data="chart.daYun" size="small" border stripe class="dy-table">
      <el-table-column prop="seq" label="#" width="46" align="center">
        <template #default="{ row }">{{ row.seq }}</template>
      </el-table-column>
      <el-table-column label="大运" width="86" align="center">
        <template #default="{ row }">
          <span class="gz">{{ row.ganZhi }}</span>
        </template>
      </el-table-column>
      <el-table-column label="十神" width="80" align="center">
        <template #default="{ row }">{{ row.ganShiShen }}</template>
      </el-table-column>
      <el-table-column label="起运" width="90" align="center">
        <template #default="{ row }">{{ row.startAge.toFixed(1) }} 岁</template>
      </el-table-column>
      <el-table-column label="年份" width="110" align="center">
        <template #default="{ row }">{{ row.startYear }}—{{ row.endYear }}</template>
      </el-table-column>
      <el-table-column label="大限" width="96" align="center">
        <template #default="{ row }">
          <el-tag size="small" effect="plain">{{ row.daXian }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="藏干十神" min-width="180">
        <template #default="{ row }">
          <span class="zg">{{ row.zangGanShiShen.join('、') }}</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
defineProps({ chart: { type: Object, required: true } })
</script>

<style scoped>
.dy-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
.dy-table { font-size: 13px; }
.gz {
  font-family: var(--font-serif);
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 1px;
}
.zg { font-size: 12px; color: var(--ink-3); }
.dy-table :deep(.el-table__cell) { padding: 6px 0; }
</style>
