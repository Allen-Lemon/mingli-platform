<template>
  <div class="bazi-pan">
    <div v-for="(p, i) in pillars" :key="i" class="pillar" :class="{ 'is-day': i === 2 }">
      <div class="pillar-head">
        <span class="pos">{{ p.position }}</span>
        <span class="gw">{{ p.gongWei }}</span>
      </div>

      <div class="gan" :class="'wx-' + p.ganWuXing">
        {{ p.gan }}
      </div>
      <div class="ss-gan">{{ i === 2 ? '日主' : p.ganShiShen || '—' }}</div>

      <div class="zhi" :class="'wx-' + p.zhiWuXing">
        {{ p.zhi }}
      </div>
      <div class="ss-zhi">{{ p.zhi }}</div>

      <div class="rows">
        <div class="row">
          <span class="k">藏干</span>
          <span class="v">
            <template v-if="p.zangGan && p.zangGan.length">
              <span v-for="(z, k) in p.zangGan" :key="k" class="zg">
                {{ z.gan }}<em>{{ z.shiShen }}</em>
              </span>
            </template>
            <span v-else>—</span>
          </span>
        </div>
        <div class="row">
          <span class="k">纳音</span><span class="v">{{ p.naYin || '—' }}</span>
        </div>
        <div v-if="p.shengXiao" class="row">
          <span class="k">生肖</span><span class="v">{{ p.shengXiao }}</span>
        </div>
      </div>

      <div class="marks">
        <el-tag v-for="(m, k) in p.marks" :key="k" size="small"
                :type="markType(m)" effect="plain">{{ m }}</el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ chart: { type: Object, required: true } })

const pillars = computed(() => [props.chart.year, props.chart.month, props.chart.day, props.chart.hour])

function markType(m) {
  if (m === '空亡') return 'info'
  if (m === '禄神' || m === '天乙贵人' || m === '文昌贵人' || m === '将星') return 'success'
  if (m === '羊刃' || m === '劫煞') return 'danger'
  if (m === '桃花' || m === '驿马') return 'warning'
  return 'info'
}
</script>

<style scoped>
.bazi-pan {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.pillar {
  background: var(--paper-2);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 14px 10px 12px;
  text-align: center;
  position: relative;
  transition: box-shadow 0.2s, transform 0.2s;
}
.pillar:hover {
  box-shadow: 0 6px 18px rgba(43, 40, 35, 0.08);
  transform: translateY(-2px);
}
.pillar.is-day {
  border-color: var(--cinnabar);
  box-shadow: inset 0 0 0 1px rgba(176, 58, 46, 0.18);
}
.pillar-head {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-bottom: 8px;
}
.pos {
  font-family: var(--font-serif);
  font-size: 14px;
  color: var(--ink);
  letter-spacing: 2px;
}
.gw {
  font-size: 10px;
  color: var(--ink-4);
  line-height: 1.3;
  min-height: 26px;
}
.gan, .zhi {
  font-family: var(--font-serif);
  font-size: 40px;
  line-height: 1.1;
  font-weight: 600;
}
.zhi { margin-top: 2px; }
.ss-gan, .ss-zhi {
  font-size: 12px;
  color: var(--ink-3);
  letter-spacing: 1px;
}
.ss-gan { margin-bottom: 8px; }
.ss-zhi { margin-bottom: 8px; }
.rows {
  text-align: left;
  border-top: 1px dashed var(--line);
  padding-top: 8px;
  font-size: 12px;
}
.row {
  display: flex;
  gap: 6px;
  margin-bottom: 3px;
  align-items: flex-start;
}
.k { color: var(--ink-4); flex: 0 0 28px; }
.v { color: var(--ink-2); flex: 1; }
.zg { display: inline-block; margin-right: 6px; }
.zg em {
  font-style: normal;
  color: var(--ink-4);
  font-size: 10px;
  margin-left: 1px;
}
.marks {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  justify-content: center;
  margin-top: 8px;
  min-height: 22px;
}
.marks :deep(.el-tag) { margin: 0; }

@media (max-width: 720px) {
  .bazi-pan { grid-template-columns: repeat(2, 1fr); }
  .gan, .zhi { font-size: 32px; }
}
</style>
