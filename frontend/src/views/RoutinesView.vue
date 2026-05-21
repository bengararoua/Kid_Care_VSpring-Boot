<template>
  <Layout>
    <div class="routines-container">
      <RoutinePlanner :child-id="childId" />
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import Layout from '@/layouts/Layout.vue' 
import RoutinePlanner from '@/components/RoutinePlanner.vue'

const route = useRoute()
const childId = ref(route.params.id || 1)
</script>
