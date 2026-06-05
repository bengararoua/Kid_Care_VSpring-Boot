<script setup>  // Déclare un bloc script avec la syntaxe "Composition API" de Vue 3 (setup automatique)
import { RouterView } from 'vue-router'  // Importe le composant RouterView qui affiche la page correspondant à la route actuelle
import { onMounted } from 'vue'  // Importe le hook onMounted qui exécute du code après le montage du composant
import api, { setAuthToken } from './api/api'  // Importe l'instance API et la fonction pour définir le token d'authentification

onMounted(() => {  // Hook exécuté une fois que le composant App est monté dans le DOM
  const token = localStorage.getItem('token')  // Récupère le token JWT stocké dans le localStorage du navigateur
  if (token && token !== 'undefined' && token !== 'null') {  // Vérifie si le token existe et n'est pas invalide
    setAuthToken(token)  // Définit le token dans les en-têtes de toutes les requêtes API
    console.log('✅ Token configuré dans App.vue')  // Affiche un message de confirmation dans la console
  } else {
    console.log('⚠️ Aucun token valide trouvé')  // Affiche un avertissement si aucun token valide n'est trouvé
  }
})
</script>

<template>
  <RouterView /> 
</template>