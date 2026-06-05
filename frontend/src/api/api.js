import axios from 'axios'

// Instance Axios configurée pour Spring Boot
const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },
  timeout: 60000, 
  withCredentials: false
})

//  injecte le JWT à chaque requête
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    console.log('🔍 Requête vers:', config.url)
    console.log('🔑 Token dans localStorage:', token ? token.substring(0, 50) + '...' : 'NON TROUVE')
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      console.log('✅ Header Authorization ajouté')
    } else {
      console.log(' PAS DE TOKEN - Requête non authentifiée')
    }
    
    return config
  },
  (error) => {
    console.error(' Erreur intercepteur request:', error)
    return Promise.reject(error)
  }
)
api.interceptors.response.use(
  (response) => {
    console.log('✅ Réponse réussie:', response.config.url, response.status)
    return response
  },
  (error) => {
    console.error(' Erreur réponse:', error.response?.status, error.response?.config?.url)
    console.error('📝 Détails erreur:', error.response?.data)
    
    if (error.response?.status === 401 || error.response?.status === 403) {
      console.log(' Authentification échouée, redirection vers login')
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export const setAuthToken = (token) => {
  if (token && token !== 'undefined' && token !== 'null') {
    localStorage.setItem('token', token)
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`
    console.log('✅ Token défini dans les headers par défaut')
  } else {
    delete api.defaults.headers.common['Authorization']
    localStorage.removeItem('token')
    console.log(' Token supprimé')
  }
}

const savedToken = localStorage.getItem('token')
if (savedToken && savedToken !== 'undefined' && savedToken !== 'null') {
  api.defaults.headers.common['Authorization'] = `Bearer ${savedToken}`
  console.log('✅ Token initialisé au démarrage')
} else {
  console.log(' Aucun token trouvé au démarrage')
}

export default api