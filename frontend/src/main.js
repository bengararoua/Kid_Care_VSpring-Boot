import { createApp } from 'vue'  // Importe la fonction createApp de Vue 3 pour créer l'application Vue
import App from './App.vue'  // Importe le composant racine App.vue (point d'entrée principal de l'interface)
import router from './router'  // Importe le routeur Vue Router pour la navigation entre les pages
import api, { setAuthToken } from './api/api'  // Importe l'instance API (axios) et la fonction pour définir le token d'authentification

// Restaurer le token au démarrage (si l'utilisateur était connecté avant de fermer l'application)
const token = localStorage.getItem('token')  // Récupère le token JWT stocké dans le localStorage du navigateur
if (token && token !== 'undefined' && token !== 'null') {  // Vérifie si le token existe et n'est pas une valeur invalide
  setAuthToken(token)  // Définit le token dans les en-têtes de toutes les requêtes API (Authorization: Bearer token)
  console.log('✅ Token restauré dans main.js')  // Affiche un message de confirmation dans la console
}

const app = createApp(App)  // Crée l'application Vue avec le composant racine App
app.use(router)  // Enregistre le routeur (permet à l'application de gérer les routes)
app.mount('#app')  // Monte l'application sur l'élément HTML avec l'id "app" (dans index.html)