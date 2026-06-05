<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/api'
import Layout from '@/layouts/Layout.vue' 
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import NotificationToast from '@/components/NotificationToast.vue'

const router = useRouter()

const toast = ref(null)
const confirmDialog = ref(null)

const user = ref({})
const loading = ref(false)

const profileForm = ref({
  name: '',
  email: ''
})

const passwordForm = ref({
  current_password: '',
  password: '',
  password_confirmation: ''
})

const activeTab = ref('profile')

const showNotification = (message, type = 'success', title = '') => {
  const titles = { success: 'Succès', error: 'Erreur', warning: 'Attention', info: 'Information' }
  toast.value?.addNotification(type, title || titles[type] || 'Notification', message, 4000)
}

const formatMemberSince = (date) => {
  if (!date) return 'Nouveau membre'
  const d = new Date(date)
  return d.toLocaleDateString('fr-FR', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  })
}

const loadUser = async () => {
  try {
    const response = await api.get('/me')
    user.value = response.data
    profileForm.value.name = user.value.name || ''
    profileForm.value.email = user.value.email || ''
    localStorage.setItem('user', JSON.stringify(user.value))
  } catch (err) {
    console.error('Erreur chargement utilisateur:', err)
    showNotification('Impossible de charger les informations utilisateur', 'error')
  }
}

const getAuthConfig = () => {
  const token = localStorage.getItem('token')
  return {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  }
}

const updateProfile = async () => {
  loading.value = true
  try {
    const response = await api.put('/profile', {
      name: profileForm.value.name,
      email: profileForm.value.email
    }, getAuthConfig())
    
    user.value = response.data
    localStorage.setItem('user', JSON.stringify(response.data))
    showNotification('Profil mis à jour avec succès !', 'success')
  } catch (err) {
    showNotification(err.response?.data?.error || 'Erreur lors de la mise à jour', 'error')
  } finally {
    loading.value = false
  }
}

const updatePassword = async () => {
  if (passwordForm.value.password !== passwordForm.value.password_confirmation) {
    showNotification('Les nouveaux mots de passe ne correspondent pas', 'error')
    return
  }

  if (passwordForm.value.password.length < 6) {
    showNotification('Le mot de passe doit contenir au moins 6 caractères', 'error')
    return
  }

  loading.value = true

  try {
    await api.put('/password', {
      current_password: passwordForm.value.current_password,
      password: passwordForm.value.password,
      password_confirmation: passwordForm.value.password_confirmation
    }, getAuthConfig())

    showNotification('Mot de passe mis à jour avec succès ! Veuillez vous reconnecter avec votre nouveau mot de passe.', 'success')
    
    setTimeout(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    }, 2000)
  } catch (err) {
    const errorMsg = err.response?.data?.error || 'Erreur lors de la mise à jour du mot de passe'
    showNotification(errorMsg, 'error')
  } finally {
    loading.value = false
  }
}

const deleteAccount = async () => {
  const confirmed = await confirmDialog.value?.show({
    title: 'Supprimer mon compte',
    message: '⚠️ Attention : Cette action est irréversible !',
    type: 'danger',
    confirmText: 'Supprimer',
    cancelText: 'Annuler'
  })
  
  if (!confirmed) return

  const passwordResult = await confirmDialog.value?.show({
    title: 'Confirmation',
    message: 'Entrez votre mot de passe :',
    type: 'warning',
    confirmText: 'Confirmer',
    cancelText: 'Annuler',
    showPasswordInput: true
  })
  
  if (!passwordResult || !passwordResult.confirmed || !passwordResult.password) {
    showNotification('Suppression annulée', 'info')
    return
  }

  loading.value = true

  try {
    const config = {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      },
      data: { password: passwordResult.password }
    }
    
    const response = await api.delete('/profile/delete', config)
    
    console.log('Réponse:', response.data)
    showNotification('Compte supprimé avec succès', 'success')
    
    setTimeout(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    }, 1500)
  } catch (err) {
    console.error('Erreur:', err)
    const errorMsg = err.response?.data?.error || err.response?.data?.message || 'Erreur lors de la suppression'
    showNotification(errorMsg, 'error')
  } finally {
    loading.value = false
  }
}
const getRoleIcon = (role) => {
  switch(role) {
    case 'parent': return '👨‍👩‍👧'
    case 'teacher': return '📚'
    case 'psychologist': return '👩‍⚕️'
    default: return '👤'
  }
}

const getRoleText = (role) => {
  switch(role) {
    case 'parent': return 'Parent'
    case 'teacher': return 'Enseignant'
    case 'psychologist': return 'Psychologue'
    default: return role
  }
}

onMounted(() => {
  loadUser()
})
</script>

<template>
  <Layout>
    <div class="profile-container">
      <NotificationToast ref="toast" />
      <ConfirmDialog ref="confirmDialog" />

      <div class="profile-header">
        <h1 class="profile-title">👤 Mon Profil</h1>
        <p class="profile-subtitle">Gérez vos paramètres de compte et préférences</p>
      </div>

      <div class="profile-grid">
        <div class="profile-sidebar">
          <div class="avatar-section">
            <div class="avatar-initials-large" :style="{ background: 'linear-gradient(135deg, #667eea, #764ba2)' }">
              {{ user.name?.charAt(0)?.toUpperCase() || '👤' }}
            </div>
            <h3 class="avatar-name">{{ user.name }}</h3>
            <p class="avatar-role">{{ getRoleIcon(user.role) }} {{ getRoleText(user.role) }}</p>
            <p class="avatar-email">{{ user.email }}</p>
          </div>

          <div class="sidebar-stats">
            <div class="stat-item">
              <span class="stat-icon">📅</span>
              <div>
                <div class="stat-label">Membre depuis</div>
                <div class="stat-value">{{ formatMemberSince(user.createdAt) }}</div>
              </div>
            </div>
            <div v-if="user.role === 'parent'" class="stat-item">
              <span class="stat-icon">⭐</span>
              <div>
                <div class="stat-label">Points</div>
                <div class="stat-value">{{ user.points || 0 }} points</div>
              </div>
            </div>
          </div>
        </div>

        <div class="profile-main">
          <div class="profile-tabs">
            <button @click="activeTab = 'profile'" class="tab-btn" :class="{ active: activeTab === 'profile' }">
              <span>👤</span> Informations du profil
            </button>
            <button @click="activeTab = 'security'" class="tab-btn" :class="{ active: activeTab === 'security' }">
              <span>🔒</span> Sécurité
            </button>
            <button @click="activeTab = 'danger'" class="tab-btn danger" :class="{ active: activeTab === 'danger' }">
              <span>⚠️</span> Zone dangereuse
            </button>
          </div>

          <div v-if="activeTab === 'profile'" class="tab-content">
            <form @submit.prevent="updateProfile" class="profile-form">
              <div class="form-group">
                <label>Nom complet</label>
                <input v-model="profileForm.name" type="text" class="form-input" placeholder="Entrez votre nom complet" required>
              </div>
              <div class="form-group">
                <label>Adresse email</label>
                <input v-model="profileForm.email" type="email" class="form-input" placeholder="Entrez votre email" required>
              </div>
              <div class="form-group">
                <label>Rôle</label>
                <input :value="getRoleText(user.role)" type="text" class="form-input" disabled>
                <small class="form-hint">Le rôle ne peut pas être modifié</small>
              </div>
              <button type="submit" class="btn-save" :disabled="loading">
                <span v-if="loading" class="spinner-small"></span>
                <span v-else>💾</span>
                {{ loading ? 'Enregistrement...' : 'Enregistrer les modifications' }}
              </button>
            </form>
          </div>

          <div v-if="activeTab === 'security'" class="tab-content">
            <form @submit.prevent="updatePassword" class="profile-form">
              <div class="form-group">
                <label>Mot de passe actuel</label>
                <input v-model="passwordForm.current_password" type="password" class="form-input" placeholder="Entrez votre mot de passe actuel" required>
              </div>
              <div class="form-group">
                <label>Nouveau mot de passe</label>
                <input v-model="passwordForm.password" type="password" class="form-input" placeholder="Nouveau mot de passe (min 6 caractères)" required>
              </div>
              <div class="form-group">
                <label>Confirmer le nouveau mot de passe</label>
                <input v-model="passwordForm.password_confirmation" type="password" class="form-input" placeholder="Confirmez votre nouveau mot de passe" required>
              </div>
              <button type="submit" class="btn-save" :disabled="loading">
                <span v-if="loading" class="spinner-small"></span>
                <span v-else>🔒</span>
                {{ loading ? 'Mise à jour...' : 'Mettre à jour le mot de passe' }}
              </button>
            </form>
          </div>

          <div v-if="activeTab === 'danger'" class="tab-content">
            <div class="danger-zone">
              <div class="danger-card">
                <div class="danger-icon">⚠️</div>
                <div class="danger-content">
                  <h3>Supprimer mon compte</h3>
                  <p>Une fois votre compte supprimé, il n'y a pas de retour possible. Toutes vos données seront définitivement supprimées.</p>
                  <button @click="deleteAccount" class="btn-danger" :disabled="loading">
                    🗑️ Supprimer mon compte
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.profile-header { 
  margin-bottom: 30px; 
  animation: slideDown 0.5s ease;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

.profile-title {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #1a0a2e, #2d1b4e);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.profile-subtitle { 
  color: #6b7280; 
  font-size: 14px; 
}

.profile-grid { 
  display: grid; 
  grid-template-columns: 280px 1fr; 
  gap: 30px; 
}

/* Sidebar */
.profile-sidebar {
  background: white;
  border-radius: 24px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  text-align: center;
  animation: slideInLeft 0.5s ease;
  transition: transform 0.3s, box-shadow 0.3s;
}

.profile-sidebar:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0,0,0,0.12);
}

@keyframes slideInLeft {
  from { opacity: 0; transform: translateX(-30px); }
  to { opacity: 1; transform: translateX(0); }
}

.avatar-initials-large {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  font-weight: 700;
  color: white;
  margin: 0 auto 20px;
  transition: transform 0.3s;
  animation: scaleIn 0.5s ease;
}

@keyframes scaleIn {
  from { transform: scale(0); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.avatar-initials-large:hover {
  transform: scale(1.05);
}

.avatar-name { 
  font-size: 20px; 
  font-weight: 700; 
  color: #1f2937; 
  margin-bottom: 8px; 
}

.avatar-role { 
  font-size: 13px; 
  color: #6b7280; 
  margin-bottom: 8px; 
}

.avatar-email { 
  font-size: 13px; 
  color: #9ca3af; 
  word-break: break-all; 
}

.sidebar-stats { 
  margin-top: 24px; 
  padding-top: 24px; 
  border-top: 1px solid #e0e6ed; 
}

.stat-item { 
  display: flex; 
  align-items: center; 
  gap: 12px; 
  padding: 10px 0;
  transition: transform 0.2s;
}

.stat-item:hover {
  transform: translateX(5px);
}

.stat-icon { 
  font-size: 24px; 
}

.stat-label { 
  font-size: 11px; 
  color: #9ca3af; 
  text-transform: uppercase; 
}

.stat-value { 
  font-size: 14px; 
  font-weight: 600; 
  color: #374151; 
}

/* Main Content */
.profile-main {
  background: white;
  border-radius: 24px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  animation: slideInRight 0.5s ease;
}

@keyframes slideInRight {
  from { opacity: 0; transform: translateX(30px); }
  to { opacity: 1; transform: translateX(0); }
}

.profile-tabs {
  display: flex;
  gap: 12px;
  border-bottom: 2px solid #e0e6ed;
  margin-bottom: 24px;
  padding-bottom: 12px;
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
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
}

.tab-btn:hover { 
  background: #f3f4f6; 
  color: #374151; 
  transform: translateY(-2px);
}

.tab-btn.active { 
  background: linear-gradient(135deg, #1a0a2e, #2d1b4e); 
  color: white; 
  box-shadow: 0 4px 12px rgba(26, 10, 46, 0.3);
}

.tab-btn.danger.active { 
  background: linear-gradient(135deg, #dc2626, #ef4444); 
}

.tab-content { 
  animation: fadeInUp 0.4s ease; 
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.profile-form { 
  max-width: 500px; 
}

.form-group { 
  margin-bottom: 24px; 
}

.form-group label { 
  display: block; 
  font-size: 13px; 
  font-weight: 600; 
  color: #4b5563; 
  margin-bottom: 8px; 
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e0e6ed;
  border-radius: 12px;
  font-size: 14px;
  transition: all 0.3s;
}

.form-input:focus { 
  outline: none; 
  border-color: #1a0a2e; 
  box-shadow: 0 0 0 3px rgba(26, 10, 46, 0.1);
  transform: translateY(-1px);
}

.form-input:disabled { 
  background: #f9fafb; 
  color: #6b7280; 
  cursor: not-allowed; 
}

.form-hint { 
  display: block; 
  font-size: 11px; 
  color: #9ca3af; 
  margin-top: 6px; 
}

.btn-save {
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
  justify-content: center;
  gap: 8px;
}

.btn-save:hover:not(:disabled) { 
  transform: translateY(-2px); 
  box-shadow: 0 6px 20px rgba(26, 10, 46, 0.3); 
}

.btn-save:disabled { 
  opacity: 0.6; 
  cursor: not-allowed; 
}

/* Danger Zone */
.danger-zone { 
  border: 2px solid #fee2e2; 
  border-radius: 20px; 
  background: linear-gradient(135deg, #fef2f2, #fff5f5);
  animation: pulseWarning 2s infinite;
}

@keyframes pulseWarning {
  0%, 100% { border-color: #fee2e2; }
  50% { border-color: #ef4444; }
}

.danger-card { 
  padding: 30px; 
  display: flex; 
  align-items: center; 
  gap: 20px; 
}

.danger-icon { 
  font-size: 48px; 
  animation: shake 0.5s ease-in-out;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

.danger-content h3 { 
  font-size: 18px; 
  color: #991b1b; 
  margin-bottom: 8px; 
}

.danger-content p { 
  font-size: 13px; 
  color: #7f1d1d; 
  margin-bottom: 16px; 
}

.btn-danger {
  background: linear-gradient(135deg, #dc2626, #ef4444);
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 30px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn-danger:hover:not(:disabled) { 
  transform: translateY(-2px); 
  box-shadow: 0 6px 20px rgba(220, 38, 38, 0.3); 
}

.spinner-small {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Responsive */
@media (max-width: 900px) {
  .profile-grid { 
    grid-template-columns: 1fr; 
  }
  .danger-card { 
    flex-direction: column; 
    text-align: center; 
  }
  .profile-tabs {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>