/**
 * ============================================================================
 * 模块：Vue Router 路由与权限守卫
 * ============================================================================
 *
 * 布局结构：
 * - /login、/register* 独立页
 * - 其余业务页嵌套在 MainLayout 下（顶栏+底栏）
 *
 * meta 约定：
 * - auth: true → 必须登录，否则 next('/login')
 * - role: 'USER'|'MERCHANT'|'ADMIN' → 角色不符则 next('/')
 *
 * 守卫触发：每次路由跳转前 beforeEach
 * 数据来源：Pinia useUserStore（启动时从 sessionStorage 恢复）
 *
 * FIXME：角色校验仅前端，真正权限以后端 UserContext.role 为准
 * ============================================================================
 */
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  { path: '/', component: () => import('../layouts/MainLayout.vue'), children: [
    { path: '', name: 'home', component: () => import('../views/Home.vue') },
    { path: 'product/:id', name: 'product', component: () => import('../views/ProductDetail.vue') },
    { path: 'shop/:id', name: 'shop', component: () => import('../views/Shop.vue') },
    { path: 'cart', name: 'cart', component: () => import('../views/Cart.vue'), meta: { auth: true } },
    { path: 'orders', name: 'orders', component: () => import('../views/Orders.vue'), meta: { auth: true } },
    { path: 'profile', name: 'profile', component: () => import('../views/Profile.vue'), meta: { auth: true } },
    { path: 'recharge', name: 'recharge', component: () => import('../views/Recharge.vue'), meta: { auth: true, role: 'USER' } },
    { path: 'merchant/products', name: 'merchantProducts', component: () => import('../views/merchant/ProductManage.vue'), meta: { auth: true, role: 'MERCHANT' } },
    { path: 'merchant/publish', name: 'publish', component: () => import('../views/merchant/Publish.vue'), meta: { auth: true, role: 'MERCHANT' } },
    { path: 'merchant/orders', name: 'merchantOrders', component: () => import('../views/merchant/MerchantOrders.vue'), meta: { auth: true, role: 'MERCHANT' } },
    { path: 'admin', name: 'admin', component: () => import('../views/admin/Dashboard.vue'), meta: { auth: true, role: 'ADMIN' } },
  ]},
  { path: '/login', component: () => import('../views/Login.vue') },
  { path: '/register', component: () => import('../views/Register.vue') },
  { path: '/register-merchant', component: () => import('../views/RegisterMerchant.vue') },
]

const router = createRouter({ history: createWebHistory(), routes })

/**
 * 全局前置守卫：登录态 + 角色路由控制
 * 入口：任意 router.push / <router-link> / 地址栏变更
 */
router.beforeEach((to, from, next) => {
  const store = useUserStore()
  if (to.meta.auth && !store.isLogin()) return next('/login')
  if (to.meta.role && store.user?.role !== to.meta.role) return next('/')
  next()
})

export default router
