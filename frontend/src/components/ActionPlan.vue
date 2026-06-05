<template>
  <div class="action-plan" v-if="plan">
    <!-- Header -->
    <div class="plan-header" :class="getRiskClass(plan.riskLevel)">
      <div class="risk-icon">
        <span v-if="plan.riskLevel === 'high'">🔴</span>
        <span v-else-if="plan.riskLevel === 'medium'">🟡</span>
        <span v-else>🟢</span>
      </div>
      <div class="risk-info">
        <h3>Niveau de risque : {{ getRiskLabel(plan.riskLevel) }}</h3>
        <p>Plan généré le {{ formatDate(plan.generatedDate) }}</p>
      </div>
    </div>

    <!-- Programme -->
    <div class="plan-schedule">
      <h3>📅 Programme de la journée</h3>

      <!-- Matin -->
      <div class="time-block">
        <div class="time-icon">🌅</div>
        <div class="time-content">
          <h4>Matin</h4>
          <ul>
            <li v-for="(activity, index) in getMorningActivities()" :key="'morning-' + index" class="activity-item">
              {{ cleanText(activity) }}
            </li>
            <li v-if="getMorningActivities().length === 0" class="activity-item empty-item">
              Aucune activité programmée
            </li>
          </ul>
        </div>
      </div>

      <!-- Après-midi -->
      <div class="time-block">
        <div class="time-icon">☀️</div>
        <div class="time-content">
          <h4>Après-midi</h4>
          <ul>
            <li v-for="(activity, index) in getAfternoonActivities()" :key="'afternoon-' + index" class="activity-item">
              {{ cleanText(activity) }}
            </li>
            <li v-if="getAfternoonActivities().length === 0" class="activity-item empty-item">
              Aucune activité programmée
            </li>
          </ul>
        </div>
      </div>

      <!-- Soirée -->
      <div class="time-block">
        <div class="time-icon">🌙</div>
        <div class="time-content">
          <h4>Soirée</h4>
          <ul>
            <li v-for="(activity, index) in getEveningActivities()" :key="'evening-' + index" class="activity-item">
              {{ cleanText(activity) }}
            </li>
            <li v-if="getEveningActivities().length === 0" class="activity-item empty-item">
              Aucune activité programmée
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Conseils -->
    <div class="plan-tips">
      <h3>💬 Conseils de communication</h3>
      <ul>
        <li v-for="(tip, index) in getCommunicationTips()" :key="'tip-' + index" class="activity-item">
          {{ cleanText(tip) }}
        </li>
        <li v-if="getCommunicationTips().length === 0" class="activity-item empty-item">
          Aucun conseil disponible
        </li>
      </ul>
    </div>

    <!-- Jeux -->
    <div class="plan-games">
      <h3>🎮 Activités recommandées</h3>
      <div class="games-grid">
        <div v-for="(game, index) in getGamesActivities()" :key="'game-' + index" class="game-card">
          {{ cleanText(game) }}
        </div>
        <div v-if="getGamesActivities().length === 0" class="game-card empty-game">
          Aucune activité recommandée
        </div>
      </div>
    </div>

    <button @click="printPlan" class="btn-print">🖨️ Imprimer le plan</button>
  </div>

  <!-- Loading -->
  <div v-else-if="loading" class="loading-plan">
    <div class="spinner"></div>
    <p>Génération du plan personnalisé...</p>
  </div>

  <!-- Empty -->
  <div v-else class="no-plan">
    <div class="no-plan-icon">📋</div>
    <p>Cliquez sur "Générer mon plan" pour obtenir un programme personnalisé</p>
    <button @click="generatePlan" class="btn-generate">✨ Générer mon plan</button>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import api from '@/api/api'

const props = defineProps({
  childId: {
    type: [Number, String],
    required: true
  }
})

const plan = ref(null)
const loading = ref(false)

const getRiskClass = (risk) => {
  return { high: 'risk-high', medium: 'risk-medium', low: 'risk-low' }[risk] || 'risk-low'
}

const getRiskLabel = (risk) => {
  return {
    high: 'Élevé - Intervention recommandée',
    medium: 'Modéré - Surveillance active',
    low: 'Faible - Maintenir les bonnes habitudes'
  }[risk] || 'Non déterminé'
}

const formatDate = (date) => {
  if (!date) return 'N/A'
  try {
    return new Date(date).toLocaleDateString('fr-FR', {
      day: 'numeric', month: 'long', year: 'numeric'
    })
  } catch (e) {
    return 'N/A'
  }
}

const cleanText = (text) => {
  if (!text) return ''
  let cleaned = text
  // Supprimer les balises <br>
  cleaned = cleaned.replace(/<br\s*\/?>/gi, ' ')
  // Supprimer les • au début
  cleaned = cleaned.replace(/^[•●]\s*/, '')
  // Supprimer les • au milieu
  cleaned = cleaned.replace(/[•●]\s*/g, ' ')
  // Supprimer les espaces multiples
  cleaned = cleaned.replace(/\s+/g, ' ').trim()
  return cleaned
}

const splitActivities = (raw) => {
  if (!raw) return []
  if (Array.isArray(raw)) return raw.filter(i => i && i.trim())

  let text = String(raw).trim()
  
  // Diviser par ||
  let parts = text.split(/\|\|/)
  
  // Nettoyer chaque partie
  return parts
    .map(p => p.trim())
    .filter(p => p && p.length > 0 && p !== '||')
}

const getMorningActivities = () => splitActivities(plan.value?.morningActivities)
const getAfternoonActivities = () => splitActivities(plan.value?.afternoonActivities)
const getEveningActivities = () => splitActivities(plan.value?.eveningActivities)
const getCommunicationTips = () => splitActivities(plan.value?.communicationTips)
const getGamesActivities = () => splitActivities(plan.value?.gamesActivities)

const generatePlan = async () => {
  if (!props.childId) return
  loading.value = true
  try {
    const response = await api.get(`/action-plan/${props.childId}/generate`)
    if (response.data && !response.data.error) {
      plan.value = response.data
      console.log('✅ Plan généré:', plan.value)
    }
  } catch (err) {
    console.error('Erreur génération plan:', err)
  } finally {
    loading.value = false
  }
}

const loadLatestPlan = async () => {
  if (!props.childId) return
  try {
    const response = await api.get(`/action-plan/${props.childId}/latest`)
    if (response.data && !response.data.error) {
      plan.value = response.data
      console.log('✅ Plan chargé:', plan.value)
    }
  } catch (err) {
    if (err.response?.status !== 404) {
      console.error('Erreur chargement plan:', err)
    }
    plan.value = null
  }
}

const printPlan = () => {
  const printContent = document.querySelector('.action-plan')
  if (!printContent) return
  const originalContent = document.body.innerHTML
  document.body.innerHTML = printContent.outerHTML
  window.print()
  document.body.innerHTML = originalContent
}

watch(() => props.childId, () => {
  loadLatestPlan()
}, { immediate: true })

defineExpose({ generatePlan, loadLatestPlan })
</script>

<style scoped>
.action-plan {
  background: white;
  border-radius: 24px;
  padding: 24px;
  margin-top: 24px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to   { opacity: 1; transform: translateY(0); }
}

.plan-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 16px;
  margin-bottom: 24px;
}

.risk-high   { background: #fee2e2; border-left: 5px solid #ef4444; }
.risk-medium { background: #fef3c7; border-left: 5px solid #f59e0b; }
.risk-low    { background: #d1fae5; border-left: 5px solid #10b981; }

.risk-icon { font-size: 48px; }
.risk-info h3 { margin-bottom: 5px; font-size: 18px; color: #1f2937; }
.risk-info p  { font-size: 12px; color: #6b7280; margin: 0; }

.plan-schedule h3,
.plan-tips h3,
.plan-games h3 {
  margin-bottom: 20px;
  color: #1f2937;
  font-size: 18px;
}

.time-block {
  display: flex;
  gap: 16px;
  background: #f9fafb;
  padding: 18px;
  border-radius: 18px;
  margin-bottom: 16px;
  transition: transform 0.2s;
}
.time-block:hover { transform: translateX(5px); }

.time-icon { font-size: 32px; }
.time-content { flex: 1; }
.time-content h4 { margin-bottom: 14px; color: #374151; font-size: 16px; }
.time-content ul, .plan-tips ul { margin: 0; padding: 0; }

.activity-item {
  list-style: none;
  background: white;
  padding: 12px 16px;
  border-radius: 12px;
  margin-bottom: 8px;
  border-left: 4px solid #667eea;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  color: #374151;
  line-height: 1.4;
  transition: all 0.2s;
  font-size: 14px;
}
.activity-item:hover {
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.empty-item {
  border-left-color: #9ca3af;
  color: #9ca3af;
  font-style: italic;
  background: #f3f4f6;
}

.games-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 14px;
}
.game-card {
  background: linear-gradient(135deg, #eef2ff, #e0e7ff);
  padding: 16px;
  border-radius: 16px;
  text-align: center;
  font-weight: 600;
  color: #1e40af;
  transition: all 0.2s;
  cursor: default;
  font-size: 14px;
}
.game-card:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.empty-game {
  background: #f3f4f6;
  color: #9ca3af;
  font-weight: normal;
}

.btn-print, .btn-generate {
  width: 100%;
  margin-top: 24px;
  padding: 14px;
  border: none;
  border-radius: 40px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}
.btn-generate {
  background: linear-gradient(135deg, #1a0a2e, #2d1b4e);
  color: white;
}
.btn-generate:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(26, 10, 46, 0.3);
}
.btn-print {
  background: #f3f4f6;
  color: #374151;
}
.btn-print:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.loading-plan, .no-plan {
  background: white;
  padding: 60px;
  border-radius: 24px;
  text-align: center;
  margin-top: 24px;
}
.spinner {
  width: 50px; height: 50px;
  border: 4px solid #e5e7eb;
  border-top-color: #1a0a2e;
  border-radius: 50%;
  margin: 0 auto 20px;
  animation: spin 1s linear infinite;
}
.no-plan-icon { font-size: 64px; margin-bottom: 14px; opacity: 0.5; }

@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 768px) {
  .action-plan { padding: 16px; }
  .time-block { flex-direction: column; text-align: center; }
  .time-content ul { text-align: left; }
  .games-grid { grid-template-columns: 1fr; }
  .activity-item { font-size: 13px; }
}
</style>