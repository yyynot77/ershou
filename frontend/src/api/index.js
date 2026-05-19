/**
 * ============================================================================
 * 模块：后端 REST API 前端封装层
 * ============================================================================
 *
 * 说明：
 * - 本文件只做「URL + 方法」映射，不包含业务逻辑
 * - 每个 export 函数被具体页面/组件 import 后调用
 * - 实际 HTTP 行为见 request.js 拦截器
 *
 * 命名约定：
 * - getXxx / searchXxx → GET
 * - 动词原形 → POST/PUT/DELETE
 *
 * TODO：可按业务域拆分为 auth.js / product.js / order.js 降低单文件体积
 * ============================================================================
 */
import request from './request'

/* ---------- 认证：Login.vue / Register.vue / RegisterMerchant.vue ---------- */
/** 获取图形验证码 → GET /api/auth/captcha → AuthController.captcha */
export const getCaptcha = () => request.get('/api/auth/captcha')
/** 登录 → POST /api/auth/login → AuthService.login → 返回 token/user */
export const login = data => request.post('/api/auth/login', data)
/** 普通用户注册 → POST /api/auth/register */
export const registerUser = data => request.post('/api/auth/register', data)
/** 商家注册 → POST /api/auth/register/merchant */
export const registerMerchant = data => request.post('/api/auth/register/merchant', data)

/* ---------- 商品：Home / ProductDetail / Shop / merchant/* ---------- */
/** 首页搜索分页 → GET /api/products/search → ProductService.search */
export const searchProducts = params => request.get('/api/products/search', { params })
/** 商品详情 → GET /api/products/{id} → ProductService.detail */
export const getProduct = id => request.get(`/api/products/${id}`)
/** 商家发布 → POST /api/products → ProductService.publish */
export const publishProduct = data => request.post('/api/products', data)
export const updateProduct = (id, data) => request.put(`/api/products/${id}`, data)
export const offShelfProduct = id => request.put(`/api/products/${id}/off-shelf`)
export const myProducts = status => request.get('/api/products/merchant/my', { params: { status } })

export const getCategories = () => request.get('/api/categories')
export const getBanners = () => request.get('/api/banners')
export const getShop = id => request.get(`/api/shops/${id}`)
/** 图片上传 → POST /api/files/upload → 返回 /uploads/xxx 路径供商品/证照使用 */
export const uploadFile = file => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/api/files/upload', fd)
}

/* ---------- 购物车：Cart.vue / ProductCard / ProductDetail / cart store ---------- */
export const getCart = () => request.get('/api/cart')
export const addCart = (productId, quantity = 1) => request.post('/api/cart', null, { params: { productId, quantity } })
export const updateCart = (id, quantity) => request.put(`/api/cart/${id}`, null, { params: { quantity } })
export const removeCart = id => request.delete(`/api/cart/${id}`)

/* ---------- 订单：Cart.vue checkout / Orders.vue / MerchantOrders.vue ---------- */
export const checkout = data => request.post('/api/orders/checkout', data)
export const myOrders = () => request.get('/api/orders/my')
export const merchantOrders = () => request.get('/api/orders/merchant')
export const shipOrder = id => request.post(`/api/orders/${id}/ship`)
export const confirmReceive = id => request.post(`/api/orders/${id}/receive`)
export const requestReturn = id => request.post(`/api/orders/${id}/return`)
export const approveReturn = (id, pass) => request.post(`/api/orders/${id}/return/approve`, null, { params: { pass } })

/* ---------- 用户中心：Profile.vue / Recharge.vue ---------- */
export const getProfile = () => request.get('/api/user/profile')
export const updateProfile = data => request.put('/api/user/profile', data)
export const getTransactions = () => request.get('/api/user/wallet/transactions')
export const selfRecharge = amount => request.post('/api/user/wallet/recharge', null, { params: { amount } })

export const reviewProduct = params => request.post('/api/reviews/product', null, { params })
export const reviewMerchant = params => request.post('/api/reviews/merchant', null, { params })
export const reviewBuyer = params => request.post('/api/reviews/buyer', null, { params })

/* ---------- 管理后台：admin/Dashboard.vue ---------- */
export const pendingUsers = () => request.get('/api/admin/users/pending')
export const auditUser = (id, pass) => request.post(`/api/admin/users/${id}/audit`, null, { params: { pass } })
export const allUsers = () => request.get('/api/admin/users')
export const updateUser = (id, data) => request.put(`/api/admin/users/${id}`, data)
export const deleteUser = id => request.delete(`/api/admin/users/${id}`)
export const pendingProducts = () => request.get('/api/admin/products/pending')
export const auditProduct = (id, pass) => request.post(`/api/admin/products/${id}/audit`, null, { params: { pass } })
export const setMerchantLevel = (id, level) => request.post(`/api/admin/merchants/${id}/level`, null, { params: { level } })
export const rechargeWallet = (userId, amount) => request.post('/api/admin/wallet/recharge', null, { params: { userId, amount } })
export const banMerchant = params => request.post(`/api/admin/merchants/${params.id}/ban`, null, { params })
export const listMerchantBans = () => request.get('/api/admin/merchant-bans')
export const liftMerchantBan = id => request.delete(`/api/admin/merchant-bans/${id}`)
export const blacklistBuyer = params => request.post('/api/admin/blacklist', null, { params })
export const listBuyerBlacklist = () => request.get('/api/admin/blacklist')
export const removeBuyerBlacklist = id => request.delete(`/api/admin/blacklist/${id}`)
export const saveBanner = data => request.post('/api/admin/banners', data)
