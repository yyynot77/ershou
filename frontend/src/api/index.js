import request from './request'

export const getCaptcha = () => request.get('/api/auth/captcha')
export const login = data => request.post('/api/auth/login', data)
export const registerUser = data => request.post('/api/auth/register', data)
export const registerMerchant = data => request.post('/api/auth/register/merchant', data)

export const searchProducts = params => request.get('/api/products/search', { params })
export const getProduct = id => request.get(`/api/products/${id}`)
export const publishProduct = data => request.post('/api/products', data)
export const updateProduct = (id, data) => request.put(`/api/products/${id}`, data)
export const offShelfProduct = id => request.put(`/api/products/${id}/off-shelf`)
export const myProducts = status => request.get('/api/products/merchant/my', { params: { status } })

export const getCategories = () => request.get('/api/categories')
export const getBanners = () => request.get('/api/banners')
export const getShop = id => request.get(`/api/shops/${id}`)
export const uploadFile = file => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/api/files/upload', fd)
}

export const getCart = () => request.get('/api/cart')
export const addCart = (productId, quantity = 1) => request.post('/api/cart', null, { params: { productId, quantity } })
export const updateCart = (id, quantity) => request.put(`/api/cart/${id}`, null, { params: { quantity } })
export const removeCart = id => request.delete(`/api/cart/${id}`)

export const checkout = data => request.post('/api/orders/checkout', data)
export const myOrders = () => request.get('/api/orders/my')
export const merchantOrders = () => request.get('/api/orders/merchant')
export const shipOrder = id => request.post(`/api/orders/${id}/ship`)
export const confirmReceive = id => request.post(`/api/orders/${id}/receive`)
export const requestReturn = id => request.post(`/api/orders/${id}/return`)
export const approveReturn = (id, pass) => request.post(`/api/orders/${id}/return/approve`, null, { params: { pass } })

export const getProfile = () => request.get('/api/user/profile')
export const updateProfile = data => request.put('/api/user/profile', data)
export const getTransactions = () => request.get('/api/user/wallet/transactions')
export const selfRecharge = amount => request.post('/api/user/wallet/recharge', null, { params: { amount } })

export const reviewProduct = params => request.post('/api/reviews/product', null, { params })
export const reviewMerchant = params => request.post('/api/reviews/merchant', null, { params })
export const reviewBuyer = params => request.post('/api/reviews/buyer', null, { params })

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
export const blacklistBuyer = params => request.post('/api/admin/blacklist', null, { params })
export const saveBanner = data => request.post('/api/admin/banners', data)
