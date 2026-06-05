<template>
  <Layout>
    <div class="chat-container">
      <NotificationToast ref="toast" />
      <ConfirmDialog ref="confirmDialog" />

      <div class="chat-sidebar" :class="{ 'sidebar-hidden': !showSidebar && isMobile }">
        <div class="sidebar-header">
          <div class="header-title">
            <span class="header-icon">💬</span>
            <h2>Messages</h2>
            <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          </div>
        </div>
        
        <div class="contacts-list">
          <div 
            v-for="contact in contacts" 
            :key="contact.id" 
            class="contact-item" 
            :class="{ active: currentContact?.id === contact.id }" 
            @click="loadConversation(contact)"
          >
            <div class="contact-avatar">
              {{ contact.name?.charAt(0)?.toUpperCase() || '?' }}
            </div>
            <div class="contact-info">
              <div class="contact-name">{{ contact.name }}</div>
              <div class="contact-role">{{ getRoleIcon(contact.role) }} {{ contact.role }}</div>
            </div>
            <div v-if="contact.unread_count > 0" class="unread-badge-mini">
              {{ contact.unread_count > 99 ? '99+' : contact.unread_count }}
            </div>
          </div>
          
          <div v-if="contacts.length === 0" class="no-contacts">
            <div class="no-contacts-icon">💬</div>
            <p>Aucun contact disponible</p>
            <small>Les contacts apparaîtront selon votre rôle</small>
          </div>
        </div>
      </div>

      <div class="chat-main" :class="{ 'chat-full': !showSidebar && isMobile }">
        <div v-if="currentContact" class="chat-header">
          <button v-if="isMobile" @click="backToContacts" class="back-btn">←</button>
          <div class="contact-avatar large">
            {{ currentContact.name?.charAt(0)?.toUpperCase() || '?' }}
          </div>
          <div class="contact-details">
            <h3>{{ currentContact.name }}</h3>
            <p>{{ getRoleIcon(currentContact.role) }} {{ currentContact.role }}</p>
          </div>
          <div class="header-actions">
            <button @click="markAllAsRead" class="mark-all-read-btn" title="Marquer tous comme lus">✓✓</button>
            <button @click="deleteConversation" class="delete-conversation-btn" title="Supprimer la conversation">🗑️</button>
          </div>
          <button @click="showAppointmentModal = true" class="appointment-btn">📅 Rendez-vous</button>
        </div>
        
        <div v-else class="chat-placeholder">
          <div class="placeholder-icon">💬</div>
          <h3>Sélectionnez un contact</h3>
          <p>Choisissez une personne pour commencer à discuter</p>
        </div>

        <div v-if="currentContact" class="messages-area" ref="messagesContainer">
          <div v-if="loading" class="loading-messages">
            <div class="spinner-small"></div>
            <p>Chargement des messages...</p>
          </div>
          <div v-else>
            <template v-for="(msg, index) in messages" :key="msg.id">
              <div 
                v-if="index === 0 || formatMessageDate(msg.created_at) !== formatMessageDate(messages[index-1]?.created_at)" 
                class="message-date-separator"
              >
                <span>{{ formatMessageDate(msg.created_at) }}</span>
              </div>
              
              <div 
                class="message" 
                :class="{ 
                  'message-sent': msg.sender_id === userId, 
                  'message-received': msg.sender_id !== userId, 
                  'message-appointment': msg.is_appointment
                }"
              >
                <div class="message-bubble">
                  <div v-if="msg.is_appointment" class="appointment-badge">📅 Rendez-vous</div>
                  <div class="message-content" v-html="formatMessageWithLinks(msg.content)"></div>
                  <div class="message-time">
                    {{ formatTime(msg.created_at) }}
                    <span v-if="msg.is_edited" class="edited-badge">(modifié)</span>
                  </div>
                  <div v-if="msg.sender_id === userId && !msg.is_temp" class="message-actions">
                    <button @click="startEditMessage(msg)" class="action-edit" title="Modifier">✏️</button>
                    <button @click="deleteMessage(msg)" class="action-delete" title="Supprimer">🗑️</button>
                  </div>
                </div>
              </div>
            </template>
            
            <div v-if="messages.length === 0 && !loading" class="no-messages">
              <div class="no-messages-icon">💬</div>
              <p>Aucun message</p>
              <small>Envoyez un premier message pour démarrer la conversation !</small>
            </div>
          </div>
        </div>

        <div v-if="currentContact" class="chat-input-area">
          <input 
            v-model="newMessage" 
            @keyup.enter="sendMessage" 
            type="text" 
            placeholder="Tapez votre message..." 
            class="chat-input" 
            :disabled="sendingMessage"
          >
          <button @click="sendMessage" class="send-btn" :disabled="!newMessage.trim() || sendingMessage">
            {{ sendingMessage ? '⏳' : '📤' }} Envoyer
          </button>
        </div>
      </div>
    </div>

    <!-- Modal d'édition de message -->
    <div v-if="editingMessage" class="modal-overlay" @click.self="cancelEdit">
      <div class="modal-content edit-modal">
        <div class="modal-header">
          <h2>✏️ Modifier le message</h2>
          <button class="modal-close" @click="cancelEdit">✕</button>
        </div>
        <div class="modal-body">
          <textarea 
            v-model="editContent" 
            class="edit-textarea"
            rows="4"
            placeholder="Votre message..."
          ></textarea>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="cancelEdit">Annuler</button>
          <button class="btn-create" @click="saveEditMessage">Enregistrer</button>
        </div>
      </div>
    </div>

    <!-- Modal création rendez-vous -->
    <div v-if="showAppointmentModal" class="modal-overlay" @click.self="showAppointmentModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <h2>📅 Créer un rendez-vous</h2>
          <button class="modal-close" @click="showAppointmentModal = false">✕</button>
        </div>
        
        <div class="modal-body">
          <div class="info-message">
            💡 Années disponibles : {{ getCurrentYear() }} - {{ getCurrentYear() + 2 }}
          </div>
          
          <div class="form-group">
            <label>Titre (optionnel)</label>
            <input v-model="appointmentData.title" type="text" placeholder="Ex: Consultation suivi" class="form-input">
          </div>
          
          <div class="form-group">
            <label>Type de rendez-vous *</label>
            <div class="appointment-types">
              <label class="type-option"><input type="radio" value="video" v-model="appointmentData.type"> 📹 Visio</label>
              <label class="type-option"><input type="radio" value="phone" v-model="appointmentData.type"> 📞 Téléphone</label>
              <label class="type-option"><input type="radio" value="in_person" v-model="appointmentData.type"> 🏢 Présentiel</label>
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>Date *</label>
              <input v-model="appointmentData.date" type="date" :min="getMinDate()" :max="getMaxDate()" class="form-input" required>
            </div>
            <div class="form-group">
              <label>Heure *</label>
              <input v-model="appointmentData.time" type="time" class="form-input" required>
            </div>
          </div>
          
          <div class="form-group">
            <label>Durée (minutes)</label>
            <select v-model="appointmentData.duration" class="form-input">
              <option value="15">15 minutes</option>
              <option value="30">30 minutes</option>
              <option value="45">45 minutes</option>
              <option value="60">1 heure</option>
            </select>
          </div>
          
          <div class="form-group">
            <label>Lieu / Lien (optionnel)</label>
            <input v-model="appointmentData.location" type="text" placeholder="Lien visio, adresse..." class="form-input">
          </div>
          
          <div class="form-group">
            <label>Notes (optionnel)</label>
            <textarea v-model="appointmentData.notes" rows="3" placeholder="Informations supplémentaires..." class="form-textarea"></textarea>
          </div>
        </div>
        
        <div class="modal-footer">
          <button class="btn-cancel" @click="showAppointmentModal = false">Annuler</button>
          <button class="btn-create" @click="createAppointment" :disabled="creatingAppointment">
            {{ creatingAppointment ? 'Création...' : 'Créer' }}
          </button>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api/api'
import Layout from '@/layouts/Layout.vue'
import NotificationToast from '@/components/NotificationToast.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'

const route = useRoute()
const contacts = ref([])
const currentContact = ref(null)
const messages = ref([])
const newMessage = ref('')
const loading = ref(false)
const unreadCount = ref(0)
const messagesContainer = ref(null)
const showSidebar = ref(true)
const isMobile = ref(window.innerWidth < 768)
const sendingMessage = ref(false)

// Toast et confirmation
const toast = ref(null)
const confirmDialog = ref(null)

// Edition de message
const editingMessage = ref(null)
const editContent = ref('')

// Rendez-vous
const showAppointmentModal = ref(false)
const creatingAppointment = ref(false)
const appointmentData = ref({
  title: '',
  date: '',
  time: '',
  duration: 30,
  location: '',
  notes: '',
  type: 'video'
})

// Utilisateur
const userId = ref(null)

// Notifications
const showSuccess = (message, title = 'Succès') => {
  toast.value?.addNotification('success', title, message, 4000)
}

const showError = (message, title = 'Erreur') => {
  toast.value?.addNotification('error', title, message, 5000)
}

// Charger l'ID utilisateur
const loadUserId = () => {
  try {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      const user = JSON.parse(userStr)
      userId.value = user.id || null
    }
  } catch (e) {
    userId.value = null
  }
}

// Formatage des messages avec liens
const formatMessageWithLinks = (text) => {
  if (!text) return ''
  
  let formattedText = text.replace(/\/appointments\/(\d+)/g, (match, id) => {
    return `<a href="/appointments/${id}" class="appointment-link" style="display: inline-block; background: linear-gradient(135deg, #667eea, #764ba2); color: white; padding: 6px 14px; border-radius: 20px; text-decoration: none; margin-top: 8px; font-size: 13px; font-weight: 500;">🔗 Voir le rendez-vous</a>`
  })

  formattedText = formattedText.replace(/(https?:\/\/[^\s]+)/g, (url) => {
    if (!url.includes('/appointments/')) {
      return `<a href="${url}" target="_blank" rel="noopener noreferrer" style="color: #667eea; text-decoration: underline;">${url}</a>`
    }
    return url
  })

  formattedText = formattedText
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    .replace(/\n/g, '<br>')

  return formattedText
}

// Charger les contacts
const loadData = async () => {
  try {
    const res = await api.get('/messages')
    contacts.value = res.data.contacts || []
    unreadCount.value = res.data.unreadCount || 0
  } catch (err) {
    showError('Impossible de charger les contacts')
  }
}

// Charger une conversation
const loadConversation = async (contact) => {
  if (currentContact.value?.id === contact.id) return

  try {
    loading.value = true
    currentContact.value = contact
    const res = await api.get(`/messages/conversation/${contact.id}`)
    
    const rawMessages = res.data.messages || []
    messages.value = rawMessages.map(msg => ({
      id: msg.id,
      sender_id: msg.sender?.id || msg.sender_id,
      receiver_id: msg.receiver?.id || msg.receiver_id,
      content: msg.content || '',
      is_read: msg.is_read || msg.isRead || false,
      is_edited: msg.is_edited || false,
      is_appointment: msg.is_appointment || false,
      created_at: msg.created_at || msg.createdAt || new Date().toISOString()
    }))

    await nextTick()
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
    if (isMobile.value) {
      showSidebar.value = false
    }
    await loadUnreadCount()
  } catch (err) {
    showError('Impossible de charger la conversation')
  } finally {
    loading.value = false
  }
}

// Envoyer un message
const sendMessage = async () => {
  if (!newMessage.value.trim() || !currentContact.value || sendingMessage.value) return
  if (!userId.value) {
    showError('Utilisateur non connecté')
    return
  }

  sendingMessage.value = true
  const messageText = newMessage.value.trim()

  const tempMessage = {
    id: Date.now(),
    sender_id: userId.value,
    receiver_id: currentContact.value.id,
    content: messageText,
    is_read: false,
    is_edited: false,
    created_at: new Date().toISOString(),
    is_temp: true
  }

  messages.value = [...messages.value, tempMessage]
  newMessage.value = ''
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }

  try {
    const res = await api.post(`/messages/send/${currentContact.value.id}`, { content: messageText })
    
    const savedMessage = {
      id: res.data.id,
      sender_id: res.data.sender?.id || res.data.sender_id,
      receiver_id: res.data.receiver?.id || res.data.receiver_id,
      content: res.data.content,
      is_read: false,
      is_edited: false,
      created_at: new Date().toISOString()
    }

    const index = messages.value.findIndex(m => m.id === tempMessage.id)
    if (index !== -1) {
      messages.value[index] = { ...savedMessage, is_temp: false }
      messages.value = [...messages.value]
    }
  } catch (err) {
    const index = messages.value.findIndex(m => m.id === tempMessage.id)
    if (index !== -1) {
      messages.value[index].error = true
      messages.value = [...messages.value]
    }
    newMessage.value = messageText
    showError('Impossible d\'envoyer le message')
  } finally {
    sendingMessage.value = false
  }
}

// MODIFIER UN MESSAGE
const startEditMessage = (message) => {
  editingMessage.value = message
  editContent.value = message.content
}

const saveEditMessage = async () => {
  if (!editContent.value.trim()) return
  
  try {
    const response = await api.put(`/messages/${editingMessage.value.id}`, { content: editContent.value })
    
    const index = messages.value.findIndex(m => m.id === editingMessage.value.id)
    if (index !== -1) {
      messages.value[index].content = editContent.value
      messages.value[index].is_edited = true
      messages.value = [...messages.value]
    }
    
    showSuccess('Message modifié avec succès')
    editingMessage.value = null
    editContent.value = ''
  } catch (err) {
    showError('Erreur lors de la modification')
  }
}

const cancelEdit = () => {
  editingMessage.value = null
  editContent.value = ''
}

// SUPPRIMER UN MESSAGE
const deleteMessage = async (message) => {
  const confirmed = await confirmDialog.value?.show({
    title: 'Supprimer le message',
    message: 'Êtes-vous sûr de vouloir supprimer ce message ?',
    type: 'danger',
    confirmText: 'Supprimer',
    cancelText: 'Annuler'
  })
  
  if (confirmed) {
    try {
      await api.delete(`/messages/${message.id}`)
      messages.value = messages.value.filter(m => m.id !== message.id)
      showSuccess('Message supprimé avec succès')
    } catch (err) {
      showError('Erreur lors de la suppression')
    }
  }
}

// SUPPRIMER TOUTE LA CONVERSATION
const deleteConversation = async () => {
  if (!currentContact.value) return
  
  const confirmed = await confirmDialog.value?.show({
    title: 'Supprimer la conversation',
    message: `Êtes-vous sûr de vouloir supprimer toute la conversation avec ${currentContact.value.name} ? Cette action est irréversible.`,
    type: 'danger',
    confirmText: 'Supprimer tout',
    cancelText: 'Annuler'
  })
  
  if (confirmed) {
    try {
      await api.delete(`/messages/conversation/${currentContact.value.id}`)
      messages.value = []
      showSuccess('Conversation supprimée avec succès')
      await loadData()
      if (isMobile.value) {
        showSidebar.value = true
        currentContact.value = null
      }
    } catch (err) {
      showError('Erreur lors de la suppression de la conversation')
    }
  }
}

// MARQUER TOUS LES MESSAGES COMME LUS
const markAllAsRead = async () => {
  if (!currentContact.value) return
  
  try {
    await api.put(`/messages/read-all/${currentContact.value.id}`)
    messages.value = messages.value.map(m => {
      if (m.receiver_id === userId.value && m.sender_id === currentContact.value.id) {
        return { ...m, is_read: true }
      }
      return m
    })
    if (currentContact.value) currentContact.value.unread_count = 0
    await loadUnreadCount()
    showSuccess('Tous les messages ont été marqués comme lus')
  } catch (err) {
    showError('Erreur lors du marquage des messages')
  }
}

// Créer un rendez-vous
const createAppointment = async () => {
  if (!appointmentData.value.date || !appointmentData.value.time) {
    showError('Veuillez remplir la date et l\'heure')
    return
  }

  creatingAppointment.value = true

  try {
    const datetime = `${appointmentData.value.date}T${appointmentData.value.time}:00`

    await api.post('/appointments', {
      receiver_id: currentContact.value.id,
      title: appointmentData.value.title || `Rendez-vous avec ${currentContact.value.name}`,
      scheduled_at: datetime,
      duration: parseInt(appointmentData.value.duration),
      location: appointmentData.value.location,
      notes: appointmentData.value.notes,
      type: appointmentData.value.type
    })

    showAppointmentModal.value = false
    appointmentData.value = {
      title: '',
      date: '',
      time: '',
      duration: 30,
      location: '',
      notes: '',
      type: 'video'
    }
    
    showSuccess(`Rendez-vous créé avec ${currentContact.value.name}`)
    await loadConversation(currentContact.value)
  } catch (err) {
    showError('Erreur lors de la création du rendez-vous')
  } finally {
    creatingAppointment.value = false
  }
}

// Utilitaires
const getCurrentYear = () => new Date().getFullYear()
const getMinDate = () => new Date().toISOString().split('T')[0]
const getMaxDate = () => {
  const max = new Date()
  max.setFullYear(max.getFullYear() + 2)
  return max.toISOString().split('T')[0]
}

const formatTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

const formatMessageDate = (date) => {
  if (!date) return ''
  const msgDate = new Date(date)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  if (msgDate.toDateString() === today.toDateString()) return "Aujourd'hui"
  if (msgDate.toDateString() === yesterday.toDateString()) return 'Hier'
  return msgDate.toLocaleDateString('fr-FR', { day: 'numeric', month: 'long' })
}

const loadUnreadCount = async () => {
  try {
    const res = await api.get('/messages/unread-count')
    unreadCount.value = res.data.unreadCount || 0
  } catch (err) {}
}

const getRoleIcon = (role) => {
  switch (role) {
    case 'parent': return '👨‍👩‍👧'
    case 'teacher': return '📚'
    case 'psychologist': return '👩‍⚕️'
    default: return '👤'
  }
}

const backToContacts = () => {
  showSidebar.value = true
  currentContact.value = null
}

const handleResize = () => {
  isMobile.value = window.innerWidth < 768
  if (!isMobile.value) showSidebar.value = true
}

// Initialisation
onMounted(async () => {
  loadUserId()
  await loadData()
  await loadUnreadCount()
  window.addEventListener('resize', handleResize)
  
  if (route.query.contactId) {
    setTimeout(async () => {
      const contact = contacts.value.find(c => c.id === parseInt(route.query.contactId))
      if (contact) await loadConversation(contact)
    }, 500)
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
/* Garde tous les styles existants de ton code */
.chat-container { display: flex; height: calc(100vh - 120px); background: white; border-radius: 24px; overflow: hidden; margin: 20px; box-shadow: 0 4px 25px rgba(0,0,0,0.08); }
.chat-sidebar { width: 320px; border-right: 1px solid #e0e6ed; background: #f9fafb; transition: transform 0.3s ease; }
.sidebar-header { padding: 20px; border-bottom: 1px solid #e0e6ed; background: white; }
.header-title { display: flex; align-items: center; gap: 10px; }
.header-icon { font-size: 24px; }
.sidebar-header h2 { font-size: 20px; margin: 0; color: #1f2937; }
.unread-badge { background: linear-gradient(135deg, #ef4444, #dc2626); color: white; border-radius: 20px; padding: 2px 8px; font-size: 12px; font-weight: 600; margin-left: auto; }
.unread-badge-mini { background: linear-gradient(135deg, #ef4444, #dc2626); color: white; border-radius: 50%; min-width: 22px; height: 22px; padding: 0 6px; font-size: 11px; font-weight: 600; display: flex; align-items: center; justify-content: center; margin-left: auto; }
.contacts-list { flex: 1; overflow-y: auto; }
.contact-item { display: flex; align-items: center; gap: 12px; padding: 14px 20px; cursor: pointer; transition: all 0.2s ease; border-bottom: 1px solid #f0f0f0; }
.contact-item:hover { background: #f3f4f6; transform: translateX(4px); }
.contact-item.active { background: linear-gradient(135deg, #eef2ff, #e0e7ff); border-left: 3px solid #667eea; }
.contact-avatar { width: 48px; height: 48px; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #1a0a2e, #2d1b4e); color: white; font-weight: 700; font-size: 18px; flex-shrink: 0; }
.contact-avatar.large { width: 55px; height: 55px; font-size: 24px; }
.contact-info { flex: 1; }
.contact-name { font-weight: 600; color: #1f2937; margin-bottom: 4px; }
.contact-role { font-size: 12px; color: #6b7280; }
.chat-main { flex: 1; display: flex; flex-direction: column; background: white; }
.chat-header { display: flex; align-items: center; gap: 15px; padding: 16px 24px; border-bottom: 1px solid #e0e6ed; background: white; }
.back-btn { background: none; border: none; font-size: 24px; cursor: pointer; color: #667eea; padding: 5px; display: none; }
.contact-details { flex: 1; }
.contact-details h3 { margin: 0; font-size: 16px; color: #1f2937; }
.contact-details p { margin: 4px 0 0; font-size: 12px; color: #6b7280; }
.header-actions { display: flex; gap: 8px; margin-right: 8px; }
.mark-all-read-btn, .delete-conversation-btn { background: none; border: none; cursor: pointer; font-size: 18px; padding: 8px; border-radius: 50%; transition: all 0.2s; width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; }
.mark-all-read-btn:hover { background: rgba(16, 185, 129, 0.1); transform: scale(1.05); }
.delete-conversation-btn:hover { background: rgba(239, 68, 68, 0.1); transform: scale(1.05); }
.appointment-btn { padding: 8px 16px; background: linear-gradient(135deg, #10b981, #059669); color: white; border: none; border-radius: 20px; cursor: pointer; font-size: 14px; font-weight: 500; display: flex; align-items: center; gap: 6px; }
.appointment-btn:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3); }
.chat-placeholder { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16px; }
.placeholder-icon { font-size: 80px; animation: bounce 2s ease infinite; }
@keyframes bounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-10px); } }
.messages-area { flex: 1; overflow-y: auto; padding: 20px; background: #f9fafb; }
.message-date-separator { text-align: center; margin: 20px 0 16px; }
.message-date-separator span { font-size: 11px; background: #e5e7eb; padding: 4px 12px; border-radius: 20px; color: #6b7280; }
.message { display: flex; margin-bottom: 16px; animation: messageAppear 0.3s ease; }
@keyframes messageAppear { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
.message-sent { justify-content: flex-end; }
.message-received { justify-content: flex-start; }
.message-appointment .message-bubble { background: linear-gradient(135deg, #d1fae5, #a7f3d0); border: 1px solid #10b981; }
.message-bubble { max-width: 70%; padding: 10px 14px; border-radius: 18px; position: relative; }
.message-sent .message-bubble { background: linear-gradient(135deg, #667eea, #764ba2); color: #1f2937; border-bottom-right-radius: 4px; }
.message-received .message-bubble { background: white; color: #1f2937; border-bottom-left-radius: 4px; box-shadow: 0 1px 2px rgba(0,0,0,0.05); }
.appointment-badge { font-size: 11px; font-weight: 700; color: #667eea; background: rgba(102, 126, 234, 0.1); padding: 3px 10px; border-radius: 20px; margin-bottom: 8px; display: inline-block; text-transform: uppercase; }
.message-content { font-size: 14px; word-wrap: break-word; line-height: 1.5; white-space: pre-wrap; }
.message-time { font-size: 10px; margin-top: 4px; opacity: 0.7; text-align: right; }
.message-actions { display: flex; gap: 8px; margin-top: 8px; justify-content: flex-end; }
.action-edit, .action-delete { background: none; border: none; cursor: pointer; font-size: 11px; padding: 4px 6px; border-radius: 8px; transition: all 0.2s; opacity: 0.6; }
.message-sent .action-edit:hover, .message-sent .action-delete:hover { opacity: 1; background: rgba(255, 255, 255, 0.2); }
.message-received .action-edit:hover, .message-received .action-delete:hover { opacity: 1; background: rgba(0, 0, 0, 0.1); }
.edited-badge { font-size: 9px; opacity: 0.6; margin-left: 6px; }
.chat-input-area { display: flex; gap: 12px; padding: 16px 24px; border-top: 1px solid #e0e6ed; background: white; }
.chat-input { flex: 1; padding: 12px 18px; border: 2px solid #e0e6ed; border-radius: 30px; font-size: 14px; outline: none; }
.chat-input:focus { border-color: #667eea; box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1); }
.send-btn { display: flex; align-items: center; gap: 8px; padding: 12px 24px; background: linear-gradient(135deg, #667eea, #764ba2); color: white; border: none; border-radius: 30px; font-weight: 600; cursor: pointer; }
.send-btn:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3); }
.send-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.loading-messages, .no-messages, .no-contacts { text-align: center; padding: 60px 20px; color: #9ca3af; }
.no-messages-icon, .no-contacts-icon { font-size: 48px; margin-bottom: 16px; opacity: 0.5; }
.spinner-small { width: 30px; height: 30px; border: 2px solid #e0e6ed; border-top-color: #667eea; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 12px; }
@keyframes spin { to { transform: rotate(360deg); } }
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-content { background: white; border-radius: 24px; width: 90%; max-width: 500px; max-height: 90vh; overflow-y: auto; }
.edit-modal .modal-content { max-width: 500px; }
.edit-textarea { width: 100%; padding: 12px; border: 2px solid #e0e6ed; border-radius: 12px; font-size: 14px; font-family: inherit; resize: vertical; }
.edit-textarea:focus { outline: none; border-color: #667eea; }
.modal-header { display: flex; justify-content: space-between; padding: 20px 24px; border-bottom: 1px solid #e0e6ed; }
.modal-header h2 { margin: 0; font-size: 24px; color: #1f2937; }
.modal-close { background: none; border: none; font-size: 24px; cursor: pointer; color: #6b7280; }
.modal-body { padding: 24px; }
.modal-footer { display: flex; justify-content: flex-end; gap: 12px; padding: 20px 24px; border-top: 1px solid #e0e6ed; }
.info-message { background: linear-gradient(135deg, #e0e7ff, #f3e8ff); padding: 10px 12px; border-radius: 12px; margin-bottom: 20px; font-size: 13px; color: #4c1d95; border-left: 3px solid #667eea; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; margin-bottom: 8px; font-weight: 500; color: #374151; font-size: 14px; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.form-input, .form-textarea, select.form-input { width: 100%; padding: 10px 12px; border: 2px solid #e0e6ed; border-radius: 12px; font-size: 14px; font-family: inherit; box-sizing: border-box; }
.form-input:focus, .form-textarea:focus, select.form-input:focus { outline: none; border-color: #667eea; box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1); }
.form-textarea { resize: vertical; }
.appointment-types { display: flex; gap: 10px; flex-wrap: wrap; }
.type-option { display: flex; align-items: center; gap: 8px; cursor: pointer; padding: 8px 14px; border: 2px solid #e0e6ed; border-radius: 12px; font-size: 14px; }
.type-option:hover { border-color: #667eea; background: #f9fafb; }
.type-option input[type="radio"] { margin: 0; cursor: pointer; }
.btn-cancel, .btn-create { padding: 10px 20px; border-radius: 12px; font-size: 14px; font-weight: 500; cursor: pointer; }
.btn-cancel { background: white; border: 2px solid #e0e6ed; color: #6b7280; }
.btn-cancel:hover { border-color: #9ca3af; color: #374151; }
.btn-create { background: linear-gradient(135deg, #10b981, #059669); border: none; color: white; }
.btn-create:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3); }
.btn-create:disabled { opacity: 0.5; cursor: not-allowed; }
@media (max-width: 768px) {
  .chat-container { margin: 10px; height: calc(100vh - 100px); }
  .chat-sidebar { position: absolute; z-index: 10; height: calc(100vh - 120px); background: white; box-shadow: 2px 0 10px rgba(0,0,0,0.1); }
  .chat-sidebar.sidebar-hidden { transform: translateX(-100%); }
  .chat-main.chat-full { width: 100%; }
  .back-btn { display: block; }
  .message-bubble { max-width: 85%; }
  .form-row { grid-template-columns: 1fr; }
  .modal-content { width: 95%; margin: 20px; }
}
</style>