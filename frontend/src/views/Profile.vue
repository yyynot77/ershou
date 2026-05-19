<template>
  <div class="profile-page fade-in" v-loading="!profile">
    <PageHeader title="个人中心" subtitle="管理资料、钱包与积分" />

    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="24" :sm="8">
        <div class="stat-card wallet" :class="{ clickable: isUser }" @click="goRecharge">
          <el-icon :size="32"><Wallet /></el-icon>
          <div>
            <span class="label">{{ isUser ? '钱包余额 · 点击充值' : '钱包余额' }}</span>
            <span class="value">¥{{ profile?.wallet?.balance ?? '0.00' }}</span>
          </div>
          <el-button v-if="isUser" type="primary" plain round size="small" class="recharge-btn" @click.stop="goRecharge">充值</el-button>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="stat-card points">
          <el-icon :size="32"><Medal /></el-icon>
          <div>
            <span class="label">我的积分</span>
            <span class="value">{{ profile?.points?.points ?? 0 }}</span>
          </div>
          <p class="tip">100积分可抵1元</p>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="stat-card role">
          <el-icon :size="32"><User /></el-icon>
          <div>
            <span class="label">账号角色</span>
            <span class="value">{{ roleLabel }}</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top:20px">
      <el-col :xs="24" :md="12">
        <div class="page-card section">
          <h3>基本信息</h3>
          <el-form v-if="profile" :model="profile.user" label-width="80px" label-position="left">
            <el-form-item label="姓名"><el-input v-model="profile.user.realName" /></el-form-item>
            <el-form-item label="手机"><el-input v-model="profile.user.phone" /></el-form-item>
            <el-form-item label="邮箱"><el-input v-model="profile.user.email" /></el-form-item>
            <el-form-item label="城市"><el-input v-model="profile.user.city" /></el-form-item>
            <el-form-item label="银行账号"><el-input v-model="profile.user.bankAccount" maxlength="16" /></el-form-item>
            <el-button type="primary" round @click="save" :loading="saving">保存修改</el-button>
          </el-form>
        </div>
      </el-col>
      <el-col :xs="24" :md="12">
        <div class="page-card section">
          <h3>钱包流水</h3>
          <el-table :data="transactions" size="small" max-height="360" stripe>
            <el-table-column prop="type" label="类型" width="72">
              <template #default="{ row }">
                <el-tag size="small" :type="row.amount > 0 ? 'success' : 'danger'">{{ typeLabel(row.type) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额">
              <template #default="{ row }">
                <span :class="row.amount > 0 ? 'in' : 'out'">{{ row.amount > 0 ? '+' : '' }}{{ row.amount }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" show-overflow-tooltip />
            <el-table-column prop="createTime" label="时间" width="160" />
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
/**
 * ============================================================================
 * 页面：个人中心（views/Profile.vue）
 * ============================================================================
 *
 * onMounted load()：getProfile + getTransactions 填表单与流水表
 * save()：updateProfile → PUT /api/user/profile
 * goRecharge()：仅 USER 角色 → router.push('/recharge')
 * ============================================================================
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Wallet, Medal, User } from '@element-plus/icons-vue'
import { getProfile, updateProfile, getTransactions } from '../api'
import { useUserStore } from '../stores/user'
import PageHeader from '../components/PageHeader.vue'

const router = useRouter()
const userStore = useUserStore()
const profile = ref(null)
const isUser = computed(() => userStore.isUser())
const transactions = ref([])
const saving = ref(false)

const roleLabel = computed(() => ({
  USER: '普通用户', MERCHANT: '商家', ADMIN: '管理员'
}[profile.value?.user?.role] || ''))

const typeLabel = t => ({ RECHARGE: '充值', PAY: '支付', REFUND: '退款', SETTLE: '结算', FEE: '手续费' }[t] || t)

const load = async () => {
  profile.value = (await getProfile()).data
  transactions.value = (await getTransactions()).data
}

const goRecharge = () => {
  if (isUser.value) router.push('/recharge')
}

const save = async () => {
  saving.value = true
  try {
    await updateProfile(profile.value.user)
    ElMessage.success('保存成功')
  } finally { saving.value = false }
}

onMounted(load)
</script>

<style scoped>
.stat-cards { margin-bottom: 8px; }
.stat-card {
  padding: 24px; border-radius: var(--radius-lg); color: #fff;
  display: flex; align-items: flex-start; gap: 16px; margin-bottom: 16px;
  box-shadow: var(--shadow);
}
.stat-card.wallet { background: linear-gradient(135deg, #0d9488, #14b8a6); position: relative; }
.stat-card.clickable { cursor: pointer; }
.stat-card .recharge-btn { margin-left: auto; align-self: center; }
.stat-card.points { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
.stat-card.role { background: linear-gradient(135deg, #6366f1, #818cf8); }
.stat-card .label { display: block; font-size: 13px; opacity: 0.9; }
.stat-card .value { font-size: 28px; font-weight: 800; }
.stat-card .tip { font-size: 11px; opacity: 0.85; margin-top: 8px; }
.section { padding: 24px; }
.section h3 { margin-bottom: 20px; font-size: 17px; }
.in { color: #10b981; font-weight: 600; }
.out { color: #ef4444; font-weight: 600; }
</style>
