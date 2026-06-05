<template>
  <div class="behavioral-test-container">
    <div class="test-header">
      <div class="test-icon">🧠</div>
      <h2>Test de dépistage comportemental</h2>
      <p class="test-subtitle">Évaluez les comportements de votre enfant dans différents domaines</p>
      <div class="disclaimer-badge">
        ⚠️ Outil de dépistage - Ne remplace pas un diagnostic médical
      </div>
    </div>

    <!-- Sélection de l'enfant -->
    <div v-if="children.length > 0 && !selectedChild" class="child-select-section">
      <h3>👶 Sélectionnez un enfant</h3>
      <div class="children-grid-select">
        <div 
          v-for="child in children" 
          :key="child.id" 
          @click="selectChild(child)"
          class="child-select-card"
        >
          <div class="child-avatar-select">{{ child.name.charAt(0) }}</div>
          <div class="child-name-select">{{ child.name }}</div>
          <div class="child-age-select">{{ child.age }} ans</div>
          <button class="btn-select">Commencer le test →</button>
        </div>
      </div>
    </div>

    <!-- Historique des tests -->
    <div v-if="selectedChild && !showTestForm && !testResult" class="test-history-section">
      <div class="history-header">
        <div class="back-to-select" @click="selectedChild = null">
          ← Changer d'enfant
        </div>
        <button @click="startNewTest" class="btn-start-test">
          📝 Nouveau test
        </button>
      </div>

      <div class="child-info-banner">
        <div class="child-avatar-history">{{ selectedChild.name.charAt(0) }}</div>
        <div class="child-details">
          <h3>{{ selectedChild.name }}</h3>
          <p>{{ selectedChild.age }} ans · {{ userRole === 'parent' ? 'Parent' : 'Enseignant' }}</p>
        </div>
      </div>

      <div v-if="testHistory.length > 0" class="history-list">
        <h3>📋 Historique des tests</h3>
        <div class="history-grid">
          <div 
            v-for="test in testHistory" 
            :key="test.id" 
            class="history-card"
            :style="{ borderLeftColor: getRiskColor(test.risk_level) }"
            @click="viewTestResult(test)"
          >
            <div class="history-date">{{ formatDate(test.created_at) }}</div>
            <div class="history-role">Réalisé par: {{ test.user_role === 'parent' ? 'Parent' : 'Enseignant' }}</div>
            <div class="history-risk">
              <span class="risk-badge" :style="{ background: getRiskColor(test.risk_level) }">
                {{ getRiskLabel(test.risk_level) }}
              </span>
            </div>
            <div class="history-score">Score global: {{ test.scores.overall.percentage }}%</div>
          </div>
        </div>
      </div>

      <div v-else class="empty-history">
        <div class="empty-icon">📭</div>
        <p>Aucun test réalisé pour cet enfant</p>
        <p class="empty-hint">Cliquez sur "Nouveau test" pour commencer</p>
      </div>
    </div>

    <!-- Formulaire du test -->
    <div v-if="showTestForm && selectedChild" class="test-form">
      <div class="test-progress">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progressPercentage + '%' }"></div>
        </div>
        <div class="progress-text">Question {{ currentQuestionIndex + 1 }} / {{ totalQuestions }}</div>
      </div>

      <div class="question-container">
        <div class="category-badge" :style="{ background: currentCategoryColor }">
          {{ currentCategoryIcon }} {{ getCategoryName(currentCategory) }}
        </div>
        
        <h3 class="question-text">{{ currentQuestion.text }}</h3>
        
        <div class="options-container">
          <div 
            v-for="(label, value) in currentQuestion.options" 
            :key="value"
            @click="selectAnswer(value)"
            class="option-card"
            :class="{ selected: answers[currentQuestionKey] === value }"
          >
            <div class="option-radio">
              <div class="radio-circle" :class="{ checked: answers[currentQuestionKey] === value }"></div>
            </div>
            <div class="option-label">{{ label }}</div>
          </div>
        </div>

        <div class="navigation-buttons">
          <button 
            v-if="currentQuestionIndex > 0" 
            @click="previousQuestion" 
            class="btn-nav prev"
          >
            ← Précédent
          </button>
          <button 
            v-if="currentQuestionIndex < totalQuestions - 1" 
            @click="nextQuestion" 
            class="btn-nav next"
            :disabled="!answers[currentQuestionKey]"
          >
            Suivant →
          </button>
          <button 
            v-if="currentQuestionIndex === totalQuestions - 1" 
            @click="submitTest" 
            class="btn-submit"
            :disabled="!allQuestionsAnswered || submitting"
          >
            {{ submitting ? 'Analyse en cours...' : '✅ Voir les résultats' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Résultats du test -->
    <div v-if="testResult" class="test-results">
      <div class="results-header" :style="{ borderBottomColor: testResult.risk_color }">
        <div class="results-icon" :style="{ background: testResult.risk_color }">
          {{ getRiskIcon(testResult.risk_level) }}
        </div>
        <h2>Résultats du test</h2>
        <p class="test-date">{{ formatDate(testResult.test.created_at) }}</p>
      </div>

      <div class="disclaimer-box">
        <div class="disclaimer-icon">⚠️</div>
        <div class="disclaimer-content">
          <strong>Information importante</strong>
          <p>{{ testResult.disclaimer }}</p>
        </div>
      </div>

      <div class="risk-card" :style="{ background: testResult.risk_color + '15', borderColor: testResult.risk_color }">
        <div class="risk-level">
          <span class="risk-label">Niveau de risque</span>
          <span class="risk-value" :style="{ color: testResult.risk_color }">
            {{ testResult.risk_label }}
          </span>
        </div>
        <div class="risk-score">Score global: <strong>{{ testResult.scores.overall.percentage }}%</strong></div>
        <div class="risk-message">{{ testResult.feedback_message }}</div>
      </div>

      <div class="scores-section">
        <h3>📊 Scores par domaine</h3>
        <div class="scores-grid">
          <div v-for="(score, category) in testResult.scores" :key="category" class="score-card" v-if="category !== 'overall'">
            <div class="score-header">
              <span class="score-category">{{ getCategoryIcon(category) }} {{ getCategoryName(category) }}</span>
              <span class="score-percentage">{{ score.percentage }}%</span>
            </div>
            <div class="score-bar">
              <div class="score-fill" :style="{ width: score.percentage + '%', background: getScoreColor(score.percentage) }"></div>
            </div>
            <div class="score-detail">{{ score.total }}/{{ score.max }}</div>
          </div>
        </div>
      </div>

      <div class="feedback-section" v-if="testResult.detailed_feedback">
        <h3>💬 Analyse détaillée</h3>
        <div class="feedback-list">
          <div 
            v-for="item in testResult.detailed_feedback" 
            :key="item.category" 
            class="feedback-item"
            :class="item.level"
          >
            <span class="feedback-icon">{{ item.level === 'high' ? '⚠️' : (item.level === 'moderate' ? '📌' : '✅') }}</span>
            <span>{{ item.message }}</span>
          </div>
        </div>
      </div>

      <div class="recommendations-section">
        <h3>🎯 Recommandations personnalisées</h3>
        <ul class="recommendations-list">
          <li v-for="(rec, index) in testResult.recommendations" :key="index">{{ rec }}</li>
        </ul>
      </div>

      <div class="results-actions">
        <button @click="resetTest" class="btn-new-test">📝 Nouveau test</button>
        <button @click="exportResults" class="btn-export">📄 Exporter les résultats</button>
        <button v-if="canContactPsychologist" @click="contactPsychologist" class="btn-contact-psychologist">👩‍⚕️ Contacter un psychologue</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const children = ref([])
const selectedChild = ref(null)
const questionsData = ref(null)
const categories = ref({})
const currentQuestionIndex = ref(0)
const answers = ref({})
const showTestForm = ref(false)
const testResult = ref(null)
const testHistory = ref([])
const submitting = ref(false)
const userRole = ref('parent')

// Charger les enfants
const loadChildren = async () => {
  try {
    const token = localStorage.getItem('token')
    const res = await axios.get('/api/children', {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    children.value = res.data || []
  } catch (err) {
    console.error('Error loading children:', err)
  }
}

// Charger les questions
const loadQuestions = async () => {
  try {
    const token = localStorage.getItem('token')
    const res = await axios.get('/api/behavioral-test/questions', {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    questionsData.value = res.data.questions
    categories.value = res.data.categories
    console.log('✅ Questions chargées:', Object.keys(questionsData.value))
  } catch (err) {
    console.error('Error loading questions:', err)
  }
}

// Charger l'historique
const loadTestHistory = async (childId) => {
  try {
    const token = localStorage.getItem('token')
    const res = await axios.get(`/api/behavioral-test/${childId}/history`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    testHistory.value = res.data || []
  } catch (err) {
    console.error('Error loading test history:', err)
  }
}

// Sélectionner un enfant
const selectChild = async (child) => {
  selectedChild.value = child
  await loadTestHistory(child.id)
}

// Démarrer un nouveau test
const startNewTest = () => {
  answers.value = {}
  currentQuestionIndex.value = 0
  testResult.value = null
  showTestForm.value = true
}

// Afficher un résultat existant
const viewTestResult = (test) => {
  testResult.value = {
    test: test,
    scores: test.scores,
    risk_level: test.risk_level,
    risk_label: getRiskLabel(test.risk_level),
    risk_color: getRiskColor(test.risk_level),
    feedback_message: test.feedback_message,
    recommendations: test.recommendations,
    disclaimer: "⚠️ Ce test est un outil de dépistage et non un diagnostic médical. Seul un professionnel de santé peut poser un diagnostic."
  }
}

// Propriétés calculées
const allQuestions = computed(() => {
  if (!questionsData.value) return []
  const questions = []
  for (const [category, qs] of Object.entries(questionsData.value)) {
    qs.forEach((q, index) => {
      questions.push({ ...q, category, key: `${category}_${index + 1}` })
    })
  }
  return questions
})

const totalQuestions = computed(() => allQuestions.value.length)
const currentQuestion = computed(() => allQuestions.value[currentQuestionIndex.value] || {})
const currentQuestionKey = computed(() => currentQuestion.value.key)
const currentCategory = computed(() => currentQuestion.value.category)
const currentCategoryIcon = computed(() => categories.value[currentCategory.value]?.icon || '📋')
const currentCategoryColor = computed(() => categories.value[currentCategory.value]?.color || '#6b7280')
const progressPercentage = computed(() => (Object.keys(answers.value).length / totalQuestions.value) * 100)
const allQuestionsAnswered = computed(() => Object.keys(answers.value).length === totalQuestions.value)
const canContactPsychologist = computed(() => selectedChild.value && selectedChild.value.psychologistId)

// Méthodes
const selectAnswer = (value) => { answers.value[currentQuestionKey.value] = value }
const nextQuestion = () => { if (answers.value[currentQuestionKey.value]) currentQuestionIndex.value++ }
const previousQuestion = () => { if (currentQuestionIndex.value > 0) currentQuestionIndex.value-- }

// Soumettre le test
const submitTest = async () => {
  if (!allQuestionsAnswered.value) return
  
  submitting.value = true
  try {
    const token = localStorage.getItem('token')
    const payload = {
      responses: answers.value
    }
    
    console.log('📤 Envoi du test:', payload)
    console.log('URL:', `/api/behavioral-test/${selectedChild.value.id}/submit`)
    
    const response = await axios.post(
      `/api/behavioral-test/${selectedChild.value.id}/submit`,
      payload,
      {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        timeout: 60000
      }
    )
    
    console.log('✅ Réponse reçue:', response.data)
    testResult.value = response.data
    showTestForm.value = false
    await loadTestHistory(selectedChild.value.id)
    
  } catch (err) {
    console.error(' Error submitting test:', err)
    if (err.response) {
      console.error('Response status:', err.response.status)
      console.error('Response data:', err.response.data)
    } else if (err.request) {
      console.error('No response received:', err.request)
    }
  } finally {
    submitting.value = false
  }
}

const resetTest = () => {
  testResult.value = null
  answers.value = {}
  currentQuestionIndex.value = 0
  showTestForm.value = true
}

const exportResults = () => {
  if (!testResult.value) return
  const data = { child: selectedChild.value, test: testResult.value.test, date: new Date().toLocaleString('fr-FR') }
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `test_${selectedChild.value.name}_${Date.now()}.json`
  a.click()
  URL.revokeObjectURL(url)
}

const contactPsychologist = () => {
  if (selectedChild.value && selectedChild.value.psychologistId) {
    router.push({ path: '/messages', query: { contactId: selectedChild.value.psychologistId, contactName: 'Psychologue', contactRole: 'psychologist' } })
  } else { router.push('/messages') }
}

const getCategoryName = (cat) => categories.value[cat]?.name || cat
const getCategoryIcon = (cat) => categories.value[cat]?.icon || '📋'
const getRiskLabel = (risk) => ({ low: 'Faible - Préoccupations mineures', moderate: 'Modéré - Surveillance recommandée', high: 'Élevé - Consultation recommandée' }[risk] || risk)
const getRiskColor = (risk) => ({ low: '#10b981', moderate: '#f59e0b', high: '#ef4444' }[risk] || '#6b7280')
const getRiskIcon = (risk) => ({ low: '🌟', moderate: '📊', high: '⚠️' }[risk] || '📋')
const getScoreColor = (pct) => pct >= 60 ? '#ef4444' : (pct >= 35 ? '#f59e0b' : '#10b981')
const formatDate = (date) => new Date(date).toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit' })

onMounted(() => {
  const userData = localStorage.getItem('user')
  if (userData) { try { userRole.value = (JSON.parse(userData).role || 'parent').toLowerCase() } catch(e) {} }
  loadChildren()
  loadQuestions()
})
</script>

<style scoped>
.behavioral-test-container { max-width: 900px; margin: 0 auto; padding: 30px 20px; animation: fadeIn 0.5s ease; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

.test-header { text-align: center; margin-bottom: 40px; }
.test-icon { font-size: 60px; margin-bottom: 16px; animation: bounce 2s ease infinite; }
@keyframes bounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-10px); } }
.test-header h2 { font-size: 28px; color: #1f2937; margin-bottom: 8px; }
.test-subtitle { color: #6b7280; margin-bottom: 16px; }
.disclaimer-badge { display: inline-block; background: #fef3c7; color: #92400e; padding: 8px 16px; border-radius: 40px; font-size: 12px; font-weight: 600; }
.disclaimer-box { display: flex; gap: 16px; padding: 16px 20px; background: #fef3c7; border-radius: 16px; margin-bottom: 24px; border-left: 4px solid #f59e0b; }
.disclaimer-icon { font-size: 24px; }
.disclaimer-content strong { display: block; margin-bottom: 4px; color: #92400e; }
.disclaimer-content p { font-size: 13px; color: #78350f; margin: 0; }

.child-select-section { background: white; border-radius: 24px; padding: 30px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }
.child-select-section h3 { margin-bottom: 20px; font-size: 20px; }
.children-grid-select { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 20px; }
.child-select-card { background: #f9fafb; border-radius: 20px; padding: 20px; text-align: center; cursor: pointer; transition: all 0.3s; border: 2px solid transparent; }
.child-select-card:hover { transform: translateY(-5px); border-color: #667eea; box-shadow: 0 8px 25px rgba(102, 126, 234, 0.15); }
.child-avatar-select { width: 70px; height: 70px; background: linear-gradient(135deg, #667eea, #764ba2); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 32px; font-weight: 700; color: white; margin: 0 auto 12px; }
.child-name-select { font-size: 18px; font-weight: 600; color: #1f2937; }
.child-age-select { font-size: 13px; color: #6b7280; margin: 4px 0 12px; }
.btn-select { background: linear-gradient(135deg, #667eea, #764ba2); color: white; border: none; padding: 8px 16px; border-radius: 30px; font-weight: 600; cursor: pointer; width: 100%; }

.test-history-section { animation: fadeIn 0.3s ease; }
.history-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 12px; }
.back-to-select { color: #667eea; cursor: pointer; font-size: 14px; }
.back-to-select:hover { text-decoration: underline; }
.btn-start-test { background: linear-gradient(135deg, #1a0a2e, #2d1b4e); color: white; border: none; padding: 10px 24px; border-radius: 40px; font-weight: 600; cursor: pointer; transition: all 0.3s; }
.btn-start-test:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(26, 10, 46, 0.3); }
.child-info-banner { display: flex; align-items: center; gap: 16px; padding: 20px; background: linear-gradient(135deg, #f3e8ff, #e0e7ff); border-radius: 20px; margin-bottom: 30px; }
.child-avatar-history { width: 60px; height: 60px; background: linear-gradient(135deg, #667eea, #764ba2); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 28px; font-weight: 700; color: white; }
.child-details h3 { margin: 0; font-size: 18px; }
.child-details p { margin: 4px 0 0; font-size: 13px; color: #4b5563; }
.history-grid { display: flex; flex-direction: column; gap: 12px; }
.history-card { background: white; border-left: 4px solid; padding: 16px 20px; border-radius: 12px; cursor: pointer; transition: all 0.2s; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.history-card:hover { transform: translateX(4px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.history-date { font-size: 12px; color: #9ca3af; }
.history-role { font-size: 13px; font-weight: 500; color: #4b5563; margin: 4px 0; }
.risk-badge { display: inline-block; padding: 4px 12px; border-radius: 20px; font-size: 11px; font-weight: 600; color: white; }
.history-score { font-size: 13px; color: #6b7280; margin-top: 4px; }
.empty-history { text-align: center; padding: 60px; background: white; border-radius: 20px; }
.empty-icon { font-size: 64px; margin-bottom: 16px; opacity: 0.5; }
.empty-hint { font-size: 13px; color: #9ca3af; margin-top: 8px; }

.test-form { background: white; border-radius: 24px; padding: 30px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); animation: slideUp 0.4s ease; }
@keyframes slideUp { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }
.test-progress { margin-bottom: 30px; }
.progress-bar { height: 8px; background: #e5e7eb; border-radius: 4px; overflow: hidden; margin-bottom: 8px; }
.progress-fill { height: 100%; background: linear-gradient(90deg, #667eea, #764ba2); border-radius: 4px; transition: width 0.3s ease; }
.progress-text { font-size: 12px; color: #6b7280; text-align: right; }
.category-badge { display: inline-block; padding: 6px 16px; border-radius: 30px; font-size: 13px; font-weight: 600; color: white; margin-bottom: 20px; }
.question-text { font-size: 22px; color: #1f2937; margin-bottom: 30px; line-height: 1.4; }
.options-container { display: flex; flex-direction: column; gap: 12px; margin-bottom: 30px; }
.option-card { display: flex; align-items: center; gap: 16px; padding: 16px 20px; background: #f9fafb; border-radius: 16px; cursor: pointer; transition: all 0.2s; border: 2px solid transparent; }
.option-card:hover { background: #f3f4f6; transform: translateX(5px); }
.option-card.selected { border-color: #667eea; background: #eef2ff; }
.option-radio { width: 24px; height: 24px; }
.radio-circle { width: 20px; height: 20px; border: 2px solid #cbd5e1; border-radius: 50%; transition: all 0.2s; }
.radio-circle.checked { border-color: #667eea; background: #667eea; box-shadow: inset 0 0 0 4px white; }
.option-label { font-size: 16px; color: #374151; }
.navigation-buttons { display: flex; justify-content: space-between; gap: 16px; }
.btn-nav, .btn-submit { padding: 12px 28px; border: none; border-radius: 40px; font-weight: 600; cursor: pointer; transition: all 0.2s; }
.btn-nav.prev { background: #f3f4f6; color: #6b7280; }
.btn-nav.next { background: linear-gradient(135deg, #667eea, #764ba2); color: white; }
.btn-nav.next:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-submit { background: linear-gradient(135deg, #10b981, #059669); color: white; width: 100%; }
.btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }

.test-results { animation: fadeIn 0.5s ease; }
.results-header { text-align: center; padding-bottom: 20px; margin-bottom: 24px; border-bottom: 3px solid; }
.results-icon { width: 70px; height: 70px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 36px; margin: 0 auto 16px; }
.results-header h2 { font-size: 24px; margin-bottom: 8px; }
.test-date { font-size: 13px; color: #9ca3af; }
.risk-card { padding: 24px; border-radius: 20px; margin-bottom: 30px; border: 1px solid; }
.risk-level { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; flex-wrap: wrap; gap: 8px; }
.risk-label { font-size: 14px; font-weight: 600; color: #6b7280; }
.risk-value { font-size: 18px; font-weight: 700; }
.risk-score { font-size: 14px; color: #6b7280; margin-bottom: 12px; }
.risk-message { font-size: 15px; line-height: 1.5; color: #374151; }
.scores-section, .feedback-section, .recommendations-section { margin-bottom: 30px; }
.scores-section h3, .feedback-section h3, .recommendations-section h3 { font-size: 18px; margin-bottom: 16px; }
.scores-grid { display: flex; flex-direction: column; gap: 16px; }
.score-card { background: #f9fafb; padding: 16px; border-radius: 16px; }
.score-header { display: flex; justify-content: space-between; margin-bottom: 8px; }
.score-category { font-weight: 600; color: #374151; }
.score-percentage { font-weight: 700; color: #1f2937; }
.score-bar { height: 8px; background: #e5e7eb; border-radius: 4px; overflow: hidden; margin-bottom: 6px; }
.score-fill { height: 100%; border-radius: 4px; transition: width 0.5s ease; }
.score-detail { font-size: 11px; color: #9ca3af; }
.feedback-list { display: flex; flex-direction: column; gap: 12px; }
.feedback-item { display: flex; align-items: center; gap: 12px; padding: 12px 16px; border-radius: 12px; font-size: 14px; }
.feedback-item.high { background: #fef2f2; border-left: 3px solid #ef4444; }
.feedback-item.moderate { background: #fffbeb; border-left: 3px solid #f59e0b; }
.feedback-item.low { background: #ecfdf5; border-left: 3px solid #10b981; }
.feedback-icon { font-size: 18px; }
.recommendations-list { list-style: none; padding: 0; }
.recommendations-list li { padding: 12px 0; border-bottom: 1px solid #f3f4f6; display: flex; align-items: center; gap: 10px; }
.recommendations-list li::before { content: "✓"; color: #10b981; font-weight: bold; }
.results-actions { display: flex; gap: 16px; flex-wrap: wrap; margin-top: 30px; padding-top: 24px; border-top: 1px solid #e5e7eb; }
.btn-new-test, .btn-export, .btn-contact-psychologist { padding: 12px 24px; border: none; border-radius: 40px; font-weight: 600; cursor: pointer; transition: all 0.2s; flex: 1; }
.btn-new-test { background: linear-gradient(135deg, #1a0a2e, #2d1b4e); color: white; }
.btn-export { background: #f3f4f6; color: #374151; }
.btn-contact-psychologist { background: linear-gradient(135deg, #10b981, #059669); color: white; }
.btn-new-test:hover, .btn-export:hover, .btn-contact-psychologist:hover { transform: translateY(-2px); }

@media (max-width: 768px) {
  .children-grid-select { grid-template-columns: 1fr; }
  .question-text { font-size: 18px; }
  .results-actions { flex-direction: column; }
  .option-card { padding: 12px 16px; }
}
</style>