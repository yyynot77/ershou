<template>
  <div class="auth-wrap">
    <div class="auth-left">
      <h1>欢迎回来</h1>
      <p>登录后即可浏览、购买校园二手好物</p>
      <ul class="features">
        <li>✓ 钱包安全支付</li>
        <li>✓ 积分抵扣优惠</li>
        <li>✓ 线下当面交易</li>
      </ul>
    </div>
    <el-card class="auth-card" shadow="always">
      <h2>账号登录</h2>
      <el-form :model="form" label-position="top" size="large" @submit.prevent="submit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" prefix-icon="User" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password prefix-icon="Lock" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="captcha-row">
            <el-input v-model="form.captchaCode" placeholder="验证码" />
            <img v-if="captchaImage" :src="captchaImage" class="captcha" @click="loadCaptcha" title="点击刷新" />
            <el-button link type="primary" @click="loadCaptcha">换一张</el-button>
          </div>
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" class="submit-btn" round>
          登 录
        </el-button>
      </el-form>
      <div class="links">
        <router-link to="/register">注册普通用户</router-link>
        <span class="dot">·</span>
        <router-link to="/register-merchant">商家入驻</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCaptcha, login } from '../api'
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'

const router = useRouter()
const store = useUserStore()
const cartStore = useCartStore()
const form = ref({ username: '', password: '', captchaKey: '', captchaCode: '' })
const captchaImage = ref('')
const loading = ref(false)

const loadCaptcha = async () => {
  const res = await getCaptcha()
  form.value.captchaKey = res.data.captchaKey
  captchaImage.value = res.data.captchaImage
}

const submit = async () => {
  loading.value = true
  try {
    const res = await login(form.value)
    store.setLogin(res.data)
    await cartStore.refresh()
    ElMessage.success('登录成功，欢迎回来！')
    router.push(store.isAdmin() ? '/admin' : '/')
  } finally { loading.value = false }
}

onMounted(loadCaptcha)
</script>

<style scoped>
.auth-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 60px;
  padding: 40px 24px;
  background: linear-gradient(135deg, #ecfdf5 0%, #f0fdfa 40%, #f8fafc 100%);
}
.auth-left { max-width: 380px; }
.auth-left h1 { font-size: 36px; font-weight: 800; color: var(--text); margin-bottom: 12px; }
.auth-left p { color: var(--text-muted); font-size: 16px; margin-bottom: 28px; line-height: 1.6; }
.features { list-style: none; }
.features li { padding: 10px 0; color: var(--primary-dark); font-size: 15px; }
.auth-card {
  width: 420px;
  border-radius: var(--radius-lg) !important;
  padding: 12px 8px;
  border: none !important;
}
.auth-card h2 { text-align: center; margin-bottom: 28px; font-size: 22px; }
.captcha-row { display: flex; align-items: center; gap: 10px; width: 100%; }
.captcha { height: 44px; border-radius: 8px; cursor: pointer; border: 1px solid var(--border); }
.submit-btn { width: 100%; height: 48px; font-size: 16px; margin-top: 8px; }
.links { text-align: center; margin-top: 24px; font-size: 14px; }
.links a { color: var(--primary); font-weight: 500; }
.dot { margin: 0 10px; color: #cbd5e1; }
@media (max-width: 768px) {
  .auth-left { display: none; }
}
</style>
