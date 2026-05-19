<template>
  <div class="auth-wrap">
    <div class="auth-left">
      <h1>商家入驻</h1>
      <p>开设你的校园小店，让闲置流转创造价值</p>
      <ul class="features">
        <li>须上传营业执照、身份证照片</li>
        <li>银行账号为 16 位数字</li>
        <li>提交后等待管理员审核</li>
      </ul>
    </div>
    <el-card class="auth-card wide" shadow="always">
      <h2>商家注册</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="登录用户名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="登录密码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="店铺名称" prop="shopName">
          <el-input v-model="form.shopName" placeholder="如：小明数码店" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="form.realName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" maxlength="11" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="银行账号（16位数字）" prop="bankAccount">
          <el-input v-model="form.bankAccount" maxlength="16" placeholder="6222021234567890" show-word-limit />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="营业执照" prop="businessLicense">
              <el-upload
                class="upload-box"
                drag
                accept="image/*"
                :show-file-list="false"
                :http-request="opt => doUpload(opt, 'businessLicense')"
              >
                <img v-if="form.businessLicense" :src="imgUrl(form.businessLicense)" class="upload-preview" />
                <template v-else>
                  <el-icon :size="32"><Upload /></el-icon>
                  <p>点击或拖拽上传图片</p>
                </template>
              </el-upload>
              <p v-if="form.businessLicense" class="upload-ok">✓ 已上传</p>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证照片" prop="idCardImage">
              <el-upload
                class="upload-box"
                drag
                accept="image/*"
                :show-file-list="false"
                :http-request="opt => doUpload(opt, 'idCardImage')"
              >
                <img v-if="form.idCardImage" :src="imgUrl(form.idCardImage)" class="upload-preview" />
                <template v-else>
                  <el-icon :size="32"><Upload /></el-icon>
                  <p>点击或拖拽上传图片</p>
                </template>
              </el-upload>
              <p v-if="form.idCardImage" class="upload-ok">✓ 已上传</p>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="验证码" prop="captchaCode">
          <div class="captcha-row">
            <el-input v-model="form.captchaCode" placeholder="验证码" />
            <img v-if="captchaImage" :src="captchaImage" class="captcha" @click="loadCaptcha" title="点击刷新" />
          </div>
        </el-form-item>
        <el-button type="primary" round class="submit-btn" :loading="submitting" @click="submit">
          提交入驻申请
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { getCaptcha, registerMerchant, uploadFile } from '../api'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const form = ref({
  username: '', password: '', shopName: '', realName: '', phone: '',
  bankAccount: '', businessLicense: '', idCardImage: '', captchaKey: '', captchaCode: ''
})
const captchaImage = ref('')

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  shopName: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  bankAccount: [
    { required: true, message: '请输入16位银行账号', trigger: 'blur' },
    { pattern: /^\d{16}$/, message: '银行账号必须为16位数字', trigger: 'blur' }
  ],
  businessLicense: [{ required: true, message: '请上传营业执照', trigger: 'change' }],
  idCardImage: [{ required: true, message: '请上传身份证照片', trigger: 'change' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const imgUrl = path => {
  if (!path) return ''
  if (path.startsWith('http') || path.startsWith('data:')) return path
  return path
}

const loadCaptcha = async () => {
  const res = await getCaptcha()
  form.value.captchaKey = res.data.captchaKey
  captchaImage.value = res.data.captchaImage
}

const doUpload = async (options, field) => {
  const file = options.file
  if (!file.type?.startsWith('image/')) {
    ElMessage.warning('请上传图片文件')
    options.onError?.(new Error('not image'))
    return
  }
  try {
    const res = await uploadFile(file)
    form.value[field] = res.data
    formRef.value?.validateField(field)
    ElMessage.success('上传成功')
    options.onSuccess?.(res.data)
  } catch (e) {
    ElMessage.error('图片上传失败，请确认后端已启动')
    options.onError?.(e)
  }
}

const submit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('请完善表单并上传证照')
    return
  }
  if (!form.value.businessLicense || !form.value.idCardImage) {
    ElMessage.warning('请先上传营业执照和身份证照片')
    return
  }
  submitting.value = true
  try {
    await registerMerchant(form.value)
    ElMessage.success('入驻申请已提交，请等待管理员审核后登录')
    router.push('/login')
  } finally {
    submitting.value = false
  }
}

onMounted(loadCaptcha)
</script>

<style scoped>
.auth-wrap {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  gap: 40px; padding: 32px 20px;
  background: linear-gradient(135deg, #fff7ed 0%, #f8fafc 100%);
}
.auth-left { max-width: 300px; }
.auth-left h1 { font-size: 30px; font-weight: 800; margin-bottom: 10px; }
.auth-left p { color: var(--text-muted); margin-bottom: 20px; }
.features { list-style: none; color: var(--primary-dark); }
.features li { padding: 8px 0; padding-left: 20px; position: relative; }
.features li::before { content: '✓'; position: absolute; left: 0; color: var(--primary); }
.auth-card.wide { width: 580px; border-radius: var(--radius-lg) !important; }
.upload-box { width: 100%; }
.upload-box :deep(.el-upload-dragger) { padding: 16px; width: 100%; }
.upload-preview { max-width: 100%; max-height: 100px; object-fit: contain; }
.upload-ok { color: #10b981; font-size: 12px; margin-top: 4px; }
.captcha-row { display: flex; gap: 10px; align-items: center; width: 100%; }
.captcha { height: 40px; border-radius: 8px; cursor: pointer; }
.submit-btn { width: 100%; height: 44px; margin-top: 8px; }
@media (max-width: 900px) { .auth-left { display: none; } }
</style>
