<template>
  <div class="history-page">
    <div class="hero">
      <h1>历史记录</h1>
      <p>保存在 MySQL 中的排盘记录，可查看当时的完整命盘与推演结果。</p>
    </div>

    <div class="card">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="按姓名 / 城市 / 日柱搜索" clearable
                  style="max-width: 260px" @keyup.enter="load" @clear="load" />
        <el-select v-model="gender" placeholder="性别" clearable style="width: 110px" @change="load">
          <el-option label="男" value="M" />
          <el-option label="女" value="F" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
        <span class="total">共 {{ total }} 条</span>
      </div>

      <el-table :data="list" v-loading="loading" size="small" border stripe>
        <el-table-column prop="name" label="姓名" width="110">
          <template #default="{ row }">{{ row.name || '—' }}</template>
        </el-table-column>
        <el-table-column label="四柱" min-width="180">
          <template #default="{ row }">
            <span class="gz">{{ row.yearGz }} {{ row.monthGz }} {{ row.dayGz }} {{ row.hourGz }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="dayMaster" label="日主" width="70" align="center" />
        <el-table-column label="性别" width="60" align="center">
          <template #default="{ row }">{{ row.gender === 'F' ? '女' : '男' }}</template>
        </el-table-column>
        <el-table-column prop="birthTime" label="出生时间" width="150" />
        <el-table-column prop="cityName" label="出生地" width="110" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="view(row)">查看</el-button>
            <el-button link type="danger" size="small" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <span class="text-muted">暂无记录（排盘时勾选「保存到数据库」即可留存）</span>
        </template>
      </el-table>

      <el-pagination v-if="total > size" class="pager" background layout="prev, pager, next"
                     :total="total" :page-size="size" :current-page="page" @current-change="onPage" />
    </div>

    <el-drawer v-model="drawer" :title="detail?.name ? detail.name + ' 的命盘' : '命盘详情'"
               size="70%" :destroy-on-close="true">
      <div v-if="detailChart">
        <BaziPan :chart="detailChart" class="drawer-pan" />
        <div class="card" style="margin-top: 16px">
          <div class="section-title">大运</div>
          <DaYunTable :chart="detailChart" />
        </div>
        <div class="card" style="margin-top: 16px">
          <div class="section-title">命理推演</div>
          <AnalysisPanel :analysis="detailAnalysis" />
        </div>
      </div>
      <el-empty v-else description="该记录未保存分析明细" />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getHistory, getRecord, deleteRecord } from '@/api'
import BaziPan from '@/components/BaziPan.vue'
import DaYunTable from '@/components/DaYunTable.vue'
import AnalysisPanel from '@/components/AnalysisPanel.vue'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const gender = ref('')
const loading = ref(false)

const drawer = ref(false)
const detail = ref(null)
const detailChart = ref(null)
const detailAnalysis = ref(null)

async function load() {
  loading.value = true
  try {
    const r = await getHistory({ page: page.value, size: size.value, keyword: keyword.value, gender: gender.value })
    list.value = r.data.list || []
    total.value = r.data.total || 0
    if (r.data.warning) ElMessage.warning(r.data.warning)
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

function onPage(p) {
  page.value = p
  load()
}

async function view(row) {
  try {
    const r = await getRecord(row.id)
    detail.value = r.data
    detailChart.value = r.data.chartJson ? JSON.parse(r.data.chartJson) : null
    detailAnalysis.value = r.data.analysisJson ? JSON.parse(r.data.analysisJson) : { sections: [], ruleHits: [], headline: '', disclaimer: '' }
    drawer.value = true
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm('确认删除该排盘记录？', '提示', { type: 'warning' })
    await deleteRecord(row.id)
    ElMessage.success('已删除')
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message)
  }
}

onMounted(load)
</script>

<style scoped>
.hero h1 { font-size: 22px; letter-spacing: 2px; margin-bottom: 6px; }
.hero p { margin: 0 0 16px; color: var(--ink-3); font-size: 13px; }
.toolbar { display: flex; gap: 10px; align-items: center; margin-bottom: 14px; flex-wrap: wrap; }
.total { margin-left: auto; color: var(--ink-3); font-size: 13px; }
.gz { font-family: var(--font-serif); letter-spacing: 2px; font-size: 14px; }
.pager { margin-top: 14px; justify-content: center; }
.drawer-pan { margin-bottom: 4px; }
</style>
