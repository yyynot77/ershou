<template>
  <div class="orders-page fade-in">
    <PageHeader title="我的订单" subtitle="查看交易进度，确认收货或申请退货" />

    <el-tabs v-model="filterStatus" @tab-change="filterOrders">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="待收货" name="active" />
      <el-tab-pane label="已完成" name="COMPLETED" />
      <el-tab-pane label="退货中" name="RETURN_REQUEST" />
    </el-tabs>

    <div v-loading="loading">
      <div v-for="o in filteredOrders" :key="o.order.id" class="order-card page-card">
        <div class="order-head">
          <span class="order-no">{{ o.order.orderNo }}</span>
          <el-tag :type="statusType(o.order.status)" effect="dark" round>{{ statusText(o.order.status) }}</el-tag>
        </div>
        <div class="order-meta">
          <el-icon><Clock /></el-icon> {{ o.order.createTime }}
          <span v-if="o.order.meetPlace"> · <el-icon><Location /></el-icon> {{ o.order.meetPlace }}</span>
        </div>
        <div class="order-items">
          <div v-for="item in o.items" :key="item.id" class="item-row">
            <span class="item-name">{{ item.productName }}</span>
            <span>x{{ item.quantity }}</span>
            <span class="item-price">¥{{ item.subtotal }}</span>
          </div>
        </div>
        <div class="order-foot">
          <span class="pay-amount">实付 <b>¥{{ o.order.payAmount }}</b></span>
          <div class="btns">
            <el-button v-if="['PAID','SHIPPED'].includes(o.order.status)" type="primary" round @click="confirm(o.order.id)">
              确认收货
            </el-button>
            <el-button v-if="['RECEIVED','COMPLETED'].includes(o.order.status)" round @click="returnReq(o.order.id)">
              申请退货
            </el-button>
            <el-button v-if="o.order.status==='COMPLETED'" round type="success" plain @click="openReview(o)">
              评价
            </el-button>
          </div>
        </div>
        <el-steps v-if="['PAID','SHIPPED','RECEIVED','COMPLETED'].includes(o.order.status)" :active="stepActive(o.order.status)" simple class="steps">
          <el-step title="已付款" />
          <el-step title="待收货" />
          <el-step title="已收货" />
          <el-step title="完成" />
        </el-steps>
      </div>
      <el-empty v-if="!filteredOrders.length && !loading" description="暂无订单" />
    </div>

    <el-dialog v-model="reviewVisible" title="发表评价" width="440px" destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="商品评分">
          <el-rate v-model="review.rating" show-text />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="review.content" type="textarea" :rows="3" placeholder="分享你的使用感受" />
        </el-form-item>
        <el-form-item label="商家服务态度">
          <el-rate v-model="review.merchantRating" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" round @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 页面：买家订单（views/Orders.vue）路由 /orders
 * 加载 myOrders()；确认收货 confirmReceive；申请退货 requestReturn
 * API → OrderController → OrderService
 */
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock, Location } from '@element-plus/icons-vue'
import { myOrders, confirmReceive, requestReturn, reviewProduct, reviewMerchant } from '../api'
import PageHeader from '../components/PageHeader.vue'

const orders = ref([])
const loading = ref(false)
const filterStatus = ref('all')
const reviewVisible = ref(false)
const review = ref({ orderId: null, productId: null, merchantId: null, rating: 5, content: '', merchantRating: 5 })

const statusText = s => ({
  PAID: '已付款', SHIPPED: '已发货', RECEIVED: '已收货', COMPLETED: '已完成',
  RETURN_REQUEST: '退货申请中', RETURNED: '已退货', CANCELLED: '已取消'
}[s] || s)

const statusType = s => ({
  PAID: 'warning', SHIPPED: 'warning', RECEIVED: 'success', COMPLETED: 'info',
  RETURN_REQUEST: 'danger', RETURNED: 'info'
}[s] || '')

const stepActive = s => ({ PAID: 1, SHIPPED: 2, RECEIVED: 3, COMPLETED: 4 }[s] || 0)

const filteredOrders = computed(() => {
  if (filterStatus.value === 'all') return orders.value
  if (filterStatus.value === 'active') return orders.value.filter(o => ['PAID', 'SHIPPED'].includes(o.order.status))
  return orders.value.filter(o => o.order.status === filterStatus.value)
})

const filterOrders = () => {}

const load = async () => {
  loading.value = true
  try { orders.value = (await myOrders()).data } finally { loading.value = false }
}

const confirm = async id => { await confirmReceive(id); ElMessage.success('已确认收货'); load() }
const returnReq = async id => { await requestReturn(id); ElMessage.success('退货申请已提交'); load() }

const openReview = o => {
  review.value = {
    orderId: o.order.id, productId: o.items[0]?.productId, merchantId: o.items[0]?.merchantId,
    rating: 5, content: '', merchantRating: 5
  }
  reviewVisible.value = true
}

const submitReview = async () => {
  await reviewProduct({ orderId: review.value.orderId, productId: review.value.productId, rating: review.value.rating, content: review.value.content })
  await reviewMerchant({ orderId: review.value.orderId, merchantId: review.value.merchantId, rating: review.value.merchantRating, content: '' })
  ElMessage.success('评价成功，感谢反馈！')
  reviewVisible.value = false
}

onMounted(load)
</script>

<style scoped>
.orders-page :deep(.el-tabs) { margin-bottom: 20px; }
.order-card { padding: 20px 24px; margin-bottom: 16px; }
.order-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.order-no { font-weight: 600; font-family: monospace; }
.order-meta { font-size: 13px; color: var(--text-muted); display: flex; align-items: center; gap: 4px; margin-bottom: 16px; }
.order-items { background: #f8fafc; border-radius: var(--radius); padding: 12px 16px; margin-bottom: 16px; }
.item-row { display: flex; gap: 12px; padding: 6px 0; font-size: 14px; }
.item-name { flex: 1; }
.item-price { color: var(--danger); font-weight: 600; }
.order-foot { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.pay-amount b { font-size: 20px; color: var(--danger); }
.btns { display: flex; gap: 8px; flex-wrap: wrap; }
.steps { margin-top: 16px; }
</style>
