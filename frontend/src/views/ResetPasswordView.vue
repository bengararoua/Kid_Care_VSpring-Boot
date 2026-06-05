<script setup>
// Bloc script avec la syntaxe Composition API de Vue 3
import { ref, onMounted } from 'vue'  // Importe ref (variables réactives) et onMounted (hook exécuté après montage)
import { useRouter, useRoute } from 'vue-router'  // Importe useRouter (navigation) et useRoute (accès aux paramètres d'URL)
import axios from 'axios'  // Importe axios pour les requêtes HTTP vers le backend

// ===== VARIABLES RÉACTIVES =====
const router = useRouter()  // Instance du routeur pour rediriger l'utilisateur
const route = useRoute()  // Instance de la route active pour lire les paramètres d'URL
const email = ref('')  // Email de l'utilisateur (liaison avec le champ de saisie)
const token = ref('')  // Token JWT de réinitialisation (reçu par email ou depuis l'URL)
const password = ref('')  // Nouveau mot de passe (saisi par l'utilisateur)
const password_confirmation = ref('')  // Confirmation du nouveau mot de passe
const error = ref('')  // Message d'erreur à afficher
const success = ref('')  // Message de succès à afficher
const loading = ref(false)  // Indicateur de chargement (désactive le bouton pendant la requête)
const step = ref('email') // Étape actuelle : 'email' (saisie email) ou 'reset' (saisie nouveau mot de passe)
const showPassword = ref(false)  // Afficher/masquer le mot de passe (champ new password)
const showConfirmPassword = ref(false)  // Afficher/masquer le mot de passe (champ confirmation)

// ===== HOOK ONMOUNTED (exécuté au chargement de la page) =====
onMounted(() => {
  // Vérifier si on a un token et un email dans l'URL (lien de réinitialisation reçu par email)
  if (route.query.token && route.query.email) {  // Si les paramètres token et email sont présents dans l'URL
    token.value = route.query.token  // Extrait le token depuis l'URL
    email.value = route.query.email  // Extrait l'email depuis l'URL
    step.value = 'reset'  // Passe directement à l'étape de réinitialisation (formulaire nouveau mot de passe)
    verifyToken()  // Vérifie que le token est encore valide avant d'afficher le formulaire
  }
})

// ===== MÉTHODE 1 : ENVOYER LA DEMANDE DE RÉINITIALISATION =====
const sendResetLink = async () => {  // Envoie l'email au backend pour générer un token
  error.value = ''  // Efface l'erreur précédente
  success.value = ''  // Efface le succès précédent
  
  if (!email.value) {  // Vérifie si l'email n'est pas vide
    error.value = 'Please enter your email address'  // Affiche une erreur
    return  // Arrête l'exécution
  }
  
  loading.value = true  // Active l'indicateur de chargement (bouton désactivé)
  
  try {
    const response = await axios.post('/api/password/email', {  // Envoie une requête POST au backend
      email: email.value  // Corps de la requête : email
    })
    
    token.value = response.data.token  // Stocke le token reçu du backend
    step.value = 'reset'  // Passe à l'étape de réinitialisation (formulaire nouveau mot de passe)
    success.value = 'Email verified! Please enter your new password.'  // Message de succès
    setTimeout(() => { success.value = '' }, 3000)  // Efface le message après 3 secondes
    
  } catch (err) {  // Capture les erreurs (email non trouvé, etc.)
    error.value = err.response?.data?.error || 'Email not found'  // Affiche l'erreur du backend ou message par défaut
    setTimeout(() => { error.value = '' }, 3000)  // Efface l'erreur après 3 secondes
  } finally {
    loading.value = false  // Désactive l'indicateur de chargement
  }
}

// ===== MÉTHODE 2 : VÉRIFIER LE TOKEN =====
const verifyToken = async () => {  // Vérifie si le token dans l'URL est valide (non expiré)
  try {
    const response = await axios.post('/api/password/check-token', {  // Envoie une requête POST au backend
      email: email.value,  // Email extrait de l'URL
      token: token.value  // Token extrait de l'URL
    })
    
    if (!response.data.valid) {  // Si le backend retourne valid = false
      error.value = response.data.expired ? 'Link has expired. Please request a new one.' : 'Invalid reset link'  // Message selon cause
      setTimeout(() => {  // Après 3 secondes
        error.value = ''  // Efface l'erreur
        step.value = 'email'  // Retour à l'étape email
        token.value = ''  // Vide le token
      }, 3000)
    }
  } catch (err) {  // Capture les erreurs réseau
    error.value = 'Invalid reset link'  // Message générique
    setTimeout(() => {  // Après 3 secondes
      error.value = ''  // Efface l'erreur
      step.value = 'email'  // Retour à l'étape email
    }, 3000)
  }
}

// ===== MÉTHODE 3 : RÉINITIALISER LE MOT DE PASSE =====
const resetPassword = async () => {  // Envoie le nouveau mot de passe au backend avec le token
  error.value = ''  // Efface l'erreur précédente
  success.value = ''  // Efface le succès précédent
  
  if (password.value !== password_confirmation.value) {  // Vérifie que les mots de passe correspondent
    error.value = 'Passwords do not match'  // Message d'erreur
    setTimeout(() => { error.value = '' }, 3000)  // Efface après 3 secondes
    return  // Arrête l'exécution
  }
  
  if (password.value.length < 6) {  // Vérifie que le mot de passe a au moins 6 caractères
    error.value = 'Password must be at least 6 characters'  // Message d'erreur
    setTimeout(() => { error.value = '' }, 3000)  // Efface après 3 secondes
    return  // Arrête l'exécution
  }
  
  loading.value = true  // Active l'indicateur de chargement
  
  try {
    await axios.post('/api/password/reset', {  // Envoie une requête POST au backend
      email: email.value,  // Email de l'utilisateur
      token: token.value,  // Token de réinitialisation
      password: password.value,  // Nouveau mot de passe
      password_confirmation: password_confirmation.value  // Confirmation du mot de passe
    })
    
    success.value = 'Password reset successfully! Redirecting to login...'  // Message de succès
    setTimeout(() => {  // Après 2 secondes
      router.push('/')  // Redirige vers la page de connexion
    }, 2000)
    
  } catch (err) {  // Capture les erreurs (token expiré, etc.)
    error.value = err.response?.data?.error || 'Failed to reset password'  // Affiche l'erreur du backend
    setTimeout(() => { error.value = '' }, 3000)  // Efface après 3 secondes
  } finally {
    loading.value = false  // Désactive l'indicateur de chargement
  }
}

// ===== MÉTHODES UTILITAIRES =====
const togglePasswordVisibility = () => {  // Inverse l'affichage du mot de passe (texte ↔ masqué)
  showPassword.value = !showPassword.value  // Bascule entre true et false
}

const toggleConfirmPasswordVisibility = () => {  // Inverse l'affichage de la confirmation du mot de passe
  showConfirmPassword.value = !showConfirmPassword.value  // Bascule entre true et false
}

const backToEmail = () => {  // Retour à l'étape de saisie de l'email (depuis l'étape reset)
  step.value = 'email'  // Passe à l'étape email
  email.value = ''  // Vide l'email
  token.value = ''  // Vide le token
  password.value = ''  // Vide le nouveau mot de passe
  password_confirmation.value = ''  // Vide la confirmation
  error.value = ''  // Efface l'erreur
  success.value = ''  // Efface le succès
}
</script>

<template>
  <!-- Structure HTML de la page -->
  <div class="auth-page">
    <div class="auth-container">
      <!-- Animation de fond avec cercles flottants -->
      <div class="background-animation">
        <div class="circle circle-1"></div>
        <div class="circle circle-2"></div>
        <div class="circle circle-3"></div>
        <div class="circle circle-4"></div>
      </div>

      <!-- Grille principale avec deux colonnes (côté gauche = fonctionnalités, côté droit = formulaire) -->
      <div class="auth-grid">
        <!-- Left Side - Features (côté gauche avec les fonctionnalités de l'application) -->
        <div class="auth-features">
          <div class="features-content">
            <div class="logo-large">
              <span class="logo-icon">🧠</span>
              <h1>KidCare <span>Insight</span></h1>
              <p>Supporting children's behavioral development</p>
            </div>

            <div class="features-list">
              <div class="feature-item">
                <div class="feature-icon">📊</div>
                <div>
                  <h3>Behavioral Tracking</h3>
                  <p>Monitor focus, mood, sleep, and social interaction with insightful charts.</p>
                </div>
              </div>
              <div class="feature-item">
                <div class="feature-icon">🤝</div>
                <div>
                  <h3>Team Collaboration</h3>
                  <p>Parents, teachers, and psychologists work together for each child.</p>
                </div>
              </div>
              <div class="feature-item">
                <div class="feature-icon">🧠</div>
                <div>
                  <h3>AI-Powered Insights</h3>
                  <p>Automatic risk assessment and personalized recommendations.</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Right Side - Form (côté droit avec le formulaire de réinitialisation) -->
        <div class="auth-form-container">
          <div class="auth-card">
            <!-- Step 1: Email Form (étape 1 : saisie de l'email) -->
            <div v-if="step === 'email'" class="reset-step">
              <div class="brand-section">
                <div class="logo-icon">🔐</div>
                <h2>Reset Password</h2>
                <p>Enter your email to reset your password</p>
              </div>

              <div v-if="error" class="message-card error">
                <span>⚠️</span>
                <span>{{ error }}</span>
              </div>

              <div v-if="success" class="message-card success">
                <span>✓</span>
                <span>{{ success }}</span>
              </div>

              <form @submit.prevent="sendResetLink">
                <div class="input-group">
                  <div class="input-icon">📧</div>
                  <input v-model="email" type="email" placeholder="Email Address" required>
                </div>

                <button type="submit" class="submit-btn" :disabled="loading">
                  {{ loading ? 'Verifying...' : 'Continue' }}
                </button>
              </form>

              <div class="back-link">
                <a href="#" @click.prevent="router.push('/')">← Back to Login</a>
              </div>
            </div>

            <!-- Step 2: Reset Password Form (étape 2 : saisie du nouveau mot de passe) -->
            <div v-else class="reset-step">
              <div class="brand-section">
                <div class="logo-icon">🔑</div>
                <h2>Create New Password</h2>
                <p>Enter your new password for {{ email }}</p>
              </div>

              <div v-if="error" class="message-card error">
                <span>⚠️</span>
                <span>{{ error }}</span>
              </div>

              <div v-if="success" class="message-card success">
                <span>✓</span>
                <span>{{ success }}</span>
              </div>

              <form @submit.prevent="resetPassword">
                <div class="input-group">
                  <div class="input-icon">🔒</div>
                  <input 
                    v-model="password" 
                    :type="showPassword ? 'text' : 'password'" 
                    placeholder="New Password (min 6 characters)"
                    required
                  >
                  <button type="button" class="password-toggle" @click="togglePasswordVisibility">
                    {{ showPassword ? '🙈' : '👁️' }}
                  </button>
                </div>

                <div class="input-group">
                  <div class="input-icon">✓</div>
                  <input 
                    v-model="password_confirmation" 
                    :type="showConfirmPassword ? 'text' : 'password'" 
                    placeholder="Confirm New Password"
                    required
                  >
                  <button type="button" class="password-toggle" @click="toggleConfirmPasswordVisibility">
                    {{ showConfirmPassword ? '🙈' : '👁️' }}
                  </button>
                </div>

                <button type="submit" class="submit-btn" :disabled="loading">
                  {{ loading ? 'Resetting...' : 'Reset Password' }}
                </button>
              </form>

              <div class="back-link">
                <a href="#" @click.prevent="backToEmail">← Back to Email</a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Footer (pied de page) -->
    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-logo">👶 KidCare Insight</div>
          <div class="footer-copyright">
            © {{ new Date().getFullYear() }} KidCare Insight. All rights reserved.
          </div>
          <div class="footer-email">
            📧 contact@kidcare-insight.com
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
/* Styles CSS propres à ce composant (scoped = limité à cette page) */
.auth-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.auth-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #e8f4f8 0%, #f0e6f5 100%);
  padding: 40px 20px;
}

.background-animation {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(79, 142, 247, 0.1), rgba(107, 203, 119, 0.1));
  animation: float 20s infinite ease-in-out;
}

.circle-1 { width: 300px; height: 300px; top: -100px; left: -100px; }
.circle-2 { width: 400px; height: 400px; bottom: -150px; right: -150px; animation-delay: 5s; }
.circle-3 { width: 200px; height: 200px; top: 50%; left: 10%; animation-delay: 2s; }
.circle-4 { width: 250px; height: 250px; bottom: 20%; right: 5%; animation-delay: 8s; }

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(5deg); }
}

.auth-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  max-width: 1200px;
  width: 100%;
  background: white;
  border-radius: 32px;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  z-index: 1;
}

.auth-features {
  background: linear-gradient(135deg, #1a0a2e 0%, #2d1b4e 50%, #1a0a2e 100%);
  padding: 48px;
  color: white;
}

.features-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.logo-large {
  margin-bottom: 48px;
}

.logo-icon {
  font-size: 48px;
}

.logo-large h1 {
  font-size: 32px;
  margin: 16px 0 8px 0;
}

.logo-large h1 span {
  color: #a855f7;
}

.logo-large p {
  opacity: 0.8;
  font-size: 14px;
}

.features-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.feature-item {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.feature-icon {
  font-size: 32px;
}

.feature-item h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
}

.feature-item p {
  margin: 0;
  opacity: 0.8;
  font-size: 14px;
  line-height: 1.5;
}

.auth-form-container {
  padding: 48px;
  background: white;
}

.auth-card {
  max-width: 400px;
  margin: 0 auto;
}

.brand-section {
  text-align: center;
  margin-bottom: 32px;
}

.brand-section .logo-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.brand-section h2 {
  font-size: 28px;
  color: #2d3748;
  margin-bottom: 8px;
}

.brand-section p {
  color: #8b9dc3;
}

.reset-step {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  border-radius: 16px;
  margin-bottom: 20px;
  font-size: 14px;
}

.message-card.error {
  background: #fee;
  color: #c62828;
  border-left: 4px solid #c62828;
}

.message-card.success {
  background: #e8f5e9;
  color: #2e7d32;
  border-left: 4px solid #2e7d32;
}

.input-group {
  position: relative;
  margin-bottom: 18px;
}

.input-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 18px;
}

.input-group input {
  width: 100%;
  padding: 14px 48px 14px 48px;
  border: 2px solid #e0e6ed;
  border-radius: 16px;
  font-size: 15px;
  transition: all 0.3s;
}

.input-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.password-toggle {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  font-size: 18px;
}

.submit-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 60px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: all 0.3s;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.back-link {
  text-align: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e0e6ed;
}

.back-link a {
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.back-link a:hover {
  color: #764ba2;
}

.footer {
  background: #1a1a2e;
  color: #cbd5e0;
  padding: 20px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.footer-logo {
  font-size: 1rem;
  font-weight: 600;
  color: white;
}

.footer-copyright {
  font-size: 0.85rem;
}

.footer-email {
  font-size: 0.85rem;
}

/* Responsive : adapté pour mobile */
@media (max-width: 900px) {
  .auth-grid {
    grid-template-columns: 1fr;
  }
  .auth-features {
    padding: 32px;
  }
  .auth-form-container {
    padding: 32px;
  }
}

@media (max-width: 480px) {
  .auth-form-container {
    padding: 24px;
  }
  .footer-content {
    flex-direction: column;
    text-align: center;
  }
}
</style>