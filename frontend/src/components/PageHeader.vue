<template>
  <div class="page-header fade-in">
    <div>
      <h1>{{ title }}</h1>
      <p v-if="subtitle" class="sub">{{ subtitle }}</p>
    </div>
    <slot name="extra" />
  </div>
</template>

<script setup>
defineProps({
  title: String,
  subtitle: String
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border);
}
.page-header h1 { font-size: 26px; font-weight: 700; color: var(--text); }
.sub { margin-top: 6px; color: var(--text-muted); font-size: 14px; }
</style>
