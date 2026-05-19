<template>
  <div v-if="detail" class="detail fade-in" v-loading="loading">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>{{ p.name }}</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="detail-main page-card">
      <el-row :gutter="32">
        <el-col :xs="24" :md="11">
          <div class="gallery">
            <img :src="currentImage" class="main-img" @error="onMainImgError" />
            <div class="thumbs" v-if="images.length > 1">
              <img
                v-for="(img, i) in images"
                :key="img.id || i"
                :src="resolveImageUrl(img.imageUrl)"
                :class="{ active: currentIndex === i }"
                @click="currentIndex = i"
              />
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :md="13">
          <div class="info">
            <el-tag v-if="p.conditionLevel" type="success" effect="dark" round>{{ p.conditionLevel }}</el-tag>
            <el-tag v-if="p.allowBargain" type="warning" effect="plain" round style="margin-left:8px">可议价</el-tag>
            <h1>{{ p.name }}</h1>
            <p class="shop-link" @click="$router.push(`/shop/${p.merchantId}`)">
              <el-icon><Shop /></el-icon> {{ shopName }} <el-icon><ArrowRight /></el-icon>
            </p>
            <div class="price-block">
              <span class="price">¥{{ p.price }}</span>
              <span v-if="p.originalPrice" class="orig">¥{{ p.originalPrice }}</span>
              <el-rate :model-value="Number(p.avgRating)||5" disabled show-score />
            </div>
            <el-row :gutter="12" class="stats">
              <el-col :span="8"><div class="stat-item"><b>{{ p.stock }}</b><span>库存</span></div></el-col>
              <el-col :span="8"><div class="stat-item"><b>{{ p.soldCount||0 }}</b><span>已售</span></div></el-col>
              <el-col :span="8"><div class="stat-item"><b>{{ p.sizeInfo||'-' }}</b><span>尺寸</span></div></el-col>
            </el-row>
            <el-divider />
            <p class="desc-title">商品描述</p>
            <p class="desc">{{ p.description || '卖家很懒，暂无描述' }}</p>
            <div class="qty-row" v-if="canBuy">
              <span>数量</span>
              <el-input-number v-model="qty" :min="1" :max="p.stock" size="large" />
            </div>
            <el-alert v-else type="warning" :closable="false" show-icon title="该商品暂不可购买" />
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="action-bar" v-if="canBuy">
      <div class="action-inner">
        <div class="action-price">¥{{ (Number(p.price) * qty).toFixed(2) }}</div>
        <div class="action-btns">
          <el-button size="large" round :icon="ShoppingCart" @click="addToCart">加入购物车</el-button>
          <el-button size="large" round type="primary" @click="buyNow">立即购买</el-button>
          <el-button size="large" circle :icon="Share" @click="copyLink" title="分享" />
        </div>
      </div>
    </div>

    <el-card class="reviews-card page-card" shadow="never">
      <template #header>
        <span>商品评价 ({{ reviews.length }})</span>
      </template>
      <div v-for="r in reviews" :key="r.id" class="review-item">
        <el-rate :model-value="r.rating" disabled size="small" />
        <p>{{ r.content || '用户未填写评价' }}</p>
        <span class="review-time">{{ r.createTime }}</span>
      </div>
      <el-empty v-if="!reviews.length" description="暂无评价，购买后来抢沙发吧" />
    </el-card>

    <el-dialog v-model="buyVisible" title="确认订单" width="500px" class="buy-dialog">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom:16px">
        支付将从钱包扣款，请与卖家约定线下交易时间与地点
      </el-alert>
      <el-form label-width="100px" size="large">
        <el-form-item label="商品合计">
          <span class="dialog-price">¥{{ (Number(p.price) * qty).toFixed(2) }}</span>
        </el-form-item>
        <el-form-item label="线下时间">
          <el-input v-model="checkout.meetTime" placeholder="如：周六下午3点" />
        </el-form-item>
        <el-form-item label="线下地点">
          <el-input v-model="checkout.meetPlace" placeholder="如：图书馆东门" />
        </el-form-item>
        <el-form-item label="使用积分">
          <el-input-number v-model="checkout.usePoints" :min="0" :step="100" />
          <span class="hint">100积分=1元</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="buyVisible=false">取消</el-button>
        <el-button type="primary" @click="doCheckout" :loading="checkingOut">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
  <el-empty v-else-if="!loading" description="商品不存在或已下架" />
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Shop, ArrowRight, ShoppingCart, Share } from '@element-plus/icons-vue'
import { getProduct, addCart, checkout as checkoutApi } from '../api'
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'
import { resolveImageUrl, PLACEHOLDER } from '../utils/image'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const cartStore = useCartStore()
const detail = ref(null)
const loading = ref(true)
const qty = ref(1)
const currentIndex = ref(0)
const mainImgFailed = ref(false)
const buyVisible = ref(false)
const checkingOut = ref(false)
const checkout = ref({ meetTime: '', meetPlace: '', usePoints: 0 })

/** 兼容旧版接口：product 字段里又套了一层 enrich */
function normalizeDetail(data) {
  if (!data) return null
  if (data.product?.product) {
    return {
      product: data.product.product,
      images: data.product.images || [],
      shopName: data.product.shopName || '',
      reviews: data.reviews || []
    }
  }
  return {
    product: data.product,
    images: data.images || [],
    shopName: data.shopName || '',
    reviews: data.reviews || []
  }
}

const p = computed(() => detail.value?.product || {})
const images = computed(() => detail.value?.images || [])
const shopName = computed(() => detail.value?.shopName || '')
const reviews = computed(() => detail.value?.reviews || [])
const productId = computed(() => p.value?.id)
const canBuy = computed(() => p.value?.status === 'PUBLISHED' && (p.value?.stock || 0) > 0)

const currentImage = computed(() => {
  if (mainImgFailed.value) return PLACEHOLDER
  const img = images.value[currentIndex.value]
  return resolveImageUrl(img?.imageUrl)
})

const onMainImgError = () => { mainImgFailed.value = true }

const load = async () => {
  const id = route.params.id
  if (!id || id === 'undefined') {
    detail.value = null
    loading.value = false
    return
  }
  loading.value = true
  mainImgFailed.value = false
  try {
    const res = await getProduct(id)
    detail.value = normalizeDetail(res.data)
    currentIndex.value = 0
    qty.value = 1
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

const addToCart = async () => {
  if (!productId.value) {
    ElMessage.error('商品信息异常，请刷新页面')
    return
  }
  if (!store.isLogin()) return router.push('/login')
  await addCart(productId.value, qty.value)
  await cartStore.refresh()
  ElMessage.success('已加入购物车')
}

const buyNow = () => {
  if (!productId.value) return ElMessage.error('商品信息异常')
  if (!store.isLogin()) return router.push('/login')
  buyVisible.value = true
}

const doCheckout = async () => {
  checkingOut.value = true
  try {
    await checkoutApi({ productId: productId.value, quantity: qty.value, ...checkout.value })
    await cartStore.refresh()
    ElMessage.success('下单成功！')
    buyVisible.value = false
    router.push('/orders')
  } finally { checkingOut.value = false }
}

const copyLink = () => {
  navigator.clipboard?.writeText(window.location.href)
  ElMessage.success('链接已复制')
}

watch(() => route.params.id, load)
onMounted(load)
</script>

<style scoped>
.breadcrumb { margin-bottom: 20px; }
.detail-main { padding: 28px; margin-bottom: 100px; }
.gallery .main-img {
  width: 100%; aspect-ratio: 1; object-fit: contain;
  background: #f8fafc; border-radius: var(--radius);
  border: 1px solid var(--border);
}
.thumbs { display: flex; gap: 10px; margin-top: 12px; flex-wrap: wrap; }
.thumbs img {
  width: 72px; height: 72px; object-fit: cover; border-radius: 8px;
  cursor: pointer; border: 2px solid transparent; opacity: 0.7;
  transition: all 0.2s;
}
.thumbs img.active, .thumbs img:hover { border-color: var(--primary); opacity: 1; }
.info h1 { font-size: 26px; margin: 12px 0; line-height: 1.3; }
.shop-link {
  display: inline-flex; align-items: center; gap: 4px;
  color: var(--primary); cursor: pointer; margin-bottom: 16px;
}
.shop-link:hover { text-decoration: underline; }
.price-block { margin-bottom: 20px; }
.price { font-size: 32px; color: var(--danger); font-weight: 800; }
.orig { font-size: 16px; color: #94a3b8; text-decoration: line-through; margin: 0 12px; }
.stats { margin-bottom: 8px; }
.stat-item {
  text-align: center; padding: 12px; background: #f8fafc;
  border-radius: var(--radius);
}
.stat-item b { display: block; font-size: 18px; color: var(--text); }
.stat-item span { font-size: 12px; color: var(--text-muted); }
.desc-title { font-weight: 600; margin-bottom: 8px; }
.desc { color: var(--text-muted); line-height: 1.8; }
.qty-row { display: flex; align-items: center; gap: 16px; margin-top: 20px; }
.action-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(10px);
  border-top: 1px solid var(--border);
  padding: 12px 20px;
  z-index: 50;
  box-shadow: 0 -4px 20px rgba(0,0,0,0.06);
}
.action-inner {
  max-width: 1280px; margin: 0 auto;
  display: flex; justify-content: space-between; align-items: center;
}
.action-price { font-size: 24px; font-weight: 800; color: var(--danger); }
.action-btns { display: flex; gap: 12px; }
.reviews-card { margin-top: 24px; }
.review-item {
  padding: 16px 0; border-bottom: 1px solid var(--border);
}
.review-item p { margin: 8px 0; color: var(--text); }
.review-time { font-size: 12px; color: var(--text-muted); }
.dialog-price { font-size: 22px; color: var(--danger); font-weight: 700; }
.hint { margin-left: 12px; font-size: 12px; color: var(--text-muted); }
</style>
