<template>
  <Transition name="modal">
    <div v-if="show" class="modal-overlay" @click.self="close">
      <div class="log-success-modal">
        <div class="modal-header">
          <div class="success-icon">🎉</div>
          <h2>Félicitations !</h2>
          <button @click="close" class="close-btn">✕</button>
        </div>

        <div class="modal-body">
          <!-- Message de motivation -->
          <p class="congrats-message">
            Tu as gagné <strong>{{ logData.pointsEarned || 10 }} points</strong> pour avoir enregistré le comportement de {{ getChildName() }} !
          </p>

          <!-- Statistiques -->
          <div class="stats-container">
            <div class="stat-box">
              <div class="stat-value">{{ logData.totalLogs || 0 }}</div>
              <div class="stat-label">total logs</div>
            </div>
            <div class="stat-box">
              <div class="stat-value">{{ logData.focusDiff || 0 }}</div>
              <div class="stat-label">vs semaine dernière</div>
            </div>
            <div class="stat-box">
              <div class="stat-value">{{ logData.weeklyAvg || '—' }}</div>
              <div class="stat-label">moyenne focus</div>
            </div>
          </div>

          <!-- Météo des émotions -->
          <div class="emotion-weather">
            <h3>🌤️ Météo des émotions</h3>
            <div class="emotion-grid">
              <div class="emotion-item" v-for="(count, moodName) in moodStats" :key="moodName">
                <div class="emotion-emoji">{{ getMoodEmoji(moodName) }}</div>
                <div class="emotion-name">{{ moodName }}</div>
                <div class="emotion-count">{{ count }}x</div>
                <div class="emotion-bar">
                  <div class="emotion-fill" :style="{
                    width: (count / maxMoodCount) * 100 + '%',
                    background: getMoodColor(moodName)
                  }"></div>
                </div>
              </div>
            </div>
            <div class="emotion-message">
              <span v-if="mostCommonMood === 'happy'">🌟 Super journée ! Continuez ainsi !</span>
              <span v-else-if="mostCommonMood === 'sad'">💪 Un petit coup de mou, c'est normal</span>
              <span v-else-if="mostCommonMood === 'angry'">😤 Besoin de calme ? Essayez un jeu relaxant</span>
              <span v-else-if="mostCommonMood === 'anxious'">😰 Rassurez-le avec des câlins et de la douceur</span>
              <span v-else-if="mostCommonMood === 'neutral'">☁️ Une journée tranquille, c'est ok aussi</span>
              <span v-else>🎯 Gardez le rythme !</span>
            </div>
          </div>

          <!-- Informations du log -->
          <div class="log-details">
            <div class="detail-row">
              <span class="detail-icon">🧠</span>
              <span class="detail-label">Focus :</span>
              <span class="detail-value">{{ logData.focus_level || 3 }}/5</span>
            </div>
            <div class="detail-row">
              <span class="detail-icon">😴</span>
              <span class="detail-label">Sommeil :</span>
              <span class="detail-value">{{ logData.sleep_hours || 8 }}h</span>
            </div>
            <div class="detail-row">
              <span class="detail-icon">😊</span>
              <span class="detail-label">Humeur :</span>
              <span class="detail-value">{{ getMoodEmoji(logData.mood) }} {{ logData.mood || 'neutral' }}</span>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="close" class="continue-btn">Continuer</button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  logData: {
    type: Object,
    default: () => ({
      mood: 'neutral',
      focus_level: 3,
      sleep_hours: 8,
      totalLogs: 0,
      focusDiff: 0,
      weeklyAvg: '—',
      pointsEarned: 10
    })
  },
  childName: {
    type: String,
    default: 'l\'enfant'
  },
  moodStats: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['close'])

const close = () => {
  emit('close')
}

const getChildName = () => {
  return props.childName || 'votre enfant'
}

// Calculs pour la météo
const maxMoodCount = computed(() => {
  const values = Object.values(props.moodStats)
  return values.length > 0 ? Math.max(...values) : 1
})

const mostCommonMood = computed(() => {
  const stats = props.moodStats
  if (Object.keys(stats).length === 0) return 'neutral'
  return Object.entries(stats).sort((a, b) => b[1] - a[1])[0]?.[0] || 'neutral'
})

const getMoodEmoji = (mood) => {
  const emojis = {
    happy: '😊',
    sad: '😢',
    angry: '😡',
    neutral: '😐',
    excited: '🤩',
    anxious: '😰'
  }
  return emojis[mood] || '😊'
}

const getMoodColor = (mood) => {
  const colors = {
    happy: '#10b981',
    sad: '#3b82f6',
    angry: '#ef4444',
    neutral: '#6b7280',
    excited: '#f59e0b',
    anxious: '#8b5cf6'
  }
  return colors[mood] || '#6b7280'
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.log-success-modal {
  background: white;
  border-radius: 28px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  padding: 24px 24px 16px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 28px 28px 0 0;
  color: white;
}

.success-icon {
  font-size: 48px;
  margin-right: 12px;
  animation: bounce 0.5s ease;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.modal-header h2 {
  font-size: 24px;
  margin: 0;
  font-weight: 700;
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  font-size: 18px;
  cursor: pointer;
  color: white;
  transition: all 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.05);
}

.modal-body {
  padding: 24px;
}

.congrats-message {
  text-align: center;
  font-size: 15px;
  color: #4b5563;
  margin-bottom: 24px;
  line-height: 1.5;
}

.congrats-message strong {
  color: #10b981;
  font-size: 18px;
}

.stats-container {
  display: flex;
  justify-content: space-around;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 20px;
}

.stat-box {
  text-align: center;
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 11px;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* Météo des émotions */
.emotion-weather {
  background: #f9fafb;
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 24px;
}

.emotion-weather h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #1f2937;
}

.emotion-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(80px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.emotion-item {
  text-align: center;
  padding: 10px;
  background: white;
  border-radius: 16px;
  transition: transform 0.2s;
}

.emotion-item:hover {
  transform: translateY(-2px);
}

.emotion-emoji {
  font-size: 28px;
  margin-bottom: 6px;
}

.emotion-name {
  font-size: 11px;
  font-weight: 600;
  color: #6b7280;
  text-transform: capitalize;
  margin-bottom: 4px;
}

.emotion-count {
  font-size: 13px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 6px;
}

.emotion-bar {
  height: 4px;
  background: #e5e7eb;
  border-radius: 2px;
  overflow: hidden;
}

.emotion-fill {
  height: 100%;
  border-radius: 2px;
  transition: width 0.5s ease;
}

.emotion-message {
  text-align: center;
  padding: 12px;
  background: linear-gradient(135deg, #f3f4f6, #e5e7eb);
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
  color: #374151;
}

/* Détails du log */
.log-details {
  background: #f9fafb;
  border-radius: 16px;
  padding: 16px;
}

.detail-row {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #e5e7eb;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-icon {
  font-size: 20px;
  width: 40px;
}

.detail-label {
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
  width: 70px;
}

.detail-value {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.modal-footer {
  padding: 16px 24px 24px;
}

.continue-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  border-radius: 40px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.continue-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* Responsive */
@media (max-width: 500px) {
  .stats-container {
    flex-wrap: wrap;
  }
  
  .emotion-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .modal-header h2 {
    font-size: 20px;
  }
  
  .success-icon {
    font-size: 36px;
  }
}
</style>