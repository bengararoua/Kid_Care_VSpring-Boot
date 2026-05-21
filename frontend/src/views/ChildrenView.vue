<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/api'
import Layout from '@/layouts/Layout.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import NotificationToast from '@/components/NotificationToast.vue'
import ActionPlan from '@/components/ActionPlan.vue'
import RoutinePlanner from '@/components/RoutinePlanner.vue'
import { Line } from 'vue-chartjs'
import { Chart as ChartJS, Title, Tooltip, Legend, LineElement, CategoryScale, LinearScale, PointElement, Filler } from 'chart.js'

ChartJS.register(Title, Tooltip, Legend, LineElement, CategoryScale, LinearScale, PointElement, Filler)

const router = useRouter()

const children = ref([])
const psychologists = ref([])
const teachers = ref([])
const parents = ref([])
const showModal = ref(false)
const editingChild = ref(null)
const loading = ref(false)
const error = ref(null)
const success = ref(null)
const selectedPsychologist = ref(null)
const showPsychologistModal = ref(false)

// Modal Détails
const showDetailsModal = ref(false)
const selectedChild = ref(null)
const childBehaviors = ref([])
const childNotes = ref([])
const childRecommendations = ref([])
const detailsLoading = ref(false)
const detailsActiveTab = ref('timeline')

// Référence ActionPlan
const actionPlanRef = ref(null)

// Formulaire
const form = ref({
  name: '',
  age: '',
  parent_id: '',
  psychologist_id: '',
  teacher_id: '',
  notes: ''
})

// Utilisateur connecté - Normalisation du rôle
const user = computed(() => {
  const userData = JSON.parse(localStorage.getItem('user') || '{}')
  if (userData.role) {
    userData.role = userData.role.toLowerCase()
  }
  return userData
})

const isParent = computed(() => user.value?.role === 'parent')
const isPsychologist = computed(() => user.value?.role === 'psychologist')
const isTeacher = computed(() => user.value?.role === 'teacher')

// Références pour les notifications
const toast = ref(null)
const confirmDialog = ref(null)

// Données pour le graphique
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
    y: { min: 0, max: 5, title: { display: true, text: 'Focus Level' }, ticks: { stepSize: 1 } },
    x: { title: { display: true, text: 'Date' }, grid: { display: false } }
  }
}

// Notifications
const showNotification = (message, type = 'success', title = '') => {
  const titles = {
    success: 'Succès',
    error: 'Erreur',
    warning: 'Attention',
    info: 'Information'
  }
  toast.value?.addNotification(type, title || titles[type] || 'Notification', message, 4000)
}

// Export PDF
const exportPDF = async (childId) => {
  try {
    const response = await api.get(`/export/child/${childId}`, {
      responseType: 'blob',
      headers: { 'Accept': 'application/pdf' }
    })
    const blob = new Blob([response.data], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `rapport_enfant_${childId}.pdf`)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
    showNotification('PDF exporté avec succès !', 'success')
  } catch (err) {
    console.error('Export error:', err)
    showNotification('Erreur lors de l\'export du PDF', 'error')
  }
}

// Charger les enfants
const loadChildren = async () => {
  try {
    loading.value = true
    const res = await api.get('/children')
    children.value = res.data
    console.log('✅ Enfants chargés:', children.value.length)
  } catch (err) {
    console.error('Error loading children:', err)
    showNotification('Impossible de charger les enfants', 'error')
  } finally {
    loading.value = false
  }
}

// Charger les psychologues
const loadPsychologists = async () => {
  try {
    const res = await api.get('/users/psychologists')
    psychologists.value = res.data
  } catch (err) {
    console.error('Error loading psychologists:', err)
  }
}

// Charger les teachers
const loadTeachers = async () => {
  try {
    const res = await api.get('/users/teachers')
    teachers.value = res.data
  } catch (err) {
    console.error('Error loading teachers:', err)
  }
}

// Charger les parents (pour le teacher)
const loadParents = async () => {
  try {
    const res = await api.get('/users/parents')
    parents.value = res.data
  } catch (err) {
    console.error('Error loading parents:', err)
  }
}

// Voir les infos du psychologue
const viewPsychologistInfo = async (child) => {
  try {
    const res = await api.get(`/children/${child.id}/psychologist`)
    selectedPsychologist.value = res.data
    showPsychologistModal.value = true
  } catch (err) {
    showNotification('Aucun psychologue assigné', 'error')
  }
}

// Envoyer un message au parent (pour psychologue)
const messageParent = (child) => {
  if (child.parent && child.parent.id) {
    router.push({
      path: '/messages',
      query: {
        contactId: child.parent.id,
        contactName: child.parent.name,
        contactRole: 'parent'
      }
    })
  } else if (child.parentId && child.parentId > 0) {
    router.push({
      path: '/messages',
      query: {
        contactId: child.parentId,
        contactName: child.parentName,
        contactRole: 'parent'
      }
    })
  } else {
    showNotification('Aucun parent assigné à cet enfant', 'error')
  }
}

// ========== MODAL DÉTAILS ==========
const openDetailsModal = async (child) => {
  selectedChild.value = child
  showDetailsModal.value = true
  detailsActiveTab.value = 'timeline'
  await loadChildDetails(child.id)
}

const closeDetailsModal = () => {
  showDetailsModal.value = false
  selectedChild.value = null
  childBehaviors.value = []
  childNotes.value = []
  childRecommendations.value = []
  detailsActiveTab.value = 'timeline'
}

const loadChildDetails = async (childId) => {
  detailsLoading.value = true
  try {
    const [behaviorsRes, notesRes, recommendationsRes] = await Promise.all([
      api.get(`/logs/${childId}`).catch(() => ({ data: [] })),
      api.get(`/children/${childId}/notes`).catch(() => ({ data: [] })),
      api.get(`/children/${childId}/recommendations`).catch(() => ({ data: [] }))
    ])

    childBehaviors.value = behaviorsRes.data || []
    childNotes.value = notesRes.data || []
    childRecommendations.value = recommendationsRes.data || []

    // Préparer les données du graphique
    if (childBehaviors.value.length > 0) {
      const sortedLogs = [...childBehaviors.value].reverse()
      chartData.value = {
        labels: sortedLogs.map(log => {
          const date = new Date(log.log_date)
          return date.toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' })
        }),
        datasets: [{
          label: 'Focus Level',
          data: sortedLogs.map(log => log.focus_level),
          borderColor: '#4F8EF7',
          backgroundColor: 'rgba(79, 142, 247, 0.1)',
          tension: 0.3,
          fill: true,
          pointBackgroundColor: '#4F8EF7',
          pointBorderColor: 'white',
          pointRadius: 4,
          pointHoverRadius: 6
        }]
      }
    }
  } catch (err) {
    console.error('Error loading child details:', err)
    showNotification('Erreur lors du chargement des détails', 'error')
  } finally {
    detailsLoading.value = false
  }
}

// Marquer une recommandation comme complétée — URL corrigée (sans /api/ préfixe)
const toggleRecommendation = async (rec) => {
  try {
    const response = await api.put(`/recommendations/${rec.id}/toggle`)
    rec.is_completed = response.data.is_completed
    showNotification(rec.is_completed ? '✅ Recommandation complétée !' : '🔄 Recommandation réouverte', 'success')
  } catch (err) {
    showNotification('Erreur lors de la mise à jour', 'error')
  }
}

// Régénérer le plan d'action
const regeneratePlan = () => {
  if (actionPlanRef.value) {
    actionPlanRef.value.generatePlan()
    showNotification('Plan d\'action régénéré avec succès !', 'success')
  }
}

// Calcul des moyennes
const calculateAverageFocus = () => {
  if (!childBehaviors.value.length) return '0.0'
  let total = 0
  let count = 0
  childBehaviors.value.forEach(log => {
    const focus = parseInt(log.focus_level)
    if (!isNaN(focus)) {
      total += focus
      count++
    }
  })
  return count > 0 ? (total / count).toFixed(1) : '0.0'
}

const calculateAverageSleep = () => {
  if (!childBehaviors.value.length) return '0.0'
  let total = 0
  let count = 0
  childBehaviors.value.forEach(log => {
    const sleep = parseFloat(log.sleep_hours)
    if (!isNaN(sleep) && sleep > 0) {
      total += sleep
      count++
    }
  })
  return count > 0 ? (total / count).toFixed(1) : '0.0'
}

// Formater la date
const formatDate = (date) => {
  if (!date) return 'N/A'
  return new Date(date).toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' })
}

// Obtenir l'emoji du mood
const getMoodEmoji = (mood) => {
  const emojis = { 'happy': '😊', 'sad': '😢', 'angry': '😡', 'neutral': '😐', 'excited': '🤩', 'anxious': '😰' }
  return emojis[mood] || '😊'
}

// Obtenir la couleur du mood
const getMoodColor = (mood) => {
  const colors = { 'happy': '#10b981', 'sad': '#3b82f6', 'angry': '#ef4444', 'neutral': '#6b7280', 'excited': '#f59e0b', 'anxious': '#8b5cf6' }
  return colors[mood] || '#6b7280'
}

// Obtenir l'icône de la catégorie
const getCategoryIcon = (category) => {
  const icons = {
    focus: '🧠', social: '👥', relaxation: '🧘', routine: '📅', sleep: '😴', nutrition: '🍎'
  }
  return icons[category] || '📌'
}

// Obtenir la couleur de la catégorie
const getCategoryColor = (category) => {
  const colors = {
    focus: '#667eea', social: '#10b981', relaxation: '#8b5cf6', routine: '#f59e0b', sleep: '#3b82f6', nutrition: '#ef4444'
  }
  return colors[category] || '#6b7280'
}

// Animation pour les cartes
const getCardDelay = (index) => `${index * 0.05}s`

// Calcul du niveau de risque
const getRiskLevel = (child) => {
  if (!child.behaviors || child.behaviors.length === 0) return 'low'
  const avgFocus = child.behaviors.slice(0, 7).reduce((sum, b) => sum + b.focus_level, 0) / Math.min(child.behaviors.length, 7)
  if (avgFocus < 2.5) return 'high'
  if (avgFocus < 3.5) return 'medium'
  return 'low'
}

const getRiskIcon = (risk) => {
  return { high: '🔴', medium: '🟡', low: '🟢' }[risk] || '🟢'
}

const getRiskLabel = (risk) => {
  return { high: 'Risque élevé', medium: 'Risque modéré', low: 'Risque faible' }[risk] || 'Risque faible'
}

// Ouvrir modal d'ajout
const openAddModal = async () => {
  if (!isParent.value && !isTeacher.value) return

  await Promise.all([loadPsychologists(), loadTeachers()])
  if (isTeacher.value) await loadParents()

  editingChild.value = null
  form.value = {
    name: '',
    age: '',
    parent_id: '',
    psychologist_id: '',
    teacher_id: '',
    notes: ''
  }
  showModal.value = true
}

// Ouvrir modal d'édition
const openEditModal = async (child) => {
  if (!isParent.value && !isTeacher.value) return

  await Promise.all([loadPsychologists(), loadTeachers()])
  if (isTeacher.value) await loadParents()

  editingChild.value = child
  form.value = {
    name: child.name || '',
    age: child.age || '',
    parent_id: child.parent_id || '',
    psychologist_id: child.psychologist_id || '',
    teacher_id: child.teacher_id || '',
    notes: child.notes || ''
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  showPsychologistModal.value = false
  editingChild.value = null
  selectedPsychologist.value = null
}

// Sauvegarder
const saveChild = async () => {
  if (!form.value.name || !form.value.age) {
    showNotification('Le nom et l\'âge sont requis', 'error')
    return
  }

  try {
    loading.value = true

    const dataToSend = {
      name: form.value.name,
      age: parseInt(form.value.age),
      notes: form.value.notes || null
    }

    if (isParent.value) {
      dataToSend.psychologist_id = form.value.psychologist_id || null
      dataToSend.teacher_id = form.value.teacher_id || null
    }

    if (isTeacher.value) {
      dataToSend.parent_id = form.value.parent_id || null
      dataToSend.psychologist_id = form.value.psychologist_id || null
      dataToSend.teacher_id = user.value.id
    }

    if (editingChild.value) {
      await api.put(`/children/${editingChild.value.id}`, dataToSend)
      showNotification('Enfant modifié avec succès !', 'success')
    } else {
      await api.post('/children', dataToSend)
      showNotification('Enfant ajouté avec succès !', 'success')
    }

    closeModal()
    await loadChildren()
  } catch (err) {
    console.error('Error saving child:', err)
    showNotification(err.response?.data?.error || 'Erreur lors de l\'enregistrement', 'error')
  } finally {
    loading.value = false
  }
}

// Supprimer avec confirmation
const deleteChild = async (child) => {
  if (!isParent.value && !isTeacher.value) return

  const confirmed = await confirmDialog.value?.show({
    title: 'Supprimer un enfant',
    message: `Êtes-vous sûr de vouloir supprimer "${child.name}" ? Toutes les données associées seront supprimées définitivement.`,
    type: 'danger',
    confirmText: 'Supprimer',
    cancelText: 'Annuler'
  })

  if (confirmed) {
    try {
      loading.value = true
      await api.delete(`/children/${child.id}`)
      showNotification(`"${child.name}" a été supprimé avec succès`, 'success')
      await loadChildren()
    } catch (err) {
      console.error('Error deleting child:', err)
      showNotification(err.response?.data?.error || 'Erreur lors de la suppression', 'error')
    } finally {
      loading.value = false
    }
  }
}

onMounted(async () => {
  const userData = JSON.parse(localStorage.getItem('user') || '{}')
  if (userData.role && userData.role !== userData.role.toLowerCase()) {
    userData.role = userData.role.toLowerCase()
    localStorage.setItem('user', JSON.stringify(userData))
  }

  await loadChildren()
  await loadPsychologists()
  await loadTeachers()

  if (isTeacher.value) {
    await loadParents()
  }
})
</script>

<template>
  <Layout>
    <div class="children-container">
      <!-- Notifications -->
      <NotificationToast ref="toast" />
      <ConfirmDialog ref="confirmDialog" />

      <div class="page-header">
        <div>
          <h1 class="page-title">👶 Gestion des enfants</h1>
          <p class="page-subtitle">
            <span v-if="isParent">Gérez vos enfants et leurs professionnels assignés</span>
            <span v-else-if="isTeacher">Gérez les enfants que vous avez ajoutés</span>
            <span v-else-if="isPsychologist">Consultez les enfants qui vous sont assignés</span>
          </p>
        </div>
        <button v-if="isParent || isTeacher" @click="openAddModal" class="btn-add" :disabled="loading">
          <span class="btn-icon">➕</span>
          Ajouter un enfant
        </button>
      </div>

      <div v-if="loading && !children.length" class="loading-state">
        <div class="spinner"></div>
        <p>Chargement des enfants...</p>
      </div>

      <div v-else-if="children.length === 0" class="empty-state">
        <div class="empty-icon">👶</div>
        <h3>Aucun enfant trouvé</h3>
        <p v-if="isParent">Cliquez sur "Ajouter un enfant" pour commencer</p>
        <p v-else-if="isTeacher">Cliquez sur "Ajouter un enfant" pour ajouter des enfants</p>
        <p v-else-if="isPsychologist">Aucun enfant ne vous est encore assigné</p>
      </div>

      <div v-else class="children-grid">
        <div
          v-for="(child, index) in children"
          :key="child.id"
          class="child-card"
          :style="{ animationDelay: getCardDelay(index) }"
        >
          <div class="card-header">
            <div class="child-avatar" :style="{ background: `linear-gradient(135deg, #${Math.floor(Math.random()*16777215).toString(16)}, #${Math.floor(Math.random()*16777215).toString(16)})` }">
              {{ child.name?.charAt(0) || '👶' }}
            </div>
            <div class="child-info">
              <h3 class="child-name">{{ child.name }}</h3>
              <p class="child-age">{{ child.age }} ans</p>
            </div>
            <div class="risk-badge" :class="getRiskLevel(child)">
              {{ getRiskIcon(getRiskLevel(child)) }} {{ getRiskLabel(getRiskLevel(child)) }}
            </div>
            <div class="card-actions">
              <button v-if="isParent || isTeacher" @click="openEditModal(child)" class="btn-icon" title="Modifier">
                ✏️
              </button>
              <button v-if="isParent || isTeacher" @click="deleteChild(child)" class="btn-icon delete" title="Supprimer">
                🗑️
              </button>
              <button @click="exportPDF(child.id)" class="btn-pdf" title="Exporter PDF">
                📄
              </button>
            </div>
          </div>

          <div class="card-body">
            <div class="info-row">
              <span class="info-label">👨‍👩‍👧 Parent :</span>
              <span class="info-value">{{ child.parent?.name || child.parentName || 'Non assigné' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">👩‍⚕️ Psychologue :</span>
              <span class="info-value">
                <span class="psychologist-name">{{ child.psychologist?.name || child.psychologistName || 'Non assigné' }}</span>
                <button v-if="(child.psychologist || child.psychologistId) && (isParent || isTeacher)"
                        @click="viewPsychologistInfo(child)"
                        class="btn-contact">
                  📞 Contacter
                </button>
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">📚 Enseignant :</span>
              <span class="info-value">{{ child.teacher?.name || child.teacherName || 'Non assigné' }}</span>
            </div>
            <div v-if="child.notes" class="info-row">
              <span class="info-label">📝 Notes :</span>
              <span class="info-value notes-value">{{ child.notes }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">📅 Créé le :</span>
              <span class="info-value">{{ formatDate(child.created_at) }}</span>
            </div>
          </div>

          <div class="card-footer">
            <button @click="openDetailsModal(child)" class="btn-view">
              📊 Voir détails
            </button>
            <button v-if="isPsychologist && (child.parent || child.parentId)" @click="messageParent(child)" class="btn-message">
              💬 Message Parent
            </button>
          </div>
        </div>
      </div>

      <!-- Modal Add/Edit -->
      <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
        <div class="modal">
          <div class="modal-header">
            <h2>{{ editingChild ? '✏️ Modifier' : '➕ Ajouter un enfant' }}</h2>
            <button @click="closeModal" class="modal-close">✕</button>
          </div>

          <div class="modal-body">
            <div class="form-group">
              <label>Nom de l'enfant *</label>
              <input v-model="form.name" type="text" placeholder="Entrez le nom" class="form-input">
            </div>

            <div class="form-group">
              <label>Âge *</label>
              <input v-model="form.age" type="number" min="1" max="18" placeholder="Âge" class="form-input">
            </div>

            <div v-if="isTeacher" class="form-group">
              <label>👨‍👩‍👧 Assigner un parent (optionnel)</label>
              <select v-model="form.parent_id" class="form-select">
                <option :value="null">— Aucun —</option>
                <option v-for="parent in parents" :key="parent.id" :value="parent.id">
                  {{ parent.name }} ({{ parent.email }})
                </option>
              </select>
            </div>

            <div v-if="isTeacher || isParent" class="form-group">
              <label>👩‍⚕️ Assigner un psychologue (optionnel)</label>
              <select v-model="form.psychologist_id" class="form-select">
                <option :value="null">— Aucun —</option>
                <option v-for="psych in psychologists" :key="psych.id" :value="psych.id">
                  {{ psych.name }} ({{ psych.email }})
                </option>
              </select>
            </div>

            <div v-if="isParent" class="form-group">
              <label>📚 Assigner un enseignant (optionnel)</label>
              <select v-model="form.teacher_id" class="form-select">
                <option :value="null">— Aucun —</option>
                <option v-for="teacher in teachers" :key="teacher.id" :value="teacher.id">
                  {{ teacher.name }} ({{ teacher.email }})
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>Notes (optionnel)</label>
              <textarea v-model="form.notes" rows="3" placeholder="Notes sur l'enfant..." class="form-textarea"></textarea>
            </div>
          </div>

          <div class="modal-footer">
            <button @click="closeModal" class="btn-cancel">Annuler</button>
            <button @click="saveChild" class="btn-save" :disabled="loading">
              {{ loading ? 'Enregistrement...' : (editingChild ? 'Modifier' : 'Ajouter') }}
            </button>
          </div>
        </div>
      </div>

      <!-- Modal Psychologist Info -->
      <div v-if="showPsychologistModal" class="modal-overlay" @click.self="closeModal">
        <div class="modal modal-small">
          <div class="modal-header">
            <h2>👩‍⚕️ Contact du psychologue</h2>
            <button @click="closeModal" class="modal-close">✕</button>
          </div>
          <div class="modal-body" v-if="selectedPsychologist">
            <div class="psychologist-info">
              <div class="psychologist-avatar">
                {{ selectedPsychologist.name?.charAt(0) || '👩‍⚕️' }}
              </div>
              <h3>{{ selectedPsychologist.name }}</h3>
              <p class="psychologist-email">📧 {{ selectedPsychologist.email }}</p>
              <div class="psychologist-actions">
                <button @click="window.location.href = `mailto:${selectedPsychologist.email}`" class="btn-email">
                  ✉️ Envoyer un email
                </button>
                <button @click="closeModal" class="btn-close-modal">Fermer</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ========== MODAL DÉTAILS COMPLET ========== -->
      <div v-if="showDetailsModal" class="modal-overlay details-modal-overlay" @click.self="closeDetailsModal">
        <div class="modal details-modal">
          <div class="modal-header">
            <div class="details-header-info">
              <div class="details-child-avatar">
                {{ selectedChild?.name?.charAt(0) || '👶' }}
              </div>
              <div>
                <h2>{{ selectedChild?.name }}</h2>
                <p class="details-child-meta">{{ selectedChild?.age }} ans · Créé le {{ formatDate(selectedChild?.created_at) }}</p>
              </div>
            </div>
            <button @click="closeDetailsModal" class="modal-close">✕</button>
          </div>

          <div class="modal-body details-body">
            <!-- Tabs -->
            <div class="details-tabs">
              <button @click="detailsActiveTab = 'timeline'" class="details-tab-btn" :class="{ active: detailsActiveTab === 'timeline' }">
                📋 Timeline
              </button>
              <button @click="detailsActiveTab = 'chart'" class="details-tab-btn" :class="{ active: detailsActiveTab === 'chart' }">
                📊 Progress Chart
              </button>
              <button v-if="isPsychologist" @click="detailsActiveTab = 'notes'" class="details-tab-btn" :class="{ active: detailsActiveTab === 'notes' }">
                📝 Notes
              </button>
              <button @click="detailsActiveTab = 'recommendations'" class="details-tab-btn" :class="{ active: detailsActiveTab === 'recommendations' }">
                🎯 Recommendations
              </button>
              <button @click="detailsActiveTab = 'actionplan'" class="details-tab-btn" :class="{ active: detailsActiveTab === 'actionplan' }">
                📋 Plan d'action
              </button>
              <button @click="detailsActiveTab = 'routine'" class="details-tab-btn" :class="{ active: detailsActiveTab === 'routine' }">
                📅 Routine
              </button>
            </div>

            <div v-if="detailsLoading" class="details-loading">
              <div class="spinner-small"></div>
              <p>Chargement...</p>
            </div>

            <div v-else>
              <!-- Timeline Tab -->
              <div v-if="detailsActiveTab === 'timeline'" class="details-tab-content">
                <div v-if="childBehaviors.length === 0" class="empty-state-small">
                  <span>📭</span>
                  <p>No behavior logs yet</p>
                  <p class="empty-hint-small">Add logs from the dashboard to track progress</p>
                </div>
                <div v-else class="timeline-list">
                  <div v-for="log in childBehaviors.slice(0, 20)" :key="log.id" class="timeline-log-item">
                    <div class="timeline-log-date">{{ formatDate(log.log_date) }}</div>
                    <div class="timeline-log-stats">
                      <span class="log-badge focus">🧠 Focus: {{ log.focus_level }}/5</span>
                      <span class="log-badge mood" :style="{ background: getMoodColor(log.mood) + '20', color: getMoodColor(log.mood) }">
                        {{ getMoodEmoji(log.mood) }} {{ log.mood }}
                      </span>
                      <span class="log-badge">😴 Sleep: {{ log.sleep_hours }}h</span>
                      <span class="log-badge">👥 Social: {{ log.social_interaction }}/5</span>
                    </div>
                    <p v-if="log.note" class="timeline-log-note">📝 {{ log.note }}</p>
                  </div>
                </div>
              </div>

              <!-- Chart Tab -->
              <div v-if="detailsActiveTab === 'chart'" class="details-tab-content">
                <div v-if="childBehaviors.length === 0" class="empty-state-small">
                  <span>📭</span>
                  <p>No data available yet</p>
                  <p class="empty-hint-small">Add logs to see the focus progression chart</p>
                </div>
                <div v-else>
                  <div class="mini-chart-container">
                    <Line :data="chartData" :options="chartOptions" />
                  </div>
                  <div class="stats-mini">
                    <div class="stat-mini">
                      <span class="stat-mini-label">Average Focus</span>
                      <span class="stat-mini-value">{{ calculateAverageFocus() }}/5</span>
                    </div>
                    <div class="stat-mini">
                      <span class="stat-mini-label">Average Sleep</span>
                      <span class="stat-mini-value">{{ calculateAverageSleep() }}h</span>
                    </div>
                    <div class="stat-mini">
                      <span class="stat-mini-label">Total Logs</span>
                      <span class="stat-mini-value">{{ childBehaviors.length }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Notes Tab (Psychologist only) -->
              <div v-if="detailsActiveTab === 'notes'" class="details-tab-content">
                <div v-if="childNotes.length === 0" class="empty-state-small">
                  <span>📝</span>
                  <p>No notes yet</p>
                  <p class="empty-hint-small">Add a note to document your observations</p>
                </div>
                <div v-else class="notes-list-small">
                  <div v-for="note in childNotes" :key="note.id" class="note-item-small">
                    <div class="note-header-small">
                      <span class="note-date-small">{{ formatDate(note.session_date) }}</span>
                      <span class="note-author-small">{{ note.psychologist?.name || 'Psychologist' }}</span>
                    </div>
                    <div class="note-content-small">{{ note.note }}</div>
                  </div>
                </div>
              </div>

              <!-- Recommendations Tab -->
              <div v-if="detailsActiveTab === 'recommendations'" class="details-tab-content">
                <div v-if="childRecommendations.length === 0" class="empty-state-small">
                  <span>🎯</span>
                  <p>No recommendations yet</p>
                  <p v-if="isPsychologist" class="empty-hint-small">Add recommendations to help guide the child's development</p>
                </div>
                <div v-else class="recommendations-list-small">
                  <div v-for="rec in childRecommendations" :key="rec.id" class="recommendation-item-small" :class="{ completed: rec.is_completed }">
                    <div class="rec-header-small">
                      <div class="rec-icon-small" :style="{ background: getCategoryColor(rec.category) }">
                        {{ getCategoryIcon(rec.category) }}
                      </div>
                      <div class="rec-info-small">
                        <span class="rec-title-small">{{ rec.title }}</span>
                        <span class="rec-category-small">{{ rec.category }}</span>
                      </div>
                      <button v-if="isParent || isTeacher" @click="toggleRecommendation(rec)" class="rec-toggle-small" :class="{ completed: rec.is_completed }">
                        {{ rec.is_completed ? '✓ Completed' : '○ Mark' }}
                      </button>
                    </div>
                    <div class="rec-description-small">{{ rec.description }}</div>
                    <div class="rec-date-small">Added {{ formatDate(rec.created_at) }}</div>
                  </div>
                </div>
              </div>

              <!-- Action Plan Tab -->
              <div v-if="detailsActiveTab === 'actionplan'" class="details-tab-content actionplan-tab">
                <div class="actionplan-header">
                  <h3>📋 Plan d'action personnalisé</h3>
                  <button @click="regeneratePlan" class="btn-regenerate-small">
                    🔄 Régénérer le plan
                  </button>
                </div>
                <ActionPlan ref="actionPlanRef" :child-id="selectedChild?.id" />
              </div>

              <!-- Routine Planner Tab -->
              <div v-if="detailsActiveTab === 'routine'" class="details-tab-content routine-tab">
                <RoutinePlanner :child-id="selectedChild?.id" />
              </div>
            </div>
          </div>

          <div class="modal-footer">
            <button @click="closeDetailsModal" class="btn-cancel">Fermer</button>
            <button v-if="isPsychologist && selectedChild?.parent" @click="messageParent(selectedChild)" class="btn-message-parent-small">
              💬 Message Parent
            </button>
          </div>
        </div>
      </div>

    </div>
  </Layout>
</template>

<style scoped>
.children-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  flex-wrap: wrap;
  gap: 20px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #1a0a2e, #2d1b4e);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.page-subtitle {
  color: #6b7280;
  font-size: 14px;
}

.btn-add {
  background: linear-gradient(135deg, #1a0a2e, #2d1b4e);
  color: white;
  padding: 12px 24px;
  border: none;
  border-radius: 40px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-add:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(26, 10, 46, 0.3);
}

.loading-state {
  text-align: center;
  padding: 60px;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 3px solid #e0e6ed;
  border-top-color: #1a0a2e;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.children-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 24px;
}

.child-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fadeInUp 0.5s ease both;
}

.child-card:hover {
  transform: translateY(-5px) scale(1.02);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.12);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: linear-gradient(135deg, #f5f3ff, #ede9fe);
  border-bottom: 1px solid #e0e6ed;
  flex-wrap: wrap;
}

.child-avatar {
  width: 55px;
  height: 55px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  color: white;
  transition: transform 0.3s;
}

.child-card:hover .child-avatar {
  transform: scale(1.05);
}

.child-info { flex: 1; }

.child-name {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.child-age {
  font-size: 13px;
  color: #6b7280;
}

.risk-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
}

.risk-badge.high { background: #fee2e2; color: #dc2626; }
.risk-badge.medium { background: #fef3c7; color: #d97706; }
.risk-badge.low { background: #d1fae5; color: #059669; }

.card-actions { display: flex; gap: 8px; }

.btn-icon, .btn-pdf {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  padding: 8px;
  border-radius: 10px;
  transition: all 0.2s;
}

.btn-icon:hover { background: rgba(0,0,0,0.05); transform: scale(1.1); }
.btn-icon.delete:hover { background: #fee2e2; color: #ef4444; }
.btn-pdf { color: #ef4444; }
.btn-pdf:hover { background: #fee2e2; transform: scale(1.1); }

.card-body { padding: 20px; }

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f3f4f6;
  transition: background 0.2s;
}

.info-row:hover { background: #f9fafb; padding-left: 8px; }
.info-row:last-child { border-bottom: none; }

.info-label {
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  min-width: 120px;
}

.info-value {
  font-size: 13px;
  color: #374151;
  text-align: right;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.notes-value {
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.psychologist-name { font-weight: 500; color: #4f46e5; }

.btn-contact {
  border: none;
  color: #4f46e5;
  cursor: pointer;
  font-size: 11px;
  padding: 4px 10px;
  border-radius: 20px;
  background: #eef2ff;
  transition: all 0.2s;
}

.btn-contact:hover { background: #e0e7ff; transform: scale(1.02); }

.card-footer {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  background: #f9fafb;
  border-top: 1px solid #e0e6ed;
}

.btn-view, .btn-message {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: 30px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-view {
  background: linear-gradient(135deg, #1a0a2e, #2d1b4e);
  color: white;
}

.btn-view:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(26,10,46,0.3); }

.btn-message { background: #eef2ff; color: #4f46e5; }
.btn-message:hover { background: #e0e7ff; transform: translateY(-2px); }

.empty-state {
  text-align: center;
  padding: 60px;
  background: white;
  border-radius: 20px;
  animation: fadeInUp 0.5s ease;
}

.empty-icon { font-size: 64px; margin-bottom: 16px; opacity: 0.5; }

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal {
  background: white;
  border-radius: 24px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.details-modal {
  max-width: 860px !important;
  width: 92%;
  max-height: 92vh;
}

.details-modal-overlay { z-index: 1001; }

.modal-small { max-width: 400px; }

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e0e6ed;
  position: sticky;
  top: 0;
  background: white;
  z-index: 10;
  border-radius: 24px 24px 0 0;
}

.modal-header h2 { font-size: 20px; margin: 0; }

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #9ca3af;
  transition: all 0.2s;
  flex-shrink: 0;
}

.modal-close:hover { transform: scale(1.1); color: #ef4444; }

.modal-body { padding: 24px; }

/* Details Modal Specific */
.details-header-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.details-child-avatar {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #1a0a2e, #2d1b4e);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
}

.details-child-meta {
  color: #6b7280;
  font-size: 13px;
  margin: 4px 0 0 0;
}

.details-tabs {
  display: flex;
  gap: 6px;
  border-bottom: 1px solid #e0e6ed;
  margin-bottom: 20px;
  padding-bottom: 10px;
  flex-wrap: wrap;
}

.details-tab-btn {
  background: none;
  border: none;
  padding: 7px 14px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  border-radius: 20px;
  color: #6b7280;
  transition: all 0.2s;
  white-space: nowrap;
}

.details-tab-btn:hover { background: #f3f4f6; }

.details-tab-btn.active {
  background: linear-gradient(135deg, #1a0a2e, #2d1b4e);
  color: white;
}

.details-tab-content {
  max-height: 460px;
  overflow-y: auto;
  padding: 4px 2px;
}

.details-loading { text-align: center; padding: 40px; }

.spinner-small {
  width: 30px;
  height: 30px;
  border: 2px solid #e0e6ed;
  border-top-color: #1a0a2e;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 12px;
}

.empty-state-small { text-align: center; padding: 40px; color: #9ca3af; }
.empty-state-small span { font-size: 48px; display: block; margin-bottom: 12px; }
.empty-hint-small { font-size: 12px; color: #cbd5e1; margin-top: 8px; }

/* Timeline dans modal */
.timeline-list { display: flex; flex-direction: column; gap: 12px; }

.timeline-log-item {
  padding: 14px;
  background: #f9fafb;
  border-radius: 12px;
  transition: all 0.2s;
  border-left: 3px solid #e0e6ed;
}

.timeline-log-item:hover { background: #f3f4f6; border-left-color: #1a0a2e; }

.timeline-log-date {
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
  margin-bottom: 8px;
}

.timeline-log-stats { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 6px; }

.log-badge {
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  background: #e5e7eb;
  color: #374151;
}

.log-badge.focus { background: #e0e7ff; color: #4f46e5; }

.timeline-log-note {
  font-size: 12px;
  color: #6b7280;
  margin: 6px 0 0;
  padding-top: 8px;
  border-top: 1px solid #e5e7eb;
}

/* Mini chart */
.mini-chart-container { height: 250px; margin-bottom: 20px; }

.stats-mini {
  display: flex;
  justify-content: space-around;
  gap: 12px;
  margin-top: 16px;
}

.stat-mini {
  text-align: center;
  flex: 1;
  padding: 14px;
  background: #f9fafb;
  border-radius: 12px;
}

.stat-mini-label { display: block; font-size: 11px; color: #6b7280; margin-bottom: 4px; }
.stat-mini-value { display: block; font-size: 22px; font-weight: 700; color: #1f2937; }

/* Notes dans modal */
.notes-list-small { display: flex; flex-direction: column; gap: 12px; }

.note-item-small {
  padding: 14px;
  background: #f9fafb;
  border-radius: 12px;
  border-left: 3px solid #667eea;
}

.note-header-small {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 11px;
}

.note-date-small { font-weight: 600; color: #374151; }
.note-author-small { color: #9ca3af; }
.note-content-small { font-size: 13px; color: #374151; line-height: 1.6; }

/* Recommendations dans modal */
.recommendations-list-small { display: flex; flex-direction: column; gap: 12px; }

.recommendation-item-small {
  padding: 14px;
  background: #f9fafb;
  border-radius: 12px;
  transition: all 0.2s;
}

.recommendation-item-small.completed { opacity: 0.6; background: #f3f4f6; }

.rec-header-small {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.rec-icon-small {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.rec-info-small { flex: 1; }
.rec-title-small { font-size: 13px; font-weight: 600; color: #1f2937; display: block; }
.rec-category-small { font-size: 10px; color: #6b7280; text-transform: capitalize; }

.rec-toggle-small {
  border: none;
  font-size: 11px;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 20px;
  transition: all 0.2s;
  background: #e5e7eb;
  color: #6b7280;
  white-space: nowrap;
}

.rec-toggle-small.completed { background: #d1fae5; color: #065f46; }

.rec-description-small {
  font-size: 12px;
  color: #4b5563;
  margin-left: 44px;
  margin-bottom: 4px;
  line-height: 1.5;
}

.rec-date-small { font-size: 10px; color: #9ca3af; margin-left: 44px; }

/* Action Plan Tab */
.actionplan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.actionplan-header h3 { font-size: 16px; font-weight: 600; color: #1f2937; margin: 0; }

.btn-regenerate-small {
  background: linear-gradient(135deg, #1a0a2e, #2d1b4e);
  color: white;
  padding: 7px 16px;
  border: none;
  border-radius: 30px;
  font-weight: 600;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-regenerate-small:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(26,10,46,0.3); }

.routine-tab { padding: 4px; }

.btn-message-parent-small {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 40px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-message-parent-small:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(16,185,129,0.3); }

/* Form Styles */
.form-group { margin-bottom: 20px; }

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #4b5563;
  margin-bottom: 8px;
}

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
  box-shadow: 0 0 0 3px rgba(26,10,46,0.1);
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #e0e6ed;
  background: white;
  border-radius: 0 0 24px 24px;
}

.btn-cancel, .btn-save {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 40px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel { background: #f3f4f6; color: #6b7280; }
.btn-cancel:hover { background: #e5e7eb; }

.btn-save {
  background: linear-gradient(135deg, #1a0a2e, #2d1b4e);
  color: white;
}

.btn-save:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(26,10,46,0.3); }
.btn-save:disabled { opacity: 0.6; cursor: not-allowed; }

/* Psychologist Modal */
.psychologist-info { text-align: center; padding: 20px; }

.psychologist-avatar {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #1a0a2e, #2d1b4e);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  margin: 0 auto 16px;
  color: white;
}

.psychologist-info h3 { font-size: 20px; margin-bottom: 8px; }
.psychologist-email { color: #6b7280; margin-bottom: 24px; }

.psychologist-actions { display: flex; gap: 12px; justify-content: center; }

.btn-email, .btn-close-modal {
  padding: 10px 20px;
  border: none;
  border-radius: 30px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-email { background: linear-gradient(135deg, #1a0a2e, #2d1b4e); color: white; }
.btn-email:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(26,10,46,0.3); }
.btn-close-modal { background: #f3f4f6; color: #6b7280; }
.btn-close-modal:hover { background: #e5e7eb; }

/* Responsive */
@media (max-width: 900px) {
  .children-grid { grid-template-columns: 1fr; }
  .page-header { flex-direction: column; text-align: center; }
  .info-row { flex-direction: column; align-items: flex-start; gap: 5px; }
  .info-value { text-align: left; justify-content: flex-start; }
  .card-header { flex-direction: column; text-align: center; }
  .risk-badge { align-self: center; }
  .details-modal { width: 96%; margin: 10px; }
  .stats-mini { flex-direction: column; }
  .rec-description-small { margin-left: 0; }
  .rec-date-small { margin-left: 0; }
  .details-tabs { justify-content: center; }
  .actionplan-header { flex-direction: column; align-items: flex-start; }
}
</style>