<template>
  <div v-if="shop" class="shop-page fade-in">
    <div class="shop-hero page-card">
      <div class="shop-avatar">{{ shop.merchant?.shopName?.charAt(0) || '店' }}</div>
      <div class="shop-info">
        <h1>{{ shop.merchant?.shopName }}</h1>
        <div class="shop-stats">
          <el-tag type="warning" effect="plain">Lv.{{ shop.merchant?.merchantLevel }} 商家</el-tag>
          <span>好评率 {{ shop.merchant?.satisfactionRate }}%</span>
          <span>累计销售 ¥{{ shop.merchant?.totalSales }}</span>
        </div>
      </div>
    </div>
    <PageHeader title="店铺商品" :subtitle="`共 ${shop.products?.length || 0} 件在售`" />
    <el-row :gutter="20">
      <el-col :xs="12" :sm="8" :md="6" v-for="item in shop.products" :key="item.product.id">
        <ProductCard :item="item" />
      </el-col>
    </el-row>
    <el-empty v-if="!shop.products?.length" description="店铺暂无商品" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getShop } from '../api'
import ProductCard from '../components/ProductCard.vue'
import PageHeader from '../components/PageHeader.vue'

const route = useRoute()
const shop = ref(null)
onMounted(async () => { shop.value = (await getShop(route.params.id)).data })
</script>

<style scoped>
.shop-hero {
  display: flex; align-items: center; gap: 24px;
  padding: 28px 32px; margin-bottom: 24px;
  background: linear-gradient(135deg, #f0fdfa 0%, #fff 60%);
}
.shop-avatar {
  width: 72px; height: 72px; border-radius: 16px;
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  color: #fff; font-size: 32px; font-weight: 800;
  display: flex; align-items: center; justify-content: center;
}
.shop-info h1 { font-size: 24px; margin-bottom: 10px; }
.shop-stats { display: flex; gap: 16px; align-items: center; color: var(--text-muted); font-size: 14px; }
</style>
