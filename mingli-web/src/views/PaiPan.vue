<template>
  <div class="paipan">
    <div class="hero">
      <h1>八字排盘 · 盲派命理推演</h1>
      <p>输入出生时间，按《盲派基本规则》结构化规则完成排盘、起大运与命理线索推演。</p>
    </div>

    <div class="layout">
      <!-- ============ 左侧：录入 ============ -->
      <aside class="form-card card">
        <div class="section-title">出生信息</div>
        <el-form :model="form" label-width="72px" size="default">
          <el-form-item label="姓名">
            <el-input v-model="form.name" placeholder="选填" clearable />
          </el-form-item>

          <el-form-item label="性别">
            <el-radio-group v-model="form.gender">
              <el-radio-button value="M" label="M">男</el-radio-button>
              <el-radio-button value="F" label="F">女</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="出生日期">
            <el-date-picker
              v-model="form.date"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择公历日期"
              style="width: 100%"
              :clearable="false"
            />
          </el-form-item>

          <el-form-item label="出生时刻">
            <div class="time-row">
              <el-select v-model="form.hour" style="width: 88px">
                <el-option v-for="h in 24" :key="h - 1" :label="pad(h - 1) + ' 时'" :value="h - 1" />
              </el-select>
              <el-select v-model="form.minute" style="width: 88px">
                <el-option v-for="m in 60" :key="m - 1" :label="pad(m - 1) + ' 分'" :value="m - 1" />
              </el-select>
            </div>
          </el-form-item>

          <el-form-item label="出生地">
            <el-cascader
              v-model="form.region"
              :options="divisions"
              :props="{ value: 'value', label: 'label', children: 'children' }"
              filterable
              clearable
              placeholder="选择 省 / 市 / 县（区）"
              style="width: 100%"
              @change="onRegionChange"
            />
          </el-form-item>

          <el-form-item label="经度">
            <el-input-number v-model="form.longitude" :precision="4" :step="0.5"
                             :min="70" :max="140" style="width: 100%" />
            <div class="hint">东经为正，用于真太阳时校正（4 分钟 / 度）</div>
          </el-form-item>

          <el-form-item label="时区">
            <el-select v-model="form.tzOffset" style="width: 100%">
              <el-option label="东八区 UTC+8（中国标准时）" :value="8" />
              <el-option label="东九区 UTC+9（日本/韩国）" :value="9" />
              <el-option label="东七区 UTC+7（中南半岛）" :value="7" />
              <el-option label="UTC+0（格林尼治）" :value="0" />
              <el-option label="西五区 UTC-5（美东）" :value="-5" />
              <el-option label="西八区 UTC-8（美西）" :value="-8" />
            </el-select>
          </el-form-item>

          <el-form-item label="校正">
            <el-switch v-model="form.useTrueSolarTime" active-text="真太阳时" />
            <div class="hint">关闭则直接使用钟表时间定时柱</div>
          </el-form-item>

          <el-form-item label="存档">
            <el-switch v-model="form.save" active-text="保存到数据库" />
          </el-form-item>

          <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="submit">
            开始排盘
          </el-button>

          <div class="quick">
            <span class="text-small text-muted">示例：</span>
            <el-link type="primary" :underline="false" @click="fill(1990, 5, 15, 14, 30, ['44','4401','440106'])">广州 1990</el-link>
            <el-link type="primary" :underline="false" @click="fill(1985, 2, 4, 6, 0, ['11','1101','110101'])">北京 1985</el-link>
            <el-link type="primary" :underline="false" @click="fill(2000, 1, 1, 0, 10, ['31','3101','310101'])">上海 2000</el-link>
          </div>
        </el-form>
      </aside>

      <!-- ============ 右侧：结果 ============ -->
      <section class="result">
        <el-empty v-if="!result && !loading" description="请输入出生时间后开始排盘">
          <template #image>
            <div class="empty-seal">卦</div>
          </template>
        </el-empty>

        <div v-if="result" class="result-inner">
          <!-- 命盘 -->
          <div class="card block">
            <div class="section-title">
              四柱命盘
              <span class="sub">{{ result.chart.fullText }}</span>
            </div>
            <BaziPan :chart="result.chart" />

            <div class="meta-grid">
              <div class="meta-item">
                <span class="mk">日主</span>
                <span class="mv">{{ result.chart.dayMaster }}
                  <em>{{ result.chart.dayMasterWuXing }} · {{ result.chart.dayMasterYang ? '阳' : '阴' }}</em>
                </span>
              </div>
              <div class="meta-item">
                <span class="mk">强弱</span>
                <span class="mv">{{ result.chart.strengthText }}
                  <em>参考分 {{ result.chart.dayMasterScore }}</em>
                </span>
              </div>
              <div class="meta-item">
                <span class="mk">空亡</span>
                <span class="mv">{{ result.chart.kongWangXun }}
                  <em>{{ result.chart.kongWang.join('、') }}</em>
                </span>
              </div>
              <div class="meta-item">
                <span class="mk">命理年</span>
                <span class="mv">{{ result.chart.mingLiYearName }}</span>
              </div>
              <div class="meta-item wide">
                <span class="mk">真太阳时</span>
                <span class="mv">{{ result.chart.trueSolarTimeText }}</span>
              </div>
              <div class="meta-item wide">
                <span class="mk">节界</span>
                <span class="mv">{{ result.chart.solarTermAtBirth }}　|
                  上节 {{ result.chart.prevJieText }}　|　下节 {{ result.chart.nextJieText }}</span>
              </div>
            </div>

            <el-alert v-if="result.chart.boundaryWarning" type="warning" show-icon :closable="false"
                      class="boundary">
              <template #title>节气临界提示</template>
              {{ result.chart.boundaryWarning }}
            </el-alert>

            <div class="wx-bar">
              <div class="wx-title">五行分布（天干 2 权 · 地支 1 权 · 藏干 1 权）</div>
              <div class="wx-track">
                <div v-for="w in wuXingList" :key="w.name" class="wx-seg"
                     :class="'bg-' + w.name" :style="{ width: w.pct + '%' }"
                     :title="w.name + ' ' + w.value">
                  <span v-if="w.pct > 8">{{ w.name }} {{ w.value }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 大运 -->
          <div class="card block">
            <div class="section-title">大运</div>
            <DaYunTable :chart="result.chart" />
          </div>

          <!-- 分析 -->
          <div class="card block">
            <div class="section-title">命理推演</div>
            <AnalysisPanel :analysis="result.analysis" />
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { analyze } from '@/api'
import divisions from '@/assets/divisions.json'
import BaziPan from '@/components/BaziPan.vue'
import DaYunTable from '@/components/DaYunTable.vue'
import AnalysisPanel from '@/components/AnalysisPanel.vue'

const loading = ref(false)
const result = ref(null)

const form = reactive({
  name: '',
  gender: 'M',
  date: '1990-05-15',
  hour: 12,
  minute: 0,
  region: ['44', '4401', '440106'], // 广东省 / 广州市 / 天河区
  cityName: '广东省/广州市/天河区',
  longitude: 113.2644,
  latitude: 23.1291,
  tzOffset: 8,
  useTrueSolarTime: true,
  save: false
})

const pad = (n) => String(n).padStart(2, '0')

const wuXingList = computed(() => {
  if (!result.value) return []
  const c = result.value.chart
  const map = { 木: 0, 火: 0, 土: 0, 金: 0, 水: 0 }
  const add = (wx, w) => { if (wx && map[wx] !== undefined) map[wx] += w }
  ;[c.year, c.month, c.day, c.hour].forEach((p) => {
    add(p.ganWuXing, 2)
    add(p.zhiWuXing, 1)
    ;(p.zangGan || []).forEach((z) => add(z.ganWuXing, 1))
  })
  const total = Object.values(map).reduce((a, b) => a + b, 0) || 1
  return Object.entries(map).map(([name, value]) => ({
    name, value, pct: +(value / total * 100).toFixed(1)
  }))
})

// 按级联路径（省/市/县 的 code 数组）在 divisions 中定位节点，返回叶子节点与标签路径
function resolveRegion(values) {
  let level = divisions
  let node = null
  const labels = []
  for (const v of values || []) {
    const hit = level.find((n) => n.value === v)
    if (!hit) break
    node = hit
    labels.push(hit.label)
    level = hit.children || []
  }
  return { node, labels }
}

// 选择出生地后，自动带入该地点的经度（真太阳时校正用），并生成可读地名
function onRegionChange(values) {
  const { node, labels } = resolveRegion(values)
  if (node) {
    form.longitude = node.longitude
    form.latitude = node.latitude
    // 直辖市的「市辖区」为冗余层级，展示时去除
    form.cityName = labels.filter((l) => l !== '市辖区').join('/')
  }
}

function fill(y, m, d, h, mi, region) {
  form.date = `${y}-${pad(m)}-${pad(d)}`
  form.hour = h
  form.minute = mi
  form.region = region
  onRegionChange(region)
  submit()
}

async function submit() {
  if (!form.date) {
    ElMessage.warning('请选择出生日期')
    return
  }
  const [y, m, d] = form.date.split('-').map(Number)
  loading.value = true
  try {
    const res = await analyze({
      name: form.name,
      gender: form.gender,
      year: y, month: m, day: d,
      hour: form.hour,
      minute: form.minute,
      longitude: form.longitude,
      tzOffset: form.tzOffset,
      cityName: form.cityName,
      useTrueSolarTime: form.useTrueSolarTime,
      save: form.save
    })
    result.value = res.data
    if (res.data.saveError) ElMessage.warning(res.data.saveError)
    else if (form.save) ElMessage.success('已保存到历史记录')
  } catch (e) {
    ElMessage.error(e.message || '排盘失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 根据默认出生地（广东省/广州市/天河区）带入经度
  onRegionChange(form.region)
})
</script>

<style scoped>
.hero { margin-bottom: 20px; }
.hero h1 {
  font-size: 24px;
  letter-spacing: 2px;
  margin-bottom: 6px;
}
.hero p {
  margin: 0;
  color: var(--ink-3);
  font-size: 13px;
}

.layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 20px;
  align-items: start;
}
.form-card { position: sticky; top: 78px; }
.hint { font-size: 11px; color: var(--ink-4); line-height: 1.5; margin-top: 2px; }
.time-row { display: flex; gap: 8px; }
.quick {
  margin-top: 14px;
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}
.empty-seal {
  width: 64px;
  height: 64px;
  margin: 0 auto;
  display: grid;
  place-items: center;
  border: 2px solid var(--line);
  border-radius: 8px;
  color: var(--ink-4);
  font-family: var(--font-serif);
  font-size: 28px;
}

.result-inner { display: flex; flex-direction: column; gap: 20px; }
.block { padding: 20px; }
.sub {
  font-family: var(--font-serif);
  font-size: 15px;
  color: var(--cinnabar);
  letter-spacing: 3px;
  margin-left: auto;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px dashed var(--line);
}
.meta-item { font-size: 12px; }
.meta-item.wide { grid-column: span 4; }
.mk { color: var(--ink-4); margin-right: 8px; }
.mv { color: var(--ink-2); }
.mv em { font-style: normal; color: var(--ink-4); margin-left: 4px; }

.boundary { margin-top: 16px; }
.boundary :deep(.el-alert__description) { font-size: 12.5px; line-height: 1.8; }
.wx-bar { margin-top: 16px; }
.wx-title { font-size: 12px; color: var(--ink-4); margin-bottom: 6px; }
.wx-track {
  display: flex;
  height: 22px;
  border-radius: 6px;
  overflow: hidden;
  background: #efe9dc;
}
.wx-seg {
  display: grid;
  place-items: center;
  font-size: 11px;
  color: #fff;
  transition: width 0.4s ease;
}
.bg-木 { background: var(--wood); }
.bg-火 { background: var(--fire); }
.bg-土 { background: var(--earth); }
.bg-金 { background: var(--metal); }
.bg-水 { background: var(--water); }

@media (max-width: 900px) {
  .layout { grid-template-columns: 1fr; }
  .form-card { position: static; }
  .meta-grid { grid-template-columns: repeat(2, 1fr); }
  .meta-item.wide { grid-column: span 2; }
}
</style>
