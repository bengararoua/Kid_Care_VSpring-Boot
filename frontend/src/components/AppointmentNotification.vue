<template>
  <Transition name="notification">
    <div v-if="visible" class="appointment-notification" :class="type">
      <div class="notification-icon">
        <span v-if="type === 'success'">✅</span>
        <span v-else-if="type === 'confirmed'">✓</span>
        <span v-else-if="type === 'cancelled'">❌</span>
        <span v-else-if="type === 'pending'">⏳</span>
        <span v-else>📅</span>
      </div>
      <div class="notification-content">
        <div class="notification-title">{{ title }}</div>
        <div class="notification-message">{{ message }}</div>
        <div v-if="appointmentDetails" class="notification-details">
          <div class="detail-chip">
            <span class="chip-icon">📅</span>
            <span>{{ formatDate(appointmentDetails.scheduled_at) }}</span>
          </div>
          <div class="detail-chip">
            <span class="chip-icon">⏰</span>
            <span>{{ formatTime(appointmentDetails.scheduled_at) }}</span>
          </div>
          <div class="detail-chip">
            <span class="chip-icon">⏱️</span>
            <span>{{ appointmentDetails.duration }} min</span>
          </div>
        </div>
      </div>
      <button @click="close" class="notification-close">✕</button>
      <div class="notification-progress"></div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'info'
  },
  title: {
    type: String,
    default: 'Notification'
  },
  message: {
    type: String,
    default: ''
  },
  duration: {
    type: Number,
    default: 5000
  },
  appointmentDetails: {
    type: Object,
    default: null
  }
})

const visible = ref(true)
let timeout = null

const emit = defineEmits(['close'])

const close = () => {
  visible.value = false
  if (timeout) clearTimeout(timeout)
  emit('close')
}

const formatDate = (date) => {
  return new Date(date).toLocaleDateString('fr-FR', {
    day: 'numeric',
    month: 'short'
  })
}

const formatTime = (date) => {
  return new Date(date).toLocaleTimeString('fr-FR', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  timeout = setTimeout(() => {
    close()
  }, props.duration)
})

onUnmounted(() => {
  if (timeout) clearTimeout(timeout)
})
</script>

<style scoped>
.appointment-notification {
  position: fixed;
  top: 80px;
  right: 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px 20px;
  min-width: 320px;
  max-width: 420px;
  z-index: 10000;
  overflow: hidden;
  animation: slideInRight 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.notification-enter-active,
.notification-leave-active {
  transition: all 0.3s ease;
}

.notification-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.notification-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

/* Types de notifications */
.appointment-notification.success {
  border-left: 4px solid #10b981;
  background: linear-gradient(135deg, #ffffff, #f0fdf4);
}

.appointment-notification.confirmed {
  border-left: 4px solid #3b82f6;
  background: linear-gradient(135deg, #ffffff, #eff6ff);
}

.appointment-notification.cancelled {
  border-left: 4px solid #ef4444;
  background: linear-gradient(135deg, #ffffff, #fef2f2);
}

.appointment-notification.pending {
  border-left: 4px solid #f59e0b;
  background: linear-gradient(135deg, #ffffff, #fffbeb);
}

.appointment-notification.info {
  border-left: 4px solid #8b5cf6;
  background: linear-gradient(135deg, #ffffff, #f5f3ff);
}

/* Icône */
.notification-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.success .notification-icon {
  background: #d1fae5;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.2);
}

.confirmed .notification-icon {
  background: #dbeafe;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
}

.cancelled .notification-icon {
  background: #fee2e2;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.2);
}

.pending .notification-icon {
  background: #fef3c7;
  box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.2);
}

.info .notification-icon {
  background: #ede9fe;
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.2);
}

/* Contenu */
.notification-content {
  flex: 1;
}

.notification-title {
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.notification-message {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.4;
}

.notification-details {
  display: flex;
  gap: 10px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.detail-chip {
  background: rgba(0, 0, 0, 0.05);
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 11px;
  display: flex;
  align-items: center;
  gap: 4px;
  color: #4b5563;
}

.chip-icon {
  font-size: 11px;
}

/* Bouton fermer */
.notification-close {
  background: none;
  border: none;
  font-size: 14px;
  cursor: pointer;
  color: #9ca3af;
  padding: 4px;
  border-radius: 8px;
  transition: all 0.2s;
  flex-shrink: 0;
}

.notification-close:hover {
  background: #f3f4f6;
  color: #ef4444;
  transform: scale(1.1);
}

/* Barre de progression */
.notification-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 3px;
  background: linear-gradient(90deg, #667eea, #764ba2);
  animation: progress 5s linear forwards;
}

.success .notification-progress {
  background: linear-gradient(90deg, #10b981, #059669);
}

.confirmed .notification-progress {
  background: linear-gradient(90deg, #3b82f6, #2563eb);
}

.cancelled .notification-progress {
  background: linear-gradient(90deg, #ef4444, #dc2626);
}

.pending .notification-progress {
  background: linear-gradient(90deg, #f59e0b, #d97706);
}

@keyframes progress {
  from {
    width: 100%;
  }
  to {
    width: 0%;
  }
}

/* Responsive */
@media (max-width: 500px) {
  .appointment-notification {
    left: 20px;
    right: 20px;
    min-width: auto;
    max-width: none;
  }
  
  .notification-details {
    flex-wrap: wrap;
  }
}
</style>