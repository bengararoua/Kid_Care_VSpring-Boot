<template>
  <div class="notifications-container">
    <button @click="toggleDropdown" class="notification-btn">
      <span class="bell-icon">🔔</span>
      <span v-if="unreadCount > 0" class="notification-badge">{{ unreadCount }}</span>
    </button>
    
    <div v-if="showDropdown" class="notifications-dropdown">
      <div class="dropdown-header">
        <h3>Notifications</h3>
      </div>
      <div class="notifications-list">
        <div v-if="notifications.length === 0" class="empty-notifications">
          <p>Aucune notification</p>
        </div>
        <div v-else v-for="notif in notifications" :key="notif.id" class="notification-item">
          <div class="notification-content">
            <div class="notification-title">{{ notif.title }}</div>
            <div class="notification-message">{{ notif.message }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const notifications = ref([])
const unreadCount = ref(0)
const showDropdown = ref(false)

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}
</script>

<style scoped>
.notifications-container {
  position: relative;
}
.notification-btn {
  background: rgba(255,255,255,0.15);
  border: none;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  cursor: pointer;
  position: relative;
}
.bell-icon {
  font-size: 20px;
  color: white;
}
.notification-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: red;
  color: white;
  border-radius: 50%;
  min-width: 18px;
  height: 18px;
  font-size: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.notifications-dropdown {
  position: absolute;
  top: 50px;
  right: 0;
  width: 350px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.15);
  z-index: 1000;
}
.dropdown-header {
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
}
.notifications-list {
  max-height: 400px;
  overflow-y: auto;
}
.notification-item {
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
}
.empty-notifications {
  padding: 30px;
  text-align: center;
  color: #999;
}
</style>