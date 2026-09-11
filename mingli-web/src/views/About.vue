<template>
  <div class="about-page">
    <div class="hero">
      <h1>关于玄枢</h1>
      <p>一个依据《盲派基本规则》结构化整理稿实现的八字排盘与命理推演平台。</p>
    </div>

    <div class="card">
      <div class="section-title">技术实现</div>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="前端">Vue 3 + Vite + Element Plus</el-descriptions-item>
        <el-descriptions-item label="后端">Java 8 + Spring Boot 2.7 + MyBatis</el-descriptions-item>
        <el-descriptions-item label="数据库">MySQL 8（规则字典 + 排盘记录）</el-descriptions-item>
        <el-descriptions-item label="历法算法">
          儒略日换算 + Meeus《Astronomical Algorithms》太阳视黄经迭代求节气
          （含章动 Δψ、光行差与 ΔT 修正）
        </el-descriptions-item>
        <el-descriptions-item label="真太阳时">经度平太阳时校正（4 分钟/度）+ 时差（Equation of Time）校正</el-descriptions-item>
        <el-descriptions-item label="排盘规则">五虎遁（年起月）、五鼠遁（日起时）、夜子时顺延、地支藏干、纳音、空亡</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="card">
      <div class="section-title">已实现的规则范围</div>
      <div class="rule-list">
        <div class="rule-item"><b>1.1—1.9</b> 禄神、羊刃、驿马、六甲空亡、四柱宫位、十神类象、十干/十二支物象、墓库象</div>
        <div class="rule-item"><b>2.1—2.3</b> 财富替代规则、取财方式判定、过河拆桥（大财结构）</div>
        <div class="rule-item"><b>3.1—3.3</b> 官命判定流程、学历判定、行业取象</div>
        <div class="rule-item"><b>四</b> 取象七原则：共象 / 合象 / 化象 / 墓象 / 制象 / 带象 / 借象</div>
        <div class="rule-item"><b>五</b> 正局与反局判定流程（步骤 1—5）</div>
        <div class="rule-item"><b>6.1—6.2</b> 婚姻判定、桃花、子女取法</div>
        <div class="rule-item"><b>7.1—7.2</b> 大限年龄段、起运、应期规则</div>
      </div>
    </div>

    <div class="card">
      <div class="section-title">精度与使用边界</div>
      <el-alert type="warning" :closable="false" show-icon class="note">
        <template #title">节气时刻精度</template>
        节气时刻采用 Meeus 截断级数求解，与《中国天文年历》对照误差约 ±15 分钟。
        若出生时刻恰好落在某一「节」前后 30 分钟内，年柱 / 月柱存在两可，
        建议以专业排盘或天文年历复核后确定。
      </el-alert>
      <el-alert type="info" :closable="false" show-icon class="note">
        <template #title">不做绝对结论</template>
        原文中标注「待验 / 不明 / 有误」或存在多种讲法者，本站一律标为「存疑」并保留多种可能，
        不以单一结论输出。分析结果为规则线索，不是确定性预测。
      </el-alert>
      <el-alert type="error" :closable="false" show-icon class="note">
        <template #title">免责声明</template>
        本平台为传统命理文献的结构化整理与文化研究工具，不构成科学结论或现实预测承诺，
        不应作为医疗、法律、投资、婚恋等重大决策依据。
      </el-alert>
    </div>
  </div>
</template>

<script setup></script>

<style scoped>
.hero h1 { font-size: 22px; letter-spacing: 2px; margin-bottom: 6px; }
.hero p { margin: 0 0 16px; color: var(--ink-3); font-size: 13px; }
.card { margin-bottom: 18px; }
.rule-list { display: flex; flex-direction: column; gap: 8px; font-size: 13px; color: var(--ink-2); }
.rule-item b {
  display: inline-block;
  min-width: 66px;
  color: var(--cinnabar);
  font-family: var(--font-serif);
}
.note { margin-bottom: 12px; }
.note :deep(.el-alert__description) { font-size: 12.5px; line-height: 1.85; }
</style>
