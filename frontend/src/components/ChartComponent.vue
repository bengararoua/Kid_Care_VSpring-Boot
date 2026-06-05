<template>
  <div class="chart-container" v-if="childId">
    <div class="chart-header">
      <div>
        <h3>📊 Focus Level Over Time</h3>
        <p class="chart-subtitle">Track your child's focus progression</p>
      </div>
      <div v-if="hasData" class="chart-stats">
        <div class="stat-chip">
          <span class="stat-label">Average</span>
          <span class="stat-value">{{ avgFocus }}/5</span>
        </div>
        <div class="stat-chip">
          <span class="stat-label">Total Logs</span>
          <span class="stat-value">{{ totalLogs }}</span>
        </div>
      </div>
    </div>
    <div class="chart-wrapper">
      <div v-if="isLoading" class="chart-loading">
        <div class="spinner-small"></div>
        <p>Loading chart data...</p>
      </div>
      <div v-else-if="!hasData" class="chart-empty">
        <span class="empty-chart-icon">📭</span>
        <p>No behavior logs yet</p>
        <p class="empty-hint">Click "Log Today" to add your first observation and see the chart!</p>
      </div>
      <Line v-else :data="chartData" :options="options" />
    </div>
  </div>
  <div v-else class="chart-placeholder">
    <span class="placeholder-icon">👶</span>
    <p>Select a child to view the focus chart</p>
  </div>
</template>

<script setup>
import { Line } from 'vue-chartjs'
import { Chart as ChartJS, Title, Tooltip, Legend, LineElement, CategoryScale, LinearScale, PointElement, Filler } from 'chart.js'
import { ref, onMounted, watch } from 'vue'
import api from '@/api/api'  // ← CHANGEMENT IMPORTANT

ChartJS.register(Title, Tooltip, Legend, LineElement, CategoryScale, LinearScale, PointElement, Filler)

const props = defineProps({
  childId: {
    type: [Number, String],
    required: true
  }
})

const chartData = ref({
  labels: [],
  datasets: [{
    label: 'Focus Level',
    data: [],
    borderColor: '#4F8EF7',
    backgroundColor: 'rgba(79, 142, 247, 0.1)',
    borderWidth: 3,
    tension: 0.3,
    fill: true,
    pointBackgroundColor: '#4F8EF7',
    pointBorderColor: 'white',
    pointRadius: 5,
    pointHoverRadius: 8,
    pointBorderWidth: 2
  }]
})

const hasData = ref(false)
const isLoading = ref(true)
const avgFocus = ref(0)
const totalLogs = ref(0)

// Helper pour extraire la valeur quel que soit le format
const getFocusLevel = (log) => {
  return log.focusLevel || log.focus_level
}

const getLogDate = (log) => {
  return log.logDate || log.log_date
}

const options = {
  responsive: true,
  maintainAspectRatio: true,
  plugins: {
    legend: {
      position: 'top',
      labels: {
        usePointStyle: true,
        boxWidth: 8,
        font: { size: 12, weight: 'bold' }
      }
    },
    tooltip: {
      mode: 'index',
      intersect: false,
      backgroundColor: 'rgba(0,0,0,0.8)',
      titleColor: 'white',
      bodyColor: '#e5e7eb',
      padding: 10,
      cornerRadius: 8,
      callbacks: {
        label: function(context) {
          return 'Focus: ' + context.raw + '/5'
        }
      }
    }
  },
  scales: {
    y: {
      min: 0,
      max: 5,
      title: {
        display: true,
        text: 'Focus Level',
        color: '#6b7280',
        font: { size: 12, weight: 'bold' }
      },
      grid: { color: '#e5e7eb' },
      ticks: {
        stepSize: 1,
        callback: function(val) { return val + '/5' }
      }
    },
    x: {
      title: {
        display: true,
        text: 'Date',
        color: '#6b7280',
        font: { size: 12, weight: 'bold' }
      },
      grid: { display: false },
      ticks: { maxRotation: 45, minRotation: 45 }
    }
  }
}

const loadChartData = async () => {
  if (!props.childId) {
    isLoading.value = false
    return
  }
  
  isLoading.value = true
  
  try {
    const res = await api.get(`/logs/${props.childId}`)  
    const logs = res.data
    
    if (logs && logs.length > 0) {
      hasData.value = true
      totalLogs.value = logs.length
      
      // Trier par date croissante
      const sortedLogs = [...logs].sort((a, b) => {
        const dateA = new Date(getLogDate(a))
        const dateB = new Date(getLogDate(b))
        return dateA - dateB
      })
      
      chartData.value.labels = sortedLogs.map(log => {
        const date = new Date(getLogDate(log))
        return date.toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' })
      })
      
      chartData.value.datasets[0].data = sortedLogs.map(log => getFocusLevel(log))
      
      // Calculer la moyenne
      const sum = chartData.value.datasets[0].data.reduce((a, b) => a + b, 0)
      avgFocus.value = (sum / chartData.value.datasets[0].data.length).toFixed(1)
    } else {
      hasData.value = false
    }
  } catch (error) {
    console.error('Error loading chart data:', error)
    hasData.value = false
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  loadChartData()
})

watch(() => props.childId, (newId) => {
  if (newId) {
    loadChartData()
  }
})
</script>

<style scoped>
.chart-container {
  background: white;
  border-radius: 24px;
  padding: 24px;
  margin-bottom: 32px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  transition: transform 0.3s ease;
}

.chart-container:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.1);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
}

.chart-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.chart-subtitle {
  font-size: 13px;
  color: #9ca3af;
}

.chart-stats {
  display: flex;
  gap: 12px;
}

.stat-chip {
  background: linear-gradient(135deg, #f3f4f6, #e5e7eb);
  padding: 8px 16px;
  border-radius: 40px;
  text-align: center;
}

.stat-chip .stat-label {
  display: block;
  font-size: 10px;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-chip .stat-value {
  display: block;
  font-size: 16px;
  font-weight: 700;
  color: #1a0a2e;
}

.chart-wrapper {
  min-height: 350px;
  position: relative;
}

.chart-loading, .chart-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  text-align: center;
}

.spinner-small {
  width: 40px;
  height: 40px;
  border: 3px solid #e0e6ed;
  border-top-color: #1a0a2e;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-chart-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.chart-empty p {
  color: #9ca3af;
  margin: 4px 0;
}

.empty-hint {
  font-size: 12px;
  color: #cbd5e1;
}

.chart-placeholder {
  background: white;
  border-radius: 24px;
  padding: 48px;
  text-align: center;
  margin-bottom: 32px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.placeholder-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 12px;
}

.chart-placeholder p {
  color: #9ca3af;
}

@media (max-width: 768px) {
  .chart-header {
    flex-direction: column;
    text-align: center;
  }
  
  .chart-stats {
    justify-content: center;
  }
  
  .chart-wrapper {
    min-height: 280px;
  }
}
</style>