<template>
  <Layout>
    <div class="child-details-container">
      <NotificationToast ref="toast" />
      <ConfirmDialog ref="confirmDialog" />

      <!-- Header -->
      <div class="details-header">
        <button @click="goBack" class="back-btn">← Retour au tableau de bord</button>
        <div class="header-info">
          <div class="child-avatar">{{ child?.name?.charAt(0) || '👶' }}</div>
          <div>
            <h1 class="child-name">{{ child?.name }}</h1>
            <p class="child-meta">{{ child?.age }} ans · Créé le {{ formatDate(child?.createdAt || child?.created_at) }}</p>
          </div>
        </div>
      </div>

      <!-- Loading -->
      <div v-if="loading && !child" class="loading-state">
        <div class="spinner"></div>
        <p>Chargement des données...</p>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="error-state">
        <span class="error-icon">⚠️</span>
        <p>{{ error }}</p>
        <button @click="loadData" class="retry-btn">Réessayer</button>
      </div>

      <div v-else class="details-content">
        <div v-if="behaviors.length > 0" class="success-banner">
          ✅ {{ behaviors.length }} log(s) trouvé(s) !
        </div>

        <!-- Tabs -->
        <div class="tabs">
          <button @click="activeTab = 'timeline'" class="tab-btn" :class="{ active: activeTab === 'timeline' }">
            📋 Timeline
          </button>
          <button @click="activeTab = 'chart'" class="tab-btn" :class="{ active: activeTab === 'chart' }">
            📊 Progression
          </button>
          <button @click="activeTab = 'notes'" class="tab-btn" :class="{ active: activeTab === 'notes' }">
            📝 Notes
          </button>
          <button @click="activeTab = 'recommendations'" class="tab-btn" :class="{ active: activeTab === 'recommendations' }">
            🎯 Recommandations
          </button>
          <button @click="activeTab = 'actionplan'" class="tab-btn" :class="{ active: activeTab === 'actionplan' }">
            📋 Plan d'action
          </button>
          <button @click="activeTab = 'routine'" class="tab-btn" :class="{ active: activeTab === 'routine' }">
            📅 Routine
          </button>
        </div>

        <div v-if="activeTab === 'timeline'" class="tab-content timeline-tab">
          <div v-if="behaviors.length === 0" class="empty-state">
            <div class="empty-icon">📭</div>
            <p>Aucun log de comportement</p>
            <p class="empty-hint">Ajoutez des logs depuis le tableau de bord pour suivre la progression</p>
          </div>
          <div v-else class="timeline">
            <div v-for="log in behaviors" :key="log.id" class="timeline-item">
              <div class="timeline-marker" :style="{ background: getMoodColor(log.mood) }"></div>
              <div class="timeline-content">
                <div class="timeline-date">{{ formatTimelineDate(log.logDate) }}</div>
                <div class="timeline-stats">
                  <div class="stat-badge focus-badge">
                    <span class="stat-label-badge">🧠 Focus</span>
                    <strong class="stat-value-badge">{{ log.focusLevel }}/5</strong>
                  </div>
                  <div class="stat-badge mood-badge" :style="{ background: getMoodColor(log.mood) + '20', color: getMoodColor(log.mood) }">
                    <span class="stat-label-badge">{{ getMoodEmoji(log.mood) }} Mood</span>
                    <strong class="stat-value-badge">{{ log.mood }}</strong>
                  </div>
                  <div class="stat-badge sleep-badge">
                    <span class="stat-label-badge">😴 Sleep</span>
                    <strong class="stat-value-badge">{{ log.sleepHours }}h</strong>
                  </div>
                  <div class="stat-badge social-badge">
                    <span class="stat-label-badge">👥 Social</span>
                    <strong class="stat-value-badge">{{ log.socialInteraction }}/5</strong>
                  </div>
                </div>
                <p v-if="log.note" class="timeline-note">📝 {{ log.note }}</p>
              </div>
            </div>
          </div>
        </div>

        <div v-if="activeTab === 'chart'" class="tab-content chart-tab">
          <div class="chart-card">
            <h3>📊 Focus Level Progression</h3>
            <p class="chart-subtitle">Track focus improvement over time</p>
            <div class="chart-wrapper">
              <div v-if="behaviors.length === 0" class="chart-empty">
                <span>📭</span>
                <p>Pas encore de données</p>
              </div>
              <Line v-else :data="chartData" :options="chartOptions" />
            </div>
          </div>
          <div class="stats-summary">
            <div class="stat-summary-card">
              <div class="stat-icon">📊</div>
              <div class="stat-info">
                <div class="stat-label">TOTAL LOGS</div>
                <div class="stat-number">{{ behaviors.length }}</div>
              </div>
            </div>
            <div class="stat-summary-card">
              <div class="stat-icon">🧠</div>
              <div class="stat-info">
                <div class="stat-label">AVERAGE FOCUS</div>
                <div class="stat-number">{{ calculateAverageFocus() }}/5</div>
              </div>
            </div>
            <div class="stat-summary-card">
              <div class="stat-icon">😴</div>
              <div class="stat-info">
                <div class="stat-label">AVERAGE SLEEP</div>
                <div class="stat-number">{{ calculateAverageSleep() }}h</div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="activeTab === 'notes'" class="tab-content notes-tab">
          <!-- Bouton Ajouter - visible SEULEMENT pour le psychologue assigné -->
          <div v-if="isPsychologist && isPsychologistAssignedToChild" class="add-section">
            <button @click="showNoteForm = !showNoteForm" class="btn-add-note">
              {{ showNoteForm ? '− Annuler' : '+ Ajouter une note' }}
            </button>

            <transition name="fade">
              <div v-if="showNoteForm" class="note-form">
                <div class="form-group">
                  <label>Date de la session</label>
                  <input v-model="noteForm.sessionDate" type="date" class="form-input" />
                </div>
                <div class="form-group">
                  <label>Note</label>
                  <textarea
                    v-model="noteForm.note"
                    rows="4"
                    placeholder="Écrivez vos observations..."
                    class="form-textarea"
                  ></textarea>
                </div>
                <button @click="addNote" class="btn-save" :disabled="loading">
                  💾 Enregistrer
                </button>
              </div>
            </transition>
          </div>

          <!-- Message pour le parent/teacher qui ne peut pas ajouter -->
          <div v-else-if="!isPsychologist && notes.length === 0 && !notesLoading" class="info-message">
            <div class="info-icon">📝</div>
            <p>Les notes sont ajoutées par le psychologue assigné à cet enfant.</p>
            <p class="info-hint">Aucune note disponible pour le moment.</p>
          </div>

          <div v-if="notesLoading" class="loading-inline">
            <div class="spinner-small"></div> Chargement des notes...
          </div>

          <div v-else-if="notes.length === 0 && !notesLoading" class="empty-state">
            <div class="empty-icon">📝</div>
            <p>Aucune note disponible</p>
            <p class="empty-hint" v-if="isPsychologist && isPsychologistAssignedToChild">
              Cliquez sur "+ Ajouter une note" pour commencer
            </p>
            <p class="empty-hint" v-else>
              Le psychologue pourra ajouter des notes lors de ses suivis
            </p>
          </div>

          <div v-else class="notes-list">
            <div v-for="note in notes" :key="note.id" class="note-card">
              <div class="note-header">
                <span class="note-icon">📋</span>
                <div>
                  <div class="note-date">{{ formatDate(note.sessionDate) }}</div>
                  <div class="note-author">
                    Ajouté par {{ note.psychologist?.name || note.psychologistName || 'Psychologue' }}
                  </div>
                </div>
                <!-- Bouton Supprimer - visible SEULEMENT pour le psychologue assigné -->
                <button 
                  v-if="isPsychologist && isPsychologistAssignedToChild" 
                  @click="deleteNote(note)" 
                  class="note-delete-btn" 
                  title="Supprimer"
                >
                  🗑️
                </button>
              </div>
              <div class="note-content">{{ note.note }}</div>
            </div>
          </div>
        </div>

        <div v-if="activeTab === 'recommendations'" class="tab-content recommendations-tab">
          <!-- Bouton Ajouter - visible SEULEMENT pour le psychologue assigné -->
          <div v-if="isPsychologist && isPsychologistAssignedToChild" class="add-section">
            <button @click="showRecommendationForm = !showRecommendationForm" class="btn-add-recommendation">
              {{ showRecommendationForm ? '− Annuler' : '+ Ajouter une recommandation' }}
            </button>

            <transition name="fade">
              <div v-if="showRecommendationForm" class="recommendation-form">
                <div class="form-group">
                  <label>Titre</label>
                  <input
                    v-model="recommendationForm.title"
                    type="text"
                    placeholder="ex: Méditation matinale"
                    class="form-input"
                  />
                </div>
                <div class="form-group">
                  <label>Catégorie</label>
                  <select v-model="recommendationForm.category" class="form-select">
                    <option value="focus">🧠 Focus</option>
                    <option value="social">👥 Social</option>
                    <option value="relaxation">🧘 Relaxation</option>
                    <option value="routine">📅 Routine</option>
                    <option value="sleep">😴 Sommeil</option>
                    <option value="nutrition">🍎 Nutrition</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>Description</label>
                  <textarea
                    v-model="recommendationForm.description"
                    rows="3"
                    placeholder="Décrivez l'activité recommandée..."
                    class="form-textarea"
                  ></textarea>
                </div>
                <button @click="addRecommendation" class="btn-save" :disabled="loading">
                  💾 Ajouter
                </button>
              </div>
            </transition>
          </div>

          <!-- Message pour le parent/teacher quand il n'y a pas de recommandations -->
          <div v-else-if="!isPsychologist && recommendations.length === 0 && !recommendationsLoading" class="info-message">
            <div class="info-icon">🎯</div>
            <p>Les recommandations sont ajoutées par le psychologue assigné à cet enfant.</p>
            <p class="info-hint">Aucune recommandation disponible pour le moment.</p>
          </div>

          <div v-if="recommendationsLoading" class="loading-inline">
            <div class="spinner-small"></div> Chargement des recommandations...
          </div>

          <div v-else-if="recommendations.length === 0 && !recommendationsLoading && !(!isPsychologist && recommendations.length === 0)" class="empty-state">
            <div class="empty-icon">🎯</div>
            <p>Aucune recommandation disponible</p>
            <p class="empty-hint" v-if="isPsychologist && isPsychologistAssignedToChild">
              Cliquez sur "+ Ajouter une recommandation" pour commencer
            </p>
            <p class="empty-hint" v-else>
              Le psychologue pourra ajouter des recommandations personnalisées
            </p>
          </div>

          <div v-else class="recommendations-list">
            <div
              v-for="rec in recommendations"
              :key="rec.id"
              class="recommendation-card"
              :class="{ completed: rec.isCompleted }"
            >
              <div class="rec-header">
                <div class="rec-icon" :style="{ background: getCategoryColor(rec.category) }">
                  {{ getCategoryIcon(rec.category) }}
                </div>
                <div class="rec-info">
                  <h4>{{ rec.title }}</h4>
                  <span class="rec-category">{{ rec.category }}</span>
                </div>
                <div class="rec-actions">
                  <!-- Toggle - visible pour tout le monde (parent, teacher, psychologue) -->
                  <button
                    @click="toggleRecommendation(rec)"
                    class="rec-toggle-btn"
                    :class="{ completed: rec.isCompleted }"
                  >
                    {{ rec.isCompleted ? '✓ Complété' : '○ À faire' }}
                  </button>
                  
                  <!-- Bouton Supprimer - visible SEULEMENT pour le psychologue assigné -->
                  <button 
                    v-if="isPsychologist && isPsychologistAssignedToChild" 
                    @click="deleteRecommendation(rec)" 
                    class="rec-delete-btn" 
                    title="Supprimer"
                  >
                    🗑️
                  </button>
                </div>
              </div>
              <div class="rec-description">{{ rec.description }}</div>
              <div class="rec-date">
                Ajouté le {{ formatDate(rec.createdAt) }}
                <span v-if="rec.psychologistName" class="rec-author"> par {{ rec.psychologistName }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- ==================== PLAN D'ACTION ==================== -->
        <div v-if="activeTab === 'actionplan'" class="tab-content actionplan-tab">
          <div class="actionplan-header">
            <h3>📋 Plan d'action personnalisé</h3>
            <button @click="regeneratePlan" class="btn-regenerate" :disabled="generatingPlan">
              {{ generatingPlan ? '🔄 Génération...' : '🔄 Régénérer le plan' }}
            </button>
          </div>
          <ActionPlan ref="actionPlan" :child-id="childId" />
        </div>

        <!-- ==================== ROUTINE ==================== -->
        <div v-if="activeTab === 'routine'" class="tab-content routine-tab">
          <RoutinePlanner :child-id="childId" />
        </div>

        <!-- Message Parent -->
        <div v-if="child?.parent && !isParent" class="message-parent-section">
          <button @click="messageParent" class="btn-message-parent">💬 Message au parent</button>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api/api'
import Layout from '@/layouts/Layout.vue'
import { Line } from 'vue-chartjs'
import {
  Chart as ChartJS, Title, Tooltip, Legend,
  LineElement, CategoryScale, LinearScale, PointElement, Filler
} from 'chart.js'
import RoutinePlanner from '@/components/RoutinePlanner.vue'
import ActionPlan from '@/components/ActionPlan.vue'
import NotificationToast from '@/components/NotificationToast.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'

ChartJS.register(Title, Tooltip, Legend, LineElement, CategoryScale, LinearScale, PointElement, Filler)

const route = useRoute()
const router = useRouter()
const childId = route.params.id

// Refs composants
const toast = ref(null)
const confirmDialog = ref(null)
const actionPlan = ref(null)

// Données
const child = ref(null)
const behaviors = ref([])
const notes = ref([])
const recommendations = ref([])

// États de chargement
const loading = ref(false)
const notesLoading = ref(false)
const recommendationsLoading = ref(false)
const error = ref(null)
const generatingPlan = ref(false)

// UI
const activeTab = ref('timeline')

// Formulaires
const showNoteForm = ref(false)
const showRecommendationForm = ref(false)

const noteForm = ref({
  note: '',
  sessionDate: new Date().toISOString().split('T')[0]
})

const recommendationForm = ref({
  title: '',
  description: '',
  category: 'focus'
})

// Utilisateur
const user = computed(() => {
  const userData = JSON.parse(localStorage.getItem('user') || '{}')
  if (userData.role) userData.role = userData.role.toLowerCase()
  return userData
})

const isPsychologist = computed(() => user.value?.role === 'psychologist')
const isParent = computed(() => user.value?.role === 'parent')
const isTeacher = computed(() => user.value?.role === 'teacher')

// Vérifier si le psychologue connecté est bien assigné à cet enfant
const isPsychologistAssignedToChild = computed(() => {
  if (!isPsychologist.value || !child.value) return false
  return child.value.psychologistId === user.value?.id || 
         child.value.psychologist?.id === user.value?.id
})

// Notifications
const showNotification = (message, type = 'success', title = '') => {
  const titles = { success: 'Succès', error: 'Erreur', warning: 'Attention', info: 'Information' }
  toast.value?.addNotification(type, title || titles[type], message, 4000)
}

// Chart
const chartData = ref({
  labels: [],
  datasets: [{
    label: 'Focus Level',
    data: [],
    borderColor: '#4F8EF7',
    backgroundColor: 'rgba(79, 142, 247, 0.1)',
    tension: 0.3,
    fill: true,
    pointBackgroundColor: '#4F8EF7',
    pointBorderColor: 'white',
    pointRadius: 4,
    pointHoverRadius: 6
  }]
})

const chartOptions = {
  responsive: true,
  maintainAspectRatio: true,
  plugins: {
    legend: { position: 'top' },
    tooltip: { mode: 'index', intersect: false }
  },
  scales: {
    y: { 
      min: 0, 
      max: 5, 
      title: { display: true, text: 'Focus Level' }, 
      ticks: { stepSize: 1 } 
    },
    x: { 
      title: { display: true, text: 'Date' }, 
      grid: { display: false } 
    }
  }
}

// Calculs stats
const calculateAverageSleep = () => {
  if (!behaviors.value.length) return '0.0'
  let total = 0, count = 0
  behaviors.value.forEach(log => {
    const v = parseFloat(log.sleepHours)
    if (!isNaN(v) && v > 0) { total += v; count++ }
  })
  return count > 0 ? (total / count).toFixed(1) : '0.0'
}

const calculateAverageFocus = () => {
  if (!behaviors.value.length) return '0.0'
  let total = 0, count = 0
  behaviors.value.forEach(log => {
    const v = parseInt(log.focusLevel)
    if (!isNaN(v)) { total += v; count++ }
  })
  return count > 0 ? (total / count).toFixed(1) : '0.0'
}

const formatTimelineDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' })
}

// Chargement principal
const loadData = async () => {
  if (!childId) return
  loading.value = true
  error.value = null

  try {
    const [childRes, behaviorsRes] = await Promise.all([
      api.get(`/children/${childId}`),
      api.get(`/logs/${childId}`)
    ])

    child.value = childRes.data
    behaviors.value = behaviorsRes.data || []

    if (behaviors.value.length > 0) {
      const sorted = [...behaviors.value].sort((a, b) => new Date(a.logDate) - new Date(b.logDate))
      chartData.value.labels = sorted.map(log => {
        return new Date(log.logDate).toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' })
      })
      chartData.value.datasets[0].data = sorted.map(log => log.focusLevel)
    }

    await Promise.all([loadNotes(), loadRecommendations()])

    showNotification('Données chargées avec succès', 'success')
  } catch (err) {
    console.error('Erreur loadData:', err)
    error.value = 'Impossible de charger les données'
    showNotification('Impossible de charger les données', 'error')
  } finally {
    loading.value = false
  }
}

const loadNotes = async () => {
  console.time('⏱️ loadNotes')
  notesLoading.value = true
  try {
    const res = await api.get(`/children/${childId}/notes`, {
      timeout: 60000
    })
    console.log('📥 Réponse brute:', res.data)
    
    if (Array.isArray(res.data)) {
      notes.value = res.data.map(item => ({
        id: item.id,
        note: item.note,
        sessionDate: item.sessionDate,
        createdAt: item.createdAt,
        psychologistName: item.psychologistName,
        psychologistId: item.psychologistId
      }))
    } else {
      notes.value = []
    }
    
    console.log('✅ Notes chargées:', notes.value.length)
    console.timeEnd('⏱️ loadNotes')
  } catch (e) {
    console.error('❌ Erreur notes:', e)
    notes.value = []
  } finally {
    notesLoading.value = false
  }
}




const loadRecommendations = async () => {
  console.time('⏱️ loadRecommendations')
  recommendationsLoading.value = true
  try {
    const res = await api.get(`/children/${childId}/recommendations`, {
      timeout: 60000
    })
    console.log('📥 Réponse brute recommandations:', res.data)
    
    if (Array.isArray(res.data)) {
      recommendations.value = res.data.map(item => ({
        id: item.id,
        title: item.title,
        description: item.description,
        category: item.category,
        isCompleted: item.completed,
        createdAt: item.createdAt,
        authorName: item.authorName
      }))
    } else {
      recommendations.value = []
    }
    
    console.log('✅ Recommandations chargées:', recommendations.value.length)
    console.timeEnd('⏱️ loadRecommendations')
  } catch (e) {
    console.error(' Erreur recommandations:', e)
    recommendations.value = []
  } finally {
    recommendationsLoading.value = false
  }
}

const addNote = async () => {
  console.log('🟢 addNote appelée');
  console.log('isPsychologist:', isPsychologist.value);
  console.log('isPsychologistAssignedToChild:', isPsychologistAssignedToChild.value);
  
  if (!isPsychologist.value || !isPsychologistAssignedToChild.value) {
    console.log('🔴 Permission refusée');
    showNotification('Vous n\'êtes pas autorisé à ajouter des notes pour cet enfant', 'error');
    return;
  }

  if (!noteForm.value.note.trim()) {
    console.log('🔴 Note vide');
    showNotification('Veuillez saisir une note', 'error');
    return;
  }
  
  console.log('🟢 Envoi de la requête...');
  loading.value = true;
  
  try {
    const payload = {
      note: noteForm.value.note,
      sessionDate: noteForm.value.sessionDate
    };
    console.log('📤 Payload:', payload);
    
    const response = await api.post(`/children/${childId}/notes`, payload);
    console.log('🟢 Réponse reçue:', response);
    console.log('🟢 Status:', response.status);
    console.log('🟢 Data:', response.data);
    
    showNotification('Note ajoutée avec succès !', 'success');
    noteForm.value = { note: '', sessionDate: new Date().toISOString().split('T')[0] };
    showNoteForm.value = false;
    await loadNotes();
  } catch (err) {
    console.error('🔴 Erreur ajout note:', err);
    console.error('🔴 Message:', err.message);
    console.error('🔴 Response:', err.response);
    if (err.response?.data?.error) {
      showNotification(err.response.data.error, 'error');
    } else {
      showNotification('Erreur lors de l\'ajout de la note', 'error');
    }
  } finally {
    loading.value = false;
  }
};

const deleteNote = async (note) => {
  if (!isPsychologist.value || !isPsychologistAssignedToChild.value) {
    showNotification('Vous n\'êtes pas autorisé à supprimer cette note', 'error')
    return
  }

  const confirmed = await confirmDialog.value?.show({
    title: 'Supprimer la note',
    message: 'Êtes-vous sûr de vouloir supprimer cette note ?',
    type: 'danger',
    confirmText: 'Supprimer',
    cancelText: 'Annuler'
  })
  if (!confirmed) return
  
  try {
    await api.delete(`/notes/${note.id}`)
    showNotification('Note supprimée !', 'success')
    notes.value = notes.value.filter(n => n.id !== note.id)
  } catch (err) {
    console.error('Erreur suppression note:', err)
    showNotification('Erreur lors de la suppression', 'error')
  }
}

// ==================== RECOMMANDATIONS (avec vérification de permission) ====================
const addRecommendation = async () => {
  if (!isPsychologist.value || !isPsychologistAssignedToChild.value) {
    showNotification('Vous n\'êtes pas autorisé à ajouter des recommandations pour cet enfant', 'error')
    return
  }

  if (!recommendationForm.value.title.trim() || !recommendationForm.value.description.trim()) {
    showNotification('Veuillez remplir tous les champs', 'error')
    return
  }
  
  loading.value = true
  try {
    await api.post(`/children/${childId}/recommendations`, {
      title: recommendationForm.value.title,
      description: recommendationForm.value.description,
      category: recommendationForm.value.category
    })
    showNotification('Recommandation ajoutée !', 'success')
    recommendationForm.value = { title: '', description: '', category: 'focus' }
    showRecommendationForm.value = false
    await loadRecommendations()
  } catch (err) {
    console.error('Erreur ajout recommandation:', err)
    showNotification(err.response?.data?.error || 'Erreur lors de l\'ajout', 'error')
  } finally {
    loading.value = false
  }
}

const toggleRecommendation = async (rec) => {
  try {
    const response = await api.put(`/recommendations/${rec.id}/toggle`)
    rec.isCompleted = response.data.isCompleted
    showNotification(
      rec.isCompleted ? '✅ Recommandation complétée !' : '🔄 Recommandation réouverte',
      'success'
    )
  } catch (err) {
    console.error('Erreur toggle:', err)
    showNotification('Erreur lors de la mise à jour', 'error')
  }
}

const deleteRecommendation = async (rec) => {
  if (!isPsychologist.value || !isPsychologistAssignedToChild.value) {
    showNotification('Vous n\'êtes pas autorisé à supprimer cette recommandation', 'error')
    return
  }

  const confirmed = await confirmDialog.value?.show({
    title: 'Supprimer la recommandation',
    message: `Êtes-vous sûr de vouloir supprimer "${rec.title}" ?`,
    type: 'danger',
    confirmText: 'Supprimer',
    cancelText: 'Annuler'
  })
  if (!confirmed) return
  
  try {
    await api.delete(`/recommendations/${rec.id}`)
    showNotification('Recommandation supprimée !', 'success')
    recommendations.value = recommendations.value.filter(r => r.id !== rec.id)
  } catch (err) {
    console.error('Erreur suppression recommandation:', err)
    showNotification('Erreur lors de la suppression', 'error')
  }
}

// ==================== PLAN D'ACTION ====================
const regeneratePlan = () => {
  if (!actionPlan.value) return
  generatingPlan.value = true
  actionPlan.value.generatePlan()
  setTimeout(() => { generatingPlan.value = false }, 3000)
  showNotification('Génération du plan en cours...', 'info')
}

// ==================== DIVERS ====================
const messageParent = () => {
  if (child.value?.parent) {
    router.push({
      path: '/messages',
      query: {
        contactId: child.value.parent.id,
        contactName: child.value.parent.name,
        contactRole: 'parent'
      }
    })
  } else {
    showNotification('Aucun parent assigné', 'error')
  }
}

const goBack = () => router.push('/dashboard')

const formatDate = (date) => {
  if (!date) return 'N/A'
  try {
    const d = new Date(date)
    if (isNaN(d.getTime())) return 'N/A'
    return d.toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' })
  } catch (e) { return 'N/A' }
}

const getCategoryIcon = (cat) => {
  const icons = { focus: '🧠', social: '👥', relaxation: '🧘', routine: '📅', sleep: '😴', nutrition: '🍎' }
  return icons[cat] || '📌'
}

const getCategoryColor = (cat) => {
  const colors = { focus: '#667eea', social: '#10b981', relaxation: '#8b5cf6', routine: '#f59e0b', sleep: '#3b82f6', nutrition: '#ef4444' }
  return colors[cat] || '#6b7280'
}

const getMoodEmoji = (mood) => {
  const emojis = { happy: '😊', sad: '😢', angry: '😡', neutral: '😐', excited: '🤩', anxious: '😰' }
  return emojis[mood] || '😊'
}

const getMoodColor = (mood) => {
  const colors = { happy: '#10b981', sad: '#3b82f6', angry: '#ef4444', neutral: '#6b7280', excited: '#f59e0b', anxious: '#8b5cf6' }
  return colors[mood] || '#6b7280'
}

onMounted(() => { loadData() })
</script>

<style scoped>
.child-details-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
}

.success-banner {
  background: #d1fae5;
  color: #065f46;
  padding: 12px;
  border-radius: 12px;
  margin-bottom: 20px;
  text-align: center;
}

.details-header { margin-bottom: 30px; }

.back-btn {
  background: none;
  border: none;
  color: #667eea;
  font-size: 14px;
  cursor: pointer;
  padding: 8px 0;
  margin-bottom: 20px;
  transition: transform 0.2s;
}
.back-btn:hover { transform: translateX(-3px); color: #764ba2; }

.header-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.child-avatar {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #1a0a2e, #2d1b4e);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  font-weight: 700;
  color: white;
}

.child-name { font-size: 28px; font-weight: 700; color: #1f2937; margin-bottom: 8px; }
.child-meta { color: #6b7280; font-size: 14px; }

/* Tabs */
.tabs {
  display: flex;
  gap: 12px;
  border-bottom: 2px solid #e0e6ed;
  margin-bottom: 24px;
  padding-bottom: 12px;
  flex-wrap: wrap;
}

.tab-btn {
  background: none;
  border: none;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  border-radius: 30px;
  color: #6b7280;
  transition: all 0.3s;
}
.tab-btn:hover  { background: #f3f4f6; }
.tab-btn.active { background: linear-gradient(135deg, #1a0a2e, #2d1b4e); color: white; }

/* Timeline */
.timeline { position: relative; padding-left: 30px; }
.timeline::before {
  content: '';
  position: absolute;
  left: 10px; top: 0; bottom: 0;
  width: 2px;
  background: linear-gradient(180deg, #667eea, #764ba2);
}
.timeline-item { position: relative; margin-bottom: 24px; }
.timeline-marker {
  position: absolute;
  left: -26px; top: 5px;
  width: 14px; height: 14px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 0 0 2px #e0e6ed;
}
.timeline-content {
  background: white;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}
.timeline-date { 
  font-size: 13px; 
  font-weight: 600;
  color: #1a0a2e; 
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #e0e6ed;
}
.timeline-stats { 
  display: flex; 
  gap: 16px; 
  flex-wrap: wrap;
}
.stat-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  padding: 6px 12px;
  background: #f3f4f6;
  border-radius: 30px;
}
.stat-label-badge { font-size: 12px; color: #6b7280; }
.stat-value-badge { font-size: 14px; font-weight: 700; color: #1f2937; }
.focus-badge .stat-value-badge { color: #667eea; }
.sleep-badge .stat-value-badge { color: #10b981; }
.social-badge .stat-value-badge { color: #f59e0b; }
.timeline-note {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  font-size: 13px;
  color: #6b7280;
}

/* Chart */
.chart-card {
  background: white;
  border-radius: 20px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}
.chart-card h3 { font-size: 18px; margin-bottom: 8px; }
.chart-subtitle { font-size: 13px; color: #9ca3af; margin-bottom: 20px; }
.chart-wrapper { min-height: 350px; }
.chart-empty { text-align: center; padding: 60px; color: #9ca3af; }
.chart-empty span { font-size: 48px; display: block; margin-bottom: 16px; }

.stats-summary { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin-top: 20px; }
.stat-summary-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  transition: transform 0.2s;
}
.stat-summary-card:hover { transform: translateY(-3px); }
.stat-icon { font-size: 40px; }
.stat-info { flex: 1; }
.stat-label { font-size: 11px; color: #9ca3af; text-transform: uppercase; letter-spacing: 0.5px; }
.stat-number { font-size: 24px; font-weight: 700; color: #1f2937; }

/* Section add */
.add-section { margin-bottom: 24px; }

.btn-add-note,
.btn-add-recommendation {
  background: linear-gradient(135deg, #1a0a2e, #2d1b4e);
  color: white;
  padding: 12px 24px;
  border: none;
  border-radius: 40px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}
.btn-add-note:hover,
.btn-add-recommendation:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(26, 10, 46, 0.3);
}

.note-form,
.recommendation-form {
  background: white;
  border-radius: 20px;
  padding: 24px;
  margin-top: 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}

/* Loading inline */
.loading-inline {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #6b7280;
  font-size: 14px;
  padding: 20px 0;
}
.spinner-small {
  width: 20px; height: 20px;
  border: 2px solid #e0e6ed;
  border-top-color: #1a0a2e;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

/* Notes */
.notes-list { display: flex; flex-direction: column; gap: 16px; }
.note-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  transition: all 0.2s;
}
.note-card:hover { transform: translateY(-2px); box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
.note-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.note-icon { font-size: 32px; }
.note-date   { font-weight: 600; color: #1f2937; font-size: 14px; }
.note-author { font-size: 11px; color: #9ca3af; }
.note-content { font-size: 14px; color: #4b5563; line-height: 1.5; }
.note-delete-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  opacity: 0.5;
  margin-left: auto;
  transition: all 0.2s;
}
.note-delete-btn:hover { opacity: 1; color: #ef4444; }

/* Recommandations */
.recommendations-list { display: flex; flex-direction: column; gap: 16px; }
.recommendation-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  transition: all 0.2s;
}
.recommendation-card:hover { transform: translateY(-2px); box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
.recommendation-card.completed { opacity: 0.7; background: #f9fafb; }
.rec-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.rec-icon {
  width: 48px; height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}
.rec-info { flex: 1; }
.rec-info h4 { font-size: 16px; font-weight: 600; margin-bottom: 4px; }
.rec-category { font-size: 11px; background: #f3f4f6; padding: 2px 8px; border-radius: 20px; color: #6b7280; }
.rec-actions { display: flex; gap: 8px; }
.rec-toggle-btn {
  padding: 6px 14px;
  border: none;
  border-radius: 30px;
  font-size: 12px;
  cursor: pointer;
  background: #f3f4f6;
  color: #6b7280;
  transition: all 0.2s;
}
.rec-toggle-btn.completed { background: #d1fae5; color: #065f46; }
.rec-toggle-btn:hover { transform: scale(1.02); }
.rec-delete-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  opacity: 0.5;
  transition: all 0.2s;
}
.rec-delete-btn:hover { opacity: 1; color: #ef4444; }
.rec-description { font-size: 14px; color: #4b5563; margin-bottom: 12px; line-height: 1.5; }
.rec-date { font-size: 11px; color: #9ca3af; }
.rec-author { font-size: 11px; color: #9ca3af; margin-left: 6px; font-weight: normal; }

/* Plan d'action */
.actionplan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}
.btn-regenerate {
  background: linear-gradient(135deg, #1a0a2e, #2d1b4e);
  color: white;
  padding: 8px 20px;
  border: none;
  border-radius: 30px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}
.btn-regenerate:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(26,10,46,0.3); }
.btn-regenerate:disabled { opacity: 0.6; cursor: not-allowed; }

/* Message parent */
.message-parent-section { margin-top: 32px; text-align: center; }
.btn-message-parent {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
  padding: 14px 28px;
  border: none;
  border-radius: 40px;
  font-weight: 600;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}
.btn-message-parent:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(16,185,129,0.3); }

/* Info message */
.info-message {
  text-align: center;
  padding: 40px 20px;
  background: #eff6ff;
  border-radius: 20px;
  margin-bottom: 20px;
}
.info-icon {
  font-size: 48px;
  margin-bottom: 16px;
}
.info-message p {
  color: #1e40af;
  margin: 4px 0;
}
.info-hint {
  font-size: 13px;
  opacity: 0.7;
}

/* Forms */
.form-group { margin-bottom: 20px; }
.form-group label { display: block; font-size: 13px; font-weight: 600; color: #4b5563; margin-bottom: 8px; }
.form-input, .form-select, .form-textarea {
  width: 100%;
  padding: 12px 14px;
  border: 2px solid #e0e6ed;
  border-radius: 12px;
  font-size: 14px;
  transition: all 0.2s;
  box-sizing: border-box;
}
.form-input:focus, .form-select:focus, .form-textarea:focus {
  outline: none;
  border-color: #1a0a2e;
}
.btn-save {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
  padding: 12px 24px;
  border: none;
  border-radius: 40px;
  font-weight: 600;
  cursor: pointer;
  width: 100%;
  transition: all 0.3s;
}
.btn-save:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(16,185,129,0.3); }
.btn-save:disabled { opacity: 0.6; cursor: not-allowed; }

/* États généraux */
.loading-state, .error-state, .empty-state {
  text-align: center;
  padding: 60px;
  background: white;
  border-radius: 24px;
}
.spinner {
  width: 50px; height: 50px;
  border: 3px solid #e0e6ed;
  border-top-color: #1a0a2e;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}
@keyframes spin { to { transform: rotate(360deg); } }
.empty-icon { font-size: 64px; margin-bottom: 16px; opacity: 0.5; }
.empty-hint { font-size: 13px; color: #9ca3af; margin-top: 8px; }
.retry-btn { margin-top: 20px; padding: 10px 24px; background: #667eea; color: white; border: none; border-radius: 30px; cursor: pointer; }

/* Transitions */
.fade-enter-active, .fade-leave-active { transition: opacity 0.25s, transform 0.25s; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateY(-8px); }

/* Responsive */
@media (max-width: 768px) {
  .stats-summary { grid-template-columns: 1fr; }
  .timeline-stats { flex-direction: column; gap: 8px; }
  .header-info { flex-direction: column; text-align: center; }
  .tabs { justify-content: center; }
  .rec-header { flex-direction: column; text-align: center; }
  .note-header { flex-wrap: wrap; }
  .actionplan-header { flex-direction: column; align-items: flex-start; }
}
</style>