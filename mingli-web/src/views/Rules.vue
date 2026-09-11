<template>
  <div class="rules-page">
    <div class="hero">
      <h1>规则库</h1>
      <p>下列查找表与规则条文来自《盲派命理规则（开发导向版）》，与后端推理引擎使用同一份数据。</p>
      <el-tag size="small" :type="dict.source === 'MySQL' ? 'success' : 'warning'" effect="plain">
        数据源：{{ dict.source }}
      </el-tag>
    </div>

    <el-tabs v-model="tab" class="tabs">
      <el-tab-pane label="规则条文" name="rule">
        <div class="card">
          <el-table :data="dict.rules" size="small" border stripe>
            <el-table-column prop="rule_code" label="编号" width="80" align="center">
              <template #default="{ row }">
                <el-tag size="small" effect="plain">{{ row.rule_code }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="分类" width="120" />
            <el-table-column prop="title" label="标题" width="180" />
            <el-table-column prop="content" label="内容" min-width="300">
              <template #default="{ row }">
                {{ row.content }}
                <el-tag v-if="row.doubtful" size="small" type="warning" effect="plain">存疑</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="十神类象" name="shishen">
        <div class="card">
          <el-table :data="shishenRows" size="small" border stripe>
            <el-table-column prop="name" label="十神" width="90" align="center" />
            <el-table-column prop="positive" label="正面心性" min-width="220" />
            <el-table-column prop="negative" label="过重负面 / 偏象" min-width="200" />
            <el-table-column prop="careers" label="可取职业" min-width="220" />
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="干支物象" name="wuxiang">
        <div class="card">
          <div class="section-title">十干物象</div>
          <el-table :data="ganRows" size="small" border stripe>
            <el-table-column prop="k" label="天干" width="90" align="center" />
            <el-table-column prop="v" label="物象" min-width="300" />
          </el-table>
          <div class="section-title" style="margin-top: 22px">十二支物象</div>
          <el-table :data="zhiRows" size="small" border stripe>
            <el-table-column prop="k" label="地支" width="90" align="center" />
            <el-table-column prop="v" label="物象" min-width="300" />
          </el-table>
          <div class="section-title" style="margin-top: 22px">墓库象</div>
          <el-table :data="mukuRows" size="small" border stripe>
            <el-table-column prop="k" label="库的类别" width="140" align="center" />
            <el-table-column prop="v" label="可取象" min-width="260" />
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="神煞对照" name="shensha">
        <div class="card">
          <div class="section-title">禄神（1.1）</div>
          <div class="chips">
            <span v-for="(v, k) in dict.lushen" :key="k" class="chip">{{ k }} → {{ v }}</span>
          </div>
          <div class="section-title" style="margin-top: 20px">羊刃（1.2，仅阳干）</div>
          <div class="chips">
            <span v-for="(v, k) in dict.yangren" :key="k" class="chip warn">{{ k }} → {{ v }}</span>
          </div>
          <div class="section-title" style="margin-top: 20px">驿马（1.3）</div>
          <el-table :data="yimaRows" size="small" border stripe>
            <el-table-column prop="k" label="年/日支" width="120" align="center" />
            <el-table-column prop="v" label="驿马支" min-width="200" />
          </el-table>
          <div class="section-title" style="margin-top: 20px">六甲空亡（1.4）</div>
          <el-table :data="kongwangRows" size="small" border stripe>
            <el-table-column prop="k" label="旬" width="140" align="center" />
            <el-table-column prop="v" label="空亡地支" min-width="200" />
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="财富职业" name="qucai">
        <div class="card">
          <div class="section-title">财富替代规则（2.1）</div>
          <el-table :data="dict.qucai" size="small" border stripe>
            <el-table-column prop="cond" label="原局条件" width="220" />
            <el-table-column prop="method" label="取财方式" width="130" />
            <el-table-column prop="xiji" label="喜忌" min-width="260" />
          </el-table>
          <div class="section-title" style="margin-top: 22px">取财方式判定（2.2）</div>
          <el-table :data="dict.qucaiMode" size="small" border stripe>
            <el-table-column prop="feature" label="做功特征" min-width="260" />
            <el-table-column prop="method" label="取财方式" width="150" />
            <el-table-column prop="industries" label="常见行业" min-width="220" />
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="宫位类象" name="gongwei">
        <div class="card">
          <el-table :data="gongweiRows" size="small" border stripe>
            <el-table-column prop="dim" label="维度" width="110" />
            <el-table-column prop="y" label="年柱" min-width="150" />
            <el-table-column prop="m" label="月柱" min-width="150" />
            <el-table-column prop="dg" label="日干" min-width="90" />
            <el-table-column prop="dz" label="日支" min-width="150" />
            <el-table-column prop="h" label="时柱" min-width="150" />
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDict } from '@/api'

const tab = ref('rule')
const dict = ref({ rules: [], lushen: {}, yangren: {}, yima: {}, kongwang: {}, gongwei: {}, shishen: {}, ganWuxiang: {}, zhiWuxiang: {}, muku: {}, qucai: [], qucaiMode: [], source: '' })

const toRows = (obj) => Object.entries(obj || {}).map(([k, v]) => ({ k, v }))

const shishenRows = computed(() =>
  Object.entries(dict.value.shishen || {}).map(([name, v]) => ({ name, ...v }))
)
const ganRows = computed(() => toRows(dict.value.ganWuxiang))
const zhiRows = computed(() => toRows(dict.value.zhiWuxiang))
const mukuRows = computed(() => toRows(dict.value.muku))
const yimaRows = computed(() => {
  const m = {}
  Object.entries(dict.value.yima || {}).forEach(([k, v]) => { m[v] = m[v] ? m[v] + ' ' + k : k })
  return Object.entries(m).map(([v, k]) => ({ k, v }))
})
const kongwangRows = computed(() => toRows(dict.value.kongwang))
const gongweiRows = computed(() =>
  Object.entries(dict.value.gongwei || {}).map(([dim, arr]) => ({
    dim, y: arr[0] || '—', m: arr[1] || '—', dg: arr[2] || '—', dz: arr[3] || '—', h: arr[4] || '—'
  }))
)

onMounted(async () => {
  try {
    const r = await getDict()
    dict.value = r.data
  } catch (e) {
    ElMessage.error('加载规则库失败：' + e.message)
  }
})
</script>

<style scoped>
.hero h1 { font-size: 22px; letter-spacing: 2px; margin-bottom: 6px; }
.hero p { margin: 0 0 10px; color: var(--ink-3); font-size: 13px; }
.tabs { margin-top: 8px; }
.chips { display: flex; flex-wrap: wrap; gap: 8px; }
.chip {
  padding: 5px 12px;
  border: 1px solid var(--line);
  border-radius: 999px;
  font-size: 13px;
  background: var(--cinnabar-soft);
  color: var(--ink-2);
}
.chip.warn { background: rgba(176, 58, 46, 0.12); color: var(--cinnabar); }
</style>
