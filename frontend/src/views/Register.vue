<template>
  <div class="auth-wrap">
    <div class="auth-left">
      <h1>加入校园二手</h1>
      <p>注册成为普通用户，发现身边的超值好物</p>
    </div>
    <el-card class="auth-card wide" shadow="always">
      <h2>用户注册</h2>
      <el-alert type="info" :closable="false" show-icon style="margin-bottom:20px">
        提交后需等待管理员审核，通过后方可登录
      </el-alert>
      <el-form :model="form" label-position="top" size="default">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="城市"><el-input v-model="form.city" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="银行账号(16位)"><el-input v-model="form.bankAccount" maxlength="16" /></el-form-item>
        <el-form-item label="验证码">
          <div class="captcha-row">
            <el-input v-model="form.captchaCode" />
            <img v-if="captchaImage" :src="captchaImage" class="captcha" @click="loadCaptcha" />
          </div>
        </el-form-item>
        <el-button type="primary" round class="submit-btn" @click="submit">提交注册</el-button>
      </el-form>
      <p class="links"><router-link to="/login">已有账号？去登录</router-link></p>
    </el-card>
  </div>
</template>

<script setup>
/** 页面：用户注册 → registerUser → 待管理员审核 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCaptcha, registerUser } from '../api'

const router = useRouter()
const form = ref({ username:'', password:'', realName:'', phone:'', email:'', city:'', gender:1, bankAccount:'', captchaKey:'', captchaCode:'' })
const captchaImage = ref('')

const loadCaptcha = async () => {
  const res = await getCaptcha()
  form.value.captchaKey = res.data.captchaKey
  captchaImage.value = res.data.captchaImage
}

const submit = async () => {
  await registerUser(form.value)
  ElMessage.success('注册成功，请等待审核')
  router.push('/login')
}

onMounted(loadCaptcha)
</script>

<style scoped>
.auth-wrap {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  gap: 48px; padding: 40px 24px;
  background: linear-gradient(135deg, #ecfdf5 0%, #f8fafc 100%);
}
.auth-left h1 { font-size: 32px; font-weight: 800; margin-bottom: 12px; }
.auth-left p { color: var(--text-muted); }
.auth-card.wide { width: 520px; border-radius: var(--radius-lg) !important; padding: 8px; }
.auth-card h2 { text-align: center; margin-bottom: 8px; }
.captcha-row { display: flex; gap: 10px; width: 100%; }
.captcha { height: 40px; border-radius: 8px; cursor: pointer; }
.submit-btn { width: 100%; height: 44px; margin-top: 8px; }
.links { text-align: center; margin-top: 16px; }
.links a { color: var(--primary); }
@media (max-width: 768px) { .auth-left { display: none; } }
</style>
