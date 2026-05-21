<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="background-animation">
        <div class="circle circle-1"></div>
        <div class="circle circle-2"></div>
        <div class="circle circle-3"></div>
        <div class="circle circle-4"></div>
      </div>

      <div class="auth-grid">
        <!-- Left Side - Features -->
        <div class="auth-features">
          <div class="features-content">
            <div class="logo-large">
              <span class="logo-icon">👶</span>
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

        <!-- Right Side - Form -->
        <div class="auth-form-container">
          <div class="auth-card">
            <div class="tab-switcher">
              <button @click="switchMode" class="tab-btn" :class="{ active: isLogin }">
                <span>🔐</span> Sign In
              </button>
              <button @click="switchMode" class="tab-btn" :class="{ active: !isLogin }">
                <span>✨</span> Create Account
              </button>
            </div>

            <div v-if="error" class="message-card error">
              <span>⚠️</span>
              <span>{{ error }}</span>
            </div>

            <div v-if="success" class="message-card success">
              <span>✓</span>
              <span>{{ success }}</span>
            </div>

            <form @submit.prevent="submit" class="auth-form">
              <!-- Name field (only for register) -->
              <div v-if="!isLogin" class="form-group">
                <label class="form-label">Full Name</label>
                <div class="input-wrapper">
                  <span class="input-icon">👤</span>
                  <input v-model="name" type="text" class="form-input" placeholder="Enter your full name" required>
                </div>
              </div>

              <!-- Email field -->
              <div class="form-group">
                <label class="form-label">Email Address</label>
                <div class="input-wrapper">
                  <span class="input-icon">📧</span>
                  <input v-model="email" type="email" class="form-input" placeholder="your@email.com" required>
                </div>
              </div>

              <!-- Password field -->
              <div class="form-group">
                <label class="form-label">Password</label>
                <div class="input-wrapper">
                  <span class="input-icon">🔒</span>
                  <input v-model="password" :type="showPassword ? 'text' : 'password'" class="form-input" placeholder="Enter your password" required>
                  <button type="button" class="password-toggle" @click="togglePasswordVisibility">
                    {{ showPassword ? '🙈' : '👁️' }}
                  </button>
                </div>
              </div>

              <!-- Confirm Password field (only for register) -->
              <div v-if="!isLogin" class="form-group">
                <label class="form-label">Confirm Password</label>
                <div class="input-wrapper">
                  <span class="input-icon">✓</span>
                  <input v-model="password_confirmation" :type="showConfirmPassword ? 'text' : 'password'" class="form-input" placeholder="Confirm your password" required>
                  <button type="button" class="password-toggle" @click="toggleConfirmPasswordVisibility">
                    {{ showConfirmPassword ? '🙈' : '👁️' }}
                  </button>
                </div>
                <small class="form-hint" v-if="!isLogin">Password must be at least 6 characters</small>
              </div>

              <!-- Role selection (only for register) -->
              <div v-if="!isLogin" class="form-group">
                <label class="form-label">I am a...</label>
                <div class="role-cards">
                  <div @click="role = 'parent'" class="role-card" :class="{ active: role === 'parent' }">
                    <div class="role-emoji">👨‍👩‍👧</div>
                    <div class="role-name">Parent</div>
                  </div>
                  <div @click="role = 'teacher'" class="role-card" :class="{ active: role === 'teacher' }">
                    <div class="role-emoji">📚</div>
                    <div class="role-name">Teacher</div>
                  </div>
                  <div @click="role = 'psychologist'" class="role-card" :class="{ active: role === 'psychologist' }">
                    <div class="role-emoji">👩‍⚕️</div>
                    <div class="role-name">Psychologist</div>
                  </div>
                </div>
              </div>

              <!-- Forgot password link (only for login) -->
              <div v-if="isLogin" class="forgot-link">
                <a href="#" @click.prevent="goToForgotPassword">Forgot Password?</a>
              </div>

              <!-- Submit button -->
              <button type="submit" class="submit-btn" :disabled="loading">
                <span v-if="loading" class="spinner-small"></span>
                <span v-else>{{ isLogin ? 'Sign In' : 'Create Account' }}</span>
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>

    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-logo">👶 KidCare Insight</div>
          <div class="footer-links">
            <a href="#" @click.prevent="goTo('/about')">About</a>
            <a href="#" @click.prevent="goTo('/privacy')">Privacy</a>
            <a href="#" @click.prevent="goTo('/terms')">Terms</a>
            <a href="#" @click.prevent="goTo('/contact')">Contact</a>
          </div>
          <div class="footer-copyright">
            © {{ new Date().getFullYear() }} KidCare Insight. All rights reserved.
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api, { setAuthToken } from '@/api/api'

const router = useRouter()
const isLogin = ref(true)
const email = ref('')
const password = ref('')
const password_confirmation = ref('')
const name = ref('')
const role = ref('parent')
const error = ref('')
const success = ref('')
const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value
}

const toggleConfirmPasswordVisibility = () => {
  showConfirmPassword.value = !showConfirmPassword.value
}

const goTo = (path) => {
  router.push(path)
}

const goToForgotPassword = () => {
  router.push('/reset-password')
}

const switchMode = () => {
  isLogin.value = !isLogin.value
  error.value = ''
  success.value = ''
  name.value = ''
  email.value = ''
  password.value = ''
  password_confirmation.value = ''
}

const submit = async () => {
  error.value = ''
  success.value = ''
  
  if (!isLogin.value) {
    if (!name.value.trim()) {
      error.value = 'Please enter your full name'
      return
    }
    
    if (password.value !== password_confirmation.value) {
      error.value = 'Passwords do not match'
      return
    }
    
    if (password.value.length < 6) {
      error.value = 'Password must be at least 6 characters'
      return
    }
  }
  
  if (!email.value) {
    error.value = 'Please enter your email address'
    return
  }
  
  loading.value = true
  
  try {
    if (isLogin.value) {
      const response = await api.post('/login', {
        email: email.value,
        password: password.value
      })
      
      // Stocker le token ET configurer axios
      const token = response.data.token
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(response.data.user))
      
      // IMPORTANT: Configurer api avec le token
      setAuthToken(token)
      
      console.log('✅ Login réussi, token stocké')
      await router.push('/dashboard')
      
    } else {
      const response = await api.post('/register', {
        name: name.value.trim(),
        email: email.value.toLowerCase().trim(),
        password: password.value,
        password_confirmation: password_confirmation.value,
        role: role.value
      })
      
      // Stocker le token ET configurer axios
      const token = response.data.token
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(response.data.user))
      
      // IMPORTANT: Configurer api avec le token
      setAuthToken(token)
      
      success.value = 'Account created successfully! Redirecting...'
      console.log('✅ Inscription réussie, token stocké')
      
      setTimeout(async () => {
        await router.push('/dashboard')
      }, 1500)
    }
  } catch (err) {
    console.error('Erreur:', err)
    if (err.response?.data?.errors) {
      const errors = err.response.data.errors
      error.value = Object.values(errors).flat().join(', ')
    } else if (err.response?.data?.error) {
      error.value = err.response.data.error
    } else {
      error.value = 'An error occurred. Please try again.'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  background: rgba(255, 255, 255, 0.1);
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
  background: linear-gradient(135deg, #1a0a2e 0%, #2d1b4e 100%);
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

.tab-switcher {
  display: flex;
  gap: 12px;
  background: #f0f2f5;
  padding: 6px;
  border-radius: 60px;
  margin-bottom: 32px;
}

.tab-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  border: none;
  background: transparent;
  border-radius: 50px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  color: #6c7a8e;
}

.tab-btn.active {
  background: white;
  color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
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

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  font-size: 18px;
}

.form-input {
  width: 100%;
  padding: 14px 16px 14px 48px;
  border: 2px solid #e0e6ed;
  border-radius: 16px;
  font-size: 15px;
  transition: all 0.3s;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.password-toggle {
  position: absolute;
  right: 16px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 18px;
  padding: 0;
}

.form-hint {
  font-size: 12px;
  color: #9ca3af;
}

.role-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.role-card {
  text-align: center;
  padding: 12px;
  border: 2px solid #e0e6ed;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.role-card.active {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.1);
}

.role-emoji {
  font-size: 28px;
  margin-bottom: 6px;
}

.role-name {
  font-size: 12px;
  font-weight: 600;
  color: #4b5563;
}

.forgot-link {
  text-align: right;
}

.forgot-link a {
  color: #8b9dc3;
  font-size: 13px;
  text-decoration: none;
}

.forgot-link a:hover {
  color: #667eea;
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
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.spinner-small {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
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

.footer-links {
  display: flex;
  gap: 1.5rem;
}

.footer-links a {
  color: #cbd5e0;
  text-decoration: none;
  transition: color 0.3s;
  cursor: pointer;
  font-size: 13px;
}

.footer-links a:hover {
  color: white;
}

.footer-copyright {
  font-size: 0.8rem;
}

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

@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    text-align: center;
  }
  .footer-links {
    justify-content: center;
  }
  .role-cards {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .auth-form-container {
    padding: 24px;
  }
}
</style>