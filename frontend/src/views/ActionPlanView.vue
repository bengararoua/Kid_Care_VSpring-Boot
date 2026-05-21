<template>
  <Layout>
    <div class="action-plan-container">
      <ActionPlan :child-id="childId" />
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import Layout from '@/layouts/Layout.vue' 
import ActionPlan from '@/components/ActionPlan.vue'

const route = useRoute()
const childId = ref(route.params.id || 1)
</script>
