<template>
  <div class="analysis">
    <el-alert v-show="analysis.headline" :title="analysis.headline" type="warning"
              :closable="false" show-icon class="headline" />

    <el-collapse v-model="active" class="sec-collapse">
      <el-collapse-item v-for="s in analysis.sections" :key="s.key" :name="s.key">
        <template #title>
          <span class="sec-title">{{ s.title }}</span>
        </template>

        <p v-if="s.summary" class="sec-summary">{{ s.summary }}</p>

        <div v-for="(it, i) in s.items" :key="i" class="item">
          <div class="item-label" :class="'lv-' + it.level">{{ it.label }}</div>
          <div class="item-value">{{ it.value }}</div>
        </div>
      </el-collapse-item>
    </el-collapse>

    <div class="rules-box">
      <div class="section-title">命中的规则条文</div>
      <el-table :data="analysis.ruleHits" size="small" border>
        <el-table-column prop="code" label="编号" width="70" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.code }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="规则" width="150" />
        <el-table-column prop="text" label="说明" min-width="240">
          <template #default="{ row }">
            {{ row.text }}
            <el-tag v-if="row.doubtful" size="small" type="warning" effect="plain" class="doubt">
              存疑
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-alert type="info" :closable="false" class="disclaimer">
      <template #title>使用边界</template>
      {{ analysis.disclaimer }}
    </el-alert>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({ analysis: { type: Object, required: true } })
const active = ref(['overview', 'caifu'])

watch(() => props.analysis, () => {
  active.value = ['overview', 'caifu']
})
</script>

<style scoped>
.headline { margin-bottom: 14px; }
.headline :deep(.el-alert__title) { font-family: var(--font-serif); font-size: 15px; }

.sec-collapse { border-top: 1px solid var(--line); }
.sec-collapse :deep(.el-collapse-item__header) { border-bottom: 1px solid var(--line); }
.sec-title {
  font-family: var(--font-serif);
  font-size: 15px;
  color: var(--ink);
}
.sec-summary {
  margin: 0 0 12px;
  padding: 10px 12px;
  background: var(--cinnabar-soft);
  border-left: 3px solid var(--cinnabar);
  border-radius: 4px;
  font-size: 13px;
  color: var(--ink-2);
}
.item {
  display: flex;
  gap: 12px;
  padding: 7px 0;
  border-bottom: 1px dashed #efe8da;
  font-size: 13px;
}
.item:last-child { border-bottom: none; }
.item-label {
  flex: 0 0 118px;
  color: var(--ink-3);
}
.item-label.lv-good { color: var(--jade); }
.item-label.lv-warn { color: var(--cinnabar); }
.item-value { flex: 1; color: var(--ink-2); line-height: 1.75; }

.rules-box { margin-top: 22px; }
.doubt { margin-left: 6px; }
.disclaimer { margin-top: 18px; }
.disclaimer :deep(.el-alert__description) { font-size: 12px; line-height: 1.8; }
</style>
