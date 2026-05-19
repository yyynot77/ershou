<template>
  <div class="product-card fade-in" @click="goDetail">
    <div class="img-wrap">
      <img :src="imageUrl" :alt="item.product.name" @error="onImgError" />
      <span v-if="item.product.conditionLevel" class="tag">{{ item.product.conditionLevel }}</span>
      <div class="overlay">
        <el-button circle :icon="ShoppingCart" type="primary" @click.stop="quickAdd" title="加入购物车" />
        <el-button circle :icon="View" @click.stop="goDetail" title="查看详情" />
      </div>
    </div>
    <div class="body">
      <h3 class="name" :title="item.product.name">{{ item.product.name }}</h3>
      <p class="shop" @click.stop="goShop">
        <el-icon><Shop /></el-icon> {{ item.shopName }}
      </p>
      <div class="meta">
        <span class="price">¥{{ item.product.price }}</span>
        <span v-if="item.product.originalPrice" class="orig">¥{{ item.product.originalPrice }}</span>
        <span class="sales">已售 {{ item.product.soldCount || 0 }}</span>
      </div>
      <div class="footer-row">
        <el-rate :model-value="Number(item.product.avgRating) || 5" disabled size="small" />
        <el-button link type="primary" class="detail-link" @click.stop="goDetail">查看详情</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * ============================================================================
 * 组件：首页/店铺商品卡片（components/ProductCard.vue）
 * ============================================================================
 *
 * 点击卡片 → goDetail() → router.push(`/product/${id}`)
 * 点击店铺名 → goShop() → /shop/:merchantId
 * 悬浮加购 → quickAdd() → addCart → cartStore.refresh()
 *
 * props.item 结构来自 ProductService.enrich：{ product, images, shopName }
 * ============================================================================
 */
import { computed, ref } from 'vue'
import { resolveImageUrl, PLACEHOLDER } from '../utils/image'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, View, Shop } from '@element-plus/icons-vue'
import { addCart } from '../api'
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'

const props = defineProps({ item: { type: Object, required: true } })
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const imgFailed = ref(false)

const imageUrl = computed(() => {
  if (imgFailed.value) return PLACEHOLDER
  return resolveImageUrl(props.item.images?.[0]?.imageUrl)
})

const onImgError = () => { imgFailed.value = true }

const goDetail = () => router.push(`/product/${props.item.product.id}`)
const goShop = () => router.push(`/shop/${props.item.product.merchantId}`)

const quickAdd = async () => {
  if (!userStore.isLogin()) {
    ElMessage.warning('请先登录')
    return router.push('/login')
  }
  await addCart(props.item.product.id, 1)
  await cartStore.refresh()
  ElMessage.success({ message: '已加入购物车', duration: 1500 })
}
</script>

<style scoped>
.product-card {
  background: var(--card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--border);
  cursor: pointer;
  transition: transform 0.25s, box-shadow 0.25s;
}
.product-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-hover);
}
.img-wrap {
  position: relative;
  aspect-ratio: 4/3;
  overflow: hidden;
  background: #f1f5f9;
}
.img-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s;
}
.product-card:hover .img-wrap img { transform: scale(1.06); }
.tag {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(13, 148, 136, 0.9);
  color: #fff;
  font-size: 11px;
  padding: 4px 10px;
  border-radius: 20px;
}
.overlay {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  opacity: 0;
  transition: opacity 0.25s;
}
.product-card:hover .overlay { opacity: 1; }
.body { padding: 14px 16px 16px; }
.name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.shop {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.shop:hover { color: var(--primary); }
.meta { display: flex; align-items: baseline; gap: 8px; margin-bottom: 6px; }
.price { color: var(--danger); font-size: 20px; font-weight: 700; }
.orig { font-size: 12px; color: #94a3b8; text-decoration: line-through; }
.sales { margin-left: auto; font-size: 11px; color: var(--text-muted); }
.footer-row { display: flex; align-items: center; justify-content: space-between; margin-top: 4px; }
.footer-row :deep(.el-rate) { pointer-events: none; }
.detail-link { font-size: 13px; padding: 0; }
</style>
