<template>
  <div class="fade-in">
    <PageHeader title="发布商品" subtitle="填写商品信息，提交后等待管理员审核" />
  <el-card class="page-card" shadow="never">
    <el-form :model="form" label-width="100px" style="max-width:600px">
      <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="类别">
        <el-select v-model="form.categoryId">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="原价"><el-input-number v-model="form.originalPrice" :min="0" /></el-form-item>
      <el-form-item label="售价"><el-input-number v-model="form.price" :min="0.01" /></el-form-item>
      <el-form-item label="成色">
        <el-select v-model="form.conditionLevel">
          <el-option label="全新" value="全新" />
          <el-option label="九成新" value="九成新" />
          <el-option label="八成新" value="八成新" />
          <el-option label="七成新" value="七成新" />
        </el-select>
      </el-form-item>
      <el-form-item label="尺寸"><el-input v-model="form.sizeInfo" /></el-form-item>
      <el-form-item label="库存"><el-input-number v-model="form.stock" :min="1" /></el-form-item>
      <el-form-item label="可议价"><el-switch v-model="form.allowBargain" :active-value="1" :inactive-value="0" /></el-form-item>
      <el-form-item label="说明"><el-input v-model="form.description" type="textarea" rows="4" /></el-form-item>
      <el-form-item label="图片">
        <el-upload list-type="picture-card" :http-request="uploadImg" :file-list="fileList">
          <el-icon><Plus /></el-icon>
        </el-upload>
      </el-form-item>
      <el-button type="primary" @click="submit">提交审核</el-button>
    </el-form>
  </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getCategories, publishProduct, uploadFile } from '../../api'
import PageHeader from '../../components/PageHeader.vue'

const form = ref({ name:'', categoryId:null, originalPrice:0, price:0, conditionLevel:'九成新', sizeInfo:'', stock:1, allowBargain:0, description:'', images:[] })
const categories = ref([])
const fileList = ref([])

const uploadImg = async ({ file }) => {
  const res = await uploadFile(file)
  form.value.images.push(res.data)
  fileList.value.push({ url: res.data })
}

const submit = async () => {
  await publishProduct(form.value)
  ElMessage.success('已提交，等待管理员审核')
  form.value = { name:'', categoryId:null, originalPrice:0, price:0, conditionLevel:'九成新', sizeInfo:'', stock:1, allowBargain:0, description:'', images:[] }
  fileList.value = []
}

onMounted(async () => { categories.value = (await getCategories()).data })
</script>
