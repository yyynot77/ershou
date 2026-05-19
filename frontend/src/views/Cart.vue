<template>
  <div class="cart-page fade-in">
    <PageHeader title="购物车" :subtitle="`共 ${items.length} 件商品`" />

    <el-row :gutter="24" v-loading="loading">
      <el-col :xs="24" :lg="16">
        <div class="page-card cart-list" v-if="items.length">
          <div v-for="row in items" :key="row.cartItem.id" class="cart-item">
            <el-checkbox v-model="row.checked" @change="updateSelected" />
            <img :src="rowImage(row)" class="thumb" @click="goProduct(row)" />
            <div class="item-info" @click="goProduct(row)">
              <h4>{{ row.product?.name }}</h4>
              <p class="shop"><el-icon><Shop /></el-icon> {{ row.shopName }}</p>
              <p class="item-price">¥{{ row.product?.price }}</p>
            </div>
            <el-input-number
              v-model="row.cartItem.quantity"
              :min="1"
              :max="maxQty(row)"
              @change="v => updateQty(row.cartItem.id, v)"
            />
            <div class="subtotal">¥{{ lineTotal(row).toFixed(2) }}</div>
            <el-button :icon="Delete" circle text type="danger" @click="remove(row.cartItem.id)" />
          </div>
        </div>
        <el-empty v-else description="购物车是空的，去逛逛吧">
          <el-button type="primary" round @click="$router.push('/')">去首页</el-button>
        </el-empty>
      </el-col>

      <el-col :xs="24" :lg="8">
        <div class="page-card checkout-panel sticky">
          <h3>结算信息</h3>
          <el-form label-position="top">
            <el-form-item label="线下交易时间">
              <el-input v-model="checkoutForm.meetTime" placeholder="如：周六下午3点" />
            </el-form-item>
            <el-form-item label="线下交易地点">
              <el-input v-model="checkoutForm.meetPlace" placeholder="如：食堂门口" />
            </el-form-item>
            <el-form-item label="使用积分抵扣">
              <el-input-number v-model="checkoutForm.usePoints" :min="0" :step="100" style="width:100%" />
              <p class="hint">100 积分 = 1 元</p>
            </el-form-item>
          </el-form>
          <el-divider />
          <div class="summary-row"><span>已选商品</span><span>{{ selectedCount }} 件</span></div>
          <div class="summary-row total"><span>合计</span><span class="total-price">¥{{ selectedTotal.toFixed(2) }}</span></div>
          <el-button
            type="primary"
            size="large"
            round
            class="checkout-btn"
            :disabled="!selectedCount"
            :loading="checkingOut"
            @click="doCheckout"
          >
            一键下单（钱包支付）
          </el-button>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Shop, Delete } from '@element-plus/icons-vue'
import { getCart, updateCart, removeCart, checkout as checkoutApi } from '../api'
import { useCartStore } from '../stores/cart'
import { resolveImageUrl } from '../utils/image'
import PageHeader from '../components/PageHeader.vue'

const router = useRouter()
const cartStore = useCartStore()
const items = ref([])
const loading = ref(false)
const checkingOut = ref(false)
const checkoutForm = ref({ meetTime: '', meetPlace: '', usePoints: 0 })

/** 保证 min(1) <= max，避免 Element Plus InputNumber 抛错卡死 */
const maxQty = row => Math.max(1, Number(row.product?.stock) || 1)

const rowImage = row => resolveImageUrl(row.image)

const lineTotal = row => (row.product?.price || 0) * (row.cartItem?.quantity || 0)

const selectedItems = computed(() => items.value.filter(i => i.checked))
const selectedCount = computed(() => selectedItems.value.reduce((s, i) => s + i.cartItem.quantity, 0))
const selectedTotal = computed(() => selectedItems.value.reduce((s, i) => s + lineTotal(i), 0))

const updateSelected = () => {}

const load = async () => {
  loading.value = true
  try {
    const data = (await getCart()).data || []
    items.value = data.map(i => {
      const stock = Math.max(1, Number(i.product?.stock) || 1)
      const qty = Math.min(Math.max(1, i.cartItem?.quantity || 1), stock)
      return {
        ...i,
        checked: true,
        cartItem: { ...i.cartItem, quantity: qty }
      }
    })
    await cartStore.refresh()
  } finally { loading.value = false }
}

const updateQty = async (id, q) => {
  try {
    await updateCart(id, q)
    await load()
  } catch {
    await load()
  }
}

const remove = async id => {
  await removeCart(id)
  await load()
}

const goProduct = row => {
  if (row.product?.id) router.push(`/product/${row.product.id}`)
}

const doCheckout = async () => {
  const ids = selectedItems.value.map(s => s.cartItem.id)
  if (!ids.length) return
  checkingOut.value = true
  try {
    await checkoutApi({ cartItemIds: ids, ...checkoutForm.value })
    ElMessage.success('下单成功！')
    await cartStore.refresh()
    router.push('/orders')
  } finally { checkingOut.value = false }
}

onMounted(load)
</script>

<style scoped>
.cart-list { padding: 8px 0; }
.cart-item {
  display: flex; align-items: center; gap: 16px;
  padding: 20px 24px; border-bottom: 1px solid var(--border);
  transition: background 0.2s;
}
.cart-item:hover { background: #f8fafc; }
.cart-item:last-child { border-bottom: none; }
.thumb {
  width: 88px; height: 88px; object-fit: cover;
  border-radius: var(--radius); cursor: pointer;
}
.item-info { flex: 1; cursor: pointer; min-width: 0; }
.item-info h4 { font-size: 15px; margin-bottom: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.shop { font-size: 12px; color: var(--text-muted); display: flex; align-items: center; gap: 4px; }
.item-price { color: var(--danger); font-weight: 600; margin-top: 4px; }
.subtotal { font-weight: 700; color: var(--text); min-width: 80px; text-align: right; }
.checkout-panel { padding: 24px; }
.checkout-panel h3 { margin-bottom: 20px; font-size: 18px; }
.sticky { position: sticky; top: 88px; }
.hint { font-size: 12px; color: var(--text-muted); margin-top: 4px; }
.summary-row { display: flex; justify-content: space-between; padding: 8px 0; color: var(--text-muted); }
.summary-row.total { font-size: 16px; color: var(--text); margin-top: 8px; }
.total-price { font-size: 26px; font-weight: 800; color: var(--danger); }
.checkout-btn { width: 100%; height: 48px; margin-top: 20px; font-size: 16px; }
</style>
