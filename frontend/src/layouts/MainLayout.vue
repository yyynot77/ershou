<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="header-inner">
        <div class="brand" @click="$router.push('/')">
          <span class="brand-icon">🎓</span>
          <span class="brand-text">校园二手<span class="accent">市场</span></span>
        </div>

        <div class="search-box" v-if="showSearch">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索闲置好物..."
            clearable
            size="large"
            @keyup.enter="doSearch"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
            <template #append>
              <el-button type="primary" @click="doSearch">搜索</el-button>
            </template>
          </el-input>
        </div>

        <el-menu mode="horizontal" :ellipsis="false" router class="nav" :default-active="route.path">
          <el-menu-item index="/"><el-icon><HomeFilled /></el-icon>首页</el-menu-item>
          <el-menu-item index="/cart" v-if="store.isLogin()">
            <el-badge :value="cartStore.count" :hidden="!cartStore.count" :max="99">
              <span class="menu-label"><el-icon><ShoppingCart /></el-icon>购物车</span>
            </el-badge>
          </el-menu-item>
          <el-menu-item index="/orders" v-if="store.isLogin() && !store.isAdmin()">
            <el-icon><List /></el-icon>订单
          </el-menu-item>
          <el-sub-menu v-if="store.isMerchant()" index="merchant">
            <template #title><el-icon><Shop /></el-icon>商家中心</template>
            <el-menu-item index="/merchant/publish">发布商品</el-menu-item>
            <el-menu-item index="/merchant/products">商品管理</el-menu-item>
            <el-menu-item index="/merchant/orders">商家订单</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/admin" v-if="store.isAdmin()">
            <el-icon><Setting /></el-icon>后台
          </el-menu-item>
        </el-menu>

        <div class="actions">
          <template v-if="store.isLogin()">
            <el-dropdown trigger="click" @command="handleCmd">
              <div class="user-chip">
                <el-avatar :size="36" class="avatar">{{ avatarText }}</el-avatar>
                <span class="uname">{{ store.user?.realName }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile"><el-icon><User /></el-icon>个人中心</el-dropdown-item>
                  <el-dropdown-item v-if="store.isMerchant()" command="publish"><el-icon><Plus /></el-icon>发布商品</el-dropdown-item>
                  <el-dropdown-item divided command="logout"><el-icon><SwitchButton /></el-icon>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button round @click="$router.push('/login')">登录</el-button>
            <el-button round type="primary" @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>

    <el-main class="main">
      <div class="page-container">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </el-main>

    <el-footer class="footer">
      <div class="footer-inner">
        <span>校园二手交易市场 · Spring Boot 综合实践</span>
        <span>线下交易 · 绿色校园</span>
      </div>
    </el-footer>
  </el-container>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Search, HomeFilled, ShoppingCart, List, Shop, Setting,
  User, Plus, SwitchButton, ArrowDown
} from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const cartStore = useCartStore()
const searchKeyword = ref('')

const showSearch = computed(() => !route.path.startsWith('/login') && !route.path.startsWith('/register'))
const avatarText = computed(() => (store.user?.realName || '?').charAt(0))

const doSearch = () => {
  router.push({ path: '/', query: { q: searchKeyword.value || undefined } })
}

const handleCmd = cmd => {
  if (cmd === 'profile') router.push('/profile')
  else if (cmd === 'publish') router.push('/merchant/publish')
  else if (cmd === 'logout') { store.logout(); cartStore.setCount(0); router.push('/login') }
}

watch(() => route.query.q, q => { if (q) searchKeyword.value = q }, { immediate: true })

watch(() => store.isLogin(), async logged => {
  if (logged) await cartStore.refresh()
}, { immediate: true })

onMounted(() => {
  if (store.isLogin()) cartStore.refresh()
})
</script>

<style scoped>
.layout { min-height: 100vh; flex-direction: column; }
.header {
  height: auto !important;
  padding: 0;
  background: rgba(255,255,255,0.92);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 12px 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}
.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  flex-shrink: 0;
}
.brand-icon { font-size: 28px; }
.brand-text { font-size: 20px; font-weight: 800; color: var(--text); }
.brand-text .accent { color: var(--primary); }
.search-box { flex: 1; min-width: 200px; max-width: 360px; }
.nav { flex: 1; border: none !important; background: transparent !important; justify-content: flex-end; }
.menu-label { display: inline-flex; align-items: center; gap: 4px; }
.actions { flex-shrink: 0; }
.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px 4px 4px;
  border-radius: 24px;
  transition: background 0.2s;
}
.user-chip:hover { background: #f1f5f9; }
.avatar { background: linear-gradient(135deg, var(--primary), var(--primary-light)); color: #fff; font-weight: 600; }
.uname { font-size: 14px; font-weight: 500; max-width: 80px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.main { flex: 1; padding: 24px 20px 40px; }
.footer {
  height: auto !important;
  padding: 20px;
  background: #fff;
  border-top: 1px solid var(--border);
}
.footer-inner {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  color: var(--text-muted);
  font-size: 13px;
}
</style>
