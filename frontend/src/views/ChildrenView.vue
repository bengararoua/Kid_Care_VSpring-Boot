<template>
  <Layout>
    <div class="children-container">
      <NotificationToast ref="toast" />
      <ConfirmDialog ref="confirmDialog" />

      <!-- En-tête -->
      <div class="page-header">
        <div>
          <h1 class="page-title">👶 Gestion des enfants</h1>
          <p class="page-subtitle">
            <span v-if="isParent">Gérez vos enfants et leurs professionnels assignés</span>
            <span v-else-if="isTeacher">Gérez les enfants que vous avez ajoutés</span>
            <span v-else-if="isPsychologist">Consultez les enfants qui vous sont assignés</span>
            <span v-else>Liste des enfants</span>
          </p>
        </div>
        <!-- Bouton Ajouter - visible pour Parent et Teacher -->
        <button v-if="isParent || isTeacher" @click="openAddModal" class="btn-add">
          ➕ Ajouter un enfant
        </button>
      </div>

      <!-- Chargement -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>Chargement des enfants...</p>
      </div>

      <!-- Liste des enfants -->
      <div v-else-if="children.length > 0" class="children-grid">
        <div v-for="child in children" :key="child.id" class="child-card">
          <div class="card-header">
            <div class="child-avatar">{{ child.name?.charAt(0) || '👶' }}</div>
            <div class="child-info">
              <h3 class="child-name">{{ child.name }}</h3>
              <p class="child-age">{{ child.age }} ans</p>
            </div>
            <!-- Boutons d'action -->
            <div class="card-actions">
              <button v-if="isParent || isTeacher" @click="openEditModal(child)" class="btn-icon" title="Modifier">✏️</button>
              <button v-if="isParent || isTeacher" @click="deleteChild(child)" class="btn-icon delete" title="Supprimer">🗑️</button>
              <button @click="exportPDF(child.id)" class="btn-pdf" title="Exporter PDF">📄</button>
            </div>
          </div>

          <div class="card-body">
            <div class="info-row">
              <span class="info-label">👨‍👩‍👧 Parent :</span>
              <span class="info-value">{{ child.parentName || child.parent?.name || 'Non assigné' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">👩‍⚕️ Psychologue :</span>
              <span class="info-value">
                {{ child.psychologistName || child.psychologist?.name || 'Non assigné' }}
                <button v-if="child.psychologistId" @click="viewPsychologistInfo(child)" class="btn-contact">📞</button>
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">📚 Enseignant :</span>
              <span class="info-value">{{ child.teacherName || child.teacher?.name || 'Non assigné' }}</span>
            </div>
            <div v-if="child.notes" class="info-row">
              <span class="info-label">📝 Notes :</span>
              <span class="info-value notes-value">{{ child.notes }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">📅 Créé le :</span>
              <span class="info-value">{{ formatDate(child.createdAt || child.created_at) }}</span>
            </div>
          </div>

          <div class="card-footer">
            <button @click="viewDetails(child)" class="btn-view">📊 Voir détails</button>
            <button v-if="isPsychologist && child.parentId" @click="messageParent(child)" class="btn-message">💬 Message Parent</button>
          </div>
        </div>
      </div>

      <!-- État vide -->
      <div v-else-if="!loading && children.length === 0" class="empty-state">
        <div class="empty-icon">👶</div>
        <h3>Aucun enfant trouvé</h3>
        <p v-if="isParent || isTeacher">Cliquez sur "Ajouter un enfant" pour commencer</p>
        <p v-else-if="isPsychologist">Aucun enfant ne vous est encore assigné</p>
      </div>

      <!-- MODAL AJOUT / MODIFICATION -->
      <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
        <div class="modal">
          <div class="modal-header">
            <h2>{{ editingChild ? '✏️ Modifier' : '➕ Ajouter un enfant' }}</h2>
            <button @click="closeModal" class="modal-close">✕</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>Nom de l'enfant *</label>
              <input v-model="form.name" type="text" class="form-input" placeholder="Entrez le nom">
            </div>
            <div class="form-group">
              <label>Âge *</label>
              <input v-model="form.age" type="number" min="1" max="18" class="form-input" placeholder="Âge">
            </div>
            <div class="form-group">
              <label>👩‍⚕️ Psychologue</label>
              <select v-model="form.psychologist_id" class="form-select">
                <option :value="null">— Aucun —</option>
                <option v-for="psych in psychologists" :key="psych.id" :value="psych.id">
                  {{ psych.name }} ({{ psych.email }})
                </option>
              </select>
            </div>
            <div class="form-group">
              <label>📚 Enseignant</label>
              <select v-model="form.teacher_id" class="form-select">
                <option :value="null">— Aucun —</option>
                <option v-for="teacher in teachers" :key="teacher.id" :value="teacher.id">
                  {{ teacher.name }} ({{ teacher.email }})
                </option>
              </select>
            </div>
            <div class="form-group">
              <label>Notes</label>
              <textarea v-model="form.notes" rows="3" class="form-textarea" placeholder="Notes sur l'enfant..."></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button @click="closeModal" class="btn-cancel">Annuler</button>
            <button @click="saveChild" class="btn-save" :disabled="saving">
              {{ saving ? 'Enregistrement...' : (editingChild ? 'Modifier' : 'Ajouter') }}
            </button>
          </div>
        </div>
      </div>

      <!-- MODAL INFO PSYCHOLOGUE -->
      <div v-if="showPsychologistModal" class="modal-overlay" @click.self="closeModal">
        <div class="modal modal-small">
          <div class="modal-header">
            <h2>👩‍⚕️ Contact du psychologue</h2>
            <button @click="closeModal" class="modal-close">✕</button>
          </div>
          <div class="modal-body" v-if="selectedPsychologist">
            <div class="psychologist-info">
              <div class="psychologist-avatar">{{ selectedPsychologist.name?.charAt(0) || '👩‍⚕️' }}</div>
              <h3>{{ selectedPsychologist.name }}</h3>
              <p class="psychologist-email">📧 {{ selectedPsychologist.email }}</p>
              <button @click="window.location.href = `mailto:${selectedPsychologist.email}`" class="btn-email">✉️ Envoyer un email</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/api'
import Layout from '@/layouts/Layout.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import NotificationToast from '@/components/NotificationToast.vue'

const router = useRouter()

// États
const children = ref([])
const psychologists = ref([])
const teachers = ref([])
const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const editingChild = ref(null)
const showPsychologistModal = ref(false)
const selectedPsychologist = ref(null)

// Formulaire
const form = ref({
  name: '',
  age: '',
  psychologist_id: null,
  teacher_id: null,
  notes: ''
})

// Toast et confirmation
const toast = ref(null)
const confirmDialog = ref(null)

// Utilisateur
const user = computed(() => {
  const userData = JSON.parse(localStorage.getItem('user') || '{}')
  if (userData.role) userData.role = userData.role.toLowerCase()
  return userData
})

const isParent = computed(() => user.value?.role === 'parent')
const isPsychologist = computed(() => user.value?.role === 'psychologist')
const isTeacher = computed(() => user.value?.role === 'teacher')

// Notifications
const showNotification = (message, type = 'success') => {
  toast.value?.addNotification(type, type === 'success' ? 'Succès' : 'Erreur', message, 4000)
}

// Export PDF
const exportPDF = async (childId) => {
  try {
    const response = await api.get(`/export/child/${childId}`, { responseType: 'blob' })
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
    showNotification('Erreur lors de l\'export', 'error')
  }
}

// Charger les enfants
const loadChildren = async () => {
  loading.value = true
  try {
    const res = await api.get('/children')
    children.value = res.data
    console.log('✅ Enfants chargés:', children.value.length)
  } catch (err) {
    console.error('Erreur:', err)
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
    console.error(err)
  }
}

// Charger les enseignants
const loadTeachers = async () => {
  try {
    const res = await api.get('/users/teachers')
    teachers.value = res.data
  } catch (err) {
    console.error(err)
  }
}

// Ouvrir modal ajout
const openAddModal = async () => {
  await Promise.all([loadPsychologists(), loadTeachers()])
  editingChild.value = null
  form.value = { name: '', age: '', psychologist_id: null, teacher_id: null, notes: '' }
  showModal.value = true
}

// Ouvrir modal édition
const openEditModal = async (child) => {
  await Promise.all([loadPsychologists(), loadTeachers()])
  editingChild.value = child
  form.value = {
    name: child.name,
    age: child.age,
    psychologist_id: child.psychologistId || child.psychologist_id || null,
    teacher_id: child.teacherId || child.teacher_id || null,
    notes: child.notes || ''
  }
  showModal.value = true
}

// Fermer modal
const closeModal = () => {
  showModal.value = false
  showPsychologistModal.value = false
  editingChild.value = null
  selectedPsychologist.value = null
}

// Sauvegarder enfant
const saveChild = async () => {
  if (!form.value.name || !form.value.age) {
    showNotification('Le nom et l\'âge sont requis', 'error')
    return
  }

  saving.value = true
  try {
    const data = {
      name: form.value.name,
      age: parseInt(form.value.age),
      notes: form.value.notes || null,
      psychologistId: form.value.psychologist_id,
      teacherId: form.value.teacher_id
    }

    if (editingChild.value) {
      await api.put(`/children/${editingChild.value.id}`, data)
      showNotification('Enfant modifié avec succès !', 'success')
    } else {
      await api.post('/children', data)
      showNotification('Enfant ajouté avec succès !', 'success')
    }

    closeModal()
    await loadChildren()
  } catch (err) {
    console.error(err)
    showNotification(err.response?.data?.error || 'Erreur lors de l\'enregistrement', 'error')
  } finally {
    saving.value = false
  }
}

// Supprimer enfant
const deleteChild = async (child) => {
  const confirmed = await confirmDialog.value?.show({
    title: 'Supprimer un enfant',
    message: `Supprimer "${child.name}" ?`,
    type: 'danger',
    confirmText: 'Supprimer'
  })
  if (!confirmed) return

  try {
    await api.delete(`/children/${child.id}`)
    showNotification(`"${child.name}" supprimé`, 'success')
    await loadChildren()
  } catch (err) {
    showNotification('Erreur lors de la suppression', 'error')
  }
}

// Voir détails
const viewDetails = (child) => {
  router.push(`/child/${child.id}`)
}

// Infos psychologue
const viewPsychologistInfo = async (child) => {
  try {
    const res = await api.get(`/children/${child.id}/psychologist`)
    selectedPsychologist.value = res.data
    showPsychologistModal.value = true
  } catch (err) {
    showNotification('Aucun psychologue assigné', 'error')
  }
}

// Message au parent
const messageParent = (child) => {
  router.push({
    path: '/messages',
    query: { contactId: child.parentId, contactName: child.parentName, contactRole: 'parent' }
  })
}

const formatDate = (date) => {
  if (!date) return 'N/A'
  return new Date(date).toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' })
}

onMounted(() => {
  loadChildren()
  loadPsychologists()
  loadTeachers()
})
</script>

<style scoped>
.children-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
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
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 24px;
}

.child-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.child-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.12);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: linear-gradient(135deg, #f5f3ff, #ede9fe);
  border-bottom: 1px solid #e0e6ed;
}

.child-avatar {
  width: 55px;
  height: 55px;
  background: linear-gradient(135deg, #1a0a2e, #2d1b4e);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  color: white;
}

.child-info {
  flex: 1;
}

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

.card-actions {
  display: flex;
  gap: 8px;
}

.btn-icon, .btn-pdf {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  padding: 8px;
  border-radius: 10px;
  transition: all 0.2s;
}

.btn-icon:hover {
  background: rgba(0, 0, 0, 0.05);
  transform: scale(1.1);
}

.btn-icon.delete:hover {
  background: #fee2e2;
  color: #ef4444;
}

.btn-pdf {
  color: #ef4444;
}

.btn-pdf:hover {
  background: #fee2e2;
  transform: scale(1.1);
}

.card-body {
  padding: 20px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f3f4f6;
}

.info-label {
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  min-width: 120px;
}

.info-value {
  font-size: 13px;
  color: #374151;
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

.btn-contact {
  background: none;
  border: none;
  font-size: 14px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  background: #eef2ff;
}

.btn-contact:hover {
  background: #e0e7ff;
}

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
}

.btn-view {
  background: linear-gradient(135deg, #1a0a2e, #2d1b4e);
  color: white;
}

.btn-view:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(26, 10, 46, 0.3);
}

.btn-message {
  background: #eef2ff;
  color: #4f46e5;
}

.btn-message:hover {
  background: #e0e7ff;
  transform: translateY(-2px);
}

.empty-state {
  text-align: center;
  padding: 60px;
  background: white;
  border-radius: 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 24px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e0e6ed;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
}

.modal-body {
  padding: 24px;
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #e0e6ed;
}

.form-group {
  margin-bottom: 20px;
}

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
}

.btn-cancel, .btn-save {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 40px;
  font-weight: 600;
  cursor: pointer;
}

.btn-cancel {
  background: #f3f4f6;
  color: #6b7280;
}

.btn-save {
  background: linear-gradient(135deg, #1a0a2e, #2d1b4e);
  color: white;
}

.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.modal-small {
  max-width: 400px;
}

.psychologist-info {
  text-align: center;
  padding: 24px;
}

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

.btn-email {
  background: linear-gradient(135deg, #1a0a2e, #2d1b4e);
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 30px;
  cursor: pointer;
}

@media (max-width: 900px) {
  .children-grid {
    grid-template-columns: 1fr;
  }
  
  .page-header {
    flex-direction: column;
    text-align: center;
  }
  
  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
  
  .info-value {
    text-align: left;
    justify-content: flex-start;
  }
}
</style>