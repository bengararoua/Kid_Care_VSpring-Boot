<script setup>
import { RouterView } from 'vue-router'
import { onMounted } from 'vue'
import api, { setAuthToken } from './api/api'

onMounted(() => {
  const token = localStorage.getItem('token')
  if (token && token !== 'undefined' && token !== 'null') {
    setAuthToken(token)
    console.log('✅ Token configuré dans App.vue')
  } else {
    console.log('⚠️ Aucun token valide trouvé')
  }
})
</script>

<template>
  <RouterView />
</template>