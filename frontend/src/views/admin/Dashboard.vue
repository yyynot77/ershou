<template>
  <el-tabs v-model="tab">
    <el-tab-pane label="待审核用户" name="users">
      <el-table :data="pendingUsers">
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="role" label="角色" />
        <el-table-column prop="phone" label="手机" />
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="auditU(row.id, true)">通过</el-button>
            <el-button type="danger" size="small" @click="auditU(row.id, false)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-tab-pane>
    <el-tab-pane label="用户管理" name="allUsers">
      <el-table :data="users">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="role" label="角色" />
        <el-table-column prop="status" label="状态" />
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button size="small" @click="recharge(row)">充值</el-button>
            <el-button v-if="row.role==='MERCHANT'" size="small" @click="setLevel(row)">设等级</el-button>
            <el-button size="small" type="danger" @click="delU(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-tab-pane>
    <el-tab-pane label="待审核商品" name="products">
      <el-table :data="pendingProducts">
        <el-table-column prop="name" label="商品名" />
        <el-table-column prop="price" label="价格" />
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="auditP(row.id, true)">通过</el-button>
            <el-button type="danger" size="small" @click="auditP(row.id, false)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-tab-pane>
    <el-tab-pane label="商家惩罚" name="ban">
      <el-form inline>
        <el-form-item label="商家ID"><el-input v-model="banForm.merchantId" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="banForm.banType">
            <el-option label="禁止发布" value="PUBLISH_BAN" />
            <el-option label="店铺关闭" value="SHOP_CLOSE" />
          </el-select>
        </el-form-item>
        <el-form-item label="原因"><el-input v-model="banForm.reason" /></el-form-item>
        <el-button type="warning" @click="doBan">执行惩罚</el-button>
      </el-form>
      <el-table :data="merchantBans" style="margin-top:16px" stripe>
        <el-table-column prop="id" label="记录ID" width="80" />
        <el-table-column prop="merchantId" label="商家ID" width="90" />
        <el-table-column prop="banType" label="类型" width="120">
          <template #default="{ row }">{{ banTypeLabel(row.banType) }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" show-overflow-tooltip />
        <el-table-column prop="endTime" label="截止时间" width="160">
          <template #default="{ row }">{{ row.endTime || '永久' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="doLiftBan(row)">解除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <p class="tab-tip">解除「店铺关闭」后，被下架商品需商家在商品管理中重新上架。</p>
    </el-tab-pane>
    <el-tab-pane label="买家拉黑" name="blacklist">
      <el-form inline>
        <el-form-item label="买家ID"><el-input v-model="blForm.buyerId" /></el-form-item>
        <el-form-item label="商家ID(空=全平台)"><el-input v-model="blForm.merchantId" /></el-form-item>
        <el-form-item label="原因"><el-input v-model="blForm.reason" /></el-form-item>
        <el-button type="danger" @click="doBl">拉黑</el-button>
      </el-form>
      <el-table :data="blacklist" style="margin-top:16px" stripe>
        <el-table-column prop="id" label="记录ID" width="80" />
        <el-table-column prop="buyerId" label="买家ID" width="90" />
        <el-table-column prop="merchantId" label="商家ID" width="100">
          <template #default="{ row }">{{ row.merchantId ?? '全平台' }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" show-overflow-tooltip />
        <el-table-column prop="createTime" label="拉黑时间" width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="doUnblacklist(row)">解除拉黑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-tab-pane>
    <el-tab-pane label="轮播图" name="banner">
      <el-form :model="banner" label-width="80px" style="max-width:500px">
        <el-form-item label="标题"><el-input v-model="banner.title" /></el-form-item>
        <el-form-item label="图片URL"><el-input v-model="banner.imageUrl" /></el-form-item>
        <el-form-item label="链接"><el-input v-model="banner.linkUrl" /></el-form-item>
        <el-button type="primary" @click="saveBannerFn">保存轮播</el-button>
      </el-form>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pendingUsers as fetchPendingUsers, auditUser, allUsers, deleteUser,
  pendingProducts as fetchPendingProducts, auditProduct,
  setMerchantLevel, rechargeWallet, banMerchant, listMerchantBans, liftMerchantBan,
  blacklistBuyer, listBuyerBlacklist, removeBuyerBlacklist, saveBanner
} from '../../api'

const tab = ref('users')
const pendingUsers = ref([])
const users = ref([])
const pendingProducts = ref([])
const merchantBans = ref([])
const blacklist = ref([])
const banForm = ref({ merchantId: '', banType: 'PUBLISH_BAN', reason: '' })
const blForm = ref({ buyerId: '', merchantId: '', reason: '' })
const banner = ref({ title: '', imageUrl: '', linkUrl: '', enabled: 1, sortOrder: 0 })

const banTypeLabel = t => ({ PUBLISH_BAN: '禁止发布', SHOP_CLOSE: '店铺关闭' }[t] || t)

const loadBans = async () => {
  merchantBans.value = (await listMerchantBans()).data || []
}

const loadBlacklist = async () => {
  blacklist.value = (await listBuyerBlacklist()).data || []
}

const load = async () => {
  pendingUsers.value = (await fetchPendingUsers()).data
  users.value = (await allUsers()).data
  pendingProducts.value = (await fetchPendingProducts()).data
  await Promise.all([loadBans(), loadBlacklist()])
}

const auditU = async (id, pass) => { await auditUser(id, pass); ElMessage.success('已处理'); load() }
const auditP = async (id, pass) => { await auditProduct(id, pass); ElMessage.success('已处理'); load() }
const delU = async id => { await deleteUser(id); load() }

const recharge = async row => {
  const { value } = await ElMessageBox.prompt('充值金额', '钱包充值', { inputPattern: /^\d+(\.\d+)?$/ })
  await rechargeWallet(row.id, value)
  ElMessage.success('充值成功')
}

const setLevel = async row => {
  const { value } = await ElMessageBox.prompt('商家等级1-5', '设置等级', { inputPattern: /^[1-5]$/ })
  await setMerchantLevel(row.id, parseInt(value))
  ElMessage.success('已设置')
}

const doBan = async () => {
  if (!banForm.value.merchantId) return ElMessage.warning('请填写商家ID')
  await banMerchant({ id: banForm.value.merchantId, banType: banForm.value.banType, reason: banForm.value.reason })
  ElMessage.success('已执行惩罚')
  await loadBans()
}

const doLiftBan = async row => {
  await ElMessageBox.confirm(`确定解除商家 ${row.merchantId} 的${banTypeLabel(row.banType)}？`, '解除惩罚')
  await liftMerchantBan(row.id)
  ElMessage.success('已解除惩罚')
  await loadBans()
}

const doBl = async () => {
  if (!blForm.value.buyerId) return ElMessage.warning('请填写买家ID')
  await blacklistBuyer({ buyerId: blForm.value.buyerId, merchantId: blForm.value.merchantId || undefined, reason: blForm.value.reason })
  ElMessage.success('已拉黑')
  await loadBlacklist()
}

const doUnblacklist = async row => {
  await ElMessageBox.confirm(`确定解除买家 ${row.buyerId} 的拉黑？`, '解除拉黑')
  await removeBuyerBlacklist(row.id)
  ElMessage.success('已解除拉黑')
  await loadBlacklist()
}

const saveBannerFn = async () => {
  await saveBanner(banner.value)
  ElMessage.success('轮播已保存')
}

onMounted(load)
</script>

<style scoped>
.tab-tip { margin-top: 12px; font-size: 13px; color: #64748b; }
</style>
