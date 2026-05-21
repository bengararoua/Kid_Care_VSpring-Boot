import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import api, { setAuthToken } from './api/api'

// Restaurer le token au démarrage
const token = localStorage.getItem('token')
if (token && token !== 'undefined' && token !== 'null') {
  setAuthToken(token)
  console.log('✅ Token restauré dans main.js')
}

const app = createApp(App)
app.use(router)
app.mount('#app')