import axios from "axios";

// Récupérer le token depuis localStorage
const getToken = () => {
  const token = localStorage.getItem("token");
  if (token && token !== "undefined" && token !== "null") {
    return token;
  }
  return null;
};

// Créer l'instance axios avec configuration de base
const api = axios.create({
  baseURL: "/api",
  timeout: 30000,
  headers: {
    "Content-Type": "application/json",
    "Accept": "application/json",
  }
});

// Intercepteur de requête - AJOUTE LE TOKEN
api.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log(`🔐 Token envoyé pour: ${config.url}`);
    } else {
      console.warn(`⚠️ Pas de token pour: ${config.url}`);
    }
    return config;
  },
  (error) => {
    console.error("Request interceptor error:", error);
    return Promise.reject(error);
  }
);

// Intercepteur de réponse - GESTION DES ERREURS 401
api.interceptors.response.use(
  (response) => {
    console.log(`✅ API Success: ${response.config.url}`);
    return response;
  },
  (error) => {
    if (error.response) {
      console.error(`❌ API Error ${error.response.status}: ${error.config?.url}`, error.response.data);
      
      // Si erreur 401 Unauthorized ou 403 Forbidden
      if (error.response.status === 401 || error.response.status === 403) {
        console.error("Erreur d'authentification, déconnexion...");
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        delete api.defaults.headers.common["Authorization"];
        
        // Rediriger vers login si pas déjà dessus
        if (window.location.pathname !== "/login" && window.location.pathname !== "/") {
          window.location.href = "/login";
        }
      }
    } else if (error.request) {
      console.error("No response received:", error.request);
    } else {
      console.error("Request error:", error.message);
    }
    return Promise.reject(error);
  }
);

// Fonction utilitaire pour définir le token après login
export const setAuthToken = (token) => {
  if (token) {
    api.defaults.headers.common["Authorization"] = `Bearer ${token}`;
    localStorage.setItem("token", token);
    console.log("✅ Token défini dans les headers");
  } else {
    delete api.defaults.headers.common["Authorization"];
    localStorage.removeItem("token");
    console.log("❌ Token retiré des headers");
  }
};

// Vérifier et restaurer le token au démarrage
const initializeToken = () => {
  const token = getToken();
  if (token) {
    setAuthToken(token);
    console.log("✅ Token restauré au démarrage");
    return true;
  }
  console.log("⚠️ Aucun token trouvé au démarrage");
  return false;
};

// Initialiser automatiquement
initializeToken();

export default api;