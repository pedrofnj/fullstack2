<template>
  <v-app>
    <v-navigation-drawer v-model="drawer" app>
      <v-list>
        <v-list-item @click="drawer = false">
          <v-list-item-title>Minhas Listas</v-list-item-title>
        </v-list-item>
        <v-list-item v-for="l in lists.items" :key="l.id" @click="goToTasks(l.id)">
          <v-list-item-title>{{ l.name }}</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>

    <v-app-bar app>
      <v-btn v-if="auth.isAuthenticated" @click="drawer = !drawer" icon><i class="bi bi-list"></i></v-btn>
      <v-toolbar-title>TaskList App</v-toolbar-title>
      <v-spacer></v-spacer>
      <span v-if="auth.user">{{ auth.user.name }}</span>
      <v-btn @click="logout" variant="text">Sair</v-btn>
    </v-app-bar>

    <v-main>
      <router-view />
    </v-main>

    <v-snackbar v-model="ui.snackbar.show" :color="ui.snackbar.color">
      {{ ui.snackbar.message }}
    </v-snackbar>
  </v-app>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/useAuthStore'
import { useTasklistsStore } from '@/stores/useTasklistsStore'
import { useUiStore } from '@/stores/useUiStore'

const router = useRouter()
const auth = useAuthStore()
const lists = useTasklistsStore()
const ui = useUiStore()
const drawer = ref(false)
const overdueCount = ref(0)

onMounted(async () => {
  if (!auth.isAuthenticated) {
    router.push('/login')
    return
  }
  await lists.fetch()
  await updateOverdueCount()
  if (overdueCount.value > 0) {
    ui.showSnackbar(`Você tem ${overdueCount.value} tarefas vencidas.`, 'error')
  }
})

const goToTasks = (listId: string) => {
  router.push({ name: 'tasks', params: { listId } })
  drawer.value = false
}

const logout = () => {
  auth.logout()
  router.push({ name: 'login' })
}

const updateOverdueCount = async () => {
  let count = 0
  const now = new Date()
  for (const l of lists.items) {
    await tasks.fetch(l.id)
    count += tasks.items.filter(t => !t.completed && t.dueDate && new Date(t.dueDate) < now).length
  }
  overdueCount.value = count
}
</script>
