<template>
  <div class="fade-in">
    <PageHeader title="商品管理" subtitle="管理已发布、已售出与下架商品" />
  <el-card class="page-card" shadow="never">
    <template #header>
      <span>筛选</span>
      <el-radio-group v-model="status" @change="load" style="float:right">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="PUBLISHED">已发布</el-radio-button>
        <el-radio-button value="SOLD">已售出</el-radio-button>
        <el-radio-button value="OFF_SHELF">已下架</el-radio-button>
        <el-radio-button value="PENDING">待审核</el-radio-button>
      </el-radio-group>
    </template>
    <el-table :data="products">
      <el-table-column prop="product.name" label="名称" />
      <el-table-column prop="product.price" label="价格" />
      <el-table-column prop="product.stock" label="库存" />
      <el-table-column prop="product.soldCount" label="销量" />
      <el-table-column prop="product.status" label="状态" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button v-if="row.product.status==='PUBLISHED'" link type="warning" @click="off(row.product.id)">下架</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { myProducts, offShelfProduct } from '../../api'
import PageHeader from '../../components/PageHeader.vue'

const status = ref('')
const products = ref([])
const load = async () => { products.value = (await myProducts(status.value || undefined)).data }
const off = async id => { await offShelfProduct(id); ElMessage.success('已下架'); load() }
onMounted(load)
</script>
