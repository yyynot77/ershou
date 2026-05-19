<template>
  <div class="home">
    <!-- 轮播 / 默认 Hero -->
    <section class="hero fade-in">
      <el-carousel v-if="banners.length" height="320px" class="banner" indicator-position="outside">
        <el-carousel-item v-for="b in banners" :key="b.id">
          <div class="banner-slide" @click="goBanner(b)">
            <img :src="b.imageUrl" class="banner-img" />
            <div class="banner-mask">
              <h2>{{ b.title || '精选好物推荐' }}</h2>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
      <div v-else class="hero-default">
        <div class="hero-content">
          <h1>让闲置<span>流动</span>起来</h1>
          <p>校园二手交易 · 安全可靠 · 线下当面交易</p>
          <el-button type="primary" size="large" round @click="$router.push('/register')">立即加入</el-button>
        </div>
      </div>
    </section>

    <!-- 分类快捷筛选 -->
    <section class="categories fade-in">
      <el-check-tag
        v-for="c in categoryList"
        :key="c.id"
        :checked="categoryId === c.id"
        @change="() => toggleCategory(c.id)"
        class="cat-tag"
      >
        {{ c.name }}
      </el-check-tag>
    </section>

    <!-- 筛选栏 -->
    <section class="toolbar page-card fade-in">
      <div class="toolbar-left">
        <span class="result-tip" v-if="!loading">共 <b>{{ total }}</b> 件好物</span>
        <el-skeleton v-else :rows="0" animated style="width:120px" />
      </div>
      <el-radio-group v-model="sortBy" @change="onSortChange" size="default">
        <el-radio-button value="default">最新上架</el-radio-button>
        <el-radio-button value="price_asc">价格从低到高</el-radio-button>
        <el-radio-button value="price_desc">价格从高到低</el-radio-button>
        <el-radio-button value="sales">销量优先</el-radio-button>
        <el-radio-button value="rating">好评优先</el-radio-button>
      </el-radio-group>
    </section>

    <!-- 商品网格 -->
    <section class="products-grid" v-loading="loading">
      <el-row :gutter="20" v-if="products.length">
        <el-col :xs="12" :sm="8" :md="6" v-for="item in products" :key="item.product.id">
          <ProductCard :item="item" class="grid-item" />
        </el-col>
      </el-row>
      <el-empty v-else-if="!loading" description="暂无商品，换个关键词试试">
        <el-button type="primary" @click="resetSearch">重置筛选</el-button>
      </el-empty>
    </section>

    <div class="pagination-wrap" v-if="total > 12">
      <el-pagination
        background
        layout="prev, pager, next, jumper"
        :total="total"
        :page-size="12"
        v-model:current-page="page"
        @current-change="search"
      />
    </div>
  </div>
</template>

<script setup>
/**
 * ============================================================================
 * 页面：首页商品流（views/Home.vue）
 * ============================================================================
 *
 * 路由：/ （MainLayout 子路由）
 *
 * 生命周期 onMounted：
 * 1. 读 route.query.q 作为搜索关键词
 * 2. getCategories / getBanners 填分类与轮播
 * 3. search() 拉商品分页列表
 *
 * 用户操作：
 * - 顶栏搜索 → MainLayout.doSearch → 改 query.q → watch 触发 search()
 * - 分类 el-check-tag → toggleCategory() → search()
 * - 排序 radio → onSortChange() → search()
 * - 点击商品卡片 → ProductCard.goDetail → /product/:id
 *
 * 数据：products/total 为 ref，赋值后 el-row 重渲染 ProductCard 列表
 * API：GET /api/products/search → ProductService.search（仅 PUBLISHED 有库存）
 * ============================================================================
 */
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchProducts, getBanners, getCategories } from '../api'
import ProductCard from '../components/ProductCard.vue'

const route = useRoute()
const router = useRouter()
const keyword = ref('')
const sortBy = ref('default')
const categoryId = ref(null)
const page = ref(1)
const products = ref([])
const total = ref(0)
const banners = ref([])
const categoryList = ref([{ id: null, name: '全部' }])
const loading = ref(false)

const search = async () => {
  loading.value = true
  try {
    const res = await searchProducts({
      keyword: keyword.value,
      categoryId: categoryId.value || undefined,
      sortBy: sortBy.value,
      page: page.value,
      size: 12
    })
    products.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const onSortChange = () => { page.value = 1; search() }

const toggleCategory = id => {
  categoryId.value = categoryId.value === id ? null : id
  page.value = 1
  search()
}

const resetSearch = () => {
  keyword.value = ''
  sortBy.value = 'default'
  categoryId.value = null
  page.value = 1
  router.push({ path: '/' })
  search()
}

const goBanner = b => {
  if (b.productId) router.push(`/product/${b.productId}`)
  else if (b.linkUrl) window.open(b.linkUrl)
}

watch(() => route.query.q, q => {
  keyword.value = q || ''
  page.value = 1
  search()
})

onMounted(async () => {
  keyword.value = route.query.q || ''
  try {
    const cats = (await getCategories()).data
    categoryList.value = [{ id: null, name: '全部' }, ...cats]
    banners.value = (await getBanners()).data || []
  } catch {}
  search()
})
</script>

<style scoped>
.hero { margin-bottom: 28px; border-radius: var(--radius-lg); overflow: hidden; box-shadow: var(--shadow); }
.banner-slide { position: relative; height: 320px; cursor: pointer; }
.banner-img { width: 100%; height: 100%; object-fit: cover; }
.banner-mask {
  position: absolute; inset: 0;
  background: linear-gradient(transparent 40%, rgba(0,0,0,0.55));
  display: flex; align-items: flex-end; padding: 32px;
}
.banner-mask h2 { color: #fff; font-size: 28px; text-shadow: 0 2px 8px rgba(0,0,0,0.3); }
.hero-default {
  height: 280px;
  background: linear-gradient(135deg, #0d9488 0%, #0f766e 50%, #134e4a 100%);
  display: flex; align-items: center; justify-content: center;
  border-radius: var(--radius-lg);
}
.hero-content { text-align: center; color: #fff; }
.hero-content h1 { font-size: 36px; font-weight: 800; margin-bottom: 12px; }
.hero-content h1 span { color: #fde68a; }
.hero-content p { opacity: 0.9; margin-bottom: 24px; font-size: 16px; }
.categories {
  display: flex; flex-wrap: wrap; gap: 10px;
  margin-bottom: 20px;
}
.cat-tag { cursor: pointer; border-radius: 20px !important; padding: 8px 18px !important; }
.toolbar {
  display: flex; justify-content: space-between; align-items: center;
  flex-wrap: wrap; gap: 16px;
  padding: 16px 20px;
  margin-bottom: 24px;
}
.result-tip { color: var(--text-muted); font-size: 14px; }
.result-tip b { color: var(--primary); font-size: 18px; }
.products-grid { min-height: 200px; }
.grid-item { margin-bottom: 20px; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 32px; }
</style>
