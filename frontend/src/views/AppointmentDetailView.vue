<template>
  <Layout>
    <div class="appointment-detail-container">
      <button @click="goBack" class="back-button">
        ← Retour aux messages
      </button>

      <div class="detail-card">
        <div class="detail-header">
          <h1>{{ appointment.title }}</h1>
          <span class="status-badge" :class="statusClass">{{ statusText }}</span>
        </div>

        <div class="detail-content">
          <div class="info-section">
            <div class="info-row">
              <span class="info-icon">📋</span>
              <span class="info-label">TYPE</span>
              <span class="info-value">{{ typeIcon }} {{ typeText }}</span>
            </div>

            <div class="info-row">
              <span class="info-icon">📅</span>
              <span class="info-label">DATE</span>
              <span class="info-value">{{ formatDate(appointment.scheduled_at) }}</span>
            </div>

            <div class="info-row">
              <span class="info-icon">⏰</span>
              <span class="info-label">HEURE</span>
              <span class="info-value">{{ formatTime(appointment.scheduled_at) }}</span>
            </div>

            <div class="info-row">
              <span class="info-icon">⏱️</span>
              <span class="info-label">DURÉE</span>
              <span class="info-value">{{ appointment.duration }} minutes</span>
            </div>

            <div v-if="appointment.location" class="info-row">
              <span class="info-icon">📍</span>
              <span class="info-label">LIEU / LIEN</span>
              <span class="info-value">
                <a v-if="appointment.location.startsWith('http')" :href="appointment.location" target="_blank">{{ appointment.location }}</a>
                <span v-else>{{ appointment.location }}</span>
              </span>
            </div>

            <div v-if="appointment.notes" class="info-row">
              <span class="info-icon">📝</span>
              <span class="info-label">NOTES</span>
              <span class="info-value">{{ appointment.notes }}</span>
            </div>
          </div>

          <div class="participants-section">
            <h3>Participants</h3>
            <div class="participant">
              <div class="participant-avatar">{{ creatorInitials }}</div>
              <div class="participant-info">
                <div class="participant-name">{{ creatorName }}</div>
                <div class="participant-role">Créateur</div>
                <div v-if="isCreator" class="participant-badge">(Vous)</div>
              </div>
            </div>
            <div class="participant">
              <div class="participant-avatar">{{ receiverInitials }}</div>
              <div class="participant-info">
                <div class="participant-name">{{ receiverName }}</div>
                <div class="participant-role">Invité(e)</div>
                <div v-if="!isCreator" class="participant-badge">(Vous)</div>
              </div>
            </div>
          </div>

          <div class="action-buttons">
            <template v-if="isCreator">
              <button v-if="appointment.status === 'pending'" @click="cancelAppointment" class="btn-cancel-appointment">
                ❌ Annuler
              </button>
              <button v-if="appointment.status === 'confirmed'" @click="markAsCompleted" class="btn-complete">
                ✓ Terminer
              </button>
              <span v-if="appointment.status === 'cancelled'" class="cancelled-message">Annulé</span>
              <span v-if="appointment.status === 'completed'" class="completed-message">Terminé</span>
            </template>

            <template v-else>
              <button v-if="appointment.status === 'pending'" @click="confirmAppointment" class="btn-confirm">
                ✓ Accepter
              </button>
              <button v-if="appointment.status === 'pending'" @click="declineAppointment" class="btn-decline">
                ✗ Refuser
              </button>
              <span v-if="appointment.status === 'confirmed'" class="confirmed-message">✓ Accepté</span>
              <span v-if="appointment.status === 'cancelled'" class="cancelled-message">Annulé</span>
              <span v-if="appointment.status === 'completed'" class="completed-message">Terminé</span>
            </template>
          </div>
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
import NotificationToast from '@/components/NotificationToast.vue'

const route = useRoute()
const router = useRouter()
const appointment = ref({
  title: '',
  type: '',
  scheduled_at: '',
  duration: 30,
  location: '',
  notes: '',
  status: 'pending',
  sender: {},
  receiver: {}
})

const currentUser = ref(null)
const toast = ref(null)

const showSuccess = (message) => {
  toast.value?.addNotification('success', 'Succès', message, 4000)
}

const showError = (message) => {
  toast.value?.addNotification('error', 'Erreur', message, 5000)
}

const goBack = () => {
  router.push('/messages')
}

const isCreator = computed(() => {
  return currentUser.value?.id === appointment.value.sender?.id
})

const creatorName = computed(() => appointment.value.sender?.name || 'Inconnu')
const receiverName = computed(() => appointment.value.receiver?.name || 'Inconnu')
const creatorInitials = computed(() => creatorName.value.charAt(0).toUpperCase())
const receiverInitials = computed(() => receiverName.value.charAt(0).toUpperCase())

const typeIcon = computed(() => {
  switch (appointment.value.type) {
    case 'video': return '📹'
    case 'phone': return '📞'
    default: return '🏢'
  }
})

const typeText = computed(() => {
  switch (appointment.value.type) {
    case 'video': return 'Visio'
    case 'phone': return 'Téléphone'
    default: return 'En personne'
  }
})

const statusText = computed(() => {
  switch (appointment.value.status) {
    case 'pending': return 'En attente'
    case 'confirmed': return 'Confirmé'
    case 'cancelled': return 'Annulé'
    case 'completed': return 'Terminé'
    default: return 'Inconnu'
  }
})

const statusClass = computed(() => {
  switch (appointment.value.status) {
    case 'pending': return 'status-pending'
    case 'confirmed': return 'status-confirmed'
    case 'cancelled': return 'status-cancelled'
    case 'completed': return 'status-completed'
    default: return ''
  }
})

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('fr-FR', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const formatTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleTimeString('fr-FR', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadCurrentUser = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    currentUser.value = JSON.parse(userStr)
  }
}

const loadAppointment = async () => {
  try {
    const id = route.params.id
    const res = await api.get(`/appointments/${id}`)
    appointment.value = res.data
  } catch (err) {
    showError('Impossible de charger le rendez-vous')
    router.push('/messages')
  }
}

const confirmAppointment = async () => {
  try {
    await api.patch(`/appointments/${appointment.value.id}/status`, { status: 'confirmed' })
    appointment.value.status = 'confirmed'
    showSuccess('Rendez-vous accepté')
    setTimeout(() => router.push('/messages'), 1500)
  } catch (err) {
    showError('Erreur')
  }
}

const declineAppointment = async () => {
  try {
    await api.patch(`/appointments/${appointment.value.id}/status`, { status: 'cancelled' })
    appointment.value.status = 'cancelled'
    showSuccess('Rendez-vous refusé')
    setTimeout(() => router.push('/messages'), 1500)
  } catch (err) {
    showError('Erreur')
  }
}

const cancelAppointment = async () => {
  if (confirm('Annuler ce rendez-vous ?')) {
    try {
      await api.patch(`/appointments/${appointment.value.id}/status`, { status: 'cancelled' })
      appointment.value.status = 'cancelled'
      showSuccess('Rendez-vous annulé')
      setTimeout(() => router.push('/messages'), 1500)
    } catch (err) {
      showError('Erreur')
    }
  }
}

const markAsCompleted = async () => {
  try {
    await api.patch(`/appointments/${appointment.value.id}/status`, { status: 'completed' })
    appointment.value.status = 'completed'
    showSuccess('Rendez-vous terminé')
    setTimeout(() => router.push('/messages'), 1500)
  } catch (err) {
    showError('Erreur')
  }
}

onMounted(() => {
  loadCurrentUser()
  loadAppointment()
})
</script>

<style scoped>
.appointment-detail-container { max-width: 800px; margin: 40px auto; padding: 0 20px; }

.back-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: none;
  border: none;
  color: #667eea;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  padding: 8px 0;
  margin-bottom: 20px;
  transition: all 0.2s;
}

.back-button:hover {
  color: #764ba2;
  transform: translateX(-4px);
}

.detail-card { background: white; border-radius: 32px; box-shadow: 0 8px 30px rgba(0,0,0,0.08); overflow: hidden; }
.detail-header { background: linear-gradient(135deg, #667eea, #764ba2); padding: 30px; color: white; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px; }
.detail-header h1 { margin: 0; font-size: 24px; }
.status-badge { padding: 6px 14px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.status-pending { background: rgba(255,255,255,0.2); color: white; }
.status-confirmed { background: #10b981; color: white; }
.status-cancelled { background: #ef4444; color: white; }
.status-completed { background: #6b7280; color: white; }
.detail-content { padding: 30px; }
.info-section { margin-bottom: 30px; }
.info-row { display: flex; align-items: flex-start; gap: 15px; padding: 15px 0; border-bottom: 1px solid #e5e7eb; }
.info-icon { font-size: 20px; width: 30px; }
.info-label { font-size: 12px; font-weight: 600; color: #6b7280; width: 100px; }
.info-value { flex: 1; font-size: 14px; color: #1f2937; font-weight: 500; }
.participants-section { background: #f9fafb; border-radius: 20px; padding: 20px; margin-bottom: 30px; }
.participants-section h3 { margin: 0 0 20px 0; font-size: 16px; color: #374151; }
.participant { display: flex; align-items: center; gap: 15px; padding: 12px 0; border-bottom: 1px solid #e5e7eb; }
.participant:last-child { border-bottom: none; }
.participant-avatar { width: 48px; height: 48px; border-radius: 50%; background: linear-gradient(135deg, #667eea, #764ba2); color: white; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 18px; }
.participant-info { flex: 1; }
.participant-name { font-weight: 600; color: #1f2937; }
.participant-role { font-size: 12px; color: #6b7280; }
.participant-badge { font-size: 11px; color: #667eea; font-weight: 600; }
.action-buttons { display: flex; gap: 15px; flex-wrap: wrap; }
.btn-confirm, .btn-decline, .btn-cancel-appointment, .btn-complete { flex: 1; padding: 14px; border-radius: 12px; font-size: 14px; font-weight: 600; cursor: pointer; border: none; transition: all 0.2s; }
.btn-confirm { background: linear-gradient(135deg, #10b981, #059669); color: white; }
.btn-confirm:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(16,185,129,0.3); }
.btn-decline { background: #f3f4f6; color: #ef4444; border: 1px solid #ef4444; }
.btn-decline:hover { background: #fee2e2; }
.btn-cancel-appointment { background: #ef4444; color: white; }
.btn-cancel-appointment:hover { background: #dc2626; }
.btn-complete { background: #6b7280; color: white; }
.btn-complete:hover { background: #4b5563; }
.confirmed-message, .completed-message, .cancelled-message { flex: 1; text-align: center; padding: 14px; border-radius: 12px; font-weight: 600; }
.confirmed-message { background: #d1fae5; color: #065f46; }
.completed-message { background: #d1fae5; color: #065f46; }
.cancelled-message { background: #fee2e2; color: #991b1b; }
@media (max-width: 640px) {
  .info-row { flex-direction: column; gap: 5px; }
  .info-label { width: auto; }
  .detail-header { flex-direction: column; text-align: center; }
  .action-buttons { flex-direction: column; }
}
</style>