<template>
  <!-- Structure HTML de la page -->
  <Layout>  <!-- Composant Layout qui enveloppe la page (barre de navigation, footer, etc.) -->
    <div class="routines-container">  <!-- Div conteneur avec classe CSS pour le style -->
      <RoutinePlanner :child-id="childId" />  <!-- Composant d'affichage des routines, avec prop childId passée dynamiquement -->
    </div>
  </Layout>
</template>

<script setup>
// Bloc script avec la syntaxe Composition API (setup automatique)
import { ref, onMounted } from 'vue'  // Importe ref (pour créer des variables réactives) et onMounted (hook après montage)
import { useRoute } from 'vue-router'  // Importe useRoute pour accéder aux paramètres de l'URL (route active)
import Layout from '@/layouts/Layout.vue'  // Importe le composant Layout depuis le dossier layouts (alias @)
import RoutinePlanner from '@/components/RoutinePlanner.vue'  // Importe le composant RoutinePlanner depuis le dossier components

const route = useRoute()  // Récupère l'objet route active (contient les paramètres d'URL)
const childId = ref(route.params.id || 1)  // Crée une variable réactive childId initialisée avec le paramètre d'URL 'id' (ou 1 si absent)
</script>