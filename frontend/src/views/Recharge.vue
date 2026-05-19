<template>
  <div class="recharge-page fade-in">
    <PageHeader title="钱包充值" subtitle="仅可向注册时绑定的银行卡充值（模拟）" />

    <el-row :gutter="24">
      <el-col :xs="24" :md="14">
        <div class="page-card bank-card">
          <h3>银行卡充值</h3>
          <p class="hint">演示环境：无需真实支付，填写金额后点击确认即可到账</p>
          <el-alert v-if="!hasBank" type="warning" :closable="false" show-icon style="margin-bottom:16px">
            请先在
            <el-link type="primary" @click="$router.push('/profile')">个人中心</el-link>
            填写并保存 16 位银行卡号后再充值
          </el-alert>
          <el-form label-width="100px" @submit.prevent="submit">
            <el-form-item label="银行卡号">
              <el-input :model-value="maskedCard" disabled placeholder="未绑定银行卡" />
            </el-form-item>
            <el-form-item label="持卡人">
              <el-input :model-value="holder" disabled />
            </el-form-item>
            <el-form-item label="充值金额">
              <el-input-number v-model="amount" :min="0.01" :max="99999" :precision="2" :step="10" style="width:100%" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" round :loading="loading" :disabled="!hasBank" @click="submit">确认充值</el-button>
              <el-button round @click="$router.push('/profile')">返回个人中心</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
      <el-col :xs="24" :md="10">
        <div class="page-card balance-card">
          <span class="label">当前余额</span>
          <span class="balance">¥{{ balance }}</span>
          <el-divider />
          <p class="tip">充值记录可在个人中心「钱包流水」中查看</p>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProfile, selfRecharge } from '../api'
import PageHeader from '../components/PageHeader.vue'

const router = useRouter()
const bankAccount = ref('')
const holder = ref('')
const amount = ref(100)
const balance = ref('0.00')
const loading = ref(false)

const hasBank = computed(() => !!bankAccount.value && bankAccount.value.length >= 16)

const maskedCard = computed(() => {
  const a = bankAccount.value
  if (!a || a.length < 8) return ''
  return a.slice(0, 4) + ' **** **** ' + a.slice(-4)
})

const loadBalance = async () => {
  const res = await getProfile()
  balance.value = res.data.wallet?.balance ?? '0.00'
  holder.value = res.data.user?.realName || ''
  bankAccount.value = res.data.user?.bankAccount || ''
}

const submit = async () => {
  if (!hasBank.value) {
    ElMessage.warning('请先在个人中心绑定银行卡')
    return router.push('/profile')
  }
  if (!amount.value || amount.value <= 0) {
    ElMessage.warning('请输入有效充值金额')
    return
  }
  loading.value = true
  try {
    const res = await selfRecharge(amount.value)
    balance.value = res.data.wallet?.balance ?? balance.value
    ElMessage.success(`已向绑定卡 ${maskedCard.value} 充值成功，当前余额 ¥${balance.value}`)
  } finally {
    loading.value = false
  }
}

onMounted(loadBalance)
</script>

<style scoped>
.bank-card h3 { margin-bottom: 8px; }
.hint { color: var(--text-muted); font-size: 13px; margin-bottom: 20px; }
.balance-card { text-align: center; padding: 32px 24px; }
.balance-card .label { display: block; color: var(--text-muted); margin-bottom: 8px; }
.balance-card .balance { font-size: 36px; font-weight: 800; color: var(--primary); }
.tip { font-size: 13px; color: var(--text-muted); }
</style>
