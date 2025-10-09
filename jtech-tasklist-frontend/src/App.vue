<template>
  <v-app>
    <v-app-bar app>
      <v-toolbar-title>TaskList App</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-btn @click="toggleTheme">
        <i :class="theme.global.name.value === 'dark' ? 'bi bi-sun' : 'bi bi-moon'"></i>
      </v-btn>
    </v-app-bar>
    <v-main>
      <router-view />
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
import { useTheme } from 'vuetify'

const theme = useTheme()

const toggleTheme = () => {
  theme.global.name.value = theme.global.name.value === 'dark' ? 'light' : 'dark'
}
</script>
