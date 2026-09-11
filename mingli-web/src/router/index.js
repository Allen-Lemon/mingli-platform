import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'paipan', component: () => import('@/views/PaiPan.vue'), meta: { title: '八字排盘' } },
  { path: '/rules', name: 'rules', component: () => import('@/views/Rules.vue'), meta: { title: '规则库' } },
  { path: '/history', name: 'history', component: () => import('@/views/History.vue'), meta: { title: '历史记录' } },
  { path: '/about', name: 'about', component: () => import('@/views/About.vue'), meta: { title: '关于' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.afterEach((to) => {
  document.title = to.meta?.title ? `${to.meta.title} · 玄枢` : '玄枢 · 盲派八字'
})

export default router
