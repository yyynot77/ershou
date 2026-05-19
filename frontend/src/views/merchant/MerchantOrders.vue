<template>
  <div class="fade-in">
    <PageHeader title="商家订单" subtitle="处理买家订单与退货申请" />
  <el-card class="page-card" shadow="never">
    <el-collapse v-for="o in orders" :key="o.order.id">
      <el-collapse-item :title="`${o.order.orderNo} | ${o.order.status}`">
        <el-table :data="o.items" size="small">
          <el-table-column prop="productName" label="商品" />
          <el-table-column prop="subtotal" label="小计" />
        </el-table>
        <el-button v-if="o.order.status==='PAID'" type="primary" @click="ship(o.order.id)">确认发货</el-button>
        <el-button v-if="o.order.status==='RETURN_REQUEST'" type="warning" @click="approve(o.order.id, true)">同意退货</el-button>
        <el-button v-if="o.order.status==='RETURN_REQUEST'" @click="approve(o.order.id, false)">拒绝退货</el-button>
        <el-button v-if="o.order.status==='COMPLETED'" @click="openReview(o)">评价买家</el-button>
      </el-collapse-item>
    </el-collapse>
    <el-dialog v-model="visible" title="评价买家" width="360px">
      <el-rate v-model="rating" />
      <el-input v-model="content" type="textarea" style="margin-top:12px" />
      <template #footer><el-button type="primary" @click="submitReview">提交</el-button></template>
    </el-dialog>
  </el-card>
  </div>
</template>

<script setup>
/**
 * 页面：商家订单（merchant/MerchantOrders.vue）
 * merchantOrders() 列表；shipOrder 发货；approveReturn 审核退货
 */
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { merchantOrders, shipOrder, approveReturn, reviewBuyer as reviewBuyerApi } from '../../api'
import PageHeader from '../../components/PageHeader.vue'

const orders = ref([])
const visible = ref(false)
const rating = ref(5)
const content = ref('')
const cur = ref({ orderId: null, buyerId: null })

const load = async () => { orders.value = (await merchantOrders()).data }
const ship = async id => { await shipOrder(id); ElMessage.success('已发货，等待买家确认收货'); load() }
const approve = async (id, pass) => { await approveReturn(id, pass); ElMessage.success(pass ? '已同意退货并退款' : '已拒绝退货'); load() }

const openReview = o => {
  cur.value = { orderId: o.order.id, buyerId: o.order.buyerId }
  visible.value = true
}

const submitReview = async () => {
  await reviewBuyerApi({ orderId: cur.value.orderId, buyerId: cur.value.buyerId, rating: rating.value, content: content.value })
  ElMessage.success('评价成功')
  visible.value = false
  load()
}

onMounted(load)
</script>
