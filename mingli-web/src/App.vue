<template>
  <div class="app-shell">
    <header class="site-header">
      <div class="header-inner">
        <div class="brand" @click="$router.push('/')">
          <span class="brand-seal">玄</span>
          <div class="brand-text">
            <strong>玄枢</strong>
            <small>盲派八字命理推演</small>
          </div>
        </div>
        <nav class="site-nav">
          <router-link to="/">排盘</router-link>
          <router-link to="/rules">规则库</router-link>
          <router-link to="/history">历史</router-link>
          <router-link to="/about">关于</router-link>
        </nav>
      </div>
    </header>

    <main class="site-main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <footer class="site-footer">
      <p>玄枢 · 盲派八字命理推演平台</p>
      <p class="disclaimer">
        本站内容为传统命理文献的结构化整理，仅作文化研究与参考，不构成科学结论或现实预测承诺，
        不应作为医疗、法律、投资、婚恋等重大决策依据。
      </p>
    </footer>
  </div>
</template>

<script setup></script>

<style scoped>
.app-shell {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}
.site-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(247, 243, 234, 0.92);
  backdrop-filter: saturate(160%) blur(8px);
  border-bottom: 1px solid var(--line);
}
.header-inner {
  max-width: 1180px;
  margin: 0 auto;
  padding: 12px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}
.brand-seal {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  background: var(--cinnabar);
  color: #fff8ec;
  border-radius: 6px;
  font-size: 20px;
  font-weight: 700;
  font-family: var(--font-serif);
  box-shadow: 0 2px 8px rgba(176, 58, 46, 0.25);
}
.brand-text strong {
  display: block;
  font-size: 18px;
  letter-spacing: 3px;
  color: var(--ink);
  font-family: var(--font-serif);
}
.brand-text small {
  font-size: 11px;
  color: var(--ink-3);
  letter-spacing: 1px;
}
.site-nav {
  display: flex;
  gap: 4px;
}
.site-nav a {
  padding: 7px 16px;
  border-radius: 999px;
  color: var(--ink-2);
  text-decoration: none;
  font-size: 14px;
  transition: all 0.2s;
}
.site-nav a:hover {
  background: rgba(176, 58, 46, 0.07);
  color: var(--cinnabar);
}
.site-nav a.router-link-exact-active {
  background: var(--cinnabar);
  color: #fff;
}
.site-main {
  flex: 1;
  width: 100%;
  max-width: 1180px;
  margin: 0 auto;
  padding: 24px;
  box-sizing: border-box;
}
.site-footer {
  border-top: 1px solid var(--line);
  padding: 20px 24px 28px;
  text-align: center;
  color: var(--ink-3);
  font-size: 12px;
  line-height: 1.8;
}
.site-footer p {
  margin: 0;
}
.disclaimer {
  max-width: 720px;
  margin: 6px auto 0 !important;
  color: var(--ink-4);
}
.fade-enter-active, .fade-leave-active { transition: opacity 0.22s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

@media (max-width: 640px) {
  .header-inner { padding: 10px 14px; }
  .brand-text strong { font-size: 16px; }
  .site-nav a { padding: 6px 10px; font-size: 13px; }
  .site-main { padding: 14px; }
}
</style>
