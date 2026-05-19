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

router.beforeEach((to, from, next) => {
  const store = useUserStore()
  if (to.meta.auth && !store.isLogin()) return next('/login')
  if (to.meta.role && store.user?.role !== to.meta.role) return next('/')
  next()
})

export default router
